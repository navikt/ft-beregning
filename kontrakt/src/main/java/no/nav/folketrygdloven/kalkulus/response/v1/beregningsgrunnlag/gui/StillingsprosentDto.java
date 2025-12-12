package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record StillingsprosentDto(
    @JsonProperty(value = "prosent") @NotNull @Valid @DecimalMin(value = "0.00") @DecimalMax(value = "100.00") @Digits(integer = 3, fraction = 2) BigDecimal prosent,
    @JsonProperty(value = "fomDato") @NotNull @Valid LocalDate fomDato, @JsonProperty(value = "tomDato") @NotNull @Valid LocalDate tomDato) {
}
