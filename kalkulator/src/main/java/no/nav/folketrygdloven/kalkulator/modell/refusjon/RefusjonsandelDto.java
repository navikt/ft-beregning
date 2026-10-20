package no.nav.folketrygdloven.kalkulator.modell.refusjon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.JournalpostId;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;

public record RefusjonsandelDto(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef, RefusjonKilde kilde,
        JournalpostId journalpostId, List<RefusjonsperiodeDto> refusjonsperioder) {

    public RefusjonsandelDto {
        arbeidsforholdRef = arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRefDto.nullRef();
        refusjonsperioder = Collections.unmodifiableList(new ArrayList<>(refusjonsperioder));
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
