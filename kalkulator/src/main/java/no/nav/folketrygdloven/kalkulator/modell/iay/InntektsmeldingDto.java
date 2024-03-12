package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.JournalpostId;

public class InntektsmeldingDto {

    private List<NaturalYtelseDto> naturalYtelser = new ArrayList<>();
    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private LocalDate startDatoPermisjon;

    private Beløp inntektBeløp;
    private Beløp refusjonBeløpPerMnd;
    private LocalDate refusjonOpphører;
    private List<RefusjonDto> endringerRefusjon = new ArrayList<>();
    private JournalpostId journalpostId;

    InntektsmeldingDto() {
    }

    public InntektsmeldingDto(InntektsmeldingDto inntektsmelding) {
        this.arbeidsgiver = inntektsmelding.getArbeidsgiver();
        this.arbeidsforholdRef = inntektsmelding.arbeidsforholdRef;
        this.startDatoPermisjon = inntektsmelding.getStartDatoPermisjon().orElse(null);
        this.inntektBeløp = inntektsmelding.getInntektBeløp();
        this.refusjonBeløpPerMnd = inntektsmelding.getRefusjonBeløpPerMnd();
        this.refusjonOpphører = inntektsmelding.getRefusjonOpphører();
        this.naturalYtelser = inntektsmelding.getNaturalYtelser().stream().map(n -> {
            final NaturalYtelseDto naturalYtelse = new NaturalYtelseDto(n);
            return naturalYtelse;
        }).collect(Collectors.toList());
        this.endringerRefusjon = inntektsmelding.getEndringerRefusjon().stream().map(r -> {
            final RefusjonDto refusjon = new RefusjonDto(r);
            return refusjon;
        }).collect(Collectors.toList());
        this.journalpostId = inntektsmelding.getJournalpostId();
    }

    /**
     * Virksomheten som har sendt inn inntektsmeldingen
     *
     * @return {@link Arbeidsgiver}
     */
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    /**
     * Liste over naturalytelser
     *
     * @return {@link NaturalYtelseDto}
     */
    public List<NaturalYtelseDto> getNaturalYtelser() {
        return Collections.unmodifiableList(naturalYtelser);
    }

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    /**
     * Arbeidsgivers arbeidsforhold referanse
     *
     * @return {@link InternArbeidsforholdRefDto}
     */
    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        // Returnere NULL OBJECT slik at vi alltid har en ref (selv om den inneholder null).
        // gjør enkelte sammenligninger (eks. gjelderFor) enklere.
        return arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRefDto.nullRef();
    }

    /**
     * Gjelder for et spesifikt arbeidsforhold
     *
     * @return {@link Boolean}
     */
    public boolean gjelderForEtSpesifiktArbeidsforhold() {
        return getArbeidsforholdRef() != null && getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold();
    }

    public boolean gjelderSammeArbeidsforhold(InntektsmeldingDto annen) {
        return getArbeidsgiver().equals(annen.getArbeidsgiver())
            && (getArbeidsforholdRef() == null || annen.getArbeidsforholdRef() == null
                || (getArbeidsforholdRef() != null && getArbeidsforholdRef().gjelderFor(annen.getArbeidsforholdRef())));
    }

    /**
     * Setter intern arbeidsdforhold Id for inntektsmelding
     *
     * @param arbeidsforholdRef Intern arbeidsforhold id
     */
    void setArbeidsforholdId(InternArbeidsforholdRefDto arbeidsforholdRef) {
        this.arbeidsforholdRef = arbeidsforholdRef != null && !InternArbeidsforholdRefDto.nullRef().equals(arbeidsforholdRef) ? arbeidsforholdRef : null;
    }

    /**
     * Startdato for permisjonen
     *
     * @return {@link LocalDate}
     */
    public Optional<LocalDate> getStartDatoPermisjon() {
        return Optional.ofNullable(startDatoPermisjon);
    }

    void setStartDatoPermisjon(LocalDate startDatoPermisjon) {
        this.startDatoPermisjon = startDatoPermisjon;
    }

    /**
     * Oppgitt årsinntekt fra arbeidsgiver
     *
     * @return {@link Beløp}
     */
    public Beløp getInntektBeløp() {
        return inntektBeløp;
    }

    void setInntektBeløp(Beløp inntektBeløp) {
        this.inntektBeløp = inntektBeløp;
    }

    void setJournalpostId(JournalpostId journalpostId) {
        this.journalpostId = journalpostId;
    }

    /**
     * Beløpet arbeidsgiver ønsker refundert
     *
     * @return {@link Beløp}
     */
    public Beløp getRefusjonBeløpPerMnd() {
        return refusjonBeløpPerMnd;
    }

    void setRefusjonBeløpPerMnd(Beløp refusjonBeløpPerMnd) {
        this.refusjonBeløpPerMnd = refusjonBeløpPerMnd;
    }

    /**
     * Dersom refusjonen opphører i stønadsperioden angis siste dag det søkes om refusjon for.
     *
     * @return {@link LocalDate}
     */
    public LocalDate getRefusjonOpphører() {
        return refusjonOpphører;
    }

    void setRefusjonOpphører(LocalDate refusjonOpphører) {
        this.refusjonOpphører = refusjonOpphører;
    }

    /**
     * Liste over endringer i refusjonsbeløp
     *
     * @return {@Link Refusjon}
     */
    public List<RefusjonDto> getEndringerRefusjon() {
        return Collections.unmodifiableList(endringerRefusjon);
    }

    void leggTil(NaturalYtelseDto naturalYtelse) {
        this.naturalYtelser.add(naturalYtelse);
    }

    void leggTil(RefusjonDto refusjon) {
        this.endringerRefusjon.add(refusjon);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof InntektsmeldingDto)) {
            return false;
        }
        InntektsmeldingDto entitet = (InntektsmeldingDto) o;
        return Objects.equals(getArbeidsgiver(), entitet.getArbeidsgiver())
            && Objects.equals(getArbeidsforholdRef(), entitet.getArbeidsforholdRef());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArbeidsgiver(), getArbeidsforholdRef());
    }

    @Override
    public String toString() {
        return "InntektsmeldingDto{" +
            "naturalYtelser=" + naturalYtelser +
            ", arbeidsgiver=" + arbeidsgiver +
            ", arbeidsforholdRef=" + arbeidsforholdRef +
            ", startDatoPermisjon=" + startDatoPermisjon +
            ", inntektBeløp=" + inntektBeløp +
            ", refusjonBeløpPerMnd=" + refusjonBeløpPerMnd +
            ", refusjonOpphører=" + refusjonOpphører +
            ", endringerRefusjon=" + endringerRefusjon +
            '}';
    }

}
