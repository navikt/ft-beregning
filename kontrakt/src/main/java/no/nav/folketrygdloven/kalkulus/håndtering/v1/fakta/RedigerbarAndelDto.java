package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;


import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class RedigerbarAndelDto {

    @JsonProperty("andelsnr")
    @Valid
    @Min(1)
    @Max(100)
    private Long andelsnr;

    @JsonProperty("lagtTilAvSaksbehandler")
    @Valid
    private Boolean lagtTilAvSaksbehandler;

    protected RedigerbarAndelDto() { // NOSONAR
        // Jackson
    }

    public RedigerbarAndelDto(@Valid @Min(0) @Max(100) Long andelsnr, @Valid Boolean lagtTilAvSaksbehandler) {
        this.andelsnr = andelsnr;
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

}
