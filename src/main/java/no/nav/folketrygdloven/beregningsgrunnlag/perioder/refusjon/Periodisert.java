package no.nav.folketrygdloven.beregningsgrunnlag.perioder.refusjon;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeSplittProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodiseringRefusjonProsesstruktur;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class Periodisert extends LeafSpecification<PeriodiseringRefusjonProsesstruktur> {

    public Periodisert(){
        super("Periodisert");
    }

    @Override
    public Evaluation evaluate(PeriodiseringRefusjonProsesstruktur grunnlag) {
        return ja();
    }

    @Override
    public String beskrivelse() {
        return "Periodisert";
    }
}
