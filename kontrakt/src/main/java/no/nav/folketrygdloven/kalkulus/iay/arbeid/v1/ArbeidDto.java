package no.nav.folketrygdloven.kalkulus.iay.arbeid.v1;

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
@JsonInclude(value = Include.NON_ABSENT, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ArbeidDto {

    @JsonProperty("yrkesaktiviteter")
    @Size()
    @Valid
    private List<YrkesaktivitetDto> yrkesaktiviteter;

    protected ArbeidDto() {
        // default ctor
    }

    public ArbeidDto(@NotNull @Valid List<YrkesaktivitetDto> yrkesaktiviteter) {
        this.yrkesaktiviteter = yrkesaktiviteter;
    }

    public List<YrkesaktivitetDto> getYrkesaktiviteter() {
        return yrkesaktiviteter;
    }
}
