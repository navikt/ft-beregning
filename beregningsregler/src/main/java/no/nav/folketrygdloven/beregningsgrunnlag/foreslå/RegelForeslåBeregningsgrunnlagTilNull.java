package no.nav.folketrygdloven.beregningsgrunnlag.foreslå;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelForeslåBeregningsgrunnlagTilNull.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=216009135")
public class RegelForeslåBeregningsgrunnlagTilNull implements RuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_NULL";

	private AktivitetStatus status;

	public RegelForeslåBeregningsgrunnlagTilNull(AktivitetStatus aktivitetStatus) {
		this.status= aktivitetStatus;
	}

	@SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        var rs = new Ruleset<BeregningsgrunnlagPeriode>();

        // FP_BR NULL Sett brutto BG til 0
        return rs.beregningsRegel(
            "FP_BR NULL", "Beregn brutto beregingsgrunnlag for ukjent status", new FastsettTilNull(status), new Beregnet());
    }
}
