package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

public class FastsettBruttoBeregningsgrunnlagSNDto {

    private Integer bruttoBeregningsgrunnlag;

    public  FastsettBruttoBeregningsgrunnlagSNDto(Integer bruttoBeregningsgrunnlag) {
        this.bruttoBeregningsgrunnlag = bruttoBeregningsgrunnlag;
    }

    public Integer getBruttoBeregningsgrunnlag() {
        return bruttoBeregningsgrunnlag;
    }
}
