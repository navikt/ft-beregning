package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;


import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class BesteberegningFødendeKvinneAndelDto {

    private Long andelsnr;

    private Boolean lagtTilAvSaksbehandler;

    private FastsatteVerdierForBesteberegningDto fastsatteVerdier;

    public BesteberegningFødendeKvinneAndelDto(Long andelsnr, Integer inntektPrMnd, Inntektskategori inntektskategori,
                                               boolean lagtTilAvSaksbehandler) {
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
        this.andelsnr = andelsnr;
        fastsatteVerdier = new FastsatteVerdierForBesteberegningDto(inntektPrMnd, inntektskategori);
    }

    public FastsatteVerdierForBesteberegningDto getFastsatteVerdier() {
        return fastsatteVerdier;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

}
