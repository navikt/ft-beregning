package no.nav.folketrygdloven.kalkulus.migrering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;

import java.math.BigDecimal;

public class SammenligningsgrunnlagPrStatusMigreringDto extends BaseMigreringDto {

    @NotNull
    @Valid
    private Periode sammenligningsperiode;

    @NotNull
    @Valid
    private SammenligningsgrunnlagType sammenligningsgrunnlagType;

    @NotNull
    @Valid
    private Beløp rapportertPrÅr;

    @NotNull
    @Valid
    private BigDecimal avvikPromille = BigDecimal.ZERO;

    public SammenligningsgrunnlagPrStatusMigreringDto() {
    }

    public SammenligningsgrunnlagPrStatusMigreringDto(Periode sammenligningsperiode,
                                                      SammenligningsgrunnlagType sammenligningsgrunnlagType,
                                                      Beløp rapportertPrÅr,
                                                      BigDecimal avvikPromille) {
        this.sammenligningsperiode = sammenligningsperiode;
        this.sammenligningsgrunnlagType = sammenligningsgrunnlagType;
        this.rapportertPrÅr = rapportertPrÅr;
        this.avvikPromille = avvikPromille;
    }

    public Periode getSammenligningsperiode() {
        return sammenligningsperiode;
    }

    public SammenligningsgrunnlagType getSammenligningsgrunnlagType() {
        return sammenligningsgrunnlagType;
    }

    public Beløp getRapportertPrÅr() {
        return rapportertPrÅr;
    }

    public BigDecimal getAvvikPromille() {
        return avvikPromille;
    }
}
