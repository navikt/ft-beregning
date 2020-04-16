package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SkalSjekkeAvvik.ID)
public class SkalSjekkeAvvik extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 26.1";
    static final String BESKRIVELSE = "Skal vi sjekke avvik?";

    public SkalSjekkeAvvik() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        if(!grunnlag.isSkalSjekkeRefusjonFørAvviksvurdering()){
            return ja();
        }
        BigDecimal maksimalRefusjon = grunnlag.getGrenseverdi().min(grunnlag.getMaksRefusjonForPeriode());
        BigDecimal totaltBeregningsgrunnlag = grunnlag.getBeregningsgrunnlagPrStatus().stream()
            .map(BeregningsgrunnlagPrStatus::getBruttoPrÅr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avkortetTotaltGrunnlag = grunnlag.getGrenseverdi().min(totaltBeregningsgrunnlag);

        return maksimalRefusjon.compareTo(avkortetTotaltGrunnlag) < 0 ? ja() : nei();
    }
}