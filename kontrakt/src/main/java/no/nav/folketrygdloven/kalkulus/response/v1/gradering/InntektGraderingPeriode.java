package no.nav.folketrygdloven.kalkulus.response.v1.gradering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class InntektGraderingPeriode {

    @Valid
    @JsonProperty("periode")
    @NotNull
    private Periode periode;

    /**
     * Inntektgradering mellom 0 og 1
     */
    @Valid
    @JsonProperty(value = "inntektgradering")
    @DecimalMin("0.00")
    @DecimalMax("1.00")
    @NotNull
    private BigDecimal inntektgradering;

    @JsonCreator
    public InntektGraderingPeriode(
            @Valid
            @JsonProperty("periode")
            Periode periode,
            @JsonProperty(value = "inntektgradering")
            @Valid
            BigDecimal inntektgradering) {
        this.periode = periode;
        this.inntektgradering = inntektgradering;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getInntektgradering() {
        return inntektgradering;
    }
}
