package no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling;


import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FordelRedigerbarAndelDto {

    @JsonProperty("andelsnr")
    @Valid
    @Min(1)
    @Max(100)
    private Long andelsnr;

    @JsonProperty("arbeidsgiverId")
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsgiverId;

    @JsonProperty("arbeidsforholdId")
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsforholdId;

    @JsonProperty("nyAndel")
    @Valid
    @NotNull
    private Boolean nyAndel;

    @JsonProperty("kilde")
    @Valid
    private AndelKilde kilde;

    protected FordelRedigerbarAndelDto() { // NOSONAR
        // Jackson
    }

    public FordelRedigerbarAndelDto(Long andelsnr,
                                    String arbeidsgiverId,
                                    String arbeidsforholdId,
                                    Boolean nyAndel,
                                    AndelKilde kilde) {
        this.andelsnr = andelsnr;
        this.arbeidsgiverId = arbeidsgiverId;
        this.arbeidsforholdId = arbeidsforholdId;
        this.nyAndel = nyAndel;
        this.kilde = kilde;
    }


    public Long getAndelsnr() {
        return andelsnr;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdId() {
        return new InternArbeidsforholdRefDto(arbeidsforholdId);
    }

    public String getArbeidsgiverId() {
        return arbeidsgiverId;
    }

    public Boolean getNyAndel() {
        return nyAndel;
    }

    public AndelKilde getKilde() {
        return kilde;
    }

}
