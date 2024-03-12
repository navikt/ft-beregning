package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class BesteberegningFødendeKvinneDto {

    @JsonProperty("besteberegningAndelListe")
    @Valid
    @NotNull
    @Size(min = 1)
    private List<BesteberegningFødendeKvinneAndelDto> besteberegningAndelListe;

    @JsonProperty("nyDagpengeAndel")
    @Valid
    private DagpengeAndelLagtTilBesteberegningDto nyDagpengeAndel;

    public BesteberegningFødendeKvinneDto() {
        // For Json deserialisering
    }

    public BesteberegningFødendeKvinneDto(@Valid @NotNull List<BesteberegningFødendeKvinneAndelDto> besteberegningAndelListe, @Valid DagpengeAndelLagtTilBesteberegningDto nyDagpengeAndel) {
        this.besteberegningAndelListe = besteberegningAndelListe;
        this.nyDagpengeAndel = nyDagpengeAndel;
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
