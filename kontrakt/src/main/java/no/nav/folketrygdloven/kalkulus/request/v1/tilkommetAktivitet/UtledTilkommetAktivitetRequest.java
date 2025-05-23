package no.nav.folketrygdloven.kalkulus.request.v1.tilkommetAktivitet;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
public class UtledTilkommetAktivitetRequest {

    @JsonProperty(value = "periode")
    @Valid
    private Periode periode;

    @JsonProperty(value = "dagsatsFeiltoleranse")
    @Valid
    private Long dagsatsFeiltoleranse;


    public UtledTilkommetAktivitetRequest() {
    }

    @JsonCreator
    public UtledTilkommetAktivitetRequest(@JsonProperty(value = "periode") Periode periode, Long dagsatsFeiltoleranse) {
        this.periode = periode;
        this.dagsatsFeiltoleranse = dagsatsFeiltoleranse;
    }


    public Long getDagsatsFeiltoleranse() {
        return dagsatsFeiltoleranse;
    }

    public Periode getPeriode() {
        return periode;
    }
}
