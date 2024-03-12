package no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling;


import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class FordelBeregningsgrunnlagAndelDto extends RedigerbarAndelDto {

    private FordelFastsatteVerdierDto fastsatteVerdier;
    private Inntektskategori forrigeInntektskategori;
    private Integer forrigeRefusjonPrÅr;
    private Integer forrigeArbeidsinntektPrÅr;

    FordelBeregningsgrunnlagAndelDto() { // NOSONAR
        // Jackson
    }

    public FordelBeregningsgrunnlagAndelDto(RedigerbarAndelDto andelDto,
                                            FordelFastsatteVerdierDto fastsatteVerdier,
                                            Inntektskategori forrigeInntektskategori, Integer forrigeRefusjonPrÅr, Integer forrigeArbeidsinntektPrÅr) {
        super(andelDto.getAndelsnr(), andelDto.getArbeidsgiverId(), andelDto.getArbeidsforholdId().getReferanse(),
                andelDto.erNyAndel(), andelDto.getKilde());
        this.fastsatteVerdier = fastsatteVerdier;
        this.forrigeArbeidsinntektPrÅr = forrigeArbeidsinntektPrÅr;
        this.forrigeInntektskategori = forrigeInntektskategori;
        this.forrigeRefusjonPrÅr = forrigeRefusjonPrÅr;
    }

    public FordelFastsatteVerdierDto getFastsatteVerdier() {
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
