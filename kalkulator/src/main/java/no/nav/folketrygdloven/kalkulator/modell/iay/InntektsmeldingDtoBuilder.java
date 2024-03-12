package no.nav.folketrygdloven.kalkulator.modell.iay;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.JournalpostId;

public class InntektsmeldingDtoBuilder {
    private final InntektsmeldingDto kladd;
    private EksternArbeidsforholdRef eksternArbeidsforholdId;
    private boolean erBygget;

    InntektsmeldingDtoBuilder(InntektsmeldingDto kladd) {
        this.kladd = kladd;
    }

    public static InntektsmeldingDtoBuilder builder() {
        return new InntektsmeldingDtoBuilder(new InntektsmeldingDto());
    }

    public InntektsmeldingDto build() {
        return build(false);
    }

    public InntektsmeldingDto build(boolean ignore) {
        var internRef = getInternArbeidsforholdRef();
        if (internRef.isPresent() && !ignore) {
            // magic - hvis har ekstern referanse må også intern referanse være spesifikk
            if ((eksternArbeidsforholdId != null && eksternArbeidsforholdId.gjelderForSpesifiktArbeidsforhold()) && internRef.get().getReferanse() == null) {
                throw new IllegalArgumentException(
                        "Begge referanser må gjelde spesifikke arbeidsforhold. " + " Ekstern: " + eksternArbeidsforholdId + ", Intern: " + internRef);
            }
        }
        erBygget = true; // Kan ikke bygge mer med samme builder, vil bare returnere samme kladd.
        return kladd;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return kladd.getArbeidsgiver();
    }

    public Optional<InternArbeidsforholdRefDto> getInternArbeidsforholdRef() {
        return Optional.ofNullable(kladd.getArbeidsforholdRef());
    }

    public InntektsmeldingDtoBuilder leggTil(NaturalYtelseDto naturalYtelse) {
        precondition();
        kladd.leggTil(naturalYtelse);
        return this;
    }

    public InntektsmeldingDtoBuilder leggTil(RefusjonDto refusjon) {
        precondition();
        kladd.leggTil(refusjon);
        return this;
    }

    public InntektsmeldingDtoBuilder medArbeidsforholdId(EksternArbeidsforholdRef arbeidsforholdId) {
        precondition();
        this.eksternArbeidsforholdId = arbeidsforholdId;
        return this;
    }

    public InntektsmeldingDtoBuilder medArbeidsforholdId(InternArbeidsforholdRefDto arbeidsforholdId) {
        precondition();
        if (arbeidsforholdId != null) {
            // magic - hvis har ekstern referanse må også intern referanse være spesifikk
            if (arbeidsforholdId.getReferanse() == null && eksternArbeidsforholdId != null && eksternArbeidsforholdId.gjelderForSpesifiktArbeidsforhold()) {
                throw new IllegalArgumentException(
                        "Begge referanser gjelde spesifikke arbeidsforhold. " + " Ekstern: " + eksternArbeidsforholdId + ", Intern: " + arbeidsforholdId);
            }
            kladd.setArbeidsforholdId(arbeidsforholdId);
        }
        return this;
    }

    public InntektsmeldingDtoBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        precondition();
        kladd.setArbeidsgiver(arbeidsgiver);
        return this;
    }

    public InntektsmeldingDtoBuilder medBeløp(Beløp verdi) {
        precondition();
        kladd.setInntektBeløp(verdi);
        return this;
    }

    public InntektsmeldingDtoBuilder medRefusjon(Beløp verdi) {
        precondition();
        kladd.setRefusjonBeløpPerMnd(verdi);
        kladd.setRefusjonOpphører(TIDENES_ENDE);
        return this;
    }

    public InntektsmeldingDtoBuilder medJournalpostId(JournalpostId journalpostId) {
        precondition();
        kladd.setJournalpostId(journalpostId);
        return this;
    }

    public InntektsmeldingDtoBuilder medRefusjon(Beløp verdi, LocalDate opphører) {
        precondition();
        kladd.setRefusjonBeløpPerMnd(verdi);
        kladd.setRefusjonOpphører(opphører);
        return this;
    }

    public InntektsmeldingDtoBuilder medStartDatoPermisjon(LocalDate startPermisjon) {
        precondition();
        kladd.setStartDatoPermisjon(startPermisjon);
        return this;
    }

    private void precondition() {
        if (erBygget) {
            throw new IllegalStateException("Inntektsmelding objekt er allerede bygget, kan ikke modifisere nå. Returnerer kun : " + kladd);
        }
    }

}
