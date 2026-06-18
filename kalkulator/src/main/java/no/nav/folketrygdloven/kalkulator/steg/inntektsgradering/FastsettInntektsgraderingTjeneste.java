package no.nav.folketrygdloven.kalkulator.steg.inntektsgradering;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.inntektgradering.FinnUttaksgradInntektsgradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class FastsettInntektsgraderingTjeneste {

    private FastsettInntektsgraderingTjeneste() {
    }

    public static BeregningsgrunnlagRegelResultat fastsettInntektsgradering(BeregningsgrunnlagInput input) {
        var grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());

        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = grunnlagBuilder.getBeregningsgrunnlagBuilder();
        List<BeregningsgrunnlagPeriodeDto> perioder = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();

        if (!harPeriodeMedReduksjon(perioder)) {
            return new BeregningsgrunnlagRegelResultat(input.getBeregningsgrunnlag(), Collections.emptyList());
        }

        for (var p : perioder) {
            List<TilkommetInntektDto> tilkomneInntekter = p.getTilkomneInntekter();
            if (tilkomneInntekter != null) {

                BeregningsgrunnlagPeriodeDto.Builder periodeBuilderFor = beregningsgrunnlagBuilder.getPeriodeBuilderFor(p.getPeriode())
                    .orElseThrow(() -> new IllegalStateException("Forventer å finne periode"));
                for (var t : tilkomneInntekter) {
                    if (t.skalRedusereUtbetaling() == true) {
                        Beløp tilkommetBeløp = utledTilkommetFraBrutto(t, p.getPeriode(), input.getYtelsespesifiktGrunnlag());
                        TilkommetInntektDto ny = new TilkommetInntektDto(t);
                        ny.setTilkommetInntektPrÅr(tilkommetBeløp);
                        periodeBuilderFor.leggTilTilkommetInntekt(ny);
                    }
                }

            }
        }
        grunnlagBuilder.medBeregningsgrunnlag(beregningsgrunnlagBuilder.build());
        var inputMedTilkommetInntekt = input.medBeregningsgrunnlagGrunnlag(grunnlagBuilder.buildUtenIdOgTilstand());
        return FinnUttaksgradInntektsgradering.finnInntektsgradering(inputMedTilkommetInntekt);
    }

    private static boolean harPeriodeMedReduksjon(List<BeregningsgrunnlagPeriodeDto> perioder) {
        return perioder.stream().anyMatch(p -> p.getTilkomneInntekter().stream().anyMatch(TilkommetInntektDto::skalRedusereUtbetaling));
    }

    private static Beløp utledTilkommetFraBrutto(TilkommetInntektDto tilkommetInntektDto,
                                                 Intervall periode,
                                                 YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            if (tilkommetInntektDto.getAktivitetStatus().erArbeidstaker() && tilkommetInntektDto.getArbeidsgiver().isPresent()) {
                var aktivitetsProsent = UtbetalingsgradTjeneste.finnAktivitetsgradForArbeid(tilkommetInntektDto.getArbeidsgiver().get(),
                    tilkommetInntektDto.getArbeidsforholdRef(), periode, ytelsespesifiktGrunnlag, true);
                var utbetalingsprosent = UtbetalingsgradTjeneste.finnUtbetalingsgradForArbeid(tilkommetInntektDto.getArbeidsgiver().get(),
                    tilkommetInntektDto.getArbeidsforholdRef(), periode, ytelsespesifiktGrunnlag, true);
                var tilommetGrad = aktivitetsProsent.map(grad -> grad.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                    .orElse(BigDecimal.valueOf(1).subtract(utbetalingsprosent.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)));
                return tilkommetInntektDto.getBruttoInntektPrÅr().multipliser(tilommetGrad);
            } else {
                var utbetalingsprosent = UtbetalingsgradTjeneste.finnUtbetalingsgradForStatus(tilkommetInntektDto.getAktivitetStatus(), periode,
                    ytelsespesifiktGrunnlag);
                var aktivitetsProsent = UtbetalingsgradTjeneste.finnAktivitetsgradForStatus(tilkommetInntektDto.getAktivitetStatus(), periode,
                    ytelsespesifiktGrunnlag);
                var tilommetGrad = aktivitetsProsent.map(grad -> grad.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                    .orElse(BigDecimal.valueOf(1).subtract(utbetalingsprosent.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)));
                return tilkommetInntektDto.getBruttoInntektPrÅr().multipliser(tilommetGrad);
            }
        }
        throw new IllegalStateException("Kun gyldig ved utbetalingsgradgrunnlag");
    }


}
