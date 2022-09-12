package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.sykepenger.BeregnPrArbeidsforholdNaturalytelseBortfaltForSykepenger;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.sykepenger.SjekkOmBeregningenGjelderSykepenger;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.sykepenger.SjekkOmBortfallAvNaturalYtelseIArbeidsgiverperiodenSykepenger;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.specification.Specification;

public class RegelBeregnBruttoPrArbeidsforhold implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR 14.1";

	private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

	public RegelBeregnBruttoPrArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
		this.arbeidsforhold = arbeidsforhold;
	}

	@SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {

        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        //FP_BR 15.6 Beregn naturalytelse -= (verdi x 12)
        //FP_BR 15.5 Naturalytelse som tilkommer ved start av perioden eller i tidligere perioder?
        Specification<BeregningsgrunnlagPeriode> tilkommetNaturalYtelse =
            rs.beregningHvisRegel(new SjekkOmTilkommetNaturalytelse(arbeidsforhold),
                new BeregnPrArbeidsforholdNaturalytelseTilkommet(arbeidsforhold), new Beregnet());

        //FP_BR 15.4 Beregn naturalytelse
        Specification<BeregningsgrunnlagPeriode> beregnBortfallNaturalYtelse =
            rs.beregningsRegel(BeregnPrArbeidsforholdNaturalytelseBortfalt.ID, BeregnPrArbeidsforholdNaturalytelseBortfalt.BESKRIVELSE,
                new BeregnPrArbeidsforholdNaturalytelseBortfalt(arbeidsforhold), tilkommetNaturalYtelse);

        //FP_BR 15.3 Bortfall av naturalytelse ved start av perioden eller i tidligere periode?
        Specification<BeregningsgrunnlagPeriode> erDetBortfallAvNaturalYtelse =
            rs.beregningHvisRegel(new SjekkOmBortfallAvNaturalytelse(arbeidsforhold), beregnBortfallNaturalYtelse, tilkommetNaturalYtelse);

        //FP_BR 15.9 Beregn bortfalt naturalytelse i arbeidsgiverperioden for sykepenger -> naturalytelseverdi * 12
        Specification<BeregningsgrunnlagPeriode> beregnBortfallAvNaturalYtelseIArbeidsgiverperiodenSykepenger =
            rs.beregningsRegel(BeregnPrArbeidsforholdNaturalytelseBortfalt.ID, BeregnPrArbeidsforholdNaturalytelseBortfalt.BESKRIVELSE,
                new BeregnPrArbeidsforholdNaturalytelseBortfaltForSykepenger(arbeidsforhold), new Beregnet());

        // FP_BR 15.8 Er det bortfall av naturalytelse i arbeidsgiverperioden (gjelder sykepenger spesifikt)?
        Specification<BeregningsgrunnlagPeriode> sjekkOmBortfallNaturalytelseIArbeidsgiverperioden =
            rs.beregningHvisRegel(new SjekkOmBortfallAvNaturalYtelseIArbeidsgiverperiodenSykepenger(arbeidsforhold),
                beregnBortfallAvNaturalYtelseIArbeidsgiverperiodenSykepenger, new Beregnet())
		            .medScope(new ServiceArgument("arbeidsforhold", arbeidsforhold.getArbeidsforhold()));

        //FP_BR 15.7 Gjelder beregningen ytelsen foreldrepenger eller sykepenger?
        Specification<BeregningsgrunnlagPeriode> gjelderBeregningenYtelsenSykepenger =
            rs.beregningHvisRegel(new SjekkOmBeregningenGjelderSykepenger(),
                sjekkOmBortfallNaturalytelseIArbeidsgiverperioden, erDetBortfallAvNaturalYtelse);

        // FP_BR 15.2 Brutto pr periode_type = inntektsmelding sats * 12
        Specification<BeregningsgrunnlagPeriode> beregnPrArbeidsforholdFraInntektsmelding =
            rs.beregningsRegel(BeregnPrArbeidsforholdFraInntektsmelding.ID, BeregnPrArbeidsforholdFraInntektsmelding.BESKRIVELSE,
                new BeregnPrArbeidsforholdFraInntektsmelding(arbeidsforhold), gjelderBeregningenYtelsenSykepenger);

        // FB_BR 14.3 Brutto pr periodetype = snitt av fastsatte inntekter av A-ordning * 12
        Specification<BeregningsgrunnlagPeriode> beregnPrArbeidsforholdFraAOrdningen = new BeregnPrArbeidsforholdFraAOrdningen(arbeidsforhold);


        // FP_BR 15.6 Rapportert inntekt = manuelt fastsatt månedsinntekt * 12
        Specification<BeregningsgrunnlagPeriode> beregnÅrsinntektVedManuellFastsettelse =
            rs.beregningsRegel(BeregnRapportertInntektVedManuellFastsettelse.ID, BeregnRapportertInntektVedManuellFastsettelse.BESKRIVELSE,
                new BeregnRapportertInntektVedManuellFastsettelse(arbeidsforhold), new Beregnet());

        // FP_BR 15.1 Foreligger inntektsmelding?
        Specification<BeregningsgrunnlagPeriode> sjekkOmInntektsmeldingForeligger =
            rs.beregningHvisRegel(new SjekkOmInntektsmeldingForeligger(arbeidsforhold),
                beregnPrArbeidsforholdFraInntektsmelding, beregnPrArbeidsforholdFraAOrdningen)
		            .medScope(new ServiceArgument("arbeidsforhold", arbeidsforhold.getArbeidsforhold()));

        // FP_BR 15.5 Har saksbehandler fastsatt månedsinntekt manuelt?
        Specification<BeregningsgrunnlagPeriode> manueltFastsattInntekt = rs.beregningHvisRegel(
            new SjekkHarSaksbehandlerSattInntektManuelt(arbeidsforhold), beregnÅrsinntektVedManuellFastsettelse, sjekkOmInntektsmeldingForeligger);

        // FP_BR 14.9 Er bruker nyoppstartet frilanser?
        Specification<BeregningsgrunnlagPeriode> sjekkOmNyoppstartetFrilanser =
            rs.beregningHvisRegel(new SjekkOmFrilansInntektErFastsattAvSaksbehandler(arbeidsforhold),
                new BeregnGrunnlagNyoppstartetFrilanser(arbeidsforhold), beregnPrArbeidsforholdFraAOrdningen);

        // FP_BR 14.1 Er bruker arbeidstaker?

        Specification<BeregningsgrunnlagPeriode> fastsettBruttoBeregningsgrunnlag =
            rs.beregningHvisRegel(new SjekkOmBrukerErArbeidstaker(arbeidsforhold), manueltFastsattInntekt, sjekkOmNyoppstartetFrilanser);

        return fastsettBruttoBeregningsgrunnlag;
    }
}
