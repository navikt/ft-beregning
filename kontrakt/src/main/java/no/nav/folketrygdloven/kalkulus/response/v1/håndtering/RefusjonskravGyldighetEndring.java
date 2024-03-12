package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class RefusjonskravGyldighetEndring {

    @JsonProperty(value = "erGyldighetUtvidet")
    @NotNull
    @Valid
    private ToggleEndring erGyldighetUtvidet;


    @JsonProperty(value = "arbeidsgiver")
    @NotNull
    @Valid
    private Aktør arbeidsgiver;

    public RefusjonskravGyldighetEndring() {
        // For json
    }

    public RefusjonskravGyldighetEndring(@NotNull @Valid ToggleEndring erGyldighetUtvidet, @NotNull @Valid Aktør arbeidsgiver) {
        this.erGyldighetUtvidet = erGyldighetUtvidet;
        this.arbeidsgiver = arbeidsgiver;
    }

    public ToggleEndring getErGyldighetUtvidet() {
        return erGyldighetUtvidet;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }
}
