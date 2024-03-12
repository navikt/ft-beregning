package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.FastsettFaktaOmBeregningVerdierTjeneste;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektUtenInntektsmeldingAndelDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektUtenInntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RedigerbarAndelFaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;

public class FastsettMånedsinntektUtenInntektsmeldingOppdaterer {

    private FastsettMånedsinntektUtenInntektsmeldingOppdaterer() {
    }

    public static void oppdater(FaktaBeregningLagreDto dto,
                                Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        FastsettMånedsinntektUtenInntektsmeldingDto fastsettMånedsinntektDto = dto.getFastsattUtenInntektsmelding();
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        List<BeregningsgrunnlagPrStatusOgAndelDto> arbeidstakerAndeleriFørstePeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bpsa -> bpsa.getAktivitetStatus().erArbeidstaker())
                .collect(Collectors.toList());
        List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe = fastsettMånedsinntektDto.getAndelListe();
        settInntektForAllePerioder(beregningsgrunnlag, forrigeBg, arbeidstakerAndeleriFørstePeriode, andelListe);
    }

    private static void settInntektForAllePerioder(BeregningsgrunnlagDto nyttBeregningsgrunnlag,
                                            Optional<BeregningsgrunnlagDto> forrigeBg,
                                            List<BeregningsgrunnlagPrStatusOgAndelDto> arbeidstakerAndeleriFørstePeriode,
                                            List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe) {
        for (FastsettMånedsinntektUtenInntektsmeldingAndelDto dtoAndel : andelListe) {
            BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagAndel = finnKorrektAndel(arbeidstakerAndeleriFørstePeriode, dtoAndel);
            for (BeregningsgrunnlagPeriodeDto periode : nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
                Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode = finnForrigePeriode(forrigeBg, periode);
                Optional<BeregningsgrunnlagPrStatusOgAndelDto> andelForArbeidsforhold = finnAndelIPeriode(beregningsgrunnlagAndel, periode);
                if (andelForArbeidsforhold.isPresent()) {
                    RedigerbarAndelFaktaOmBeregningDto redigerbarAndel = lagRedigerbarAndel(andelForArbeidsforhold.get());
                    FastsettFaktaOmBeregningVerdierTjeneste.fastsettVerdierForAndel(redigerbarAndel, mapTilFastsatteVerdier(dtoAndel), periode, forrigePeriode);
                }
            }
        }
    }

    private static FastsatteVerdierDto mapTilFastsatteVerdier(FastsettMånedsinntektUtenInntektsmeldingAndelDto dtoAndel) {
        return FastsatteVerdierDto.Builder.ny().medFastsattBeløpPrMnd(dtoAndel.getFastsattBeløp()).medInntektskategori(dtoAndel.getInntektskategori()).build();
    }

    private static RedigerbarAndelFaktaOmBeregningDto lagRedigerbarAndel(BeregningsgrunnlagPrStatusOgAndelDto andelForArbeidsforhold) {
        return new RedigerbarAndelFaktaOmBeregningDto(false, andelForArbeidsforhold.getAndelsnr(), false);
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnAndelIPeriode(BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagAndel, BeregningsgrunnlagPeriodeDto periode) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(andel -> Objects.equals(andel.getArbeidsforholdRef(), beregningsgrunnlagAndel.getArbeidsforholdRef())
                        && Objects.equals(andel.getArbeidsgiver(), beregningsgrunnlagAndel.getArbeidsgiver())).findFirst();
    }

    private static Optional<BeregningsgrunnlagPeriodeDto> finnForrigePeriode(Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagPeriodeDto periode) {
        return forrigeBg.stream()
                .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .filter(p -> periode.getPeriode().inkluderer(p.getPeriode().getFomDato())).findFirst();
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto finnKorrektAndel(List<BeregningsgrunnlagPrStatusOgAndelDto> arbeidstakerAndeler, FastsettMånedsinntektUtenInntektsmeldingAndelDto dtoAndel) {
        return arbeidstakerAndeler.stream()
                .filter(bgAndel -> dtoAndel.getAndelsnr().equals(bgAndel.getAndelsnr()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Fant ikke andel for andelsnr " + dtoAndel.getAndelsnr()));
    }
}
