package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDateTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record AvklaringsbehovDto(@JsonProperty(value = "definisjon") @NotNull @Valid AvklaringsbehovDefinisjon definisjon,
                                 @JsonProperty(value = "status") @NotNull @Valid AvklaringsbehovStatus status,
                                 @JsonProperty(value = "kanLoses") @NotNull @Valid boolean kanLoses,
                                 @JsonProperty(value = "erTrukket") @NotNull @Valid boolean erTrukket,
                                 @JsonProperty(value = "begrunnelse")
                                 @Size(max = 5000)
                                 @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}§]+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
                                 @Valid String begrunnelse,
                                 @JsonProperty(value = "vurdertAv")
                                 @Size(max = 20)
                                 @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}§]+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
                                 @Valid String vurdertAv,
                                 @JsonProperty(value = "vurdertTidspunkt")
                                 @Valid LocalDateTime vurdertTidspunkt) {

}
