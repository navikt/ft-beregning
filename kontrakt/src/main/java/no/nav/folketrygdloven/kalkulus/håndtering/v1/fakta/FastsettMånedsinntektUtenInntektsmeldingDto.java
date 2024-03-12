package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FastsettMånedsinntektUtenInntektsmeldingDto {

    @JsonProperty("andelListe")
    @Valid
    @Size(min = 1)
    private List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe;

    public FastsettMånedsinntektUtenInntektsmeldingDto() {
    }

    public FastsettMånedsinntektUtenInntektsmeldingDto(List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe) {
        this.andelListe = andelListe;
    }

    public List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> getAndelListe() {
        return andelListe;
    }

    public void setAndelListe(List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe) {
        this.andelListe = andelListe;
    }
}
