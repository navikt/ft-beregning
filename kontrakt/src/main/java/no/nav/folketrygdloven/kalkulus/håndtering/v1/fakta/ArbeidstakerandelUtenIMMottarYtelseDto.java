package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ArbeidstakerandelUtenIMMottarYtelseDto {

    @JsonProperty("andelsnr")
    @Valid
    @NotNull
    private long andelsnr;

    @JsonProperty("mottarYtelse")
    @Valid
    @NotNull
    private Boolean mottarYtelse;

    public ArbeidstakerandelUtenIMMottarYtelseDto() {
    }

    public ArbeidstakerandelUtenIMMottarYtelseDto(long andelsnr, Boolean mottarYtelse) {
        this.andelsnr = andelsnr;
        this.mottarYtelse = mottarYtelse;
    }

    public long getAndelsnr() {
        return andelsnr;
    }

    public Boolean getMottarYtelse() {
        return mottarYtelse;
    }
}
