package no.nav.folketrygdloven.beregningsgrunnlag.fortsettForeslå;

import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlagTilNull;
import no.nav.folketrygdloven.beregningsgrunnlag.inaktiv.RegelBeregningsgrunnlagInaktivMedAvviksvurdering;
import no.nav.folketrygdloven.beregningsgrunnlag.militær.RegelForeslåBeregningsgrunnlagMilitær;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.RegelBeregningsgrunnlagSN;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Fortsett foreslå beregningsgrunnlag
 * Beregner foreslått beregningsgrunnlag for statuser som ikke er blitt beregnet i steg {@link no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlag}
 * Dette vil si statusene selvstendig næringsdrivende, militær eller siviltjeneste, og midlertidig inaktiv.
 * Hvis en kombinasjonsstatus består av to statuser beregnes i ulike steg, beregnes de delvis i forrige steg og delvis i dette.
 * SN beregnes etter §8-35
 * ATFL_SN beregnes etter §8-41, som i denne regelen beregner SN etter §8-35
 * MS beregnes etter §14-7 4.ledd
 * MIDL_INAKTIV beregnes etter §8-47
 * */
public class RegelFortsettForeslåBeregningsgrunnlag implements EksportRegel<BeregningsgrunnlagPeriode> {

    public static final String ID = "BG-FORTSETT-FORESLÅ";

	private BeregningsgrunnlagPeriode regelmodell;

	public RegelFortsettForeslåBeregningsgrunnlag(BeregningsgrunnlagPeriode regelmodell) {
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
		var speclist = regelmodell.getAktivitetStatuser().stream()
				.map(AktivitetStatusMedHjemmel::getAktivitetStatus)
				.map(this::velgSpecification)
				.collect(Collectors.toList());
		Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlag =
				rs.beregningsRegel("FP_BR fortsett pr status", "Fastsett beregningsgrunnlag fortsett pr status", speclist, new Beregnet());

		return rs.beregningHvisRegel(new SkalKjøreFortsettForeslå(), foreslåBeregningsgrunnlag, new Beregnet());
    }

	private Specification<BeregningsgrunnlagPeriode> velgSpecification(AktivitetStatus aktivitetStatus) {
		if (regelmodell.getBeregningsgrunnlagPrStatus().isEmpty()) {
			return new IkkeBeregnet(new BeregningUtfallMerknad(BeregningUtfallÅrsak.UDEFINERT));
		}
		var sporingsproperty = new ServiceArgument("aktivitetStatus", aktivitetStatus);
		if (!aktivitetStatus.erSelvstendigNæringsdrivende() && !aktivitetStatus.erMilitær() && !aktivitetStatus.equals(AktivitetStatus.MIDL_INAKTIV)) {
			return new Beregnet();
		}
		return switch (aktivitetStatus) {
			case SN, ATFL_SN -> new RegelBeregningsgrunnlagSN().getSpecification().medEvaluationProperty(sporingsproperty);
			case MS -> new RegelForeslåBeregningsgrunnlagMilitær().getSpecification().medEvaluationProperty(sporingsproperty);
			case MIDL_INAKTIV -> new RegelBeregningsgrunnlagInaktivMedAvviksvurdering().getSpecification().medEvaluationProperty(sporingsproperty);
			default -> new RegelForeslåBeregningsgrunnlagTilNull(aktivitetStatus).getSpecification().medEvaluationProperty(sporingsproperty);
		};

	}
}
