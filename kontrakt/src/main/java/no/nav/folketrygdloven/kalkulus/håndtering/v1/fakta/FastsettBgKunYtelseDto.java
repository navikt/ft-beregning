package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.ArrayList;
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
public class FastsettBgKunYtelseDto {

    @JsonProperty("andeler")
    @Valid
    @Size(min = 1)
    private List<FastsattBrukersAndel> andeler;

    @JsonProperty("kunYtelseFordeling")
    @Valid
    private Boolean skalBrukeBesteberegning;

    public FastsettBgKunYtelseDto() {
    }

    public FastsettBgKunYtelseDto(List<FastsattBrukersAndel> andeler, Boolean skalBrukeBesteberegning) { // NOSONAR
        this.andeler = new ArrayList<>(andeler);
        this.skalBrukeBesteberegning = skalBrukeBesteberegning;
    }

    public List<FastsattBrukersAndel> getAndeler() {
        return andeler;
    }

    public Boolean getSkalBrukeBesteberegning() {
        return skalBrukeBesteberegning;
    }
}
