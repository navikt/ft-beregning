package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

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
		modell.leggTilFordeltAndel(FordelAndelModell.builder().medAktivitetStatus(AktivitetStatus.ATFL).build());
		var evaluate = this.getSpecification().evaluate(modell);
		return evaluate;
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

	    return sjekkOmSkalFordeleFraBrukersAndel;
    }
}
