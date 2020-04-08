package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.SjekkBeregningsgrunnlagStørreEnnGrenseverdi;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFinnGrenseverdiFRISINN extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FRISINN 6.1";

    public RegelFinnGrenseverdiFRISINN(BeregningsgrunnlagPeriode regelmodell) {
        super(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // Fastsett avkortet BG
        Specification<BeregningsgrunnlagPeriode> fastsettUavkortetGrenseverdi = new FinnGrenseverdiForTotalUnder6G();

        // Fastsett avkortet BG
        Specification<BeregningsgrunnlagPeriode> fastsettAvkortetGrenseverdi = new FinnGrenseverdiForTotalOver6G();

        // FP_BR_29.4 4. Brutto beregnings-grunnlag totalt > 6G?
        Specification<BeregningsgrunnlagPeriode> beregnEventuellAvkorting = rs.beregningHvisRegel(
            new SjekkBeregningsgrunnlagStørreEnnGrenseverdi(),
            fastsettAvkortetGrenseverdi,
            fastsettUavkortetGrenseverdi);

        return beregnEventuellAvkorting;
    }
}
