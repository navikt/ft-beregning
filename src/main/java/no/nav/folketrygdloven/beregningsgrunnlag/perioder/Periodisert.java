package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeSplittProsesstruktur;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class Periodisert extends LeafSpecification<PeriodeSplittProsesstruktur> {

    public Periodisert(){
        super("Periodisert");
    }

    @Override
    public Evaluation evaluate(PeriodeSplittProsesstruktur grunnlag) {
        return ja();
    }

    @Override
    public String beskrivelse() {
        return "Periodisert";
    }
}
