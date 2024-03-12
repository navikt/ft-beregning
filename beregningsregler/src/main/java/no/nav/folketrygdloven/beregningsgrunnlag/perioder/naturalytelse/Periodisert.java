package no.nav.folketrygdloven.beregningsgrunnlag.perioder.naturalytelse;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodiseringNaturalytelseProsesstruktur;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class Periodisert extends LeafSpecification<PeriodiseringNaturalytelseProsesstruktur> {

    public Periodisert(){
        super("Periodisert");
    }

    @Override
    public Evaluation evaluate(PeriodiseringNaturalytelseProsesstruktur grunnlag) {
        return ja();
    }

    @Override
    public String beskrivelse() {
        return "Periodisert";
    }
}
