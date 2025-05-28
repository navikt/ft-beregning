package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SkalSjekkeÅrsinntektMotSammenligningsgrunnlag.ID)
class SkalSjekkeÅrsinntektMotSammenligningsgrunnlag extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 27.2";
    static final String BESKRIVELSE = "Skal vi sammenligne brutto mot sammenligningsgrunnlaget?";


    SkalSjekkeÅrsinntektMotSammenligningsgrunnlag() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var søkerHarMilitærstatus = grunnlag.getAktivitetStatuser().stream().anyMatch(ak -> ak.getAktivitetStatus().equals(AktivitetStatus.MS));
        // Hvis søker skal ha militærberegning og har under 3G skal ikke avvik beregnes
        var bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        var naturalytelseBortfaltPrÅr = bgps.samletNaturalytelseBortfaltMinusTilkommetPrÅr();
        var rapporertInntekt = bgps.getBeregnetPrÅr().add(naturalytelseBortfaltPrÅr);
        var beløpMilitærHarKravPå = grunnlag.getMinsteinntektMilitærHarKravPå();

        if (søkerHarMilitærstatus && søkerSkalHaMilitærberegning(rapporertInntekt, beløpMilitærHarKravPå)) {
            var resultat = nei();
            regelsporing(rapporertInntekt, beløpMilitærHarKravPå, true, resultat);
            return resultat;
        }

        var resultat = ja();
        regelsporing(rapporertInntekt, beløpMilitærHarKravPå, false, resultat);
        return resultat;
    }

    private boolean søkerSkalHaMilitærberegning(BigDecimal rapporertInntekt, BigDecimal beløpMilitærHarKravPå) {
        return rapporertInntekt.compareTo(beløpMilitærHarKravPå) < 0;
    }

    private void regelsporing(BigDecimal rapporertInntekt,
                              BigDecimal beløpMilitærHarKravPå,
                              boolean søkerSkalHaMilitærBeregning,
                              SingleEvaluation resultat) {
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("rapportertInntekt", rapporertInntekt);
        resultater.put("beløpMilitærHarKravPå", beløpMilitærHarKravPå);
        resultater.put("søkerSkalHaMilitærberegning", søkerSkalHaMilitærBeregning);
        resultat.setEvaluationProperties(resultater);
    }
}
