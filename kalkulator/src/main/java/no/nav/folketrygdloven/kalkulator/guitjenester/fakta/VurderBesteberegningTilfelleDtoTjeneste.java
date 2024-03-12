package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.VurderBesteberegningDto;

public class VurderBesteberegningTilfelleDtoTjeneste {

    public void lagDto(BeregningsgrunnlagGUIInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        if (!harBgTilfelle(input.getBeregningsgrunnlag())) {
            return;
        }
        BeregningsgrunnlagTilstand aktivTilstand = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagTilstand();
        settVerdier(input.getBeregningsgrunnlag(), aktivTilstand, faktaOmBeregningDto);
    }

    private void settVerdier(BeregningsgrunnlagDto bg, BeregningsgrunnlagTilstand aktivTilstand, FaktaOmBeregningDto faktaOmBeregningDto) {
        VurderBesteberegningDto vurderBesteberegning = new VurderBesteberegningDto();
        vurderBesteberegning.setSkalHaBesteberegning(harBesteberegning(bg, aktivTilstand));
        faktaOmBeregningDto.setVurderBesteberegning(vurderBesteberegning);
    }

    private boolean harBgTilfelle(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getFaktaOmBeregningTilfeller().contains(FaktaOmBeregningTilfelle.VURDER_BESTEBEREGNING)
            || beregningsgrunnlag.getFaktaOmBeregningTilfeller().contains(FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE);
    }

    private Boolean harBesteberegning(BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagTilstand aktivTilstand) {
        if (aktivTilstand.erFør(BeregningsgrunnlagTilstand.KOFAKBER_UT)) {
            return null;
        }
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()).anyMatch(andel -> andel.getBesteberegningPrÅr() != null);
    }
}
