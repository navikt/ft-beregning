package no.nav.folketrygdloven.kalkulus.response.v1.simulerTilkommetInntekt;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class SimulertTilkommetInntekt {

    @JsonProperty(value = "antallSakerMedAksjonspunkt")
    @Valid
    @Min(0)
    @Max(1000000)
    private long antallSakerMedAksjonspunkt;

    @JsonProperty(value = "saksnummerMedAksjonspunkt")
    @Valid
    @Size(min = 0, max = 10000)
    private Set<String> saksnummerMedAksjonspunkt;

    @JsonProperty(value = "antallSakerMedManuellFordelingOgTilkommetInntekt")
    @Valid
    @Min(0)
    @Max(1000000)
    private long antallSakerMedManuellFordelingOgTilkommetInntekt;

    @JsonProperty(value = "antallSakerMedReduksjon")
    @Valid
    @Min(0)
    @Max(1000000)
    private long antallSakerMedReduksjon;


    @JsonProperty(value = "antallSakerSimulert")
    @Valid
    @Min(0)
    @Max(1000000)
    private long antallSakerSimulert;

    @JsonProperty(value = "antallSakerPrTilkommetStatus")
    @Valid
    private Map<String, Integer> antallSakerPrTilkommetStatus;


    public SimulertTilkommetInntekt(long antallSakerMedAksjonspunkt,
                                    Set<String> saksnummerMedAksjonspunkt,
                                    long antallSakerMedManuellFordelingOgTilkommetInntekt,
                                    long antallSakerMedReduksjon,
                                    long antallSakerSimulert,
                                    Map<String, Integer> antallSakerPrTilkommetStatus) {
        this.antallSakerMedAksjonspunkt = antallSakerMedAksjonspunkt;
        this.saksnummerMedAksjonspunkt = saksnummerMedAksjonspunkt;
        this.antallSakerMedReduksjon = antallSakerMedReduksjon;
        this.antallSakerSimulert = antallSakerSimulert;
        this.antallSakerMedManuellFordelingOgTilkommetInntekt = antallSakerMedManuellFordelingOgTilkommetInntekt;
        this.antallSakerPrTilkommetStatus = antallSakerPrTilkommetStatus;
    }

    public SimulertTilkommetInntekt() {
    }

    public long getAntallSakerMedAksjonspunkt() {
        return antallSakerMedAksjonspunkt;
    }

    public long getAntallSakerSimulert() {
        return antallSakerSimulert;
    }

    public long getAntallSakerMedReduksjon() {
        return antallSakerMedReduksjon;
    }

    public long getAntallSakerMedManuellFordelingOgTilkommetInntekt() {
        return antallSakerMedManuellFordelingOgTilkommetInntekt;
    }

    public Map<String, Integer> getAntallSakerPrTilkommetStatus() {
        return antallSakerPrTilkommetStatus;
    }

    public Set<String> getSaksnummerMedAksjonspunkt() {
        return saksnummerMedAksjonspunkt;
    }
}
