package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkAvvikSammenligningsgrunnlagMotAvviksgrense.ID)
public class SjekkAvvikSammenligningsgrunnlagMotAvviksgrense extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 26.1";
    static final String BESKRIVELSE = "Har rapportert inntekt avvik mot sammenligningsgrunnlag > 25%?";

    public SjekkAvvikSammenligningsgrunnlagMotAvviksgrense() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        if(skalRefusjonSjekkesFørAksjonspunkt(grunnlag)) {
            return direkteUtbetalingTilBrukerOgAvvik(grunnlag) ? ja() : nei();
        }
        return erAvvikStørreEnn25Prosent(grunnlag) ? ja() : nei();
    }

    private boolean erAvvikStørreEnn25Prosent(BeregningsgrunnlagPeriode grunnlag){
        if(grunnlag.skalSplitteSammenligningsgrunnlagToggle()){
            return grunnlag.getBeregningsgrunnlag().getSammenligningsGrunnlagPrAktivitetstatus(AktivitetStatus.AT).getAvvikProsent().compareTo(grunnlag.getAvviksgrenseProsent()) > 0;
        }
        return grunnlag.getBeregningsgrunnlag().getSammenligningsGrunnlag().getAvvikProsent().compareTo(grunnlag.getAvviksgrenseProsent()) > 0;
    }

    private boolean direkteUtbetalingTilBrukerOgAvvik(BeregningsgrunnlagPeriode grunnlag){
        return girDirekteUtbetalingTilBruker(grunnlag) && erAvvikStørreEnn25Prosent(grunnlag);
    }

    private boolean skalRefusjonSjekkesFørAksjonspunkt(BeregningsgrunnlagPeriode grunnlag){
        return grunnlag.skalSjekkeRefusjonFørAvviksvurdering();
    }

    private boolean girDirekteUtbetalingTilBruker(BeregningsgrunnlagPeriode grunnlag){
        BigDecimal minsteRefusjon = grunnlag.getGrenseverdi().min(grunnlag.finnMinsteTotalRefusjonForPeriode());
        BigDecimal totaltBeregningsgrunnlag = grunnlag.getBeregningsgrunnlagPrStatus().stream()
            .map(BeregningsgrunnlagPrStatus::getGradertBruttoPrÅr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avkortetTotaltGrunnlag = grunnlag.getGrenseverdi().min(totaltBeregningsgrunnlag);
        return minsteRefusjon.compareTo(avkortetTotaltGrunnlag) < 0;
    }
}
