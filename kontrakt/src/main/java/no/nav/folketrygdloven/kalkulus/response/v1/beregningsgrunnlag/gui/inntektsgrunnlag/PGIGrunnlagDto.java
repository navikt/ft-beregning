package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.PGIType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class PGIGrunnlagDto {

    @Valid
    @NotNull
    @JsonProperty(value = "pgiType")
    private PGIType pgiType;

    @Valid
    @JsonProperty("beløp")
    private Beløp beløp;

    public PGIGrunnlagDto() {
    }

    public PGIGrunnlagDto(@Valid @NotNull PGIType pgiType,
                          @Valid Beløp beløp) {
        this.pgiType = pgiType;
        this.beløp = beløp;
    }

    public PGIType getPgiType() {
        return pgiType;
    }

    public Beløp getBeløp() {
        return beløp;
    }
}
