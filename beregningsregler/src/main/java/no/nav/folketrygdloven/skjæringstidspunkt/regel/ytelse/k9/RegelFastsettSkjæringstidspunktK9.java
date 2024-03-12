package no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse.k9;

import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.folketrygdloven.skjæringstidspunkt.regel.FastsettSkjæringstidspunktLikOpptjening;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Fastsetter sjæringstidspunkt for beregning lik skjæringstidspunkt for opptjening for alle k9 ytelser.
 */
public class RegelFastsettSkjæringstidspunktK9 implements EksportRegel<AktivitetStatusModell> {

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
