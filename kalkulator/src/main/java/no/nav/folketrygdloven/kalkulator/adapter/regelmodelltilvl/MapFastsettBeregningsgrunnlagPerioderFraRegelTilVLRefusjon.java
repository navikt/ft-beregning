package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapHjemmelFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

public class MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjon extends MapFastsettBeregningsgrunnlagPerioderFraRegelTilVL {

    @Override
    protected void mapAndeler(SplittetPeriode splittetPeriode,
                              List<BeregningsgrunnlagPrStatusOgAndelDto> andelListe, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        andelListe.forEach(eksisterendeAndel -> mapEksisterendeAndel(splittetPeriode, beregningsgrunnlagPeriode, eksisterendeAndel));
        splittetPeriode.getNyeAndeler()
                .forEach(nyAndel -> mapNyAndel(beregningsgrunnlagPeriode, nyAndel));
    }

    private void mapNyAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, SplittetAndel nyAndel) {
        Arbeidsgiver arbeidsgiver = MapArbeidsforholdFraRegelTilVL.map(nyAndel.getArbeidsforhold().getReferanseType(), nyAndel.getArbeidsforhold().getOrgnr(), nyAndel.getArbeidsforhold().getAktørId());
        InternArbeidsforholdRefDto iaRef = InternArbeidsforholdRefDto.ref(nyAndel.getArbeidsforhold().getArbeidsforholdId());
        BGAndelArbeidsforholdDto.Builder andelArbeidsforholdBuilder = BGAndelArbeidsforholdDto.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(iaRef)
                .medArbeidsperiodeFom(nyAndel.getArbeidsperiodeFom())
                .medArbeidsperiodeTom(nyAndel.getArbeidsperiodeTom())
                .medRefusjonskravPrÅr(Beløp.fra(nyAndel.getRefusjonskravPrÅr()),
                        MapHjemmelFraRegelTilVL.map(nyAndel.getAnvendtRefusjonskravfristHjemmel()),
                        mapUtfall(nyAndel.getRefusjonskravFristUtfall()));
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medKilde(AndelKilde.PROSESS_PERIODISERING)
                .medBGAndelArbeidsforhold(andelArbeidsforholdBuilder)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medArbforholdType(OpptjeningAktivitetType.ARBEID)
                .build(beregningsgrunnlagPeriode);
    }

    private Utfall mapUtfall(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Utfall refusjonskravFristUtfall) {
        return switch (refusjonskravFristUtfall) {
            case GODKJENT -> Utfall.GODKJENT;
            case UNDERKJENT -> Utfall.UNDERKJENT;
            default -> Utfall.UDEFINERT;
        };
    }

    private void mapEksisterendeAndel(SplittetPeriode splittetPeriode, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                      BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.kopier(eksisterendeAndel);
        Optional<EksisterendeAndel> regelMatchOpt = finnEksisterendeAndelFraRegel(splittetPeriode, eksisterendeAndel);
        regelMatchOpt.ifPresent(regelAndel -> {
            BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforholdDtoBuilder = andelBuilder.getBgAndelArbeidsforholdDtoBuilder();
            BGAndelArbeidsforholdDto.Builder andelArbeidsforholdBuilder = bgAndelArbeidsforholdDtoBuilder
                    .medRefusjonskravPrÅr(Beløp.fra(regelAndel.getRefusjonskravPrÅr().orElse(null)),
                            MapHjemmelFraRegelTilVL.map(regelAndel.getAnvendtRefusjonskravfristHjemmel()),
                            regelAndel.getRefusjonskravFristVurderingUtfall().map(this::mapUtfall).orElse(Utfall.UDEFINERT));
            andelBuilder.medBGAndelArbeidsforhold(andelArbeidsforholdBuilder);
        });
        andelBuilder.build(beregningsgrunnlagPeriode);
    }

}
