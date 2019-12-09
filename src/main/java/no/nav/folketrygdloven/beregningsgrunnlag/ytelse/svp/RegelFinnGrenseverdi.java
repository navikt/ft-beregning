package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp;

import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.SjekkBeregningsgrunnlagStørreEnnGrenseverdi;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFinnGrenseverdi extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_29";

    public RegelFinnGrenseverdi(BeregningsgrunnlagPeriode regelmodell) {
        super(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // Fastsett grenseverdi for fastsetting av beregningsgrunnlag
        Specification<BeregningsgrunnlagPeriode> fastsettGrenseverdi = rs.beregningsRegel(FinnGrenseverdi.ID, FinnGrenseverdi.BESKRIVELSE,
            new FinnGrenseverdi(), new Beregnet());

        // Fastsett avkortet BG
        Specification<BeregningsgrunnlagPeriode> fastsettAvkortet = rs.beregningsRegel(
            RegelFastsettAndelBGOver6G.ID,
            RegelFastsettAndelBGOver6G.BESKRIVELSE,
            new RegelFastsettAndelBGOver6G(regelmodell).getSpecification(),
            fastsettGrenseverdi);

        // Fastsett uten avkorting
        Specification<BeregningsgrunnlagPeriode> fastsettUtenAvkorting = rs.beregningsRegel(
            "FP_BR_29.6",
            "Fastsett BG uten avkorting",
            new FastsettAndelLikBruttoBG(),
            fastsettGrenseverdi);

        // FP_BR_29.4 4. Brutto beregnings-grunnlag totalt > 6G?
        Specification<BeregningsgrunnlagPeriode> beregnEventuellAvkorting = rs.beregningHvisRegel(
            new SjekkBeregningsgrunnlagStørreEnnGrenseverdi(),
            fastsettAvkortet,
            fastsettUtenAvkorting);

        return beregnEventuellAvkorting;
    }
}
