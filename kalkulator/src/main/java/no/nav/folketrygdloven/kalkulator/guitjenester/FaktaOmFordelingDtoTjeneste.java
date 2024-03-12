package no.nav.folketrygdloven.kalkulator.guitjenester;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelingDto;

public class FaktaOmFordelingDtoTjeneste {

    private FaktaOmFordelingDtoTjeneste() {
        // Skjul
    }

    public static Optional<FordelingDto> lagDto(BeregningsgrunnlagGUIInput input) {
        BeregningsgrunnlagTilstand tilstandForAktivtGrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagTilstand();

        if (tilstandForAktivtGrunnlag.erEtter(BeregningsgrunnlagTilstand.VURDERT_VILKÅR)) {
            FordelingDto dto = new FordelingDto();
            FordelBeregningsgrunnlagDtoTjeneste.lagDto(input, dto);
            dto.setVurderNyttInntektsforholdDto(VurderNyeInntektsforholdDtoTjeneste.lagDto(input));
            VurderStortingsperiodeDtoTjeneste.lagDto(input).ifPresent(dto::setVurderRepresentererStortinget);
            return harSattMinstEttFelt(dto) ? Optional.of(dto) : Optional.empty();
        }
        return Optional.empty();
    }

    private static boolean harSattMinstEttFelt(FordelingDto dto) {
        return dto.getFordelBeregningsgrunnlag() != null || dto.getVurderNyttInntektsforholdDto() != null || dto.getVurderRepresentererStortinget() != null;
    }

}
