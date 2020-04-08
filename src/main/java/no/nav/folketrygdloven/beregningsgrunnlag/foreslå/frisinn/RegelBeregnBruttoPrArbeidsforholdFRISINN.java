package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.specification.Specification;

public class RegelBeregnBruttoPrArbeidsforholdFRISINN extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FRISINN 2.1";

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {

        ServiceArgument arg = getServiceArgument();
        if (arg == null || !(arg.getVerdi() instanceof BeregningsgrunnlagPrArbeidsforhold)) {
            throw new IllegalStateException("Utviklerfeil: Arbeidsforhold må angis som parameter");
        }
        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = (BeregningsgrunnlagPrArbeidsforhold) arg.getVerdi();

        return new BeregnPrArbeidsforholdFraAOrdningenFRISINN(arbeidsforhold);
    }
}
