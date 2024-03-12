package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

public class FastsettMånedsinntektFLDto {

    private Integer maanedsinntekt;

    public FastsettMånedsinntektFLDto(Integer maanedsInntekt) { // NOSONAR
        this.maanedsinntekt = maanedsInntekt;
    }

    public void setMaanedsinntekt(Integer maanedsinntekt) {
        this.maanedsinntekt = maanedsinntekt;
    }

    public Integer getMaanedsinntekt() {
        return maanedsinntekt;
    }
}
