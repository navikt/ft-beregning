package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class OmsorgspengerGrunnlag extends YtelsesSpesifiktGrunnlag {

    private final BigDecimal gradertRefusjonVedSkjæringstidspunkt;
    private final boolean erSøktForFLEllerSN;

    public OmsorgspengerGrunnlag(BigDecimal gradertRefusjonVedSkjæringstidspunkt,
                                 boolean erSøktForFLEllerSN) {
        super("OMP");
        this.erSøktForFLEllerSN = erSøktForFLEllerSN;
        this.gradertRefusjonVedSkjæringstidspunkt = gradertRefusjonVedSkjæringstidspunkt;
    }


    public boolean erDirekteUtbetaling() {
    	if (erSøktForFLEllerSN) {
    		return true;
	    }
        BeregningsgrunnlagPeriode førstePeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BigDecimal minsteRefusjon = førstePeriode.getGrenseverdi().min(gradertRefusjonVedSkjæringstidspunkt);
        BigDecimal totaltBeregningsgrunnlag = førstePeriode.getBeregningsgrunnlagPrStatus().stream()
            .map(BeregningsgrunnlagPrStatus::getGradertBruttoPrÅr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avkortetTotaltGrunnlag = førstePeriode.getGrenseverdi().min(totaltBeregningsgrunnlag);
        return minsteRefusjon.compareTo(avkortetTotaltGrunnlag) < 0;
    }


}
