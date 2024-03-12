package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fakta;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class ArbeidsforholdDto {

    @Valid
    @JsonProperty("andelsnr")
    @Min(0)
    @Max(1000)
    private Long andelsnr;

    @Valid
    @JsonProperty(value = "arbeidsgiverIdent")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsgiverIdent;

    @Valid
    @JsonProperty(value = "arbeidsforholdId")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsforholdId;


    public ArbeidsforholdDto() {
        // Hibernate
    }

    public ArbeidsforholdDto(Long andelsnr, String arbeidsgiverIdent, String arbeidsforholdId) {
        this.andelsnr = andelsnr;
        this.arbeidsgiverIdent = arbeidsgiverIdent;
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public void setArbeidsforholdId(String arbeidsforholdId) {
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public String getArbeidsgiverIdent() {
        return arbeidsgiverIdent;
    }

    public void setArbeidsgiverIdent(String arbeidsgiverIdent) {
        this.arbeidsgiverIdent = arbeidsgiverIdent;
    }


    public Long getAndelsnr() {
        return andelsnr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArbeidsforholdDto that = (ArbeidsforholdDto) o;
        return Objects.equals(arbeidsforholdId, that.arbeidsforholdId) &&
                Objects.equals(arbeidsgiverIdent, that.arbeidsgiverIdent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiverIdent, arbeidsforholdId);
    }


}
