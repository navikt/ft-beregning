package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelteAndelerModell;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFordelBeregningsgrunnlag implements EksportRegel<FordelPeriodeModell> {

	public static final String ID = "FP_BR 22.3";
	private FordelModell modell;

	public RegelFordelBeregningsgrunnlag() {
		super();
	}

	@Override
	public Evaluation evaluer(FordelPeriodeModell input, Object output) {
		this.modell = new FordelModell(input);
		var evaluate = this.getSpecification().evaluate(modell);
		if (!(output instanceof List)) {
			throw new IllegalStateException("Ugyldig output container i fordelregel, forventet en ArrayList av FordelAndelModell men mottok " + output);
		}
		oppdaterOutput((ArrayList<FordelAndelModell>) output);
		return evaluate;
	}

	private void oppdaterOutput(ArrayList<FordelAndelModell> output) {
		// Bør få løypa som ikke er andelsmessig over på mellomregning også, så man ikke trenger endre input
		if (modell.getMellomregninger().isEmpty()) {
			output.addAll(modell.getInput().getAndeler());
		} else {
			validerAtBruttoErUendret(modell);
			modell.getMellomregninger().forEach(mellomregning -> output.addAll(mellomregning.getFordelteAndeler()));
		}
	}

	private void validerAtBruttoErUendret(FordelModell modell) {
		BigDecimal bruttoInn = modell.getInput().getAndeler().stream()
				.filter(a -> a.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0)
				.map(a -> a.getForeslåttPrÅr().orElse(BigDecimal.ZERO))
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
		BigDecimal bruttoUt = modell.getMellomregninger().stream()
				.map(FordelteAndelerModell::getFordelteAndeler)
				.flatMap(Collection::stream)
				.map(a -> a.getFordeltPrÅr().orElseThrow())
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
		if (bruttoInn.compareTo(bruttoUt) != 0) {
			throw new IllegalStateException("Missmatch mellom fordelt beløp før og etter andelsmessig fordeling." +
					" Inn i regel var brutto " + bruttoInn + ". Ut av regel var brutto " + bruttoUt);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Specification<FordelModell> getSpecification() {
		Ruleset<FordelModell> rs = new Ruleset<>();

		Specification<FordelModell> fastsettFordelingAvBeregningsgrunnlag = new FastsettNyFordeling(modell).getSpecification();

		Specification<FordelModell> sjekkRefusjonMotBeregningsgrunnlag = rs.beregningHvisRegel(new SjekkHarRefusjonSomOverstigerBeregningsgrunnlag(),
				fastsettFordelingAvBeregningsgrunnlag, new SettAndelerUtenSøktYtelseTilNull());

		Specification<FordelModell> fordelBruttoAndelsmessig = rs.beregningsRegel(RegelFordelBeregningsgrunnlagAndelsmessig.ID, RegelFordelBeregningsgrunnlagAndelsmessig.BESKRIVELSE, new RegelFordelBeregningsgrunnlagAndelsmessig().getSpecification(), new Fordelt());

		Specification<FordelModell> sjekkOmBruttoKanDekkeAllRefusjon = rs.beregningHvisRegel(new FinnesMerRefusjonEnnBruttoTilgjengeligOgFlereAndelerKreverRefusjon(), fordelBruttoAndelsmessig, sjekkRefusjonMotBeregningsgrunnlag);

		Specification<FordelModell> sjekkOmDetFinnesTilkommetRefkrav = rs.beregningHvisRegel(new FinnesTilkommetArbeidsandelMedRefusjonskrav(), sjekkOmBruttoKanDekkeAllRefusjon, sjekkRefusjonMotBeregningsgrunnlag);

		return sjekkOmDetFinnesTilkommetRefkrav;
	}
}
