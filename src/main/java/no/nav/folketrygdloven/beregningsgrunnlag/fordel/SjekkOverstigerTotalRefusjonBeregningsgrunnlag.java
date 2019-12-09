package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class SjekkOverstigerTotalRefusjonBeregningsgrunnlag extends LeafSpecification<BeregningsgrunnlagPeriode> {

    private static final String ID = "FP_BR 22.3.2";
    private static final String BESKRIVELSE = "Overstiger totalt refusjonskrav totalt beregningsgrunnlag?";

    SjekkOverstigerTotalRefusjonBeregningsgrunnlag() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BigDecimal totalRefusjon = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL)
            .getArbeidsforholdIkkeFrilans().stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getRefusjonskravPrÅr)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        BigDecimal totaltBg = grunnlag.getBeregningsgrunnlagPrStatusSomSkalBrukes()
            .stream()
            .map(BeregningsgrunnlagPrStatus::getBruttoInkludertNaturalytelsePrÅr)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        SingleEvaluation resultat = totalRefusjon.compareTo(totaltBg) > 0 ? ja() : nei();
        resultat.setEvaluationProperty("totalRefusjonPrÅr", totalRefusjon);
        resultat.setEvaluationProperty("totaltBG", totaltBg);
        return resultat;
    }
}
