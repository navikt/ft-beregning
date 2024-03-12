package no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VurderTilkomneInntektsforholdPeriodeDto {

    @JsonProperty("tilkomneInntektsforhold")
    @Valid
    @Size(min = 1, max = 100)
    private List<NyttInntektsforholdDto> tilkomneInntektsforhold;

    @JsonProperty("fom")
    @Valid
    @NotNull
    private LocalDate fom;

    @JsonProperty("tom")
    @Valid
    @NotNull
    private LocalDate tom;

    public VurderTilkomneInntektsforholdPeriodeDto() {
        // For Json deserialisering
    }

    public VurderTilkomneInntektsforholdPeriodeDto(List<NyttInntektsforholdDto> tilkomneInntektsforhold, LocalDate fom, LocalDate tom) { // NOSONAR
        this.tilkomneInntektsforhold = tilkomneInntektsforhold;
        this.fom = fom;
        this.tom = tom;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public List<NyttInntektsforholdDto> getTilkomneInntektsforhold() {
        return tilkomneInntektsforhold;
    }
}
