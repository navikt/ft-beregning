package no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter;

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
public class AvklarteAktiviteterDto {

    @JsonProperty("beregningsaktivitetLagreDtoList")
    @Valid
    @Size(min = 1)
    private List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList;

    public AvklarteAktiviteterDto() {
    }

    public AvklarteAktiviteterDto(@Valid @Size(min = 1) List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList) {
        this.beregningsaktivitetLagreDtoList = beregningsaktivitetLagreDtoList;
    }

    public List<BeregningsaktivitetLagreDto> getBeregningsaktivitetLagreDtoList() {
        return beregningsaktivitetLagreDtoList;
    }
}
