package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
public class BeregnForRequest {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;


    @JsonProperty(value = "originalEksternReferanser", required = true)
    @Valid
    private List<UUID> originalEksternReferanser;

    @JsonProperty(value = "kalkulatorInput")
    @Valid
    private KalkulatorInputDto kalkulatorInput;


    @JsonProperty(value = "forlengelsePerioder")
    @Size()
    @Valid
    private List<Periode> forlengelsePerioder;

    @JsonCreator
    public BeregnForRequest(@JsonProperty(value = "eksternReferanse", required = true) UUID eksternReferanse,
                            @JsonProperty(value = "originalEksternReferanser") List<UUID> originalEksternReferanser,
                            @JsonProperty(value = "kalkulatorInput") KalkulatorInputDto kalkulatorInput,
                            @JsonProperty(value = "forlengelsePerioder") List<Periode>  forlengelsePerioder) {
        this.eksternReferanse = eksternReferanse;
        this.originalEksternReferanser = originalEksternReferanser;
        this.kalkulatorInput = kalkulatorInput;
        this.forlengelsePerioder = forlengelsePerioder;
    }

    public UUID getEksternReferanse() {
        return eksternReferanse;
    }

    public List<UUID> getOriginalEksternReferanser() {
        return originalEksternReferanser;
    }

    public KalkulatorInputDto getKalkulatorInput() {
        return kalkulatorInput;
    }

    public List<Periode> getForlengelsePerioder() {
        return forlengelsePerioder;
    }

    @AssertTrue(message = "Kan ikke ha originalreferanse lik referanse som beregnes")
    public boolean isSkalVereUnikeReferanser() {
        if (originalEksternReferanser != null) {
            return originalEksternReferanser.stream().noneMatch(r -> r.equals(eksternReferanse));
        }
        return true;
    }

}
