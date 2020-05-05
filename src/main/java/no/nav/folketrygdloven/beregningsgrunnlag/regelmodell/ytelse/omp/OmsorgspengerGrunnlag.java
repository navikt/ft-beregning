package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp;

import java.math.BigDecimal;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class OmsorgspengerGrunnlag extends YtelsesSpesifiktGrunnlag {

    private BigDecimal gradertRefusjonVedSkjæringstidspunkt;

    public OmsorgspengerGrunnlag(BigDecimal gradertRefusjonVedSkjæringstidspunkt) {
        super("OMP");
        this.gradertRefusjonVedSkjæringstidspunkt = gradertRefusjonVedSkjæringstidspunkt;
    }

    public BigDecimal getGradertRefusjonVedSkjæringstidspunkt() {
        return gradertRefusjonVedSkjæringstidspunkt;
    }
}
