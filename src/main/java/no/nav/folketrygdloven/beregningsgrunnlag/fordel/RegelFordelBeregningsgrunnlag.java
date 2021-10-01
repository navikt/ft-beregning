package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFordelBeregningsgrunnlag extends DynamicRuleService<FordelPeriodeModell> {

    public static final String ID = "FP_BR 22.3";

    public RegelFordelBeregningsgrunnlag(FordelPeriodeModell regelmodell) {
        super(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<FordelPeriodeModell> getSpecification() {
        Ruleset<FordelPeriodeModell> rs = new Ruleset<>();

        Specification<FordelPeriodeModell> fastsettFordelingAvBeregningsgrunnlag = new FastsettNyFordeling(regelmodell).getSpecification();

        Specification<FordelPeriodeModell> sjekkRefusjonMotBeregningsgrunnlag = rs.beregningHvisRegel(new SjekkHarRefusjonSomOverstigerBeregningsgrunnlag(),
            fastsettFordelingAvBeregningsgrunnlag, new Fordelt());

	    Specification<FordelPeriodeModell> omfordelFraBrukersAndel = rs.beregningsRegel(OmfordelFraBrukersAndel.ID,
			    OmfordelFraBrukersAndel.BESKRIVELSE, new OmfordelFraBrukersAndel(), sjekkRefusjonMotBeregningsgrunnlag);

	    Specification<FordelPeriodeModell> sjekkOmSkalFordeleFraBrukersAndel = rs.beregningHvisRegel(new SkalOmfordeleFraBrukersAndelTilFLEllerSN(), omfordelFraBrukersAndel, sjekkRefusjonMotBeregningsgrunnlag);

	    return sjekkOmSkalFordeleFraBrukersAndel;
    }
}
