package no.nav.folketrygdloven.beregningsgrunnlag.vurder.frisinn;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class SjekkFrilansUtenInntekt extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FRISINN 3.4";
    static final String BESKRIVELSE = "Er beregningsgrunnlag fra SN/FL mindre enn en 0,75G?";

    SjekkFrilansUtenInntekt() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var erSøktKunFrilansUtenInntekt = false;
        var atflStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        var frilansandel = atflStatus == null ? null : atflStatus.getFrilansArbeidsforhold().orElse(null);
        if (frilansandel != null && frilansandel.getErSøktYtelseFor() && !erSøktForNæring(grunnlag)) {
            var førstePeriode = grunnlag.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
            var atflStatusFørstePeriode = førstePeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
            var frilansandelFørstePeriode = atflStatusFørstePeriode == null ? null : atflStatusFørstePeriode.getFrilansArbeidsforhold().orElse(null);
            var inntektFrilans = frilansandelFørstePeriode != null ? frilansandelFørstePeriode.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO) : BigDecimal.ZERO;
            erSøktKunFrilansUtenInntekt = inntektFrilans.compareTo(BigDecimal.ZERO) == 0;
        }
        var resultat = erSøktKunFrilansUtenInntekt ? ja() : nei();
        resultat.setEvaluationProperty("erSøktKunFrilansUtenInntekt", erSøktKunFrilansUtenInntekt);
        return resultat;
    }

    private boolean erSøktForNæring(BeregningsgrunnlagPeriode grunnlag) {
        var snStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        return snStatus != null && snStatus.erSøktYtelseFor();
    }
}
