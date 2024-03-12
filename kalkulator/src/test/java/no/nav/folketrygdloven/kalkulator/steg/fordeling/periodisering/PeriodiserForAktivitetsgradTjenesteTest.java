package no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

class PeriodiserForAktivitetsgradTjenesteTest {

    @Test
    void skal_ikke_splitte_grunnlag_uten_aktivitetsgrad() {
        // Arrange
        var stp = LocalDate.now();
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlagMedEnPeriode(stp);
        var ytelsespesifiktGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(new UtbetalingsgradPrAktivitetDto(
                new AktivitetDto(Arbeidsgiver.virksomhet("123456789"), InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp, stp.plusMonths(1)), Utbetalingsgrad.valueOf(10), null))
        )), stp);

        // Act
        var resultat = PeriodiserForAktivitetsgradTjeneste.splittVedEndringIAktivitetsgrad(bg, ytelsespesifiktGrunnlag);

        // Assert
        var resultatperioder = resultat.getBeregningsgrunnlagPerioder();
        assertThat(resultatperioder.size()).isEqualTo(1);
        assertThat(resultatperioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp);
        assertThat(resultatperioder.get(0).getBeregningsgrunnlagPeriodeTom()).isEqualTo(TIDENES_ENDE);
        assertThat(resultatperioder.get(0).getPeriodeÅrsaker().size()).isEqualTo(0);
    }

    @Test
    void skal_splitte_grunnlag_med_endring_i_aktivitetsgrad_for_en_arbeidsgiver() {
        // Arrange
        var stp = LocalDate.now();
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlagMedEnPeriode(stp);
        var ytelsespesifiktGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(new UtbetalingsgradPrAktivitetDto(
                new AktivitetDto(Arbeidsgiver.virksomhet("123456789"), InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp, stp.plusMonths(1)), Utbetalingsgrad.valueOf(10), Aktivitetsgrad.fra(BigDecimal.TEN)))
        )), stp);

        // Act
        var resultat = PeriodiserForAktivitetsgradTjeneste.splittVedEndringIAktivitetsgrad(bg, ytelsespesifiktGrunnlag);

        // Assert
        var resultatperioder = resultat.getBeregningsgrunnlagPerioder();
        assertThat(resultatperioder.size()).isEqualTo(2);
        assertThat(resultatperioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp);
        assertThat(resultatperioder.get(0).getBeregningsgrunnlagPeriodeTom()).isEqualTo(stp.plusMonths(1));

        assertThat(resultatperioder.get(1).getBeregningsgrunnlagPeriodeTom()).isEqualTo(TIDENES_ENDE);
        assertThat(resultatperioder.get(1).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp.plusMonths(1).plusDays(1));
        assertThat(resultatperioder.get(1).getPeriodeÅrsaker().size()).isEqualTo(1);
    }

    @Test
    void skal_splitte_grunnlag_med_endring_i_aktivitetsgrad_for_en_arbeidsgiver_også_når_det_ikke_er_endring_hos_en_annen_arbeidsgiver() {
        // Arrange
        var stp = LocalDate.now();
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlagMedEnPeriode(stp);
        var ytelsespesifiktGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(
                new UtbetalingsgradPrAktivitetDto(new AktivitetDto(Arbeidsgiver.virksomhet("111111111"), InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID), List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp, TIDENES_ENDE), Utbetalingsgrad.valueOf(0), Aktivitetsgrad.ZERO))),
                new UtbetalingsgradPrAktivitetDto(new AktivitetDto(Arbeidsgiver.virksomhet("123456789"), InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID), List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp, stp.plusMonths(1)), Utbetalingsgrad.valueOf(10), Aktivitetsgrad.fra(10))))
        ), stp);

        // Act
        var resultat = PeriodiserForAktivitetsgradTjeneste.splittVedEndringIAktivitetsgrad(bg, ytelsespesifiktGrunnlag);

        // Assert
        var resultatperioder = resultat.getBeregningsgrunnlagPerioder();
        assertThat(resultatperioder.size()).isEqualTo(2);
        assertThat(resultatperioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp);
        assertThat(resultatperioder.get(0).getBeregningsgrunnlagPeriodeTom()).isEqualTo(stp.plusMonths(1));

        assertThat(resultatperioder.get(1).getBeregningsgrunnlagPeriodeTom()).isEqualTo(TIDENES_ENDE);
        assertThat(resultatperioder.get(1).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp.plusMonths(1).plusDays(1));
        assertThat(resultatperioder.get(1).getPeriodeÅrsaker().size()).isEqualTo(1);
    }

    @Test
    void skal_splitte_grunnlag_med_to_endringer_i_aktivitetsgrad_for_en_arbeidsgiver() {
        // Arrange
        var stp = LocalDate.now();
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlagMedEnPeriode(stp);
        var ytelsespesifiktGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(new UtbetalingsgradPrAktivitetDto(
                new AktivitetDto(Arbeidsgiver.virksomhet("123456789"), InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp, stp.plusMonths(1)), Utbetalingsgrad.valueOf(10), Aktivitetsgrad.fra(10)),
                        new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp.plusMonths(1).plusDays(1), stp.plusMonths(2)), Utbetalingsgrad.valueOf(10), Aktivitetsgrad.fra(50)))
        )), stp);

        // Act
        var resultat = PeriodiserForAktivitetsgradTjeneste.splittVedEndringIAktivitetsgrad(bg, ytelsespesifiktGrunnlag);

        // Assert
        var resultatperioder = resultat.getBeregningsgrunnlagPerioder();
        assertThat(resultatperioder.size()).isEqualTo(3);
        assertThat(resultatperioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp);
        assertThat(resultatperioder.get(0).getBeregningsgrunnlagPeriodeTom()).isEqualTo(stp.plusMonths(1));

        assertThat(resultatperioder.get(1).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp.plusMonths(1).plusDays(1));
        assertThat(resultatperioder.get(1).getBeregningsgrunnlagPeriodeTom()).isEqualTo(stp.plusMonths(2));
        assertThat(resultatperioder.get(1).getPeriodeÅrsaker().size()).isEqualTo(1);

        assertThat(resultatperioder.get(2).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp.plusMonths(2).plusDays(1));
        assertThat(resultatperioder.get(2).getBeregningsgrunnlagPeriodeTom()).isEqualTo(TIDENES_ENDE);
        assertThat(resultatperioder.get(2).getPeriodeÅrsaker().size()).isEqualTo(1);
    }


    @Test
    void skal_splitte_grunnlag_med_endring_i_aktivitetsgrad_for_to_arbeidsgivere() {
        // Arrange
        var stp = LocalDate.now();
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlagMedEnPeriode(stp);
        var ytelsespesifiktGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(new UtbetalingsgradPrAktivitetDto(
                        new AktivitetDto(Arbeidsgiver.virksomhet("123456789"), InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp, stp.plusMonths(1)), Utbetalingsgrad.valueOf(10), Aktivitetsgrad.fra(10)))
                ),
                new UtbetalingsgradPrAktivitetDto(
                        new AktivitetDto(Arbeidsgiver.virksomhet("987654321"), InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp, stp.plusMonths(2)), Utbetalingsgrad.valueOf(10), Aktivitetsgrad.fra(50)))
                )), stp);

        // Act
        var resultat = PeriodiserForAktivitetsgradTjeneste.splittVedEndringIAktivitetsgrad(bg, ytelsespesifiktGrunnlag);

        // Assert
        var resultatperioder = resultat.getBeregningsgrunnlagPerioder();
        assertThat(resultatperioder.size()).isEqualTo(3);
        assertThat(resultatperioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp);
        assertThat(resultatperioder.get(0).getBeregningsgrunnlagPeriodeTom()).isEqualTo(stp.plusMonths(1));

        assertThat(resultatperioder.get(1).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp.plusMonths(1).plusDays(1));
        assertThat(resultatperioder.get(1).getBeregningsgrunnlagPeriodeTom()).isEqualTo(stp.plusMonths(2));
        assertThat(resultatperioder.get(1).getPeriodeÅrsaker().size()).isEqualTo(1);

        assertThat(resultatperioder.get(2).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp.plusMonths(2).plusDays(1));
        assertThat(resultatperioder.get(2).getBeregningsgrunnlagPeriodeTom()).isEqualTo(TIDENES_ENDE);
        assertThat(resultatperioder.get(2).getPeriodeÅrsaker().size()).isEqualTo(1);
    }


    @Test
    void skal_splitte_grunnlag_med_endring_i_aktivitetsgrad_for_to_arbeidsgivere_med_hull() {
        // Arrange
        var stp = LocalDate.now();
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlagMedEnPeriode(stp);
        var ytelsespesifiktGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(new UtbetalingsgradPrAktivitetDto(
                        new AktivitetDto(Arbeidsgiver.virksomhet("123456789"), InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp, stp.plusMonths(1)), Utbetalingsgrad.valueOf(10), Aktivitetsgrad.fra(10)))
                ),
                new UtbetalingsgradPrAktivitetDto(
                        new AktivitetDto(Arbeidsgiver.virksomhet("987654321"), InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp, stp.plusMonths(2)), Utbetalingsgrad.valueOf(10), Aktivitetsgrad.fra(50)),
                                new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp.plusMonths(3), stp.plusMonths(4)), Utbetalingsgrad.valueOf(10), Aktivitetsgrad.fra(50)))
                )), stp);

        // Act
        var resultat = PeriodiserForAktivitetsgradTjeneste.splittVedEndringIAktivitetsgrad(bg, ytelsespesifiktGrunnlag);

        // Assert
        var resultatperioder = resultat.getBeregningsgrunnlagPerioder();
        assertThat(resultatperioder.size()).isEqualTo(5);
        assertThat(resultatperioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp);
        assertThat(resultatperioder.get(0).getBeregningsgrunnlagPeriodeTom()).isEqualTo(stp.plusMonths(1));

        assertThat(resultatperioder.get(1).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp.plusMonths(1).plusDays(1));
        assertThat(resultatperioder.get(1).getBeregningsgrunnlagPeriodeTom()).isEqualTo(stp.plusMonths(2));
        assertThat(resultatperioder.get(1).getPeriodeÅrsaker().size()).isEqualTo(1);

        assertThat(resultatperioder.get(2).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp.plusMonths(2).plusDays(1));
        assertThat(resultatperioder.get(2).getBeregningsgrunnlagPeriodeTom()).isEqualTo(stp.plusMonths(3).minusDays(1));
        assertThat(resultatperioder.get(2).getPeriodeÅrsaker().size()).isEqualTo(1);

        assertThat(resultatperioder.get(3).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp.plusMonths(3));
        assertThat(resultatperioder.get(3).getBeregningsgrunnlagPeriodeTom()).isEqualTo(stp.plusMonths(4));
        assertThat(resultatperioder.get(3).getPeriodeÅrsaker().size()).isEqualTo(1);

        assertThat(resultatperioder.get(4).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp.plusMonths(4).plusDays(1));
        assertThat(resultatperioder.get(4).getBeregningsgrunnlagPeriodeTom()).isEqualTo(TIDENES_ENDE);
        assertThat(resultatperioder.get(4).getPeriodeÅrsaker().size()).isEqualTo(1);
    }

    @Test
    void skal_splitte_grunnlag_med_to_perioder_uten_endringer_i_aktivitetsgrad_for_en_arbeidsgiver() {
        // Arrange
        var stp = LocalDate.now();
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlagMedEnPeriode(stp);
        var ytelsespesifiktGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(new UtbetalingsgradPrAktivitetDto(
                new AktivitetDto(Arbeidsgiver.virksomhet("123456789"), InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp, stp.plusMonths(1)), Utbetalingsgrad.valueOf(10), Aktivitetsgrad.fra(10)),
                        new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp.plusMonths(1).plusDays(1), stp.plusMonths(2)), Utbetalingsgrad.valueOf(10), Aktivitetsgrad.fra(10)))
        )), stp);

        // Act
        var resultat = PeriodiserForAktivitetsgradTjeneste.splittVedEndringIAktivitetsgrad(bg, ytelsespesifiktGrunnlag);

        // Assert
        var resultatperioder = resultat.getBeregningsgrunnlagPerioder();
        assertThat(resultatperioder.size()).isEqualTo(2);
        assertThat(resultatperioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp);
        assertThat(resultatperioder.get(0).getBeregningsgrunnlagPeriodeTom()).isEqualTo(stp.plusMonths(2));
        assertThat(resultatperioder.get(0).getPeriodeÅrsaker().size()).isEqualTo(0);

        assertThat(resultatperioder.get(1).getBeregningsgrunnlagPeriodeFom()).isEqualTo(stp.plusMonths(2).plusDays(1));
        assertThat(resultatperioder.get(1).getBeregningsgrunnlagPeriodeTom()).isEqualTo(TIDENES_ENDE);
        assertThat(resultatperioder.get(1).getPeriodeÅrsaker().size()).isEqualTo(1);
    }


    private static BeregningsgrunnlagDto lagBeregningsgrunnlagMedEnPeriode(LocalDate stp) {
        var bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(stp)
                .build();
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(stp, TIDENES_ENDE)
                .build(bg);
        return bg;
    }
}
