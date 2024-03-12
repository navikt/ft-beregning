package no.nav.folketrygdloven.kalkulus.iay.arbeid.v1;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.IayProsent;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class PermisjonDto {

    @JsonProperty("periode")
    @Valid
    @NotNull
    private Periode periode;

    /**
     * Prosent sats med to desimaler - min 0.00 - 100.00.
     * Pga inntastingfeil og manglende validering i LPS systemer og Altinn har man historisk akseptert mottatt permisjonsprosenter langt over
     * 100%. C'est la vie.
     */
    @JsonProperty("prosentsats")
    @Valid
    private IayProsent prosentsats;

    @Valid
    @JsonProperty(value = "permisjonsbeskrivelseType")
    private PermisjonsbeskrivelseType permisjonsbeskrivelseType;

    PermisjonDto(){
        // Skjul default constructor
    }

    public PermisjonDto(@Valid @NotNull Periode periode,
                        @Valid IayProsent prosentsats,
                        @Valid PermisjonsbeskrivelseType permisjonsbeskrivelseType) {
        this.periode = periode;
        this.prosentsats = prosentsats;
        this.permisjonsbeskrivelseType = permisjonsbeskrivelseType;
    }

    public Periode getPeriode() {
        return periode;
    }

    public IayProsent getProsentsats() {
        return prosentsats;
    }

    public PermisjonsbeskrivelseType getPermisjonsbeskrivelseType() {
        return permisjonsbeskrivelseType;
    }

}
