package no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse.psb;

import no.nav.folketrygdloven.skjæringstidspunkt.regel.SjekkOmAktivitetRettFørOpptjening;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelFastsettSkjæringstidspunktPSB.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=234395762")
public class RegelFastsettSkjæringstidspunktPSB implements RuleService<AktivitetStatusModell> {

    static final String ID = "FP_BR_21";

    @Override
    public Evaluation evaluer(AktivitetStatusModell regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<AktivitetStatusModell> getSpecification() {

        Ruleset<AktivitetStatusModell> rs = new Ruleset<>();

//       Vurder §8-47

	    // TODO: Implementer vurdering av §8-47
        Specification<AktivitetStatusModell> vurderMidlertidigUteAvArbeid = new FastsettSkjæringstidspunktLikOpptjening();

//      FP_BR 21.6 Skjæringstidspunkt for beregning = skjæringstidspunkt for opptjening

        Specification<AktivitetStatusModell> likSkjæringstidspunktForOpptjening = new FastsettSkjæringstidspunktLikOpptjening();

//      FP_BR 21.1 Er tom-dato for siste aktivitet dagen før skjæringstidspunkt for opptjening?

        Specification<AktivitetStatusModell> erDetAktivitetFremTilStpForOpptjening =
                rs.beregningHvisRegel(new SjekkOmAktivitetRettFørOpptjening(), likSkjæringstidspunktForOpptjening, vurderMidlertidigUteAvArbeid);


        return erDetAktivitetFremTilStpForOpptjening;
    }
}
