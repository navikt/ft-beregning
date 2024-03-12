package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.List;

import no.nav.folketrygdloven.kalkulus.kodeverk.Arbeidskategori;

public class YtelseGrunnlagDto {

    private Arbeidskategori arbeidskategori;
    private List<YtelseFordelingDto> fordeling;


    public YtelseGrunnlagDto() {
    }

    public YtelseGrunnlagDto(Arbeidskategori arbeidskategori,
                             List<YtelseFordelingDto> fordeling) {
        this.arbeidskategori = arbeidskategori;
        this.fordeling = fordeling;
    }

    public List<YtelseFordelingDto> getFordeling() {
        return fordeling;
    }

    public Arbeidskategori getArbeidskategori() {
        return arbeidskategori;
    }

}
