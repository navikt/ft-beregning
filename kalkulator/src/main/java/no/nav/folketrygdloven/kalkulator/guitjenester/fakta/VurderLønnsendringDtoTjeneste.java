package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;

public class VurderLønnsendringDtoTjeneste {

    public void lagDto(BeregningsgrunnlagGUIInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<FaktaOmBeregningTilfelle> tilfeller = beregningsgrunnlag.getFaktaOmBeregningTilfeller();
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING)) {
            List<FaktaOmBeregningAndelDto> arbeidsforholdUtenInntektsmeldingDtoList = FaktaOmBeregningAndelDtoTjeneste.lagArbeidsforholdUtenInntektsmeldingDtoList(
                    beregningsgrunnlag,
                    input.getFaktaAggregat(),
                    input.getIayGrunnlag(), input.getInntektsmeldinger());
            if (!arbeidsforholdUtenInntektsmeldingDtoList.isEmpty()) {
                faktaOmBeregningDto.setArbeidsforholdMedLønnsendringUtenIM(arbeidsforholdUtenInntektsmeldingDtoList);
            }
        }
    }
}
