package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmArbeidstakerArbeidsforholdFinnes.ID)
class SjekkOmArbeidstakerArbeidsforholdFinnes extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 28.4";
    static final String BESKRIVELSE = "Har bruker arbeidstaker arbeidsforhold";


    SjekkOmArbeidstakerArbeidsforholdFinnes() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforholdIkkeFrilans().isEmpty() ? nei() : ja();
    }
}
