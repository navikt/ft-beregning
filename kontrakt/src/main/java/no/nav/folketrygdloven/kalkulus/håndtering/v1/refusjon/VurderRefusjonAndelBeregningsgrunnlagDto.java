package no.nav.folketrygdloven.kalkulus.håndtering.v1.refusjon;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VurderRefusjonAndelBeregningsgrunnlagDto {

    @JsonProperty("arbeidsgiverOrgnr")
    @Valid
    @Pattern(regexp = "^\\d{9}$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsgiverOrgnr;

    @JsonProperty("arbeidsgiverAktørId")
    @Valid
    @Pattern(regexp = "^\\d{13}$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsgiverAktørId;

    @JsonProperty("internArbeidsforholdRef")
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String internArbeidsforholdRef;

    @JsonProperty("fullRefusjonFom")
    @Valid
    @NotNull
    private LocalDate fullRefusjonFom;

    @JsonProperty("delvisRefusjonBeløpPrMnd")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer delvisRefusjonBeløpPrMnd;

    public VurderRefusjonAndelBeregningsgrunnlagDto() {
        // For Json deserialisering
    }

    public VurderRefusjonAndelBeregningsgrunnlagDto(@Valid String arbeidsgiverOrgnr,
                                                    @Valid String arbeidsgiverAktørId,
                                                    @Valid String internArbeidsforholdRef,
                                                    @Valid @NotNull LocalDate fullRefusjonFom,
                                                    @Valid Integer delvisRefusjonBeløpPrMnd) {
        if (arbeidsgiverAktørId == null && arbeidsgiverOrgnr == null) {
            throw new IllegalStateException("Både orgnr og aktørId er null, udyldig tilstand");
        }
        if (arbeidsgiverAktørId != null && arbeidsgiverOrgnr != null) {
            throw new IllegalStateException("Både orgnr og aktørId er satt, udyldig tilstand");
        }
        this.arbeidsgiverOrgnr = arbeidsgiverOrgnr;
        this.arbeidsgiverAktørId = arbeidsgiverAktørId;
        this.internArbeidsforholdRef = internArbeidsforholdRef;
        this.fullRefusjonFom = fullRefusjonFom;
        this.delvisRefusjonBeløpPrMnd = delvisRefusjonBeløpPrMnd;
    }

    public String getArbeidsgiverOrgnr() {
        return arbeidsgiverOrgnr;
    }

    public String getArbeidsgiverAktørId() {
        return arbeidsgiverAktørId;
    }

    public String getInternArbeidsforholdRef() {
        return internArbeidsforholdRef;
    }

    public LocalDate getFullRefusjonFom() {
        return fullRefusjonFom;
    }

    public Integer getDelvisRefusjonBeløpPrMnd() {
        return delvisRefusjonBeløpPrMnd;
    }
}
