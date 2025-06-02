package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFordelBeregningsgrunnlagAndelsmessig implements RuleService<FordelPeriodeModell> {

    public static final String ID = "FORDEL_ANDELSMESSIG";
	public static final String BESKRIVELSE = "Fordel beregningsgrunnlag andelsmessig etter fraksjon av foreslått inntekt eller inntekt fra inntektsmelding";

	@SuppressWarnings("unchecked")
    @Override
    public Specification<FordelModell> getSpecification() {
        var rs = new Ruleset<FordelModell>();


        var utførFordeling = rs.beregningsRegel(FordelMålbeløpPrAndel.ID, FordelMålbeløpPrAndel.BESKRIVELSE, new FordelMålbeløpPrAndel(), new Fordelt());

        var bestemBeløpSomSkalFordeles = rs.beregningsRegel(FinnMålbeløpForFordelingenPrAndel.ID, FinnMålbeløpForFordelingenPrAndel.BESKRIVELSE, new FinnMålbeløpForFordelingenPrAndel(), utførFordeling);

        var fastsettFraksjonPrAndel = rs.beregningsRegel(FinnFraksjonPrAndel.ID, FinnFraksjonPrAndel.BESKRIVELSE, new FinnFraksjonPrAndel(), bestemBeløpSomSkalFordeles);

	    return fastsettFraksjonPrAndel;
    }
}
