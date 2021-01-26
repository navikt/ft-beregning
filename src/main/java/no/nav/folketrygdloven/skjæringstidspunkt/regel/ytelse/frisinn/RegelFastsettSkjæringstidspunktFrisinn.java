package no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse.frisinn;

import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFRISINN;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(value = RegelFastsettSkjæringstidspunktFrisinn.ID, specificationReference = "https://confluence.adeo.no/display/SIF/30.+Beregningsgrunnlag")
public class RegelFastsettSkjæringstidspunktFrisinn implements RuleService<AktivitetStatusModellFRISINN> {

    static final String ID = "FRISINN 1";

    @Override
    public Evaluation evaluer(AktivitetStatusModellFRISINN regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<AktivitetStatusModellFRISINN> getSpecification() {
        return new FastsettSkjæringstidspunktFørPeriodeMedYtelse();
    }
}
