package no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse;

import no.nav.folketrygdloven.skjæringstidspunkt.regel.FastsettSkjæringstidspunktLikOpptjening;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(value = RegelFastsettSkjæringstidspunktFrisinn.ID, specificationReference = "https://confluence.adeo.no/display/SIF/30.+Beregningsgrunnlag")
public class RegelFastsettSkjæringstidspunktFrisinn implements RuleService<AktivitetStatusModell> {

    static final String ID = "FRISINN 1";

    @Override
    public Evaluation evaluer(AktivitetStatusModell regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<AktivitetStatusModell> getSpecification() {
        return new FastsettSkjæringstidspunktLikOpptjening();
    }
}
