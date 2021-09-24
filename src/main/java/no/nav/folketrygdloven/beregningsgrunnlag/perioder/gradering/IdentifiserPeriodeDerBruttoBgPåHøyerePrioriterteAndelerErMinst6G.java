package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.GraderingPrAktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;

class IdentifiserPeriodeDerBruttoBgPåHøyerePrioriterteAndelerErMinst6G {
    private IdentifiserPeriodeDerBruttoBgPåHøyerePrioriterteAndelerErMinst6G() {
        // skjul default
    }

    static Optional<LocalDate> vurder(
		    PeriodeModellGradering input,
		    GraderingPrAktivitet andelGradering) {
        Optional<LocalDate> høyerePrioritertAndeler6GFomOpt = input.getPeriodisertBruttoBeregningsgrunnlagList().stream()
            .filter(periodisertBg -> periodisertBg.getPeriode().overlapper(andelGradering.getPerioder()))
            .filter(periodisertBg -> ErHøyerePrioriterteAndelerBruttoMinst6G.vurder(input.getGrunnbeløp(), periodisertBg, andelGradering))
            .map(PeriodisertBruttoBeregningsgrunnlag::getPeriode)
            .map(Periode::getFom)
            .sorted()
            .findFirst();
        return høyerePrioritertAndeler6GFomOpt.map(høyerePrioritertFom ->
        {
            LocalDate graderingFom = andelGradering.getPerioder().getFom();
            return høyerePrioritertFom.isBefore(graderingFom) ? graderingFom : høyerePrioritertFom;
        });
    }
}
