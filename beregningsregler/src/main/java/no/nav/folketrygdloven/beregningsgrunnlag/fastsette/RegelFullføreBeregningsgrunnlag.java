package no.nav.folketrygdloven.beregningsgrunnlag.fastsette;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.RegelFastsettUtenAvkortingATFL;
import no.nav.folketrygdloven.beregningsgrunnlag.avkorting.RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G;
import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.refusjon.FastsettMaksimalRefusjon;
import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.refusjon.over6g.RegelFastsettAvkortetVedRefusjonOver6G;
import no.nav.folketrygdloven.beregningsgrunnlag.reduksjon.FastsettDagsatsPrAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.reduksjon.ReduserBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFullføreBeregningsgrunnlag implements EksportRegel<BeregningsgrunnlagPeriode> {

	public static final String ID = "FP_BR_29";

	private BeregningsgrunnlagPeriode regelmodell;

	public RegelFullføreBeregningsgrunnlag(BeregningsgrunnlagPeriode regelmodell) {
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

		// FP_BR_6.1 18. Fastsett Reduksjon for hver beregningsgrunnlagsandel og totalt beregningrunnlag etter reduksjon
		// FP_BR_6.2 19. Fastsett dagsats pr beregningsgrunnlagsandel og totalt
        var fastsettReduksjon = rs.beregningsRegel(ReduserBeregningsgrunnlag.ID, ReduserBeregningsgrunnlag.BESKRIVELSE,
				new ReduserBeregningsgrunnlag(), new FastsettDagsatsPrAndel());

		// 13 Fastsett avkortet BG når refusjon over 6G
        var fastsettAvkortetVedRefusjonOver6G = rs.beregningsRegel(
				RegelFastsettAvkortetVedRefusjonOver6G.ID,
				RegelFastsettAvkortetVedRefusjonOver6G.BESKRIVELSE,
				new RegelFastsettAvkortetVedRefusjonOver6G(regelmodell).getSpecification(),
				fastsettReduksjon);

		// 8. Fastsett avkortet BG når refusjon under 6G
        var fastsettAvkortetVedRefusjonUnder6G = rs.beregningsRegel(
				RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G.ID,
				RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G.BESKRIVELSE,
				new RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G(regelmodell).getSpecification(),
				fastsettReduksjon);

		// FP_BR_29.7 7. Sjekk om summen av maksimal refusjon overstiger 6G
        var sjekkMaksimaltRefusjonskrav = rs.beregningHvisRegel(new SjekkSumMaxRefusjonskravStørreEnn6G(),
				fastsettAvkortetVedRefusjonOver6G, fastsettAvkortetVedRefusjonUnder6G);

		// 6. Fastsett uten avkorting
        var fastsettUtenAvkorting = rs.beregningsRegel("FP_BR_29.6", "Fastsett BG uten avkorting",
				new RegelFastsettUtenAvkortingATFL().getSpecification(), fastsettReduksjon);

		// FP_BR_29.4 4. Brutto beregnings-grunnlag totalt > 6G?
        var beregnEventuellAvkorting = rs.beregningHvisRegel(new SjekkGradertBeregningsgrunnlagStørreEnnGrenseverdi(), sjekkMaksimaltRefusjonskrav, fastsettUtenAvkorting);

		// FP_BR_29.3 3. For hver beregningsgrunnlagsandel: Fastsett Refusjonskrav for beregnings-grunnlagsandel
        var fastsettMaksimalRefusjon = rs.beregningsRegel(FastsettMaksimalRefusjon.ID, FastsettMaksimalRefusjon.BESKRIVELSE,
				new FastsettMaksimalRefusjon(), beregnEventuellAvkorting);

		return fastsettMaksimalRefusjon;
	}
}
