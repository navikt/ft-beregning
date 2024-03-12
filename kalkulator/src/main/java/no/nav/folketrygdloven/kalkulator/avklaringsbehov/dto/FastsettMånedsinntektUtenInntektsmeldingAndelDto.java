package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;


import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class FastsettMånedsinntektUtenInntektsmeldingAndelDto {

    private Long andelsnr;

    private Integer fastsattBeløp;

    private Inntektskategori inntektskategori;

    public FastsettMånedsinntektUtenInntektsmeldingAndelDto(Long andelsnr, FastsatteVerdierDto fastsatteVerdier) { // NOSONAR
        this.andelsnr = andelsnr;
        this.fastsattBeløp = fastsatteVerdier.getFastsattBeløp();
        this.inntektskategori = fastsatteVerdier.getInntektskategori();
    }

    public Long getAndelsnr() {
        return andelsnr;
    }


    public Integer getFastsattBeløp() {
        return fastsattBeløp;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

}
