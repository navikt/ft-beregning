package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FaktaOmBeregningTilfelleDto {

    @JsonProperty(value = "tilfeller")
    @Valid
    @Size(min = 1)
    private List<FaktaOmBeregningTilfelle> tilfeller;

    public FaktaOmBeregningTilfelleDto(@Valid @NotEmpty List<FaktaOmBeregningTilfelle> tilfeller) {
        this.tilfeller = tilfeller;
    }

    public FaktaOmBeregningTilfelleDto() {
    }

    public List<FaktaOmBeregningTilfelle> getTilfeller() {
        return tilfeller;
    }
}
