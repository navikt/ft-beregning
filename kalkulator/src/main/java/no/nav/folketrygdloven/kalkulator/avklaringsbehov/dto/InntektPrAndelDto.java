package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

public class InntektPrAndelDto {

    private Integer inntekt;

    private Long andelsnr;

    public InntektPrAndelDto(Integer inntekt, Long andelsnr) {
        this.inntekt = inntekt;
        this.andelsnr = andelsnr;
    }

    public Integer getInntekt() {
        return inntekt;
    }

    public void setInntekt(Integer inntekt) {
        this.inntekt = inntekt;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public void setAndelsnr(Long andelsnr) {
        this.andelsnr = andelsnr;
    }
}
