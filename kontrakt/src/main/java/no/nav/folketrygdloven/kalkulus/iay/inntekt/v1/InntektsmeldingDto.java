package no.nav.folketrygdloven.kalkulus.iay.inntekt.v1;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    @Size
    private List<@Valid NaturalYtelseDto> naturalYtelser;

    @JsonProperty(value = "endringerRefusjon")
    @Size
    private List<@Valid RefusjonDto> endringerRefusjon;

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

    @JsonProperty(value = "innsendingsdato")
    @Valid
    private LocalDate innsendingsdato;

    @JsonProperty(value = "type")
    @Valid
    private InntektsmeldingType type;

    protected InntektsmeldingDto() {
        // default ctor
    }

    public InntektsmeldingDto(@Valid @NotNull Aktør arbeidsgiver,
                              @Valid @NotNull Beløp inntektBeløp,
                              List<@Valid NaturalYtelseDto> naturalYtelser,
                              List<@Valid RefusjonDto> endringerRefusjon,
                              @Valid InternArbeidsforholdRefDto arbeidsforholdRef,
                              @Valid LocalDate startDatoPermisjon,
                              @Valid LocalDate refusjonOpphører,
                              @Valid Beløp refusjonBeløpPerMnd,
                              @Valid JournalpostId journalpostId,
                              @Valid InntektsmeldingType type) {
        this.arbeidsgiver = arbeidsgiver;
        this.inntektBeløp = inntektBeløp;
        this.naturalYtelser = naturalYtelser;
        this.endringerRefusjon = endringerRefusjon;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.startDatoPermisjon = startDatoPermisjon;
        this.refusjonOpphører = refusjonOpphører;
        this.refusjonBeløpPerMnd = refusjonBeløpPerMnd;
        this.journalpostId = journalpostId;
        this.type = type;
    }

    public InntektsmeldingDto(Aktør arbeidsgiver,
                              Beløp inntektBeløp,
                              List<NaturalYtelseDto> naturalYtelser,
                              List<RefusjonDto> endringerRefusjon,
                              InternArbeidsforholdRefDto arbeidsforholdRef,
                              LocalDate startDatoPermisjon,
                              LocalDate refusjonOpphører,
                              Beløp refusjonBeløpPerMnd,
                              JournalpostId journalpostId,
                              LocalDate innsendingsdato) {
        this.arbeidsgiver = arbeidsgiver;
        this.inntektBeløp = inntektBeløp;
        this.naturalYtelser = naturalYtelser;
        this.endringerRefusjon = endringerRefusjon;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.startDatoPermisjon = startDatoPermisjon;
        this.refusjonOpphører = refusjonOpphører;
        this.refusjonBeløpPerMnd = refusjonBeløpPerMnd;
        this.journalpostId = journalpostId;
        this.innsendingsdato = innsendingsdato;
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

    public LocalDate getInnsendingsdato() {
        return innsendingsdato;
    }

    public InntektsmeldingType getType() {
        return type;
    }
}
