package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.AvslagUnderEnHalvG;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

public class RegelVurderBeregningsgrunnlag implements EksportRegel<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_29";

	@Override
	public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

	@SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // FP_VK_32.2 2. Opprett regelmerknad (avslag)
        Specification<BeregningsgrunnlagPeriode> avslagUnderEnHalvG = new AvslagUnderEnHalvG();

        // FP_VK_32.1 1. Brutto BG > 0,5G ?
        Specification<BeregningsgrunnlagPeriode> sjekkOmBGUnderHalvG = rs.beregningHvisRegel(new SjekkBeregningsgrunnlagMindreEnn(),
            avslagUnderEnHalvG, new Beregnet());

		// FP_VK_32.3 Gjelder omsorgspenger til arbeidsgiver ?
		Specification<BeregningsgrunnlagPeriode> sjekkOmsorgspengerTilArbeidsgiver = rs.beregningHvisRegel(new SjekkErOmsorgspengerTilArbeidsgiver(),
				new Beregnet(), sjekkOmBGUnderHalvG);

        return sjekkOmsorgspengerTilArbeidsgiver;
    }
}
