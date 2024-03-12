package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.util.List;

public class VurderRefusjonBeregningsgrunnlagDto {

    private List<VurderRefusjonAndelBeregningsgrunnlagDto> andeler;

    public VurderRefusjonBeregningsgrunnlagDto(List<VurderRefusjonAndelBeregningsgrunnlagDto> andeler) {
        this.andeler = andeler;
    }

    public List<VurderRefusjonAndelBeregningsgrunnlagDto> getAndeler() {
        return andeler;
    }
}
