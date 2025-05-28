package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelingTilfelle;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.AktivitetStatusMatcher;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.NyPeriodeDto;

/**
 * Lager perioder for nye aktiviteter med utbetalingsgrad som ikke har gradering eller refusjon
 */
class NyAktivitetMedSøktYtelseFordeling {

    static List<NyPeriodeDto> lagPerioderForNyAktivitetMedSøktYtelse(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                     FordelingTilfelle tilfelle,
                                                                     BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                     FordelBeregningsgrunnlagArbeidsforholdDto endringAf) {
        var gjelderYtelseMedUtbetalingsgrad = ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag;
        var gjelderNyAktivitet = tilfelle.equals(FordelingTilfelle.NY_AKTIVITET);
        var harIkkeRefusjonEllerGradering = endringAf.getPerioderMedGraderingEllerRefusjon().isEmpty();
        if (harIkkeRefusjonEllerGradering && gjelderNyAktivitet && gjelderYtelseMedUtbetalingsgrad) {
            var utbetalingsgradGrunnlag = (UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag;
            return utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet()
                    .stream()
                    .filter(utbAktivitet -> matcherAndelAktivitetMedUtbetalingsgrad(andel, utbAktivitet))
                    .flatMap(utbetalingsgradPrAktivitetDto -> utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().stream())
                    .map(NyAktivitetMedSøktYtelseFordeling::mapTilNyPeriode)
                    .collect(Collectors.toList());

        }
        return Collections.emptyList();
    }

    private static boolean matcherAndelAktivitetMedUtbetalingsgrad(BeregningsgrunnlagPrStatusOgAndelDto andel, UtbetalingsgradPrAktivitetDto utbAktivitet) {
        var arbeidsforhold = utbAktivitet.getUtbetalingsgradArbeidsforhold();
        var uttakArbeidType = arbeidsforhold.getUttakArbeidType();
        if (andel.getAktivitetStatus().erArbeidstaker()) {
            return AktivitetStatusMatcher.matcherStatus(andel.getAktivitetStatus(), uttakArbeidType) && matcherArbeidSøktYtelse(andel, arbeidsforhold);
        }
        return AktivitetStatusMatcher.matcherStatus(andel.getAktivitetStatus(), uttakArbeidType);
    }

    private static Boolean matcherArbeidSøktYtelse(BeregningsgrunnlagPrStatusOgAndelDto andel, AktivitetDto arbeidsforhold) {
        return andel.getBgAndelArbeidsforhold()
                .map(arb -> arb.getArbeidsgiver() != null && arbeidsforhold.getArbeidsgiver().isPresent() &&
                        arb.getArbeidsgiver().getIdentifikator().equals(arbeidsforhold.getArbeidsgiver().get().getIdentifikator()) &&
                        arb.getArbeidsforholdRef().gjelderFor(arbeidsforhold.getInternArbeidsforholdRef())).orElse(false);
    }

    private static NyPeriodeDto mapTilNyPeriode(PeriodeMedUtbetalingsgradDto p) {
        var endringIYtelsePeriode = new NyPeriodeDto(false, false, true);
        endringIYtelsePeriode.setFom(p.getPeriode().getFomDato());
        endringIYtelsePeriode.setTom(p.getPeriode().getTomDato());
        return endringIYtelsePeriode;
    }


}
