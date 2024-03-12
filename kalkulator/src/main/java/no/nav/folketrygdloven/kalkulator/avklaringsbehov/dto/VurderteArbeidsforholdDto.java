package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

public class VurderteArbeidsforholdDto  {

    private Long andelsnr;
    private boolean tidsbegrensetArbeidsforhold;

    public VurderteArbeidsforholdDto(Long andelsnr,
                                     boolean tidsbegrensetArbeidsforhold) {
        this.andelsnr = andelsnr;
        this.tidsbegrensetArbeidsforhold = tidsbegrensetArbeidsforhold;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public boolean isTidsbegrensetArbeidsforhold() {
        return tidsbegrensetArbeidsforhold;
    }

}
