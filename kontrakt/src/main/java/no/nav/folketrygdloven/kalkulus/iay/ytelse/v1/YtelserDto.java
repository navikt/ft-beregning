package no.nav.folketrygdloven.kalkulus.iay.ytelse.v1;

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

import no.nav.folketrygdloven.kalkulus.beregning.v1.besteberegning.Ytelsegrunnlag;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class YtelserDto {

    @JsonProperty("ytelser")
    @Size(min = 1)
    @Valid
    private List<YtelseDto> ytelser;

    protected YtelserDto() {
        // default ctor
    }

    public YtelserDto(@NotNull @Valid List<YtelseDto> ytelser) {
        this.ytelser = ytelser;
    }

    public List<YtelseDto> getYtelser() {
        return ytelser;
    }
}
