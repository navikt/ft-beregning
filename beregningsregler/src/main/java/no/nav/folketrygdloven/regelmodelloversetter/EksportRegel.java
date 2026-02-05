package no.nav.folketrygdloven.regelmodelloversetter;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.fpsak.nare.RuleService;

public interface EksportRegel<T> extends RuleService<T> {

	default RegelResultat evaluerRegel(T input) {
		var inputJson = RegelJsonMapper.asJson(input);
		var evaluation = evaluer(input);
		return RegelmodellOversetter.getRegelResultat(evaluation, inputJson);
	}

	default RegelResultat evaluerRegel(T input, Object output) {
		var inputJson = RegelJsonMapper.asJson(input);
		var evaluation = evaluer(input, output);
		return RegelmodellOversetter.getRegelResultat(evaluation, inputJson);
	}
}
