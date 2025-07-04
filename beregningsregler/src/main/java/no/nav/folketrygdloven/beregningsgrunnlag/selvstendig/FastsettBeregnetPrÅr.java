package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettBeregnetPrÅr.ID)
public class FastsettBeregnetPrÅr extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.7";
    private static final String BESKRIVELSE = "Fastsett beregnet pr år";

	private AktivitetStatus aktivitetStatus;

    public FastsettBeregnetPrÅr(AktivitetStatus aktivitetStatus) {
        super(ID, BESKRIVELSE);
		this.aktivitetStatus = aktivitetStatus;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var bgps = grunnlag.getBeregningsgrunnlagPrStatus(aktivitetStatus);
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("skjæringstidspunkt", grunnlag.getSkjæringstidspunkt());
        resultater.put("grunnbeløp", grunnlag.getBeregningsgrunnlag().verdiAvG(grunnlag.getSkjæringstidspunkt()));
        resultater.put("beregnetPrÅr", bgps.getBeregnetPrÅr());
        return beregnet(resultater);
    }

}
