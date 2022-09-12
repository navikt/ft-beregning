package no.nav.folketrygdloven.beregningsgrunnlag.foreslå;

import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.RegelBeregningsgrunnlagATFL;
import no.nav.folketrygdloven.beregningsgrunnlag.inaktiv.RegelBeregningsgrunnlagInaktiv;
import no.nav.folketrygdloven.beregningsgrunnlag.kombinasjon.RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNNy;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.RegelForeslåBeregningsgrunnlagTY;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.dagpengerelleraap.RegelFastsettBeregningsgrunnlagDPellerAAP;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


public class RegelForeslåBeregningsgrunnlagNy implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "BG-FORESLÅ";

	private BeregningsgrunnlagPeriode regelmodell;

	public RegelForeslåBeregningsgrunnlagNy(BeregningsgrunnlagPeriode regelmodell) {
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
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // Fastsett alle BG per status
	    if (regelmodell.getBeregningsgrunnlagPrStatus().isEmpty()) {
		    return new IkkeBeregnet(new BeregningUtfallMerknad(BeregningUtfallÅrsak.UDEFINERT));
	    }
		var speclist = regelmodell.getAktivitetStatuser().stream()
				.map(AktivitetStatusMedHjemmel::getAktivitetStatus)
				.map(as -> RegelForeslåBeregningsgrunnlagNy.velgSpecification(regelmodell, as))
				.collect(Collectors.toList());
        Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlag =
            rs.beregningsRegel("FP_BR pr status", "Fastsett beregningsgrunnlag pr status", speclist, new Beregnet());

        return foreslåBeregningsgrunnlag;
    }

	private static Specification<BeregningsgrunnlagPeriode> velgSpecification(BeregningsgrunnlagPeriode regelmodell, AktivitetStatus aktivitetStatus) {
		if (aktivitetStatus.erAAPellerDP()) {
			return new RegelFastsettBeregningsgrunnlagDPellerAAP().getSpecification();
		}
		return switch (aktivitetStatus) {
			case MS, SN -> new Beregnet();
			case ATFL -> new RegelBeregningsgrunnlagATFL(regelmodell).getSpecification();
			case ATFL_SN -> new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNNy(regelmodell).getSpecification();
			case KUN_YTELSE -> new RegelForeslåBeregningsgrunnlagTY(regelmodell).getSpecification();
			case MIDL_INAKTIV -> new RegelBeregningsgrunnlagInaktiv(regelmodell).getSpecification();
			default -> new RegelForeslåBeregningsgrunnlagTilNull(aktivitetStatus).getSpecification();
		};
	}
}
