package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;


import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class FastsettBeregningsgrunnlagAndelDto extends RedigerbarAndelFaktaOmBeregningDto {

    private FastsatteVerdierDto fastsatteVerdier;
    private Inntektskategori forrigeInntektskategori;
    private Integer forrigeRefusjonPrÅr;
    private Integer forrigeArbeidsinntektPrÅr;

    public FastsettBeregningsgrunnlagAndelDto(RedigerbarAndelFaktaOmBeregningDto andelDto,
                                              FastsatteVerdierDto fastsatteVerdier, Inntektskategori forrigeInntektskategori, Integer forrigeRefusjonPrÅr, Integer forrigeArbeidsinntektPrÅr) {
        super(andelDto.getAndelsnr().orElse(null), andelDto.getNyAndel(), andelDto.getAktivitetStatus().orElse(null), andelDto.getLagtTilAvSaksbehandler());
        this.fastsatteVerdier = fastsatteVerdier;
        this.forrigeArbeidsinntektPrÅr = forrigeArbeidsinntektPrÅr;
        this.forrigeInntektskategori = forrigeInntektskategori;
        this.forrigeRefusjonPrÅr = forrigeRefusjonPrÅr;
    }

    public FastsatteVerdierDto getFastsatteVerdier() {
        return fastsatteVerdier;
    }

    public Inntektskategori getForrigeInntektskategori() {
        return forrigeInntektskategori;
    }

    public Integer getForrigeRefusjonPrÅr() {
        return forrigeRefusjonPrÅr;
    }

    public Integer getForrigeArbeidsinntektPrÅr() {
        return forrigeArbeidsinntektPrÅr;
    }
}
