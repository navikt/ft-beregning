package no.nav.folketrygdloven.kalkulator.steg.refusjon;


import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

/**
 * Tjeneste for å finne andeler i nytt beregningsgrunnlag som har økt refusjon siden orginalbehandlingen.
 */
public final class AndelerMedØktRefusjonTjeneste {

    /**
     * Når skrudd på: andeler regnes kun med dersom selve refusjonskravet fra inntektsmelding faktisk
     * har økt inn i perioden. Skiller reelle kravsendringer fra rene utbetalingsgrad-svingninger
     * (f.eks. gjenopptakelse etter ferie), slik at avklaringsbehov og GUI-visning holdes konsistente.
     */
    public static final String TOGGLE_KREV_ENDRET_KRAV = "refusjon.avklaringsbehov.krev-endret-krav";

    private AndelerMedØktRefusjonTjeneste() {
        // Skjuler default
    }

    public static Map<Intervall, List<RefusjonAndel>> finnAndelerMedØktRefusjon(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                BeregningsgrunnlagDto forrigeGrunnlag,
                                                                                Beløp grenseverdi,
                                                                                YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                                List<KravperioderPrArbeidsforholdDto> kravPrArbeidsgiver,
                                                                                boolean krevEndretKrav) {
        if (beregningsgrunnlag == null || forrigeGrunnlag == null) {
            return Collections.emptyMap();
        }
        var alleredeUtbetaltTOM = FinnAlleredeUtbetaltTom.finn(forrigeGrunnlag);
        if (alleredeUtbetaltTOM.isEmpty()) {
            return Collections.emptyMap();
        }
        var andeler = BeregningRefusjonTjeneste.finnUtbetaltePerioderMedAndelerMedØktRefusjon(beregningsgrunnlag, forrigeGrunnlag,
            alleredeUtbetaltTOM.get(), grenseverdi, ytelsespesifiktGrunnlag);
        if (!krevEndretKrav) {
            return andeler;
        }
        var andelerMedØktKrav = new LinkedHashMap<Intervall, List<RefusjonAndel>>();
        var skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();
        andeler.forEach((periode, andelerIPeriode) -> {
            var medØktKrav = andelerIPeriode.stream()
                .filter(andel -> RefusjonskravFraInntektsmeldingTjeneste.refusjonskravHarØktInnIPeriode(kravPrArbeidsgiver, andel.getArbeidsgiver(),
                    periode, skjæringstidspunkt))
                .toList();
            if (!medØktKrav.isEmpty()) {
                andelerMedØktKrav.put(periode, medØktKrav);
            }
        });
        return andelerMedØktKrav;
    }
}
