package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import java.util.Arrays;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.SjekkOmFørsteBeregningsgrunnlagsperiode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelBeregningsgrunnlagSN.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=174163430")
public class RegelBeregningsgrunnlagSN implements RuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_2";

	@Override
    public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        var rs = new Ruleset<BeregningsgrunnlagPeriode>();

//      FP_BR 2.7 Fastsette beregnet pr år
        Specification<BeregningsgrunnlagPeriode> fastsettBeregnetPrÅr = new FastsettBeregnetPrÅr(AktivitetStatus.SN);

//      FP_BR 2.6 Opprette regelmerknad for å fastsette brutto_pr_aar manuelt
        var opprettRegelmerknad =
            rs.beregningsRegel("FP_BR 2.6", "Opprett regelmerknad", fastsettBeregnetPrÅr,
                new IkkeBeregnet(new BeregningUtfallMerknad(BeregningUtfallÅrsak.VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT)));

//      FP_BR 2.5 Er avvik > 25 %
        var sjekkOmDifferanseStørreEnn25Prosent =
            rs.beregningHvisRegel(new SjekkOmDifferanseStørreEnn25Prosent(AktivitetStatus.SN), opprettRegelmerknad, fastsettBeregnetPrÅr);

//      FP_BR 2.4 Fastsett sammenligningsgrunnlag og beregn avvik
        var beregnAvvik =
            rs.beregningsRegel("FP_BR 2.4", "Fastsett sammenligningsgrunnlag og beregn avvik",
                new FastsettSammenligningsgrunnlagForAktivitetstatus(AktivitetStatus.SN), sjekkOmDifferanseStørreEnn25Prosent);

	    // Første beregningsgrunnlagsperiode? Sammenligninggrunnlag skal fastsettes og sjekkes mot bare om det er første periode
        var sjekkOmFørstePeriode =
			    rs.beregningHvisRegel(new SjekkOmFørsteBeregningsgrunnlagsperiode(), beregnAvvik, fastsettBeregnetPrÅr);

//      FP_BR 2.3/2.3.3 Har bruker oppgitt varig endring eller nyoppstartet virksomhet?
        var sjekkOmVarigEndringIVirksomhet =
            rs.beregningHvisRegel(new SjekkOmVarigEndringIVirksomhetEllerNyoppstartetNæring(), sjekkOmFørstePeriode, fastsettBeregnetPrÅr);

//      FP_BR 2.8 Beregn beregningsgrunnlag SN
        var beregnBruttoSN =
            rs.beregningsRegel("FP_BR 2.8", "Beregn brutto beregningsgrunnlag selvstendig næringsdrivende",
                new BeregnBruttoBeregningsgrunnlagFraPGI(AktivitetStatus.SN), sjekkOmVarigEndringIVirksomhet);

//      FP_BR 2.19 Har saksbehandler fastsatt beregningsgrunnlaget manuelt?
        var sjekkOmManueltFastsattInntekt =
            rs.beregningHvisRegel(new SjekkOmManueltFastsattBeregningsgrunnlagForAktivitetstatus(AktivitetStatus.SN), sjekkOmVarigEndringIVirksomhet,
                beregnBruttoSN);

//      FP_BR 2.18 Er bruker SN som er ny i arbeidslivet?
        var sjekkOmNyIArbeidslivetSN =
            rs.beregningHvisRegel(new SjekkOmBrukerErNyIArbeidslivet(), new IkkeBeregnet(SjekkOmBrukerErNyIArbeidslivet.FASTSETT_BG_FOR_SN_NY_I_ARBEIDSLIVET),
                sjekkOmManueltFastsattInntekt);


        // FP_BR 2.20 Er beregningsgrunnlaget besteberegnet?
        var erBeregningsgrunnlagetBesteberegnet =
            rs.beregningHvisRegel(new SjekkOmBeregninsgrunnlagErBesteberegnet(), new Beregnet(),
                sjekkOmNyIArbeidslivetSN);


//      FP_BR 2.2 Beregn gjennomsnittlig PGI
//      FP_BR 2.9 Beregn oppjustert inntekt for årene i beregningsperioden
//      FP_BR 2.1 Fastsett beregningsperiode
        var foreslåBeregningsgrunnlagForSelvstendigNæringsdrivende =
            rs.beregningsRegel("FP_BR 2", "Foreslå beregningsgrunnlag for selvstendig næringsdrivende",
                Arrays.asList(new FastsettBeregningsperiodeForAktivitetstatus(AktivitetStatus.SN), new SettHjemmelSN(), new BeregnOppjustertInntektForAktivitetstatus(AktivitetStatus.SN), new BeregnGjennomsnittligPGIForAktivitetstatus(AktivitetStatus.SN)), erBeregningsgrunnlagetBesteberegnet);

        return foreslåBeregningsgrunnlagForSelvstendigNæringsdrivende;
    }
}
