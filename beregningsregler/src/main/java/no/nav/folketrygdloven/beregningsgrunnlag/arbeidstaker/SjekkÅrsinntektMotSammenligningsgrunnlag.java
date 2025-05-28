package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkÅrsinntektMotSammenligningsgrunnlag.ID)
class SjekkÅrsinntektMotSammenligningsgrunnlag extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 27.1";
    static final String BESKRIVELSE = "Har beregnet årsinntekt avvik mot sammenligningsgrunnlag mer enn 25% ?";


    SjekkÅrsinntektMotSammenligningsgrunnlag() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.AT_FL);
	    if (sg.getRapportertPrÅr() == null) {
           throw new IllegalStateException("Utviklerfeil: Skal alltid ha rapportertPrÅr her.");
        }

        if (sg.getRapportertPrÅr().compareTo(BigDecimal.ZERO) <= 0) {
            sg.setAvvikProsent(BigDecimal.valueOf(100)); //Setter avviksprosenten til 100 når ingen inntekt (for ikke å dele på 0), saksbehandler avgjør deretter
            var resultat = ja();
            regelsporing(grunnlag, sg, resultat);
            return resultat;
        }

        var bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        var naturalytelseBortfaltPrÅr = bgps.samletNaturalytelseBortfaltMinusTilkommetPrÅr();
        var rapporertInntekt = bgps.getBeregnetPrÅr().add(naturalytelseBortfaltPrÅr);

        var diff = rapporertInntekt.subtract(sg.getRapportertPrÅr()).abs();
        var avvikProsent = diff.divide(sg.getRapportertPrÅr(), 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        sg.setAvvikProsent(avvikProsent);

        var resultat = avvikProsent.compareTo(grunnlag.getAvviksgrenseProsent()) > 0 ? ja() : nei();
        regelsporing(grunnlag, sg, resultat);
        return resultat;
    }

    private void regelsporing(BeregningsgrunnlagPeriode grunnlag, SammenligningsGrunnlag sg, SingleEvaluation resultat) {
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("opptjentPrÅr", grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBeregnetPrÅr());
        resultater.put("sammenligningsgrunnlag", sg.getRapportertPrÅr());
        resultater.put("avvikProsent", sg.getAvvikProsent());
        resultat.setEvaluationProperties(resultater);
    }
}
