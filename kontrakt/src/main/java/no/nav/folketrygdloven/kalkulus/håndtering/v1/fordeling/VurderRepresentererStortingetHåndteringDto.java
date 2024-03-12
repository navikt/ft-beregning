package no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VurderRepresentererStortingetHåndteringDto extends HåndterBeregningDto {

    @JsonProperty("fom")
    @Valid
    private LocalDate fom;

    @JsonProperty("tom")
    @Valid
    private LocalDate tom;

    @Valid
    @JsonProperty(value = "representererStortinget")
    private boolean representererStortinget;

    public VurderRepresentererStortingetHåndteringDto() {
        super(AvklaringsbehovDefinisjon.VURDER_REPRESENTERER_STORTINGET);
    }

    public VurderRepresentererStortingetHåndteringDto(LocalDate fom, LocalDate tom, boolean representererStortinget) {
        super(AvklaringsbehovDefinisjon.VURDER_REPRESENTERER_STORTINGET);
        this.fom = fom;
        this.tom = tom;
        this.representererStortinget = representererStortinget;
    }


    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public boolean getRepresentererStortinget() {
        return representererStortinget;
    }

    public void setRepresentererStortinget(boolean representererStortinget) {
        this.representererStortinget = representererStortinget;
    }
}
