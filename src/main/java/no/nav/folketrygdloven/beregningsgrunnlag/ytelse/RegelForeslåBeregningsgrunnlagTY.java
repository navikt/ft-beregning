package no.nav.folketrygdloven.beregningsgrunnlag.ytelse;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Beregner bruker med direkte overgang uten andre aktiviteter enn ytelse
 */

@RuleDocumentation(value = RegelForeslåBeregningsgrunnlagTY.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=216009135")
public class RegelForeslåBeregningsgrunnlagTY implements RuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 30";


	private BeregningsgrunnlagPeriode regelmodell;

	public RegelForeslåBeregningsgrunnlagTY(BeregningsgrunnlagPeriode regelmodell) {
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

        // FP_BR 30 Foreslå beregningsgrunnlag for status tilstøtende ytelse
        Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlagTY = rs.beregningsRegel(ForeslåBeregningsgrunnlagTY.ID, ForeslåBeregningsgrunnlagTY.BESKRIVELSE,
            new ForeslåBeregningsgrunnlagTY(), new Beregnet());

	    List<BeregningsgrunnlagPrStatus> brukersAndeler = regelmodell.getBeregningsgrunnlagPrStatuser(AktivitetStatus.BA);
		var speclist = brukersAndeler.stream()
				.map(ba -> new RegelBeregnBruttoYtelseAndel(ba).getSpecification()
						.medScope(new ServiceArgument("statusAndel", ba.getAktivitetStatus()))) // TODO (PE) - bruke inntektskategori eller noe annet?
				.toList();


	    return rs.beregningsRegel("FP_BR 14.X", "Fastsett beregningsgrunnlag pr inntektskategori", speclist, foreslåBeregningsgrunnlagTY);
    }
}
