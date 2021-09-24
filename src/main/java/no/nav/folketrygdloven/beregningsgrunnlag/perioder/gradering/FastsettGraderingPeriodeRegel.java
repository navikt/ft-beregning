package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.perioder.IdentifiserPeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.perioder.PeriodiserBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.perioder.Periodisert;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeSplittProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Splitter beregningsgrunnlaget i perioder på grunn av gradering

 */
public class FastsettGraderingPeriodeRegel implements RuleService<PeriodeModellGradering> {

    static final String ID = "FT_40";

    @Override
    public Evaluation evaluer(PeriodeModellGradering input, Object perioder) {
        var inputOgmellomregninger = new PeriodeSplittProsesstruktur(input);
        Evaluation evaluate = this.getSpecification().evaluate(inputOgmellomregninger);
        oppdaterOutput((List<SplittetPeriode>) perioder, inputOgmellomregninger);
        return evaluate;
    }

    private void oppdaterOutput(List<SplittetPeriode> outputContainer, PeriodeSplittProsesstruktur inputOgmellomregninger) {
        List<SplittetPeriode> splittetPerioder = inputOgmellomregninger.getSplittetPerioder();
        outputContainer.addAll(splittetPerioder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Specification<PeriodeSplittProsesstruktur> getSpecification() {

        Ruleset<PeriodeSplittProsesstruktur> rs = new Ruleset<>();

        var periodiser = rs.beregningsRegel(
            PeriodiserBeregningsgrunnlag.ID,
            PeriodiserBeregningsgrunnlag.BESKRIVELSE,
            new PeriodiserBeregningsgrunnlag(),
            new Periodisert());

        var identifiserÅrsaker = rs.beregningsRegel(
		        IdentifiserGraderingPerioder.ID,
		        IdentifiserGraderingPerioder.BESKRIVELSE,
            new IdentifiserGraderingPerioder(),
            periodiser);

        return identifiserÅrsaker;

    }

}
