package no.nav.folketrygdloven.beregningsgrunnlag.inaktiv;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

/**
 * Sjekker om det finnes en andel for et arbeidsforhold og om denne har mottatt inntektsmelding.
 */
class SjekkHarArbeidsforholdMedIM extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "MIDL_INAKT_HAR_IM";
    static final String BESKRIVELSE = "Har arbeidsforhold med inntektsmelding?";

    SjekkHarArbeidsforholdMedIM() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
	    BeregningsgrunnlagPrStatus atflStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
	    boolean alleArbeidsforholdHarInntektsmelding = atflStatus != null && atflStatus
			    .getArbeidsforholdIkkeFrilans()
			    .stream().allMatch(a ->
					    grunnlag.getInntektsgrunnlag().finnesInntektsdata(Inntektskilde.INNTEKTSMELDING, a));

        return alleArbeidsforholdHarInntektsmelding ? ja() : nei();
    }

}
