package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFordelBeregningsgrunnlagAndelsmessig implements RuleService<FordelPeriodeModell> {

    public static final String ID = "FP_BR 22.3";
	public static final String BESKRIVELSE = "Fordel beregningsgrunnlag andelsmessig etter fraksjon av foreslått inntekt eller inntekt fra inntektsmelding";
	private FordelModell modell;

	public RegelFordelBeregningsgrunnlagAndelsmessig(FordelModell modell) {
		this.modell = modell;
	}

	@SuppressWarnings("unchecked")
    @Override
    public Specification<FordelModell> getSpecification() {
        Ruleset<FordelModell> rs = new Ruleset<>();


		Specification<FordelModell> utførFordeling = rs.beregningsRegel(FordelMålbeløpPrAndel.ID, FordelMålbeløpPrAndel.BESKRIVELSE, new FordelMålbeløpPrAndel(), new Fordelt());

		Specification<FordelModell> bestemBeløpSomSkalFordeles = rs.beregningsRegel(FinnMålbeløpForFordelingenPrAndel.ID, FinnMålbeløpForFordelingenPrAndel.BESKRIVELSE, new FinnMålbeløpForFordelingenPrAndel(), utførFordeling);

		Specification<FordelModell> fastsettFraksjonPrAndel = rs.beregningsRegel(FinnFraksjonPrAndel.ID, FinnFraksjonPrAndel.BESKRIVELSE, new FinnFraksjonPrAndel(), bestemBeløpSomSkalFordeles);

	    return fastsettFraksjonPrAndel;
    }
}
