package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

public class FastsatteAndelerTidsbegrensetDto {

    private Long andelsnr;
    private Integer bruttoFastsattInntekt;

    public FastsatteAndelerTidsbegrensetDto(Long andelsnr,
                                            Integer bruttoFastsattInntekt) {
        this.andelsnr = andelsnr;
        this.bruttoFastsattInntekt = bruttoFastsattInntekt;
    }
    public Long getAndelsnr() { return andelsnr; }

    public Integer getBruttoFastsattInntekt() {
        return bruttoFastsattInntekt;
    }

}
