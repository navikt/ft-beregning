package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SettAvvikÅrsinntektMotSammenligningsgrunnlagAt.ID)
class SettAvvikÅrsinntektMotSammenligningsgrunnlagAt extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 28.7";
    static final String BESKRIVELSE = "Sett beregnet årsinntekt avvik mot sammenligningsgrunnlag";


    SettAvvikÅrsinntektMotSammenligningsgrunnlagAt() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
        SammenligningsGrunnlag sg = grunnlag.getBeregningsgrunnlag().getSammenligningsGrunnlagPrAktivitetstatus().get(AktivitetStatus.AT);
        if (sg == null || sg.getRapportertPrÅr() == null) {
            throw new IllegalStateException("Utviklerfeil: Skal alltid ha sammenligningsgrunnlag her.");
        }

        if (sg.getRapportertPrÅr().compareTo(BigDecimal.ZERO) <= 0) {
            sg.setAvvikProsent(BigDecimal.valueOf(100)); //Setter avviksprosenten til 100 når ingen inntekt (for ikke å dele på 0), saksbehandler avgjør deretter
            resultater.put("avvikProsent", sg.getAvvikProsent());
            return beregnet(resultater);
        }

        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        BigDecimal naturalytelseBortfaltPrÅr = bgps.samletNaturalytelseBortfaltMinusTilkommetPrÅr();
        BigDecimal rapporertInntekt = bgps.getBeregnetPrÅrForAT().add(naturalytelseBortfaltPrÅr);

        BigDecimal diff = rapporertInntekt.subtract(sg.getRapportertPrÅr()).abs();
        BigDecimal avvikProsent = diff.divide(sg.getRapportertPrÅr(), 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        sg.setAvvikProsent(avvikProsent);
        resultater.put("avvikProsent", sg.getAvvikProsent());
        return beregnet(resultater);
    }
}
