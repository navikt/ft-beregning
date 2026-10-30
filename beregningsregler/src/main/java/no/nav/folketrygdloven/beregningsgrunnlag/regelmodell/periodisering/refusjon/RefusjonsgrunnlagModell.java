package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;

public record RefusjonsgrunnlagModell(List<Refusjonskrav> refusjonskravListe,
                                      LocalDate skjæringstidspunkt) {
    public record Refusjonskrav(Arbeidsforholdsinformasjon arbeidsforholdsinformasjon, Periode periode, BigDecimal refusjonPerMnd) {}

    public record Arbeidsforholdsinformasjon(Arbeidsgiver arbeidsgiver, String arbeidsforholdId, LocalDate startdato) {}
}
