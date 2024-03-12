package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;

public class NyOppstartetFLDtoTjeneste {

    public void lagDto(BeregningsgrunnlagGUIInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<FaktaOmBeregningTilfelle> tilfeller = beregningsgrunnlag.getFaktaOmBeregningTilfeller();
        if (!tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL)  || faktaOmBeregningDto.getFrilansAndel() != null) {
            return;
        }
        FaktaOmBeregningAndelDtoTjeneste.lagFrilansAndelDto(beregningsgrunnlag, input.getIayGrunnlag()).ifPresent(faktaOmBeregningDto::setFrilansAndel);
    }
}
