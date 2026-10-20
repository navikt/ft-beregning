package no.nav.folketrygdloven.kalkulator.modell.refusjon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;

public record FordelingGrunnlagDto(List<RefusjonsandelDto> refusjonsandeler) {

    public FordelingGrunnlagDto {
        refusjonsandeler = Collections.unmodifiableList(new ArrayList<>(refusjonsandeler));
        validerDomeneregler(refusjonsandeler);
    }

    @Override
    public String toString() {
        return "FordelingGrunnlagDto[refusjonsandeler=" + refusjonsandeler + "]";
    }

    private static void validerDomeneregler(List<RefusjonsandelDto> refusjonsandeler) {
        var nøkler = new HashSet<RefusjonsandelNøkkel>();
        for (var andel : refusjonsandeler) {
            var nøkkel = new RefusjonsandelNøkkel(andel.arbeidsgiver(), andel.arbeidsforholdRef(), andel.kilde());
            if (!nøkler.add(nøkkel)) {
                throw new IllegalArgumentException("Flere refusjonsandeler med samme arbeidsgiver, arbeidsforholdsreferanse og kilde.");
            }
        }
    }

    private record RefusjonsandelNøkkel(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef, RefusjonKilde kilde) {
    }
}
