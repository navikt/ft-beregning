package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


public class FastsatteVerdierDto {

    private Integer fastsattBeløp;
    private Integer fastsattÅrsbeløp;
    private Inntektskategori inntektskategori;
    private Boolean skalHaBesteberegning;

    private FastsatteVerdierDto() {}

    private FastsatteVerdierDto(Integer fastsattBeløp,
                                Integer fastsattÅrsbeløp,
                                Inntektskategori inntektskategori,
                                Boolean skalHaBesteberegning) {
        this.fastsattBeløp = fastsattBeløp;
        this.fastsattÅrsbeløp = fastsattÅrsbeløp;
        this.inntektskategori = inntektskategori;
        this.skalHaBesteberegning = skalHaBesteberegning;
    }

    public Integer getFastsattBeløp() {
        return fastsattBeløp;
    }


    public Beløp finnEllerUtregnFastsattBeløpPrÅr() {
        if (fastsattÅrsbeløp != null) {
            return Beløp.fra(fastsattÅrsbeløp);
        }
        if (fastsattBeløp == null) {
            throw new IllegalStateException("Feil under oppdatering: Hverken årslønn eller månedslønn er satt.");
        }
        return Beløp.fra((long) fastsattBeløp).multipliser(KonfigTjeneste.getMånederIÅr());
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Boolean getSkalHaBesteberegning() {
        return skalHaBesteberegning;
    }

    public static class Builder {

        public Builder(FastsatteVerdierDto fastsatteVerdierDto) {
            kladd = new FastsatteVerdierDto(
                    fastsatteVerdierDto.fastsattBeløp,
                    fastsatteVerdierDto.fastsattÅrsbeløp,
                    fastsatteVerdierDto.inntektskategori,
                    fastsatteVerdierDto.skalHaBesteberegning
            );
        }

        public Builder() {
            kladd = new FastsatteVerdierDto();
        }

        private FastsatteVerdierDto kladd;

        public static Builder ny() {
            return new Builder();
        }

        public static Builder oppdater(FastsatteVerdierDto fastsatteVerdierDto) {
            return new Builder(fastsatteVerdierDto);
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            kladd.inntektskategori = inntektskategori;
            return this;
        }

        public Builder medFastsattBeløpPrÅr(Integer fastsattBeløpPrÅr) {
            kladd.fastsattÅrsbeløp = fastsattBeløpPrÅr;
            return this;
        }

        public Builder medFastsattBeløpPrMnd(Integer fastsattBeløpPrMnd) {
            kladd.fastsattBeløp = fastsattBeløpPrMnd;
            return this;
        }

        public Builder medSkalHaBesteberegning(Boolean skalHaBesteberegning) {
            kladd.skalHaBesteberegning = skalHaBesteberegning;
            return this;
        }

        public FastsatteVerdierDto build() {
            return kladd;
        }

    }
}
