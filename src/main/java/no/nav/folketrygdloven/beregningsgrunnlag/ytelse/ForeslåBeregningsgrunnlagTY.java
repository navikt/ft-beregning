package no.nav.folketrygdloven.beregningsgrunnlag.ytelse;

import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel.F_14_7;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel.F_9_9;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ForeslåBeregningsgrunnlagTY.ID)
class ForeslåBeregningsgrunnlagTY extends LeafSpecification<BeregningsgrunnlagPeriode> {

	static final String ID = "FP_BR 30";
	static final String BESKRIVELSE = "Foreslå beregningsgrunnlag for status tilstøtende ytelse";

	ForeslåBeregningsgrunnlagTY() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		Map<String, Object> resultater = new HashMap<>();
		var hjemmel = grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag().erKap9Ytelse() ? F_9_9 : F_14_7;
		grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.KUN_YTELSE)
				.setHjemmel(hjemmel);
		BigDecimal brutto = grunnlag.getBruttoPrÅr();
		resultater.put("beregnetPrÅr", brutto);
		resultater.put("hjemmel", hjemmel);
		return beregnet(resultater);
	}
}
