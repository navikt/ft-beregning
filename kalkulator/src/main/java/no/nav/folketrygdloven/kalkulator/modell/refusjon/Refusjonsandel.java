package no.nav.folketrygdloven.kalkulator.modell.refusjon;

import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.JournalpostId;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;

public record Refusjonsandel(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef, RefusjonKilde kilde,
                             JournalpostId journalpostId, List<Refusjonsperiode> refusjonsperioder) {

    public Refusjonsandel {
        arbeidsforholdRef = arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRefDto.nullRef();
        if (refusjonsperioder.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Refusjonsperioder kan ikke inneholde null.");
        }
        refusjonsperioder = List.copyOf(refusjonsperioder);
        if (kilde == RefusjonKilde.INNTEKTSMELDING && journalpostId == null) {
            throw new IllegalArgumentException("Journalpost er påkrevd for kilde INNTEKTSMELDING.");
        }
        if (kilde == RefusjonKilde.SAKSBEHANDLER && journalpostId != null) {
            throw new IllegalArgumentException("Journalpost er ikke tillatt for kilde SAKSBEHANDLER.");
        }
    }

    @Override
    public String toString() {
        return "RefusjonsandelDto[kilde=" + kilde + ", refusjonsperioder=" + refusjonsperioder + "]";
    }

}
