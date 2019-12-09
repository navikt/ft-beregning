package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelSammenligningsgrunnlagFL implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_28.1";

    public RegelSammenligningsgrunnlagFL() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        IkkeBeregnet fastsettesVedSkjønnFL = new FastsettesVedSkjønnFL();


        // FP_BR 28.3 Har rapportert inntekt inkludert bortfaltnaturalytelse for 1. periode avvik mot sammenligningsgrunnlag > 25%?

        Specification<BeregningsgrunnlagPeriode> sjekkÅrsinntektMotSammenligningsgrunnlag =
                rs.beregningHvisRegel(new SjekkÅrsinntektMotSammenligningsgrunnlagFL(), fastsettesVedSkjønnFL, new Beregnet());

        // FP_BR 28.2 Sammenligningsgrunnlag pr år = sum av 12 siste måneder

        Specification<BeregningsgrunnlagPeriode> fastsettSammenligningsgrunnlag =
                rs.beregningsRegel("FP_BR 17.1", "Fastsett sammenligningsgrunnlag for ATFL",
                        new FastsettSammenligningsgrunnlagFL(), sjekkÅrsinntektMotSammenligningsgrunnlag);

        return fastsettSammenligningsgrunnlag;
    }
}
