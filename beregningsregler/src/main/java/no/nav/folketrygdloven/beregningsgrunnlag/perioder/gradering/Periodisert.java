package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodiseringGraderingProsesstruktur;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class Periodisert extends LeafSpecification<PeriodiseringGraderingProsesstruktur> {

    public Periodisert(){
        super("Periodisert");
    }

    @Override
    public Evaluation evaluate(PeriodiseringGraderingProsesstruktur grunnlag) {
        return ja();
    }

    @Override
    public String beskrivelse() {
        return "Periodisert";
    }
}
