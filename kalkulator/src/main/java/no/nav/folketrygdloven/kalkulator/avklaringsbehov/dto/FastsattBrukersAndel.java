package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;


import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class FastsattBrukersAndel {

    private Long andelsnr;
    private Boolean nyAndel;
    private Boolean lagtTilAvSaksbehandler;
    private Integer fastsattBeløp;
    private Inntektskategori inntektskategori;

    public FastsattBrukersAndel(Boolean nyAndel,
                                Long andelsnr,
                                Boolean lagtTilAvSaksbehandler,
                                Integer fastsattBeløp,
                                Inntektskategori inntektskategori) {
        this.fastsattBeløp = fastsattBeløp;
        this.inntektskategori = inntektskategori;
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
        this.andelsnr = andelsnr;
        this.nyAndel = nyAndel;
    }


    public Long getAndelsnr() {
        return andelsnr;
    }

    public void setAndelsnr(Long andelsnr) {
        this.andelsnr = andelsnr;
    }

    public Boolean getNyAndel() {
        return nyAndel;
    }

    public void setNyAndel(Boolean nyAndel) {
        this.nyAndel = nyAndel;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public void setLagtTilAvSaksbehandler(Boolean lagtTilAvSaksbehandler) {
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
    }

    public Integer getFastsattBeløp() {
        return fastsattBeløp;
    }

    public void setFastsattBeløp(Integer fastsattBeløp) {
        this.fastsattBeløp = fastsattBeløp;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public void setInntektskategori(Inntektskategori inntektskategori) {
        this.inntektskategori = inntektskategori;
    }
}
