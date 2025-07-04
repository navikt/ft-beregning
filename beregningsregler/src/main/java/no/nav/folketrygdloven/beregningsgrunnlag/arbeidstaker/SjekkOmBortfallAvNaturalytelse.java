package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBortfallAvNaturalytelse.ID)
class SjekkOmBortfallAvNaturalytelse extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 15.3";
    static final String BESKRIVELSE = "Har bruker bortfall av naturalytelse ved start av perioden eller i tidligere periode?";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    SjekkOmBortfallAvNaturalytelse(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var fom = grunnlag.getSkjæringstidspunkt();
        var tom = grunnlag.getBeregningsgrunnlagPeriode().getFom();
        var naturalytelse = grunnlag.getInntektsgrunnlag().finnTotaltNaturalytelseBeløpMedOpphørsdatoIPeriodeForArbeidsforhold(arbeidsforhold.getArbeidsforhold(), fom, tom);
        return naturalytelse.isPresent() ? ja() : nei();
    }
}
