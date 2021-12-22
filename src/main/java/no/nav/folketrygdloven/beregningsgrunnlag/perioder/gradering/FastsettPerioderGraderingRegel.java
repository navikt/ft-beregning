package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodiseringGraderingProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Splitter beregningsgrunnlaget i perioder på grunn av naturalytelse
 */
public class FastsettPerioderGraderingRegel implements RuleService<PeriodeModellGradering> {

    static final String ID = "FT_43";

    @Override
    public Evaluation evaluer(PeriodeModellGradering input, Object perioder) {
        var inputOgmellomregninger = new PeriodiseringGraderingProsesstruktur(input);
        Evaluation evaluate = this.getSpecification().evaluate(inputOgmellomregninger);
        oppdaterOutput((List<SplittetPeriode>) perioder, inputOgmellomregninger);
        return evaluate;
    }

    private void oppdaterOutput(List<SplittetPeriode> outputContainer, PeriodiseringGraderingProsesstruktur inputOgmellomregninger) {
        List<SplittetPeriode> splittetPerioder = inputOgmellomregninger.getSplittetPerioder();
        outputContainer.addAll(splittetPerioder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Specification<PeriodiseringGraderingProsesstruktur> getSpecification() {

        Ruleset<PeriodiseringGraderingProsesstruktur> rs = new Ruleset<>();

        var periodiser = rs.beregningsRegel(
		        PeriodiserForGradering.ID,
		        PeriodiserForGradering.BESKRIVELSE,
            new PeriodiserForGradering(),
            new Periodisert());

        var identifiserÅrsaker = rs.beregningsRegel(
            IdentifiserPeriodeÅrsakerGradering.ID,
		        IdentifiserPeriodeÅrsakerGradering.BESKRIVELSE,
            new IdentifiserPeriodeÅrsakerGradering(),
            periodiser);

        return identifiserÅrsaker;

    }

}
