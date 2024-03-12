package no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling;

import java.time.LocalDate;
import java.util.List;

public class FordelBeregningsgrunnlagPeriodeDto {

    private List<FordelBeregningsgrunnlagAndelDto> andeler;
    private LocalDate fom;
    private LocalDate tom;


    public FordelBeregningsgrunnlagPeriodeDto(List<FordelBeregningsgrunnlagAndelDto> andeler, LocalDate fom, LocalDate tom) { // NOSONAR
        this.andeler = andeler;
        this.fom = fom;
        this.tom = tom;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public List<FordelBeregningsgrunnlagAndelDto> getAndeler() {
        return andeler;
    }

}
