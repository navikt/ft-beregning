package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ytelse;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.fp.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

public class ForeldrepengerGrunnlagMapper {

    public YtelsesSpesifiktGrunnlag map(BeregningsgrunnlagDto beregningsgrunnlag) {
        boolean harVærtBesteberegnet = beregningsgrunnlag.getFaktaOmBeregningTilfeller().stream()
                .anyMatch(FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE::equals);
        return new ForeldrepengerGrunnlag(harVærtBesteberegnet);
    }

}
