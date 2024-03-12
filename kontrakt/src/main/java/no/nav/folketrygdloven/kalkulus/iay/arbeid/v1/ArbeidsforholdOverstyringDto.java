package no.nav.folketrygdloven.kalkulus.iay.arbeid.v1;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ArbeidsforholdOverstyringDto {

    @JsonProperty("arbeidsgiver")
    @Valid
    @NotNull
    private Aktør arbeidsgiver;

    @JsonProperty("arbeidsforholdRefDto")
    @Valid
    private InternArbeidsforholdRefDto arbeidsforholdRefDto;

    @JsonProperty("handling")
    @Valid
    private ArbeidsforholdHandlingType handling;


    public ArbeidsforholdOverstyringDto() {
        // default ctor
    }

    public ArbeidsforholdOverstyringDto(@Valid @NotNull Aktør arbeidsgiver, @Valid InternArbeidsforholdRefDto arbeidsforholdRefDto, @Valid ArbeidsforholdHandlingType handling) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRefDto = arbeidsforholdRefDto;
        this.handling = handling;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRefDto() {
        return arbeidsforholdRefDto;
    }

    public ArbeidsforholdHandlingType getHandling() {
        return handling;
    }
}
