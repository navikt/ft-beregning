package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.time.LocalDate;
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
public class FastsattePerioderTidsbegrensetDto {

    @JsonProperty("periodeFom")
    @Valid
    @NotNull
    private LocalDate periodeFom;

    @JsonProperty("periodeTom")
    @Valid
    @NotNull
    private LocalDate periodeTom;

    @JsonProperty("fastsatteTidsbegrensedeAndeler")
    @Valid
    @Size(min = 1)
    private List<FastsatteAndelerTidsbegrensetDto> fastsatteTidsbegrensedeAndeler;

    public FastsattePerioderTidsbegrensetDto() {
        // def ctc
    }

    public FastsattePerioderTidsbegrensetDto(@Valid @NotNull LocalDate periodeFom, @Valid @NotNull LocalDate periodeTom, @Valid @NotNull List<FastsatteAndelerTidsbegrensetDto> fastsatteTidsbegrensedeAndeler) {
        this.periodeFom = periodeFom;
        this.periodeTom = periodeTom;
        this.fastsatteTidsbegrensedeAndeler = fastsatteTidsbegrensedeAndeler;
    }

    public LocalDate getPeriodeFom() {
        return periodeFom;
    }

    public LocalDate getPeriodeTom() {
        return periodeTom;
    }

    public List<FastsatteAndelerTidsbegrensetDto> getFastsatteTidsbegrensedeAndeler() {
        return fastsatteTidsbegrensedeAndeler;
    }
}
