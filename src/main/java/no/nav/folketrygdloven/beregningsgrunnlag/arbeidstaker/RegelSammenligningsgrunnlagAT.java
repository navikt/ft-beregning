package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelSammenligningsgrunnlagAT implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_28.5";

    public RegelSammenligningsgrunnlagAT() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        IkkeBeregnet fastsettesVedSkjønnUtenTidsbegrensetArbeidsforhold = new FastsettesVedSkjønnUtenTidsbegrensetArbeidsforholdAT();

        IkkeBeregnet fastsettesVedSkjønnEtterTidsbegrensetArbeidsforhold = new FastsettesVedSkjønnEtterTidsbegrensetArbeidsforholdAT();


        // FP_BR 28.8 Er perioden opprettet som følge av et tidsbegrenset arbeidsforhold?

        Specification<BeregningsgrunnlagPeriode> sjekkOmPeriodenErEtterTidsbegrensetArbeidsforhold =
            rs.beregningHvisRegel(new SjekkPeriodeÅrsakErTidsbegrensetArbeidsforhold(), fastsettesVedSkjønnEtterTidsbegrensetArbeidsforhold, fastsettesVedSkjønnUtenTidsbegrensetArbeidsforhold);

        // FP_BR 26.1 Har rapportert inntekt avvik mot sammenligningsgrunnlag > 25%?

        Specification<BeregningsgrunnlagPeriode> sjekkAvvikSammenligningsgrunnlagMotAvviksgrense =
            rs.beregningHvisRegel(new SjekkAvvikSammenligningsgrunnlagMotAvviksgrense(), sjekkOmPeriodenErEtterTidsbegrensetArbeidsforhold, new Beregnet());

        // FP_BR 28.7 Sett avvik inntekter mot beregnet

        Specification<BeregningsgrunnlagPeriode> sjekkÅrsinntektMotSammenligningsgrunnlag =
            rs.beregningsRegel("FP_BR 28.6", "Sett beregnet årsinntekt avvik mot sammenligningsgrunnlag",
                new SettAvvikÅrsinntektMotSammenligningsgrunnlagAt(), sjekkAvvikSammenligningsgrunnlagMotAvviksgrense);


        // FP_BR 28.6 Sammenligningsgrunnlag pr år = sum av 12 siste måneder

        Specification<BeregningsgrunnlagPeriode> fastsettSammenligningsgrunnlag =
            rs.beregningsRegel("FP_BR 28.6", "Fastsett sammenligningsgrunnlag for AT",
                new FastsettSammenligningsgrunnlagAT(), sjekkÅrsinntektMotSammenligningsgrunnlag);

        // Første beregningsgrunnlagperiode?
        Specification<BeregningsgrunnlagPeriode> sjekkOmFørstePeriode =
            rs.beregningHvisRegel(new SjekkOmFørsteBeregningsgrunnlagsperiode(), fastsettSammenligningsgrunnlag, sjekkAvvikSammenligningsgrunnlagMotAvviksgrense);

        return sjekkOmFørstePeriode;
    }
}
