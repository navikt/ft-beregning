package no.nav.folketrygdloven.beregningsgrunnlag.perioder.naturalytelse;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodeModellNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodiseringNaturalytelseProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Splitter beregningsgrunnlaget i perioder på grunn av naturalytelse
 */
public class FastsettPerioderNaturalytelseRegel implements EksportRegel<PeriodeModellNaturalytelse> {

    static final String ID = "FT_41";

    @Override
    public Evaluation evaluer(PeriodeModellNaturalytelse input, Object perioder) {
        var inputOgmellomregninger = new PeriodiseringNaturalytelseProsesstruktur(input);
        var evaluate = this.getSpecification().evaluate(inputOgmellomregninger);
        oppdaterOutput((List<SplittetPeriode>) perioder, inputOgmellomregninger);
        return evaluate;
    }

    private void oppdaterOutput(List<SplittetPeriode> outputContainer, PeriodiseringNaturalytelseProsesstruktur inputOgmellomregninger) {
        var splittetPerioder = inputOgmellomregninger.getSplittetPerioder();
        outputContainer.addAll(splittetPerioder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Specification<PeriodiseringNaturalytelseProsesstruktur> getSpecification() {

        var rs = new Ruleset<PeriodiseringNaturalytelseProsesstruktur>();

        var periodiser = rs.beregningsRegel(
            PeriodiserForNaturalytelse.ID,
		        PeriodiserForNaturalytelse.BESKRIVELSE,
            new PeriodiserForNaturalytelse(),
            new Periodisert());

        var identifiserÅrsaker = rs.beregningsRegel(
            IdentifiserPeriodeÅrsakerNaturalytelse.ID,
            IdentifiserPeriodeÅrsakerNaturalytelse.BESKRIVELSE,
            new IdentifiserPeriodeÅrsakerNaturalytelse(),
            periodiser);

        return identifiserÅrsaker;

    }

}
