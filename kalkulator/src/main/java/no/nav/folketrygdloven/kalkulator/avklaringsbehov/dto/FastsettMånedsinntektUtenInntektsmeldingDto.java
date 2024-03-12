package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.util.List;

public class FastsettMånedsinntektUtenInntektsmeldingDto {

    private List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe;

    public FastsettMånedsinntektUtenInntektsmeldingDto() {
    }

    public FastsettMånedsinntektUtenInntektsmeldingDto(List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe) {
        this.andelListe = andelListe;
    }

    public List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> getAndelListe() {
        return andelListe;
    }

    public void setAndelListe(List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe) {
        this.andelListe = andelListe;
    }
}
