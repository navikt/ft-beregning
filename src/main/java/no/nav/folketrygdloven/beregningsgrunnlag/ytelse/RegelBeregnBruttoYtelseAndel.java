package no.nav.folketrygdloven.beregningsgrunnlag.ytelse;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelBeregnBruttoYtelseAndel implements RuleService<BeregningsgrunnlagPeriode> {

	public static final String ID = "FP_BR 30.1";
	public static final String BESKRIVELSE = "Kjører beregning for en andel. Enten satt av saksbehandler eller som skal fastsettes fra ytelsevedtak";


	private BeregningsgrunnlagPeriode regelmodell;

	public RegelBeregnBruttoYtelseAndel(BeregningsgrunnlagPeriode regelmodell) {
		super();
		this.regelmodell = regelmodell;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Specification<BeregningsgrunnlagPeriode> getSpecification() {

		Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

		// FP_BR 15.6 Rapportert inntekt = manuelt fastsatt månedsinntekt * 12
		Specification<BeregningsgrunnlagPeriode> fastsettFraYtelsevedtak =
				rs.beregningsRegel(BeregnFraYtelsevedtak.ID,
						BeregnFraYtelsevedtak.BESKRIVELSE,
						new BeregnFraYtelsevedtak(), new Beregnet());
		// FP_BR 14.1 Er bruker arbeidstaker?

		Specification<BeregningsgrunnlagPeriode> fastsettBruttoBeregningsgrunnlag =
				rs.beregningHvisRegel(new SjekkHarSaksbehandlerSattInntektFraYtelseManuelt(),
						new Beregnet(),
						fastsettFraYtelsevedtak);

		return fastsettBruttoBeregningsgrunnlag;
	}
}
