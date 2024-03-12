package no.nav.folketrygdloven.beregningsgrunnlag.perioder.utbetalingsgrad;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.PeriodeModellUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.PeriodiseringUtbetalingsgradProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Splitter beregningsgrunnlaget i perioder på grunn av endring i utbetalingsgrad
 */
public class FastsettPerioderForUtbetalingsgradRegel implements EksportRegel<PeriodeModellUtbetalingsgrad> {

    static final String ID = "FT_40";

    @Override
    public Evaluation evaluer(PeriodeModellUtbetalingsgrad input, Object perioder) {
        var inputOgmellomregninger = new PeriodiseringUtbetalingsgradProsesstruktur(input);
        Evaluation evaluate = this.getSpecification().evaluate(inputOgmellomregninger);
        oppdaterOutput((List<SplittetPeriode>) perioder, inputOgmellomregninger);
        return evaluate;
    }

    private void oppdaterOutput(List<SplittetPeriode> outputContainer, PeriodiseringUtbetalingsgradProsesstruktur inputOgmellomregninger) {
        List<SplittetPeriode> splittetPerioder = inputOgmellomregninger.getSplittetPerioder();
        outputContainer.addAll(splittetPerioder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Specification<PeriodiseringUtbetalingsgradProsesstruktur> getSpecification() {

        Ruleset<PeriodiseringUtbetalingsgradProsesstruktur> rs = new Ruleset<>();

        var periodiser = rs.beregningsRegel(
            PeriodiserForUtbetalingsgrad.ID,
            PeriodiserForUtbetalingsgrad.BESKRIVELSE,
            new PeriodiserForUtbetalingsgrad(),
            new Periodisert());

        var identifiserÅrsaker = rs.beregningsRegel(
            IdentifiserPeriodeÅrsakForUtbetalingsgrad.ID,
            IdentifiserPeriodeÅrsakForUtbetalingsgrad.BESKRIVELSE,
            new IdentifiserPeriodeÅrsakForUtbetalingsgrad(),
            periodiser);

        return identifiserÅrsaker;

    }

}
