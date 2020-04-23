package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;

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
        if(!grunnlag.skalSjekkeRefusjonFørAvviksvurdering()){
            return ja();
        }
        BigDecimal maksimalRefusjon = grunnlag.getGrenseverdi().min(grunnlag.finnMaksRefusjonForPeriode());
        BigDecimal totaltBeregningsgrunnlag = grunnlag.getBeregningsgrunnlagPrStatus().stream()
            .map(BeregningsgrunnlagPrStatus::getBruttoPrÅr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avkortetTotaltGrunnlag = grunnlag.getGrenseverdi().min(totaltBeregningsgrunnlag);

        return maksimalRefusjon.compareTo(avkortetTotaltGrunnlag) < 0 ? ja() : nei();
    }
}
