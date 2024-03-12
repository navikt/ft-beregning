package no.nav.folketrygdloven.beregningsgrunnlag.inaktiv;

import java.util.Arrays;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.SjekkOmFørsteBeregningsgrunnlagsperiode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.BeregnBruttoBeregningsgrunnlagFraPGI;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.BeregnGjennomsnittligPGIForAktivitetstatus;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.BeregnOppjustertInntektForAktivitetstatus;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.FastsettBeregnetPrÅr;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.FastsettBeregningsperiodeForAktivitetstatus;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.FastsettSammenligningsgrunnlagForAktivitetstatus;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.SjekkOmDifferanseStørreEnn25Prosent;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.SjekkOmManueltFastsattBeregningsgrunnlagForAktivitetstatus;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Beregner bruker som er midlertidig utenfor inntektsgivende arbeid etter §8-47
 * <p>
 * Normal beregning er snittinntekt fra de tre siste ferdiglignede år.
 * Dersom det er mottatt inntektsmelding eller registert inntekt i a-ordningen på skjæringstidspunktet regnes det avvik mot denne inntekten i henhold til § 8-35 tredje ledd.
 */
@RuleDocumentation(value = RegelBeregningsgrunnlagInaktivMedAvviksvurdering.ID)
public class RegelBeregningsgrunnlagInaktivMedAvviksvurdering implements RuleService<BeregningsgrunnlagPeriode> {

	static final String ID = "BR_8_47";

	@Override
	public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Specification<BeregningsgrunnlagPeriode> getSpecification() {
		Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

//      FP_BR 2.7 Fastsette beregnet pr år
		Specification<BeregningsgrunnlagPeriode> fastsettBeregnetPrÅr = new FastsettBeregnetPrÅr(AktivitetStatus.BA);

//      FP_BR 2.6 Opprette regelmerknad for å fastsette brutto_pr_aar manuelt
		Specification<BeregningsgrunnlagPeriode> opprettRegelmerknad =
				rs.beregningsRegel("BR_8_47_4", "Opprett regelmerknad", fastsettBeregnetPrÅr,
						new IkkeBeregnet(new BeregningUtfallMerknad(BeregningUtfallÅrsak.VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT_MIDLERTIDIG_INAKTIV)));

//      FP_BR 2.5 Er avvik > 25 %
		Specification<BeregningsgrunnlagPeriode> sjekkOmDifferanseStørreEnn25Prosent =
				rs.beregningHvisRegel(new SjekkOmDifferanseStørreEnn25Prosent(AktivitetStatus.BA), opprettRegelmerknad, fastsettBeregnetPrÅr);

//      FP_BR 2.4 Fastsett sammenligningsgrunnlag og beregn avvik
		Specification<BeregningsgrunnlagPeriode> beregnAvvik =
				rs.beregningsRegel("BR_8_47_3", "Fastsett sammenligningsgrunnlag og beregn avvik",
						new FastsettSammenligningsgrunnlagForAktivitetstatus(AktivitetStatus.BA), sjekkOmDifferanseStørreEnn25Prosent);

		// Første beregningsgrunnlagsperiode? Sammenligninggrunnlag skal fastsettes og sjekkes mot bare om det er første periode
		Specification<BeregningsgrunnlagPeriode> sjekkOmFørstePeriode =
				rs.beregningHvisRegel(new SjekkOmFørsteBeregningsgrunnlagsperiode(), beregnAvvik, fastsettBeregnetPrÅr);

//      FP_BR 2.3/2.3.3 Har bruker oppgitt varig endring eller nyoppstartet virksomhet?
		Specification<BeregningsgrunnlagPeriode> sjekkOmInnrapportertInntektVedSkjæringstidspunkt =
				rs.beregningHvisRegel(new SjekkOmBrukerHarRapporterteInntekterVedSkjæringstidspunkt(), sjekkOmFørstePeriode, fastsettBeregnetPrÅr);

		//      Beregn beregningsgrunnlag
		Specification<BeregningsgrunnlagPeriode> beregnBrutto =
				rs.beregningsRegel("BR_8_47_2", "Beregn brutto beregningsgrunnlag brukers andel",
						new BeregnBruttoBeregningsgrunnlagFraPGI(AktivitetStatus.BA), sjekkOmInnrapportertInntektVedSkjæringstidspunkt);

//      Har saksbehandler fastsatt beregningsgrunnlaget manuelt?
		Specification<BeregningsgrunnlagPeriode> sjekkOmManueltFastsattInntekt =
				rs.beregningHvisRegel(new SjekkOmManueltFastsattBeregningsgrunnlagForAktivitetstatus(AktivitetStatus.BA), sjekkOmInnrapportertInntektVedSkjæringstidspunkt,
						beregnBrutto);

//      Beregn gjennomsnittlig PGI
//      Beregn oppjustert inntekt for årene i beregningsperioden
//      Fastsett beregningsperiode
		Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlagForBrukersAndel =
				rs.beregningsRegel("BR_8_47_1", "Foreslå beregningsgrunnlag for brukers andel",
						Arrays.asList(new FastsettBeregningsperiodeForAktivitetstatus(AktivitetStatus.BA),
								new SettHjemmelInaktiv(),
								new BeregnOppjustertInntektForAktivitetstatus(AktivitetStatus.BA),
								new BeregnGjennomsnittligPGIForAktivitetstatus(AktivitetStatus.BA)), sjekkOmManueltFastsattInntekt);

		return foreslåBeregningsgrunnlagForBrukersAndel;
	}
}
