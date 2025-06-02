package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.SjekkBeregningsgrunnlagStørreEnnGrenseverdi;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFinnGrenseverdiFRISINN implements EksportRegel<BeregningsgrunnlagPeriode> {

    public static final String ID = "FRISINN 6.1";
    public static final String BESKRIVELSE = "Finner grenseverdi for FRISINN ytelsen";

	@Override
	public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

	@SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        var rs = new Ruleset<BeregningsgrunnlagPeriode>();

        // Fastsett avkortet BG
        Specification<BeregningsgrunnlagPeriode> fastsettUavkortetGrenseverdi = new FinnGrenseverdiForTotalUnder6G();

        // Fastsett avkortet BG
        Specification<BeregningsgrunnlagPeriode> fastsettAvkortetGrenseverdi = new FinnGrenseverdiForTotalOver6G();

        // FP_BR_29.4 4. Brutto beregnings-grunnlag totalt > 6G?
        var beregnEventuellAvkorting = rs.beregningHvisRegel(
            new SjekkBeregningsgrunnlagStørreEnnGrenseverdi(),
            fastsettAvkortetGrenseverdi,
            fastsettUavkortetGrenseverdi);

        Specification<BeregningsgrunnlagPeriode> settGrenseverdiTilNull = new SettGrenseverdiTilNull();

        // FRISINN 6.10: Er vilkår oppfylt?
        var erVilkårOppfylt = rs.beregningHvisRegel(
            new ErVilkårOppfylt(),
            beregnEventuellAvkorting,
            settGrenseverdiTilNull);


        return erVilkårOppfylt;
    }
}
