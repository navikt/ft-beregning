package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.ATogFLISammeOrganisasjonDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;

public class VurderATFLISammeOrgDtoTjeneste {

    public void lagDto(BeregningsgrunnlagGUIInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<FaktaOmBeregningTilfelle> tilfeller = beregningsgrunnlag.getFaktaOmBeregningTilfeller();
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON)) {
            List<ATogFLISammeOrganisasjonDto> aTogFLISammeOrganisasjonDto = FaktaOmBeregningAndelDtoTjeneste.lagATogFLISAmmeOrganisasjonListe(beregningsgrunnlag, input.getInntektsmeldinger(), input.getIayGrunnlag());
            if (faktaOmBeregningDto.getFrilansAndel() == null) {
                FaktaOmBeregningAndelDtoTjeneste.lagFrilansAndelDto(beregningsgrunnlag, input.getIayGrunnlag()).ifPresent(faktaOmBeregningDto::setFrilansAndel);
            }
            faktaOmBeregningDto.setArbeidstakerOgFrilanserISammeOrganisasjonListe(aTogFLISammeOrganisasjonDto);
        }
    }
}
