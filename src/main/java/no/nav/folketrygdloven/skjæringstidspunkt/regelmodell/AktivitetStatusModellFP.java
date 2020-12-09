package no.nav.folketrygdloven.skjæringstidspunkt.regelmodell;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

import java.time.DayOfWeek;
import java.time.LocalDate;

import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class AktivitetStatusModellFP extends AktivitetStatusModell {

    public AktivitetStatusModellFP() {
    }

    public AktivitetStatusModellFP(AktivitetStatusModell aktivitetStatusModell) {
        super(aktivitetStatusModell);
    }

    @Override
    public LocalDate sisteAktivitetsdato() {
        if (liggerDagenFørSkjæringstidspunktForOpptjeningIHelga()) {
            LocalDate fredagFørStpOpptjening = finnFredagFørStpOpptjening();
            LocalDate lørdagFørStpOpptjening = finnFredagFørStpOpptjening().plusDays(1);
            if (harAktivitetSomSlutterPåDato(fredagFørStpOpptjening)) {
                return fredagFørStpOpptjening;
            } else if (harAktivitetSomSlutterPåDato(lørdagFørStpOpptjening)) {
                return lørdagFørStpOpptjening;
            } else {
                return finnSisteAktivitetsdatoFraSistePeriode();
            }
        } else {
            return finnSisteAktivitetsdatoFraSistePeriode();
        }
    }

    private boolean harAktivitetSomSlutterPåDato(LocalDate fredagFørStpOpptjening) {
        return aktivePerioder.stream().anyMatch(a -> a.getPeriode().getTom().isEqual(fredagFørStpOpptjening));
    }

    private boolean liggerDagenFørSkjæringstidspunktForOpptjeningIHelga() {
        LocalDate dagenFørStpOpptjening = skjæringstidspunktForOpptjening.minusDays(1);
        return dagenFørStpOpptjening.getDayOfWeek().equals(SUNDAY) || dagenFørStpOpptjening.getDayOfWeek().equals(SATURDAY);
    }

    private LocalDate finnFredagFørStpOpptjening() {
        if (!liggerDagenFørSkjæringstidspunktForOpptjeningIHelga()) {
            throw new IllegalStateException("Dagen før skjæringstidspunkt ligger ikke i helga.");
        }
        int daysBetween = skjæringstidspunktForOpptjening.minusDays(1).getDayOfWeek().getValue() - DayOfWeek.FRIDAY.getValue();
        return skjæringstidspunktForOpptjening.minusDays(daysBetween+1L);
    }



}
