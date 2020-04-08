package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBrukerErArbeidstakerFRISINN.ID)
class SjekkOmBrukerErArbeidstakerFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 14.1";
    static final String BESKRIVELSE = "Er bruker arbeidstaker?";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    SjekkOmBrukerErArbeidstakerFRISINN(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return arbeidsforhold.erFrilanser() ? nei() : ja();
    }
}
