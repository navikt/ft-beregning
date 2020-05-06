package no.nav.folketrygdloven.skjæringstidspunkt.status.fp;

import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.status.FastsettKombinasjoner;
import no.nav.folketrygdloven.skjæringstidspunkt.status.FastsettStatusForBeregningsgrunnlag;
import no.nav.folketrygdloven.skjæringstidspunkt.status.FastsettStatusOgAndelPrPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.status.SjekkAktuelleKombinasjoner;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelFastsettStatusVedSkjæringstidspunktFP.ID, specificationReference = "https://confluence.adeo.no/display/MODNAV/Funksjonell+beskrivelse+-+Fastsette+status")
public class RegelFastsettStatusVedSkjæringstidspunktFP implements RuleService<AktivitetStatusModell> {

    static final String ID = "FP_BR_19";

    @Override
    public Evaluation evaluer(AktivitetStatusModell regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<AktivitetStatusModell> getSpecification() {

        Ruleset<AktivitetStatusModell> rs = new Ruleset<>();

//      FP_BR_19_5 Fastsett status for beregningsrunnlag (liste)

        Specification<AktivitetStatusModell> fastsettStatusUtenKombinasjonerForBG = new FastsettStatusForBeregningsgrunnlag();

//      FP_BR_19_4 Sett kombinasjoner

        Specification<AktivitetStatusModell> fastsettKombinasjoner = new FastsettKombinasjoner();

//      FP_BR_19_3 Aktuelle kombinasjoner?

        Specification<AktivitetStatusModell> sjekkAktuelleKombinasjoner =
            rs.beregningHvisRegel(new SjekkAktuelleKombinasjoner(), fastsettKombinasjoner, fastsettStatusUtenKombinasjonerForBG);

//      FP_BR_19_1 Hent aktiviteter på skjæringstidspunkt
//      FP_BR_19_2 Fastsett status per andel og periode

        Specification<AktivitetStatusModell> startFastsettStatusVedSkjæringtidspunktForBeregning =
            rs.beregningsRegel(FastsettStatusOgAndelPrPeriodeFP.ID, FastsettStatusOgAndelPrPeriodeFP.BESKRIVELSE,
                new FastsettStatusOgAndelPrPeriodeFP(), sjekkAktuelleKombinasjoner);

//      Start fastsett status ved skjæringstidspunkt for beregning

        return startFastsettStatusVedSkjæringtidspunktForBeregning;
    }
}
