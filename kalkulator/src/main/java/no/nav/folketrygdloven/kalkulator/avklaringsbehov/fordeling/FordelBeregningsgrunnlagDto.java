package no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling;

import java.util.List;

public class FordelBeregningsgrunnlagDto {

    private List<FordelBeregningsgrunnlagPeriodeDto> endretBeregningsgrunnlagPerioder;

    public FordelBeregningsgrunnlagDto(List<FordelBeregningsgrunnlagPeriodeDto> endretBeregningsgrunnlagPerioder) { // NOSONAR
        this.endretBeregningsgrunnlagPerioder = endretBeregningsgrunnlagPerioder;
    }

    public List<FordelBeregningsgrunnlagPeriodeDto> getEndretBeregningsgrunnlagPerioder() {
        return endretBeregningsgrunnlagPerioder;
    }
}
