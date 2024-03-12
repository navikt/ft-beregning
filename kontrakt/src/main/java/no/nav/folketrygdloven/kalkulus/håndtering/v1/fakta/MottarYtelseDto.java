package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

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


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class MottarYtelseDto {

    @JsonProperty("frilansMottarYtelse")
    @Valid
    private Boolean frilansMottarYtelse;

    @JsonProperty("arbeidstakerUtenIMMottarYtelse")
    @Valid
    @NotNull
    @Size()
    private List<ArbeidstakerandelUtenIMMottarYtelseDto> arbeidstakerUtenIMMottarYtelse;

    public MottarYtelseDto() {
    }

    public MottarYtelseDto(@Valid Boolean frilansMottarYtelse, @Valid @NotNull List<ArbeidstakerandelUtenIMMottarYtelseDto> arbeidstakerUtenIMMottarYtelse) {
        this.frilansMottarYtelse = frilansMottarYtelse;
        this.arbeidstakerUtenIMMottarYtelse = arbeidstakerUtenIMMottarYtelse;
    }

    public Boolean getFrilansMottarYtelse() {
        return frilansMottarYtelse;
    }

    public List<ArbeidstakerandelUtenIMMottarYtelseDto> getArbeidstakerUtenIMMottarYtelse() {
        return arbeidstakerUtenIMMottarYtelse;
    }
}
