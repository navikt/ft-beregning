package no.nav.folketrygdloven.kalkulus.iay.arbeid.v1;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = ALWAYS, content = ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ArbeidsforholdReferanseDto {

    @JsonProperty("arbeidsgiver")
    @Valid
    @NotNull
    private Aktør arbeidsgiver;

    @JsonProperty("internReferanse")
    @Valid
    private InternArbeidsforholdRefDto internReferanse;

    @JsonProperty("eksternReferanse")
    @Valid
    private EksternArbeidsforholdRef eksternReferanse;

    public ArbeidsforholdReferanseDto() {
        // For Json serialisering
    }

    public ArbeidsforholdReferanseDto(@Valid @NotNull Aktør arbeidsgiver, @Valid InternArbeidsforholdRefDto internReferanse, @Valid EksternArbeidsforholdRef eksternReferanse) {
        this.arbeidsgiver = arbeidsgiver;
        this.internReferanse = internReferanse;
        this.eksternReferanse = eksternReferanse;
    }

    public InternArbeidsforholdRefDto getInternReferanse() {
        return internReferanse;
    }

    public EksternArbeidsforholdRef getEksternReferanse() {
        return eksternReferanse;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }
}
