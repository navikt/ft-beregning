package no.nav.folketrygdloven.beregningsgrunnlag.perioder.refusjon;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodeModellRefusjon;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodiseringRefusjonProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Splitter beregningsgrunnlaget i perioder på grunn av refusjonskrav
 */
public class FastsettPerioderRefusjonRegel implements RuleService<PeriodeModellRefusjon> {

	static final String ID = "FT_42";

	@Override
	public Evaluation evaluer(PeriodeModellRefusjon input, Object perioder) {
		var inputOgmellomregninger = new PeriodiseringRefusjonProsesstruktur(input);
		Evaluation evaluate = this.getSpecification().evaluate(inputOgmellomregninger);
		oppdaterOutput((List<SplittetPeriode>) perioder, inputOgmellomregninger);
		return evaluate;
	}

	private void oppdaterOutput(List<SplittetPeriode> outputContainer, PeriodiseringRefusjonProsesstruktur inputOgmellomregninger) {
		List<SplittetPeriode> splittetPerioder = inputOgmellomregninger.getSplittetPerioder();
		outputContainer.addAll(splittetPerioder);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Specification<PeriodiseringRefusjonProsesstruktur> getSpecification() {

		Ruleset<PeriodiseringRefusjonProsesstruktur> rs = new Ruleset<>();

		var periodiser = rs.beregningsRegel(
				PeriodiserForRefusjon.ID,
				PeriodiserForRefusjon.BESKRIVELSE,
				new PeriodiserForRefusjon(),
				new Periodisert());

		var identifiserÅrsaker = rs.beregningsRegel(
				IdentifiserPeriodeÅrsakerRefusjon.ID,
				IdentifiserPeriodeÅrsakerRefusjon.BESKRIVELSE,
				new IdentifiserPeriodeÅrsakerRefusjon(),
				periodiser);

		return identifiserÅrsaker;

	}

}
