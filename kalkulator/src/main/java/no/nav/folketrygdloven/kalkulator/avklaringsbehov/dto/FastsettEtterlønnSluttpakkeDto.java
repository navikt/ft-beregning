package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

public class FastsettEtterlønnSluttpakkeDto {

    private Integer fastsattPrMnd;

    public FastsettEtterlønnSluttpakkeDto(Integer fastsattPrMnd) { // NOSONAR
        this.fastsattPrMnd = fastsattPrMnd;
    }

    public Integer getFastsattPrMnd() {
        return fastsattPrMnd;
    }

    public void setFastsattPrMnd(Integer fastsattPrMnd) {
        this.fastsattPrMnd = fastsattPrMnd;
    }
}
