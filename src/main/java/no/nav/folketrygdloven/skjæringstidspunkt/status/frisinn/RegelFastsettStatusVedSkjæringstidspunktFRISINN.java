package no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn;

import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFRISINN;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


@RuleDocumentation(value = RegelFastsettStatusVedSkjæringstidspunktFRISINN.ID, specificationReference = "https://confluence.adeo.no/display/SIF/30.+Beregningsgrunnlag")
public class RegelFastsettStatusVedSkjæringstidspunktFRISINN implements RuleService<AktivitetStatusModellFRISINN> {

    static final String ID = "FP_BR_19";

    @Override
    public Evaluation evaluer(AktivitetStatusModellFRISINN regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<AktivitetStatusModellFRISINN> getSpecification() {

        Ruleset<AktivitetStatusModellFRISINN> rs = new Ruleset<>();

//      FP_BR_19_5 Fastsett status for beregningsrunnlag (liste)

        Specification<AktivitetStatusModellFRISINN> fastsettStatusUtenKombinasjonerForBG = new FastsettStatusForBeregningsgrunnlagFRISINN();

//      FP_BR_19_4 Sett kombinasjoner

        Specification<AktivitetStatusModellFRISINN> fastsettKombinasjoner = new FastsettKombinasjonerFRISINN();

//      FP_BR_19_3 Aktuelle kombinasjoner?

        Specification<AktivitetStatusModellFRISINN> sjekkAktuelleKombinasjoner =
            rs.beregningHvisRegel(new SjekkAktuelleKombinasjonerFRISINN(), fastsettKombinasjoner, fastsettStatusUtenKombinasjonerForBG);

//      FP_BR_19_1 Hent aktiviteter på skjæringstidspunkt
//      FP_BR_19_2 Fastsett status per andel og periode

        Specification<AktivitetStatusModellFRISINN> startFastsettStatusVedSkjæringtidspunktForBeregning =
            rs.beregningsRegel(FastsettStatusOgAndelPrPeriodeFRISINN.ID, FastsettStatusOgAndelPrPeriodeFRISINN.BESKRIVELSE,
                new FastsettStatusOgAndelPrPeriodeFRISINN(), sjekkAktuelleKombinasjoner);

//      Start fastsett status ved skjæringstidspunkt for beregning

        return startFastsettStatusVedSkjæringtidspunktForBeregning;
    }
}
