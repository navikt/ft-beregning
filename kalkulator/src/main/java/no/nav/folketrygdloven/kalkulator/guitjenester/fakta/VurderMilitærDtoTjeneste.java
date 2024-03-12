package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.VurderMilitærDto;

public class VurderMilitærDtoTjeneste {

    public void lagDto(BeregningsgrunnlagGUIInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        List<BeregningsgrunnlagAktivitetStatusDto> aktivitetStatuser = input.getBeregningsgrunnlag().getAktivitetStatuser();
        BeregningsgrunnlagTilstand aktivTilstand = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagTilstand();
        VurderMilitærDto dto;
        if (aktivTilstand.erFør(BeregningsgrunnlagTilstand.KOFAKBER_UT)) {
            dto = new VurderMilitærDto(null);
        } else {
            dto = new VurderMilitærDto(aktivitetStatuser.stream().anyMatch(status -> status.getAktivitetStatus().equals(AktivitetStatus.MILITÆR_ELLER_SIVIL)));
        }
        faktaOmBeregningDto.setVurderMilitaer(dto);
    }
}
