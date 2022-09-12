package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.specification.Specification;

public class RegelBeregnBruttoPrArbeidsforholdFRISINN implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FRISINN 2.1";

	private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

	public RegelBeregnBruttoPrArbeidsforholdFRISINN(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
		this.arbeidsforhold = arbeidsforhold;
	}

	@SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {

        return new BeregnPrArbeidsforholdFraAOrdningenFRISINN(arbeidsforhold);
    }
}
