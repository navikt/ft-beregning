package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;

public record RefusjonsgrunnlagModell(List<Refusjonskrav> refusjonskravListe,
                                      LocalDate skjæringstidspunkt) {
    // Valider ingen duplikate andeler
    public RefusjonsgrunnlagModell {
        refusjonskravListe = List.copyOf(refusjonskravListe);
        var arbeidsforhold = refusjonskravListe.stream()
            .map(Refusjonskrav::arbeidsforholdsinformasjon)
            .map(a -> new ArbeidsforholdNøkkel(a.arbeidsgiver().erOrganisasjon(), a.arbeidsgiver().getIdentifikator(),
                a.arbeidsforholdId()))
            .collect(Collectors.toSet());
        if (arbeidsforhold.size() != refusjonskravListe.size()) {
            throw new IllegalArgumentException("Flere refusjonskrav med samme arbeidsgiver og arbeidsforholdsreferanse.");
        }
    }

    private record ArbeidsforholdNøkkel(boolean organisasjon, String arbeidsgiverId, String arbeidsforholdId) {}

    public record Refusjonskrav(Arbeidsforholdsinformasjon arbeidsforholdsinformasjon, String journalpostId, List<Kravperioder> kravperioder) {}

    public record Kravperioder(Periode periode, BigDecimal refusjonPerMnd){}

    public record Arbeidsforholdsinformasjon(Arbeidsgiver arbeidsgiver, String arbeidsforholdId, LocalDate startdato) {}
}
