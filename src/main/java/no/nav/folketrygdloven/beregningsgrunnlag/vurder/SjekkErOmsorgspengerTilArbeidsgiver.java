package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkErOmsorgspengerTilArbeidsgiver.ID)
class SjekkErOmsorgspengerTilArbeidsgiver extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FT_VK_32.3";
    static final String BESKRIVELSE = "Gjelder utbetaling til bruker etter § 9-8?";

    SjekkErOmsorgspengerTilArbeidsgiver() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

		if (grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag() instanceof OmsorgspengerGrunnlag ompGrunnlag) {

			return ompGrunnlag.omfattesAvKap9Paragraf9() ? nei() : ja();
		}

	    return nei();

    }


}
