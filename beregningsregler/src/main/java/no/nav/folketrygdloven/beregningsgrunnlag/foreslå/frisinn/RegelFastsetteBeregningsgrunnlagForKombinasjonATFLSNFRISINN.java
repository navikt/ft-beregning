package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNFRISINN implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FRISINN 2.8";

	private BeregningsgrunnlagPeriode regelmodell;

	public RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNFRISINN(BeregningsgrunnlagPeriode regelmodell) {
		this.regelmodell = regelmodell;
	}

	@SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        var rs = new Ruleset<BeregningsgrunnlagPeriode>();

        var beregnBruttoSN =
            rs.beregningsRegel("FP_BR 2.2 - 2.10", "Beregn SN-andel", new BeregnBruttoBeregningsgrunnlagSNFRISINN(), new Beregnet());

        var fastsettBeregningsperiode =
            rs.beregningsRegel("FP_BR 2", "Fastsett beregningsperiode.",
                new FastsettBeregningsperiodeSNFRISINN(), beregnBruttoSN);

        var beregningsgrunnlagKombinasjon =
            rs.beregningsRegel("FP_BR_14-15-27-28", "Beregn beregningsgrunnlag for arbeidstaker/frilanser)",
                new RegelBeregningsgrunnlagATFLFRISINN(regelmodell).getSpecification(), fastsettBeregningsperiode);

        return beregningsgrunnlagKombinasjon;
    }
}
