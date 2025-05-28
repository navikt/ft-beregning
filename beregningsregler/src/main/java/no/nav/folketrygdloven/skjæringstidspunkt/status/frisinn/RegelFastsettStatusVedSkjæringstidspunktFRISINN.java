package no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn;

import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.status.FastsettKombinasjoner;
import no.nav.folketrygdloven.skjæringstidspunkt.status.FastsettStatusForBeregningsgrunnlag;
import no.nav.folketrygdloven.skjæringstidspunkt.status.SjekkAktuelleKombinasjoner;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


@RuleDocumentation(value = RegelFastsettStatusVedSkjæringstidspunktFRISINN.ID, specificationReference = "https://confluence.adeo.no/display/SIF/30.+Beregningsgrunnlag")
public class RegelFastsettStatusVedSkjæringstidspunktFRISINN implements EksportRegel<AktivitetStatusModell> {

    static final String ID = "FP_BR_19";

    @Override
    public Evaluation evaluer(AktivitetStatusModell regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<AktivitetStatusModell> getSpecification() {

        var rs = new Ruleset<AktivitetStatusModell>();

//      FP_BR_19_5 Fastsett status for beregningsrunnlag (liste)

        Specification<AktivitetStatusModell> fastsettStatusUtenKombinasjonerForBG = new FastsettStatusForBeregningsgrunnlag();

//      FP_BR_19_4 Sett kombinasjoner

        Specification<AktivitetStatusModell> fastsettKombinasjoner = new FastsettKombinasjoner();

//      FP_BR_19_3 Aktuelle kombinasjoner?

        var sjekkAktuelleKombinasjoner =
            rs.beregningHvisRegel(new SjekkAktuelleKombinasjoner(), fastsettKombinasjoner, fastsettStatusUtenKombinasjonerForBG);

//      FP_BR_19_1 Hent aktiviteter på skjæringstidspunkt
//      FP_BR_19_2 Fastsett status per andel og periode

        var startFastsettStatusVedSkjæringtidspunktForBeregning =
            rs.beregningsRegel(FastsettStatusOgAndelPrPeriodeFRISINN.ID, FastsettStatusOgAndelPrPeriodeFRISINN.BESKRIVELSE,
                new FastsettStatusOgAndelPrPeriodeFRISINN(), sjekkAktuelleKombinasjoner);

//      Start fastsett status ved skjæringstidspunkt for beregning

        return startFastsettStatusVedSkjæringtidspunktForBeregning;
    }
}
