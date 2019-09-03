package no.nav.foreldrepenger.beregningsgrunnlag.perioder;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AndelGradering;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.PeriodeModell;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.PeriodisertBruttoBeregningsgrunnlag;

class IdentifiserPeriodeDerBruttoBgPåHøyerePrioriterteAndelerErMinst6G {
    private IdentifiserPeriodeDerBruttoBgPåHøyerePrioriterteAndelerErMinst6G() {
        // skjul default
    }

    static Optional<LocalDate> vurder(
        PeriodeModell input,
        AndelGradering andelGradering,
        Periode gradering) {
        Optional<LocalDate> høyerePrioritertAndeler6GFomOpt = input.getPeriodisertBruttoBeregningsgrunnlagList().stream()
            .filter(periodisertBg -> periodisertBg.getPeriode().overlapper(gradering))
            .filter(periodisertBg -> ErHøyerePrioriterteAndelerBruttoMinst6G.vurder(input.getGrunnbeløp(), periodisertBg, andelGradering))
            .map(PeriodisertBruttoBeregningsgrunnlag::getPeriode)
            .map(Periode::getFom)
            .sorted()
            .findFirst();
        return høyerePrioritertAndeler6GFomOpt.map(høyerePrioritertFom ->
        {
            LocalDate graderingFom = gradering.getFom();
            return høyerePrioritertFom.isBefore(graderingFom) ? graderingFom : høyerePrioritertFom;
        });
    }
}
