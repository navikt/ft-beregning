package no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi;

import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.SjekkBeregningsgrunnlagStørreEnnGrenseverdi;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Fastsatt;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFinnGrenseverdiMedFordeling implements EksportRegel<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_29_med_fordeling";

	private BeregningsgrunnlagPeriode regelmodell;

	public RegelFinnGrenseverdiMedFordeling(BeregningsgrunnlagPeriode regelmodell) {
		this.regelmodell = regelmodell;
	}

	@Override
	public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

	@SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        var rs = new Ruleset<BeregningsgrunnlagPeriode>();

        // Fastsett grenseverdi for fastsetting av beregningsgrunnlag
        var fastsettGrenseverdi = rs.beregningsRegel(FinnGrenseverdi.ID, FinnGrenseverdi.BESKRIVELSE,
            new FinnGrenseverdi(), new Fastsatt());

        // Fastsett avkortet BG
        var fastsettAvkortet = rs.beregningsRegel(
            RegelFastsettAndelBGOver6G.ID,
            RegelFastsettAndelBGOver6G.BESKRIVELSE,
            new RegelFastsettAndelBGOver6G(regelmodell).getSpecification(),
            fastsettGrenseverdi);

        // Fastsett uten avkorting
        var fastsettUtenAvkorting = rs.beregningsRegel(
            "FP_BR_29.6_med_fordeling",
            "Fastsett BG uten avkorting",
            new FastsettAndelLikBruttoBG(),
            fastsettGrenseverdi);

        // FP_BR_29.4 4. Brutto beregnings-grunnlag totalt > 6G?
        var beregnEventuellAvkorting = rs.beregningHvisRegel(
            new SjekkBeregningsgrunnlagStørreEnnGrenseverdi(),
            fastsettAvkortet,
            fastsettUtenAvkorting);

        return beregnEventuellAvkorting;
    }
}
