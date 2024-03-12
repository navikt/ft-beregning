package no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


public class FordelFastsatteVerdierDto {

    private Integer refusjonPrÅr;
    private Integer fastsattBeløp;
    private Integer fastsattÅrsbeløp;
    private Integer fastsattÅrsbeløpInklNaturalytelse;
    private Inntektskategori inntektskategori;

    private FordelFastsatteVerdierDto() {}

    private FordelFastsatteVerdierDto(Integer refusjonPrÅr,
                                      Integer fastsattBeløp,
                                      Integer fastsattÅrsbeløp,
                                      Inntektskategori inntektskategori,
                                      Integer fastsattÅrsbeløpInklNaturalytelse) {
        this.refusjonPrÅr = refusjonPrÅr;
        this.fastsattBeløp = fastsattBeløp;
        this.fastsattÅrsbeløp = fastsattÅrsbeløp;
        this.inntektskategori = inntektskategori;
        this.fastsattÅrsbeløpInklNaturalytelse = fastsattÅrsbeløpInklNaturalytelse;
    }

    public Integer getRefusjonPrÅr() {
        return refusjonPrÅr;
    }

    public Integer getFastsattBeløp() {
        return fastsattBeløp;
    }

    public Integer getFastsattÅrsbeløpInklNaturalytelse() {
        return fastsattÅrsbeløpInklNaturalytelse;
    }

    public Beløp finnEllerUtregnFastsattBeløpPrÅr() {
        if (fastsattÅrsbeløpInklNaturalytelse != null) {
            return Beløp.fra(fastsattÅrsbeløpInklNaturalytelse);
        }
        if (fastsattÅrsbeløp != null) {
            return Beløp.fra(fastsattÅrsbeløp);
        }
        if (fastsattBeløp == null) {
            throw new IllegalStateException("Feil under oppdatering: Hverken årslønn eller månedslønn er satt.");
        }
        return Beløp.fra(fastsattBeløp).multipliser(KonfigTjeneste.getMånederIÅr());
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }


    public static class Builder {

        public Builder(FordelFastsatteVerdierDto fordelFastsatteVerdierDto) {
            kladd = new FordelFastsatteVerdierDto(
                    fordelFastsatteVerdierDto.refusjonPrÅr,
                    fordelFastsatteVerdierDto.fastsattBeløp,
                    fordelFastsatteVerdierDto.fastsattÅrsbeløp,
                    fordelFastsatteVerdierDto.inntektskategori,
                    fordelFastsatteVerdierDto.fastsattÅrsbeløpInklNaturalytelse
            );
        }

        public Builder() {
            kladd = new FordelFastsatteVerdierDto();
        }

        private FordelFastsatteVerdierDto kladd;

        public static Builder ny() {
            return new Builder();
        }

        public static Builder oppdater(FordelFastsatteVerdierDto fordelFastsatteVerdierDto) {
            return new Builder(fordelFastsatteVerdierDto);
        }

        public Builder medRefusjonPrÅr(Integer refusjonPrÅr) {
            kladd.refusjonPrÅr = refusjonPrÅr;
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            kladd.inntektskategori = inntektskategori;
            return this;
        }

        public Builder medFastsattBeløpPrÅr(Integer fastsattBeløpPrÅr) {
            kladd.fastsattÅrsbeløp = fastsattBeløpPrÅr;
            return this;
        }

        public Builder medFastsattBeløpPrÅrInklNaturalytelse(Integer fastsattBeløpPrÅrInklNaturalytelse) {
            kladd.fastsattÅrsbeløpInklNaturalytelse = fastsattBeløpPrÅrInklNaturalytelse;
            return this;
        }

        public Builder medFastsattBeløpPrMnd(Integer fastsattBeløpPrMnd) {
            kladd.fastsattBeløp = fastsattBeløpPrMnd;
            return this;
        }

        public FordelFastsatteVerdierDto build() {
            return kladd;
        }

    }
}
