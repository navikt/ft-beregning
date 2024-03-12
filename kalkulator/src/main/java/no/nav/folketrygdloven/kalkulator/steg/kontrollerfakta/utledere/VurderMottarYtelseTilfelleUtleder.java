package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.VurderMottarYtelseTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


public class VurderMottarYtelseTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input, BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        return VurderMottarYtelseTjeneste.skalVurdereMottattYtelse(beregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger()) ?
            Optional.of(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE) : Optional.empty();
    }

}
