package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SkalSetteAksjonspunkt.ID)
class SkalSetteAksjonspunkt extends LeafSpecification<BeregningsgrunnlagPeriode> {
    static final String ID = "FP_BR 26.1";
    static final String BESKRIVELSE = "Skal vi sette aksjonspunkt?";

    public SkalSetteAksjonspunkt() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        if(skalAlltidSetteAksjonspunkt(grunnlag)){
            return ja();
        }
        OmsorgspengerGrunnlag ompGrunnlag = (OmsorgspengerGrunnlag) grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag();
        BigDecimal minsteRefusjon = grunnlag.getGrenseverdi().min(ompGrunnlag.getGradertRefusjonVedSkjæringstidspunkt());
        BigDecimal totaltBeregningsgrunnlag = grunnlag.getBeregningsgrunnlagPrStatus().stream()
            .map(BeregningsgrunnlagPrStatus::getGradertBruttoPrÅr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avkortetTotaltGrunnlag = grunnlag.getGrenseverdi().min(totaltBeregningsgrunnlag);

        return minsteRefusjon.compareTo(avkortetTotaltGrunnlag) < 0 ? ja() : nei();
    }

    private boolean skalAlltidSetteAksjonspunkt(BeregningsgrunnlagPeriode grunnlag){
        return !grunnlag.skalSjekkeRefusjonFørAvviksvurdering() || erFrilanser(grunnlag);
    }

    private boolean erFrilanser(BeregningsgrunnlagPeriode grunnlag){
        return grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getFrilansArbeidsforhold().isPresent();
    }
}

