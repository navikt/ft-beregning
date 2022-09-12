package no.nav.folketrygdloven.beregningsgrunnlag.fortsettForeslå;

import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlagTilNull;
import no.nav.folketrygdloven.beregningsgrunnlag.militær.RegelForeslåBeregningsgrunnlagMilitær;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.RegelBeregningsgrunnlagSN;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


public class RegelFortsettForeslåBeregningsgrunnlag implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "BG-FORTSETT-FORESLÅ";

	private BeregningsgrunnlagPeriode regelmodell;

	public RegelFortsettForeslåBeregningsgrunnlag(BeregningsgrunnlagPeriode regelmodell) {
		super();
		this.regelmodell = regelmodell;
	}

	@Override
	public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
	    if (regelmodell.getBeregningsgrunnlagPrStatus().isEmpty()) {
		    return new IkkeBeregnet(new BeregningUtfallMerknad(BeregningUtfallÅrsak.UDEFINERT));
	    }

	    Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();
	    var speclist = regelmodell.getAktivitetStatuser().stream()
			    .map(AktivitetStatusMedHjemmel::getAktivitetStatus)
			    .map(RegelFortsettForeslåBeregningsgrunnlag::velgSpecification)
			    .collect(Collectors.toList());
	    Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlag =
			    rs.beregningsRegel("FP_BR fortsett pr status", "Fastsett beregningsgrunnlag fortsett pr status", speclist, new Beregnet());

	    return rs.beregningHvisRegel(new SkalKjøreFortsettForeslå(), foreslåBeregningsgrunnlag, new Beregnet());
    }

	private  static Specification<BeregningsgrunnlagPeriode> velgSpecification(AktivitetStatus aktivitetStatus) {
		if (!aktivitetStatus.erSelvstendigNæringsdrivende() && !aktivitetStatus.erMilitær()) {
			return new Beregnet();
		}
		return switch (aktivitetStatus) {
			case SN -> new RegelBeregningsgrunnlagSN().getSpecification();
			case ATFL_SN -> new RegelBeregningsgrunnlagSN().getSpecification();
			case MS -> new RegelForeslåBeregningsgrunnlagMilitær().getSpecification();
			default -> new RegelForeslåBeregningsgrunnlagTilNull(aktivitetStatus).getSpecification();
		};
	}
}
