package no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VurderTilkommetInntektHåndteringDto extends HåndterBeregningDto {

    @JsonProperty("tilkomneInntektsforhold")
    @Valid
    @Size(min = 1, max = 500)
    private List<VurderTilkomneInntektsforholdPeriodeDto> tilkomneInntektsforholdPerioder;

    public VurderTilkommetInntektHåndteringDto() {
        super(AvklaringsbehovDefinisjon.VURDER_NYTT_INNTKTSFRHLD);
    }

    public VurderTilkommetInntektHåndteringDto(@Valid @NotNull @Size() List<VurderTilkomneInntektsforholdPeriodeDto> tilkomneInntektsforholdPerioder) {
        super(AvklaringsbehovDefinisjon.VURDER_NYTT_INNTKTSFRHLD);
        this.tilkomneInntektsforholdPerioder = tilkomneInntektsforholdPerioder;
    }

    public List<VurderTilkomneInntektsforholdPeriodeDto> getTilkomneInntektsforholdPerioder() {
        return tilkomneInntektsforholdPerioder;
    }

}
