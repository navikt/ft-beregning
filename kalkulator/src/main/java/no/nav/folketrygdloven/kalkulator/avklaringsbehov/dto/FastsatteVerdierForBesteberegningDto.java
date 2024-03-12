package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


public class FastsatteVerdierForBesteberegningDto {

    private Integer fastsattBeløp;

    private Inntektskategori inntektskategori;

    public FastsatteVerdierForBesteberegningDto(Integer fastsattBeløp,
                                                Inntektskategori inntektskategori) {
        this.fastsattBeløp = fastsattBeløp;
        this.inntektskategori = inntektskategori;
    }

    public Integer getFastsattBeløp() {
        return fastsattBeløp;
    }

    public Integer finnFastsattBeløpPrÅr() {
        if (fastsattBeløp == null) {
            throw new IllegalStateException("Feil under oppdatering: Månedslønn er ikke satt");
        }
        return fastsattBeløp * KonfigTjeneste.getMånederIÅrInt();
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Boolean getSkalHaBesteberegning() {
        return true;
    }

}
