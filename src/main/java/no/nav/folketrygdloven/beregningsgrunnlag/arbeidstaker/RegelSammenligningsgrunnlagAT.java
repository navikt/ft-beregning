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

        // FP_BR 26.1 Skal vi sette aksjonspunkt?

        Specification<BeregningsgrunnlagPeriode> skalSetteAksjonspunkt =
            rs.beregningHvisRegel(new SkalSetteAksjonspunkt(), sjekkOmPeriodenErEtterTidsbegrensetArbeidsforhold, new Beregnet());

        // FP_BR 28.7 Har rapportert inntekt inkludert bortfaltnaturalytelse for 1. periode avvik mot sammenligningsgrunnlag > 25%?

        Specification<BeregningsgrunnlagPeriode> sjekkÅrsinntektMotSammenligningsgrunnlag =
            rs.beregningHvisRegel(new SjekkÅrsinntektMotSammenligningsgrunnlagAT(), skalSetteAksjonspunkt, new Beregnet());

        // FP_BR 28.6 Sammenligningsgrunnlag pr år = sum av 12 siste måneder

        Specification<BeregningsgrunnlagPeriode> fastsettSammenligningsgrunnlag =
            rs.beregningsRegel("FP_BR 28.6", "Fastsett sammenligningsgrunnlag for AT",
                new FastsettSammenligningsgrunnlagAT(), sjekkÅrsinntektMotSammenligningsgrunnlag);

        return fastsettSammenligningsgrunnlag;
    }
}
