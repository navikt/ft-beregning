package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFrilansArbeidsforholdFinnes.ID)
class SjekkOmFrilansArbeidsforholdFinnes extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 28.0";
    static final String BESKRIVELSE = "Har bruker frilans arbeidsforhold";


    SjekkOmFrilansArbeidsforholdFinnes() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getFrilansArbeidsforhold().isPresent() ? ja() : nei();
    }
}
