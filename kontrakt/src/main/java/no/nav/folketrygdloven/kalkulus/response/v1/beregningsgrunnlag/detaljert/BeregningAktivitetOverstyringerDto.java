package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningAktivitetOverstyringerDto {

    @JsonProperty(value = "overstyringer")
    @Size
    @Valid
    private List<BeregningAktivitetOverstyringDto> overstyringer;

    public BeregningAktivitetOverstyringerDto() {
    }

    public BeregningAktivitetOverstyringerDto(@NotNull @Valid List<BeregningAktivitetOverstyringDto> overstyringer) {
        this.overstyringer = overstyringer;
    }

    public List<BeregningAktivitetOverstyringDto> getOverstyringer() {
        return overstyringer;
    }
}
