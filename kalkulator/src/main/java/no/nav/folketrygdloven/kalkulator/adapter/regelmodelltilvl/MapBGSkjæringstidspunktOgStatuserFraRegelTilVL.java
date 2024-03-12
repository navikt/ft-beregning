package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapAktivitetStatusVedSkjæringstidspunktFraRegelTilVL.mapAktivitetStatusfraRegelmodell;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapOpptjeningAktivitetFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.util.FinnArbeidsperiode;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

public class MapBGSkjæringstidspunktOgStatuserFraRegelTilVL {

    private MapBGSkjæringstidspunktOgStatuserFraRegelTilVL() {}

    public static BeregningsgrunnlagDto mapForSkjæringstidspunktOgStatuser(
        KoblingReferanse ref,
        AktivitetStatusModell regelModell,
        List<RegelResultat> regelResultater,
        InntektArbeidYtelseGrunnlagDto iayGrunnlag,
        List<Grunnbeløp> grunnbeløpSatser) {

        Objects.requireNonNull(regelModell, "regelmodell");
        // Regelresultat brukes kun til logging
        Objects.requireNonNull(regelResultater, "regelresultater");
        if (regelResultater.size() != 2) {
            throw new IllegalStateException("Antall regelresultater må være 2 for å spore regellogg");
        }

        if (regelModell.getAktivitetStatuser().containsAll(Arrays.asList(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.DP,
            no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.AAP))) {
            throw new IllegalStateException("Ugyldig kombinasjon av statuser: Kan ikke både ha status AAP og DP samtidig");
        }
        LocalDate skjæringstidspunktForBeregning = regelModell.getSkjæringstidspunktForBeregning();

        Grunnbeløp grunnbeløp = grunnbeløpSatser.stream()
            .filter(g -> Periode.of(g.getFom(), g.getTom()).inneholder(ref.getFørsteUttaksdato()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Fant ikke grunnbeløp for gitt dato " + ref.getFørsteUttaksdato()));

        var beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(skjæringstidspunktForBeregning)
            .medGrunnbeløp(Beløp.fra(grunnbeløp.getGVerdi().longValue()))
            .build();
        regelModell.getAktivitetStatuser()
            .forEach(as -> BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(mapAktivitetStatusfraRegelmodell(regelModell, as))
                .build(beregningsgrunnlag));
        var beregningsgrunnlagPeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(skjæringstidspunktForBeregning, null)
            .build(beregningsgrunnlag);

        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());

        opprettBeregningsgrunnlagPrStatusOgAndelForSkjæringstidspunkt(filter, regelModell, beregningsgrunnlagPeriode);
        return beregningsgrunnlag;
    }

    private static void opprettBeregningsgrunnlagPrStatusOgAndelForSkjæringstidspunkt(YrkesaktivitetFilterDto filter,
                                                                                      AktivitetStatusModell regelmodell,
                                                                                      BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        var skjæringstidspunkt = regelmodell.getSkjæringstidspunktForBeregning();
        FinnArbeidsperiode finnArbeidsperiodeTjeneste = new FinnArbeidsperiode(filter);
        regelmodell.getBeregningsgrunnlagPrStatusListe().stream()
            .filter(bgps -> erATFL(bgps.getAktivitetStatus()))
            .forEach(bgps -> bgps.getArbeidsforholdList()
                .forEach(af -> {
                    var arbeidsgiver = MapArbeidsforholdFraRegelTilVL.map(af.getReferanseType(), af.getOrgnr(), af.getAktørId());
                    var iaRef = InternArbeidsforholdRefDto.ref(af.getArbeidsforholdId());
                    var andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                        .medArbforholdType(MapOpptjeningAktivitetFraRegelTilVL.map(af.getAktivitet()))
                        .medAktivitetStatus(af.erFrilanser() ? AktivitetStatus.FRILANSER : AktivitetStatus.ARBEIDSTAKER);
                    if (af.getReferanseType() != null || af.getArbeidsforholdId() != null) {
                        Intervall arbeidsperiode = finnArbeidsperiodeTjeneste.finnArbeidsperiode(arbeidsgiver, iaRef, skjæringstidspunkt);
                        BGAndelArbeidsforholdDto.Builder bgArbeidsforholdBuilder = BGAndelArbeidsforholdDto.builder()
                            .medArbeidsgiver(arbeidsgiver)
                            .medArbeidsforholdRef(af.getArbeidsforholdId())
                            .medArbeidsperiodeTom(arbeidsperiode.getTomDato())
                            .medArbeidsperiodeFom(arbeidsperiode.getFomDato());
                        andelBuilder.medBGAndelArbeidsforhold(bgArbeidsforholdBuilder);
                    }
                    andelBuilder.build(beregningsgrunnlagPeriode);
                }));
        regelmodell.getBeregningsgrunnlagPrStatusListe().stream()
            .filter(bgps -> !(erATFL(bgps.getAktivitetStatus())))
            .forEach(bgps -> BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(mapAktivitetStatusfraRegelmodell(regelmodell, bgps.getAktivitetStatus()))
                .medArbforholdType(MapOpptjeningAktivitetFraRegelTilVL.map(bgps.getAktivitetStatus()))
                .build(beregningsgrunnlagPeriode));
    }

    private static boolean erATFL(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus aktivitetStatus) {
        return no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL.equals(aktivitetStatus);
    }
}
