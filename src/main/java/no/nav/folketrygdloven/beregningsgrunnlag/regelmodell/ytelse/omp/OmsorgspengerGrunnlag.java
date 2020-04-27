package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp;

import java.math.BigDecimal;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class OmsorgspengerGrunnlag extends YtelsesSpesifiktGrunnlag {

    private List<OmsorgspengerGrunnlagPeriode> perioder;

    public OmsorgspengerGrunnlag(List<OmsorgspengerGrunnlagPeriode> perioder) {
        super("OMP");
        this.perioder = perioder;
    }

    public List<OmsorgspengerGrunnlagPeriode> getPerioder() {
        return perioder;
    }

    public BigDecimal finnLavstetTotalRefusjonForPeriode(Periode periode) {
        return getPerioder().stream()
            .filter(p -> p.getPeriode().overlapper(periode))
            .findFirst()
            .map(OmsorgspengerGrunnlagPeriode::getMinsteRefusjonForPeriode)
            .orElse(BigDecimal.ZERO);

    }

}
