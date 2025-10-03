package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.RefusjonskravSomKommerForSentDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class RefusjonTilVurderingDto {

    @Valid
    @JsonProperty("andeler")
    @Size
    @NotNull
    private List<RefusjonAndelTilVurderingDto> andeler;

    @Valid
    @JsonProperty("refusjonskravSomKommerForSentListe")
    @Size
    @NotNull
    private List<RefusjonskravSomKommerForSentDto> refusjonskravSomKommerForSentListe;

    public RefusjonTilVurderingDto() {
    }

    public RefusjonTilVurderingDto(@Valid @NotNull List<RefusjonAndelTilVurderingDto> andeler, @Valid @NotNull List<RefusjonskravSomKommerForSentDto> refusjonskravSomKommerForSentListe) {
        this.andeler = andeler;
        this.refusjonskravSomKommerForSentListe = refusjonskravSomKommerForSentListe;
    }

    public List<RefusjonAndelTilVurderingDto> getAndeler() {
        return andeler;
    }

    public List<RefusjonskravSomKommerForSentDto> getRefusjonskravSomKommerForSentListe() {
        return refusjonskravSomKommerForSentListe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (RefusjonTilVurderingDto) o;
        return Objects.equals(andeler, that.andeler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(andeler);
    }
}
