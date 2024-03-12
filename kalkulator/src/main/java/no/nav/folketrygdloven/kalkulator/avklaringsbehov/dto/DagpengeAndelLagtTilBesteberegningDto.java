package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;


import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class DagpengeAndelLagtTilBesteberegningDto {

    private FastsatteVerdierForBesteberegningDto fastsatteVerdier;

    public DagpengeAndelLagtTilBesteberegningDto(int fastsattBeløp, Inntektskategori inntektskategori) {
        this.fastsatteVerdier = new FastsatteVerdierForBesteberegningDto(fastsattBeløp, inntektskategori);
    }

    public FastsatteVerdierForBesteberegningDto getFastsatteVerdier() {
        return fastsatteVerdier;
    }

}
