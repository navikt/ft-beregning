package no.nav.folketrygdloven.beregningsgrunnlag.inaktiv;

import java.util.Arrays;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.BeregnGjennomsnittligPGIForAktivitetstatus;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.BeregnOppjustertInntektForAktivitetstatus;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.FastsettBeregningsperiodeForAktivitetstatus;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.SjekkOmManueltFastsattBeregningsgrunnlagForAktivitetstatus;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Beregning av midlertidig ute av inntektsgivende etter §8-47 der det ikke er mottatt inntektsmelding.
 *
 * Bruker beregnes ved å ta snittinntekt fra de tre siste ferdiglignede år.
 *
 */
public class RegelBeregningsgrunnlagInaktivUtenIM implements RuleService<BeregningsgrunnlagPeriode> {


	@Override
	public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Specification<BeregningsgrunnlagPeriode> getSpecification() {
		Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

//      Beregn beregningsgrunnlag
		Specification<BeregningsgrunnlagPeriode> beregnBrutto =
				rs.beregningsRegel("FP_BR 2.8", "Beregn brutto beregningsgrunnlag brukers andel",
						new BeregnBruttoBeregningsgrunnlagInaktivUtenIM(), new Beregnet());

//      Har saksbehandler fastsatt beregningsgrunnlaget manuelt?
		Specification<BeregningsgrunnlagPeriode> sjekkOmManueltFastsattInntekt =
				rs.beregningHvisRegel(new SjekkOmManueltFastsattBeregningsgrunnlagForAktivitetstatus(AktivitetStatus.BA), new Beregnet(),
						beregnBrutto);

//      Beregn gjennomsnittlig PGI
//      Beregn oppjustert inntekt for årene i beregningsperioden
//      Fastsett beregningsperiode
		Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlagForSelvstendigNæringsdrivende =
				rs.beregningsRegel("FP_BR 2", "Foreslå beregningsgrunnlag for brukers andel",
						Arrays.asList(new FastsettBeregningsperiodeForAktivitetstatus(AktivitetStatus.BA),
								new BeregnOppjustertInntektForAktivitetstatus(AktivitetStatus.BA),
								new BeregnGjennomsnittligPGIForAktivitetstatus(AktivitetStatus.BA)), sjekkOmManueltFastsattInntekt);

		return foreslåBeregningsgrunnlagForSelvstendigNæringsdrivende;
	}
}
