package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SkalSetteAksjonspunkt.ID)
public class SkalSetteAksjonspunkt extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 26.1";
    static final String BESKRIVELSE = "Skal vi sette aksjonspunkt?";

    public SkalSetteAksjonspunkt() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        if(grunnlag.skalSjekkeRefusjonFørAvviksvurdering()){
            return direkteUtbetalingTilBrukerOgAvvik(grunnlag) ? ja() : nei();
        }
        return erAvvikStørreEnn25Prosent(grunnlag) ? ja() : nei();
    }

    private boolean direkteUtbetalingTilBrukerOgAvvik(BeregningsgrunnlagPeriode grunnlag){
        return girDirekteUtbetalingTilBruker(grunnlag) && erAvvikStørreEnn25Prosent(grunnlag);
    }

    private boolean girDirekteUtbetalingTilBruker(BeregningsgrunnlagPeriode grunnlag){
        BigDecimal minsteRefusjon = grunnlag.getGrenseverdi().min(grunnlag.finnMinsteTotalRefusjonForPeriode());
        BigDecimal totaltBeregningsgrunnlag = grunnlag.getBeregningsgrunnlagPrStatus().stream()
            .map(BeregningsgrunnlagPrStatus::getGradertBruttoPrÅr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avkortetTotaltGrunnlag = grunnlag.getGrenseverdi().min(totaltBeregningsgrunnlag);
        return minsteRefusjon.compareTo(avkortetTotaltGrunnlag) < 0;
    }

    private boolean erAvvikStørreEnn25Prosent(BeregningsgrunnlagPeriode grunnlag){
        if(grunnlag.isSplitteATFLToggleErPå()){
            return grunnlag.getBeregningsgrunnlag().getSammenligningsGrunnlagPrAktivitetstatus(AktivitetStatus.AT).getAvvikProsent().compareTo(grunnlag.getAvviksgrenseProsent()) > 0;
        }
        return grunnlag.getBeregningsgrunnlag().getSammenligningsGrunnlag().getAvvikProsent().compareTo(grunnlag.getAvviksgrenseProsent()) > 0;
    }
}
