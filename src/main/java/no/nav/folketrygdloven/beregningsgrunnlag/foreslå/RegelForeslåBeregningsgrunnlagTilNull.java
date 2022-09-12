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

	private AktivitetStatus aktivitetStatus;

	public RegelForeslåBeregningsgrunnlagTilNull(AktivitetStatus aktivitetStatus) {
		super();
		this.aktivitetStatus = aktivitetStatus;
	}

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // FP_BR NULL Sett brutto BG til 0
        Specification<BeregningsgrunnlagPeriode> settTilNull = rs.beregningsRegel("FP_BR NULL", "Beregn brutto beregingsgrunnlag for ukjent status",
            new FastsettTilNull(aktivitetStatus), new Beregnet());


        return settTilNull;
    }
}
