package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.reduksjon.ReduserBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ReduserBeregningsgrunnlag.ID)
public class FastsettForSN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR 6.2";
    public static final String BESKRIVELSE = "Fastsetter beregnignsgrunnlag for frilans";

    public FastsettForSN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();


        var atflAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        var fastsattTilFrilans = atflAndel == null ? BigDecimal.ZERO : atflAndel
            .getFrilansArbeidsforhold()
            .map(BeregningsgrunnlagPrArbeidsforhold::getAvkortetPrÅr)
            .orElse(BigDecimal.ZERO);

        var totalTilFastsetting = grunnlag.getGrenseverdi().subtract(fastsattTilFrilans).max(BigDecimal.ZERO);

        var snStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);

        if (snStatus != null) {
            var bortfaltSN = snStatus.getGradertBruttoInkludertNaturalytelsePrÅr();
            if (bortfaltSN.compareTo(totalTilFastsetting) >= 0) {
                BeregningsgrunnlagPrStatus.builder(snStatus)
                    .medAvkortetPrÅr(totalTilFastsetting)
                    .build();
            } else {
                throw new IllegalStateException("Skal ikke ha igjen grunnlag etter fastsetting");
            }
            resultater.put("avkortetSN", snStatus.getAvkortetPrÅr());
        }
        var resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;

    }
}
