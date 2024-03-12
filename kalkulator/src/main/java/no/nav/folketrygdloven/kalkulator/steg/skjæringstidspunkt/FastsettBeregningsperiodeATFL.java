package no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;


public class FastsettBeregningsperiodeATFL {

    private FastsettBeregningsperiodeATFL() {}

    public static BeregningsgrunnlagDto fastsettBeregningsperiodeForATFL(BeregningsgrunnlagDto beregningsgrunnlag, Intervall beregningsperiode) {
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();
        nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(a -> a.getAktivitetStatus().erArbeidstaker() || a.getAktivitetStatus().erFrilanser())
                    .forEach(a -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(a).medBeregningsperiode(beregningsperiode));
        });
        return nyttBeregningsgrunnlag;
    }


}
