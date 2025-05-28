package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;

public class VurderLønnsendringDtoTjeneste {

    public void lagDto(BeregningsgrunnlagGUIInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        var tilfeller = beregningsgrunnlag.getFaktaOmBeregningTilfeller();
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING)) {
            var arbeidsforholdUtenInntektsmeldingDtoList = FaktaOmBeregningAndelDtoTjeneste.lagArbeidsforholdUtenInntektsmeldingDtoList(
                    beregningsgrunnlag,
                    input.getFaktaAggregat(),
                    input.getIayGrunnlag(), input.getInntektsmeldinger());
            if (!arbeidsforholdUtenInntektsmeldingDtoList.isEmpty()) {
                faktaOmBeregningDto.setArbeidsforholdMedLønnsendringUtenIM(arbeidsforholdUtenInntektsmeldingDtoList);
            }
        }
    }
}
