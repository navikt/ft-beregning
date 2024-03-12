package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.util.List;

public class BesteberegningFødendeKvinneDto {

    private List<BesteberegningFødendeKvinneAndelDto> besteberegningAndelListe;

    private DagpengeAndelLagtTilBesteberegningDto nyDagpengeAndel;

    public BesteberegningFødendeKvinneDto(List<BesteberegningFødendeKvinneAndelDto> besteberegningAndelListe) {
        this.besteberegningAndelListe = besteberegningAndelListe;
    }

    public BesteberegningFødendeKvinneDto(List<BesteberegningFødendeKvinneAndelDto> besteberegningAndelListe, DagpengeAndelLagtTilBesteberegningDto nyDagpengeAndel) {
        this.nyDagpengeAndel = nyDagpengeAndel;
        this.besteberegningAndelListe = besteberegningAndelListe;
    }

    public List<BesteberegningFødendeKvinneAndelDto> getBesteberegningAndelListe() {
        return besteberegningAndelListe;
    }

    public void setBesteberegningAndelListe(List<BesteberegningFødendeKvinneAndelDto> besteberegningAndelListe) {
        this.besteberegningAndelListe = besteberegningAndelListe;
    }

    public DagpengeAndelLagtTilBesteberegningDto getNyDagpengeAndel() {
        return nyDagpengeAndel;
    }
}
