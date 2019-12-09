package no.nav.folketrygdloven.beregningsgrunnlag.foreslå;

import no.finn.unleash.Unleash;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;


public class RegelForeslåBeregningsgrunnlag extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "BG-FORESLÅ";
    private Unleash unleash;
    private static final String TOGGLE_SPLITTE_SAMMENLIGNING = "fpsak.splitteSammenligningATFL";

    public RegelForeslåBeregningsgrunnlag(BeregningsgrunnlagPeriode regelmodell, Unleash unleash) {
        super(regelmodell);
        this.unleash = unleash;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // Fastsett alle BG per status
        Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlag;
        if(unleash.isEnabled(TOGGLE_SPLITTE_SAMMENLIGNING, false)){
            regelmodell.setSplitteATFLToggleErPå(true);
            foreslåBeregningsgrunnlag =
                rs.beregningsRegel("FP_BR pr status", "Fastsett beregningsgrunnlag pr status", RegelForeslåBeregningsgrunnlagPrStatusATFLSplitt.class, regelmodell, "aktivitetStatus", regelmodell.getAktivitetStatuser(), new Beregnet());

        } else{
            regelmodell.setSplitteATFLToggleErPå(false);
            foreslåBeregningsgrunnlag =
                rs.beregningsRegel("FP_BR pr status", "Fastsett beregningsgrunnlag pr status", RegelForeslåBeregningsgrunnlagPrStatus.class, regelmodell, "aktivitetStatus", regelmodell.getAktivitetStatuser(), new Beregnet());

        }

        return foreslåBeregningsgrunnlag;
    }
}
