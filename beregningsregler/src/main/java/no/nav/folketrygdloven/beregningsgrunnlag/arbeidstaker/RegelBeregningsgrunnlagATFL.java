package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.specification.Specification;

public class RegelBeregningsgrunnlagATFL implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_14-15-27-28";
    private BeregningsgrunnlagPeriode regelmodell;

    public RegelBeregningsgrunnlagATFL(BeregningsgrunnlagPeriode regelmodell) {
        super();
        this.regelmodell = regelmodell;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        // TODO: Tror det er inni her man avgjør om ap settes
        var rs = new Ruleset<BeregningsgrunnlagPeriode>();

        IkkeBeregnet fastsettesVedSkjønnUtenTidsbegrensetArbeidsforhold = new FastsettesVedSkjønnUtenTidsbegrensetArbeidsforhold();

        IkkeBeregnet fastsettesVedSkjønnEtterTidsbegrensetArbeidsforhold = new FastsettesVedSkjønnEtterTidsbegrensetArbeidsforhold();


        // FP_BR 27.4 Er perioden opprettet som følge av et tidsbegrenset arbeidsforhold?

        var sjekkOmPeriodenErEtterTidsbegrensetArbeidsforhold =
            rs.beregningHvisRegel(new SjekkPeriodeÅrsakErTidsbegrensetArbeidsforhold(), fastsettesVedSkjønnEtterTidsbegrensetArbeidsforhold, fastsettesVedSkjønnUtenTidsbegrensetArbeidsforhold);

        // FP_BR 26.1 Skal vi sette aksjonspunkt?

        var skalSetteAksjonspunkt =
            rs.beregningHvisRegel(new SkalGjøreAvviksvurdering(), sjekkOmPeriodenErEtterTidsbegrensetArbeidsforhold, new Beregnet());

        // FP_BR 27.1 Har rapportert inntekt inkludert bortfaltnaturalytelse for 1. periode avvik mot sammenligningsgrunnlag > 25%?

        var sjekkÅrsinntektMotSammenligningsgrunnlag =
                rs.beregningHvisRegel(new SjekkÅrsinntektMotSammenligningsgrunnlag(), skalSetteAksjonspunkt, new Beregnet());


        // FP_BR 17.1 17.2 27.1 Sammenligningsgrunnlag pr år = sum av 12 siste måneder

        var fastsettSammenligningsgrunnlag =
                rs.beregningsRegel("FP_BR 17.1", "Fastsett sammenligningsgrunnlag for ATFL",
                        new FastsettSammenligningsgrunnlag(), sjekkÅrsinntektMotSammenligningsgrunnlag);

        // FP_BR 27.2 Skal vi sammeligne inntekt mot sammenligningsgrunnlaget?
        var skalÅrsinntektVurderesMotSammenligningsgrunnlaget =
            rs.beregningHvisRegel(new SkalSjekkeÅrsinntektMotSammenligningsgrunnlag(), fastsettSammenligningsgrunnlag, new Beregnet());

        // Første beregningsgrunnlagsperiode? Sjekk om vi skal fastsette sammenligninggrunnlag og sjekke det rapportert inntekt

        var sjekkOmFørstePeriode =
            rs.beregningHvisRegel(new SjekkOmFørsteBeregningsgrunnlagsperiode(), skalÅrsinntektVurderesMotSammenligningsgrunnlaget, new Beregnet());


        // Skal ATFL avviksvurderes?
        var sjekkHarBrukerKombinasjonsstatus =
                rs.beregningHvisRegel(new SkalAvviksvurdereATFL(), sjekkOmFørstePeriode, new Beregnet());

        // FP_BR 14.3 14.5 14.6 28.4 Beregnet pr år = sum alle inntekter

        var fastsettBeregnetPrÅr =
                rs.beregningsRegel("FP_BR 14.5", "Fastsett beregnet pr år for ATFL",
                        new FastsettBeregnetPrÅr(), sjekkHarBrukerKombinasjonsstatus);

        // For hver arbeidsgiver eller frilansinntekt: Fastsett brutto pr periodetype
        // FB_BR 14.3 28.2 Brutto pr periodetype = snitt av fastsatte inntekter av A-ordning * 12
        // FP_BR 15.2 Brutto pr periode_type = inntektsmelding sats * 12
        // FP_BR 15.1 Foreligger inntektsmelding?

        var arbeidsforhold = regelmodell.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
	    var speclist = arbeidsforhold.stream()
			    .map(a -> new RegelBeregnBruttoPrArbeidsforhold(a).getSpecification()
					    .medEvaluationProperty(new ServiceArgument("arbeidsforhold", a.getArbeidsforhold())))
			    .toList();
        var beregningsgrunnlagATFL =
                rs.beregningsRegel("FP_BR 14.X", "Fastsett beregningsgrunnlag pr arbeidsforhold", speclist, fastsettBeregnetPrÅr);

        // FP_BR X.X Ingen regelberegning hvis besteberegning gjelder

        var sjekkOmBesteberegning =
                rs.beregningHvisRegel(new SjekkOmBesteberegning(), new Beregnet(), beregningsgrunnlagATFL);

        return sjekkOmBesteberegning;
    }
}
