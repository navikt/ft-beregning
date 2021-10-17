package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

import java.util.ArrayList;
import java.util.List;

public class RegelFordelBeregningsgrunnlag implements RuleService<FordelPeriodeModell> {

    public static final String ID = "FP_BR 22.3";
	private FordelPeriodeModell input;
	private FordelModell modell;

	public RegelFordelBeregningsgrunnlag(FordelPeriodeModell input) {
		super();
		this.input = input;
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
		if (modell.getMellomregninger().isEmpty()) {
			output.addAll(modell.getInput().getAndeler());
		} else {
			modell.getMellomregninger().forEach(mellomregning -> output.addAll(mellomregning.getFordelteAndeler()));
		}
	}

	@SuppressWarnings("unchecked")
    @Override
    public Specification<FordelModell> getSpecification() {
        Ruleset<FordelModell> rs = new Ruleset<>();

        Specification<FordelModell> fastsettFordelingAvBeregningsgrunnlag = new FastsettNyFordeling(modell).getSpecification();

        Specification<FordelModell> sjekkRefusjonMotBeregningsgrunnlag = rs.beregningHvisRegel(new SjekkHarRefusjonSomOverstigerBeregningsgrunnlag(),
            fastsettFordelingAvBeregningsgrunnlag, new Fordelt());

	    Specification<FordelModell> omfordelFraBrukersAndel = rs.beregningsRegel(OmfordelFraBrukersAndel.ID,
			    OmfordelFraBrukersAndel.BESKRIVELSE, new OmfordelFraBrukersAndel(), sjekkRefusjonMotBeregningsgrunnlag);

	    Specification<FordelModell> sjekkOmSkalFordeleFraBrukersAndel = rs.beregningHvisRegel(new SkalOmfordeleFraBrukersAndelTilFLEllerSN(), omfordelFraBrukersAndel, sjekkRefusjonMotBeregningsgrunnlag);

		Specification<FordelModell> fordelBruttoAndelsmessig = rs.beregningsRegel(RegelFordelBeregningsgrunnlagAndelsmessig.ID, RegelFordelBeregningsgrunnlagAndelsmessig.BESKRIVELSE, new RegelFordelBeregningsgrunnlagAndelsmessig(modell).getSpecification(), new Fordelt());

		Specification<FordelModell> sjekkOmBruttoKanDekkeAllRefusjon = rs.beregningHvisRegel(new FinnesMerRefusjonEnnBruttoTilgjengelig(), fordelBruttoAndelsmessig, sjekkOmSkalFordeleFraBrukersAndel);

		Specification<FordelModell> sjekkOmDetFinnesTilkommetRefkrav = rs.beregningHvisRegel(new FinnesTilkommetArbeidsandelMedRefusjonskrav(), sjekkOmBruttoKanDekkeAllRefusjon, sjekkOmSkalFordeleFraBrukersAndel);

	    return sjekkOmDetFinnesTilkommetRefkrav;
    }
}
