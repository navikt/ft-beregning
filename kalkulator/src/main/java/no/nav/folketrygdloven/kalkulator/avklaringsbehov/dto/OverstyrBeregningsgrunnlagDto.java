package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.util.List;

public class OverstyrBeregningsgrunnlagDto {

    private FaktaBeregningLagreDto fakta;

    private List<FastsettBeregningsgrunnlagAndelDto> overstyrteAndeler;

    public OverstyrBeregningsgrunnlagDto(List<FastsettBeregningsgrunnlagAndelDto> overstyrteAndeler, FaktaBeregningLagreDto fakta) { // NOSONAR
        this.overstyrteAndeler = overstyrteAndeler;
        this.fakta = fakta;
    }

    public FaktaBeregningLagreDto getFakta() {
        return fakta;
    }

    public List<FastsettBeregningsgrunnlagAndelDto> getOverstyrteAndeler() {
        return overstyrteAndeler;
    }
}
