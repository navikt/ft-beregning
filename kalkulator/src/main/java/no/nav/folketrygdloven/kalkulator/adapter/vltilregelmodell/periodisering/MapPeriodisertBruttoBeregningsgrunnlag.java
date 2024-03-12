package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapArbeidsforholdFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapAktivitetStatusV2FraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

public final class MapPeriodisertBruttoBeregningsgrunnlag {

    private MapPeriodisertBruttoBeregningsgrunnlag() {
        // skjul default
    }

    public static List<PeriodisertBruttoBeregningsgrunnlag> map(BeregningsgrunnlagDto vlBeregningsgrunnlag) {
        return vlBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .map(MapPeriodisertBruttoBeregningsgrunnlag::mapPeriode)
                .collect(Collectors.toList());
    }

    private static PeriodisertBruttoBeregningsgrunnlag mapPeriode(BeregningsgrunnlagPeriodeDto bgp) {
        Periode regelPeriode = Periode.of(bgp.getBeregningsgrunnlagPeriodeFom(), bgp.getBeregningsgrunnlagPeriodeTom());
        PeriodisertBruttoBeregningsgrunnlag.Builder periodeBuilder = PeriodisertBruttoBeregningsgrunnlag.builder()
                .medPeriode(regelPeriode);
        bgp.getBeregningsgrunnlagPrStatusOgAndelList().forEach(a ->
                periodeBuilder.leggTilBruttoBeregningsgrunnlag(mapBruttoBG(a))
        );
        return periodeBuilder.build();
    }

    private static BruttoBeregningsgrunnlag mapBruttoBG(BeregningsgrunnlagPrStatusOgAndelDto a) {
        AktivitetStatusV2 regelAktivitetStatus = MapAktivitetStatusV2FraVLTilRegel.map(
                a.getAktivitetStatus(),
                a.getGjeldendeInntektskategori());

        Optional<Arbeidsforhold> arbeidsforhold = a.getBgAndelArbeidsforhold()
                .map(bga ->
                        MapArbeidsforholdFraVLTilRegel.mapArbeidsforhold(
                                bga.getArbeidsgiver(),
                                bga.getArbeidsforholdRef())
                );
        return BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(regelAktivitetStatus)
                .medArbeidsforhold(arbeidsforhold.orElse(null))
                .medBruttoPrÅr(Beløp.safeVerdi(a.getBruttoInkludertNaturalYtelser()))
                .medRefusjonPrÅr(a.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).map(Beløp::verdi).orElse(BigDecimal.ZERO))
                .build();
    }
}
