package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMilitærIOpptjeningsperiodenOgLavereBGEnnHarRettPå.ID)
class SjekkOmMilitærIOpptjeningsperiodenOgLavereBGEnnHarRettPå extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_VK_32.3";
    static final String BESKRIVELSE = "Sjekk om totalt brutto BG < 3G (2G for svangerskapspenger) og bruker har militær eller sivilforsvarstjeneste i opptjeningsperioden";

    SjekkOmMilitærIOpptjeningsperiodenOgLavereBGEnnHarRettPå() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Beregningsgrunnlag bg = grunnlag.getBeregningsgrunnlag();
        BigDecimal beløpMilitærHarKravPå = grunnlag.getGrunnbeløp().multiply(BigDecimal.valueOf(bg.getAntallGrunnbeløpMilitærHarKravPå()));
        boolean bruttoBGUnderKravPå = grunnlag.getBruttoPrÅrInkludertNaturalytelser().compareTo(beløpMilitærHarKravPå) < 0;

        SingleEvaluation resultat = bruttoBGUnderKravPå && bg.harHattMilitærIOpptjeningsperioden() ? ja() : nei();

        resultat.setEvaluationProperty("harMilitærIOpptjeningsperioden", bg.harHattMilitærIOpptjeningsperioden());
        resultat.setEvaluationProperty("bruttoPrÅr", grunnlag.getBruttoPrÅr());
        resultat.setEvaluationProperty("beløpMilitærHarKravPå", beløpMilitærHarKravPå);
        return resultat;
    }
}
