package no.nav.folketrygdloven.kalkulus.iay.inntekt.v1;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.JournalpostId;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class InntektsmeldingDto {

    @JsonProperty(value = "arbeidsgiver")
    @Valid
    @NotNull
    private Aktør arbeidsgiver;

    @JsonProperty(value = "inntektBeløp")
    @Valid
    @NotNull
    private Beløp inntektBeløp;

    @JsonProperty(value = "naturalYtelser")
    @Valid
    @Size
    private List<NaturalYtelseDto> naturalYtelser;

    @JsonProperty(value = "endringerRefusjon")
    @Valid
    @Size
    private List<RefusjonDto> endringerRefusjon;

    @JsonProperty(value = "arbeidsforholdRef")
    @Valid
    private InternArbeidsforholdRefDto arbeidsforholdRef;

    @JsonProperty(value = "startDatoPermisjon")
    @Valid
    private LocalDate startDatoPermisjon;

    @JsonProperty(value = "refusjonOpphører")
    @Valid
    private LocalDate refusjonOpphører;

    @JsonProperty(value = "refusjonBeløpPerMnd")
    @Valid
    private Beløp refusjonBeløpPerMnd;

    /** JournalpostId - for sporing. */
    @JsonProperty(value = "journalpostId")
    @Valid
    private JournalpostId journalpostId;

    protected InntektsmeldingDto() {
        // default ctor
    }

    public InntektsmeldingDto(@Valid @NotNull Aktør arbeidsgiver,
                              @Valid @NotNull Beløp inntektBeløp,
                              @Valid List<NaturalYtelseDto> naturalYtelser,
                              @Valid List<RefusjonDto> endringerRefusjon,
                              @Valid InternArbeidsforholdRefDto arbeidsforholdRef,
                              @Valid LocalDate startDatoPermisjon,
                              @Valid LocalDate refusjonOpphører,
                              @Valid Beløp refusjonBeløpPerMnd,
                              @Valid JournalpostId journalpostId
                              ) {
        this.arbeidsgiver = arbeidsgiver;
        this.inntektBeløp = inntektBeløp;
        this.naturalYtelser = naturalYtelser;
        this.endringerRefusjon = endringerRefusjon;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.startDatoPermisjon = startDatoPermisjon;
        this.refusjonOpphører = refusjonOpphører;
        this.refusjonBeløpPerMnd = refusjonBeløpPerMnd;
        this.journalpostId = journalpostId;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public Beløp getInntektBeløp() {
        return inntektBeløp;
    }

    public List<NaturalYtelseDto> getNaturalYtelser() {
        return naturalYtelser;
    }

    public List<RefusjonDto> getEndringerRefusjon() {
        return endringerRefusjon;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public LocalDate getStartDatoPermisjon() {
        return startDatoPermisjon;
    }

    public LocalDate getRefusjonOpphører() {
        return refusjonOpphører;
    }

    public Beløp getRefusjonBeløpPerMnd() {
        return refusjonBeløpPerMnd;
    }

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

}
