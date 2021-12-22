package no.nav.folketrygdloven.beregningsgrunnlag.perioder.utbetalingsgrad;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.PeriodiseringUtbetalingsgradProsesstruktur;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class Periodisert extends LeafSpecification<PeriodiseringUtbetalingsgradProsesstruktur> {

    public Periodisert(){
        super("Periodisert");
    }

    @Override
    public Evaluation evaluate(PeriodiseringUtbetalingsgradProsesstruktur grunnlag) {
        return ja();
    }

    @Override
    public String beskrivelse() {
        return "Periodisert";
    }
}
