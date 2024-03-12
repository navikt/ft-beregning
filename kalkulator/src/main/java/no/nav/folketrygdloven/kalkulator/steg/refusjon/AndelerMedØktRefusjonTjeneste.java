package no.nav.folketrygdloven.kalkulator.steg.refusjon;


import java.util.Collections;
import java.util.List;
import java.util.Map;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

/**
 * Tjeneste for å finne andeler i nytt beregningsgrunnlag som har økt refusjon siden orginalbehandlingen.
 */
public final class AndelerMedØktRefusjonTjeneste {

    private AndelerMedØktRefusjonTjeneste() {
        // Skjuler default
    }

    public static Map<Intervall, List<RefusjonAndel>> finnAndelerMedØktRefusjon(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                BeregningsgrunnlagDto originaltGrunnlag,
                                                                                Beløp grenseverdi,
                                                                                YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (beregningsgrunnlag == null || originaltGrunnlag == null) {
            return Collections.emptyMap();
        }
        var alleredeUtbetaltTOM = FinnAlleredeUtbetaltTom.finn(originaltGrunnlag);
        if (alleredeUtbetaltTOM.isEmpty()) {
            return Collections.emptyMap();
        }
        return BeregningRefusjonTjeneste.finnUtbetaltePerioderMedAndelerMedØktRefusjon(beregningsgrunnlag, originaltGrunnlag, alleredeUtbetaltTOM.get(), grenseverdi, ytelsespesifiktGrunnlag);
    }
}
