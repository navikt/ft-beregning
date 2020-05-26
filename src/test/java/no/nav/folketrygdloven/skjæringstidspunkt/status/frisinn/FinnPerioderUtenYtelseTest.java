package no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FinnPerioderUtenYtelseTest {

    @Test
    void skal_finne_12_mnd_før_stp_uten_ytelse() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        LocalDate stp = LocalDate.of(2020, 3, 15);

        // Act
        List<Periode> perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder.size()).isEqualTo(12);
        for (int i = 12; i >= 1; i--) {
            LocalDate måned = stp.minusMonths(i);
            assertThat(perioder.get(12 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(12 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }

    @Test
    void skal_finne_de_første_7_mnd_av_de_siste_12_før_stp() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        LocalDate stp = LocalDate.of(2020, 3, 15);
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
            .medInntekt(BigDecimal.TEN)
            .medPeriode(Periode.of(LocalDate.of(2019, 10, 1), LocalDate.of(2020, 2, 29)))
            .build());

        // Act
        List<Periode> perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder.size()).isEqualTo(7);
        for (int i = 12; i >= 6; i--) {
            LocalDate måned = stp.minusMonths(i);
            assertThat(perioder.get(12 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(12 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }


    @Test
    void skal_finne_de_første_6_mnd_av_de_siste_12_før_stp() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        LocalDate stp = LocalDate.of(2020, 3, 15);
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
            .medInntekt(BigDecimal.TEN)
            .medPeriode(Periode.of(LocalDate.of(2019, 9, 1), LocalDate.of(2020, 2, 29)))
            .build());

        // Act
        List<Periode> perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder.size()).isEqualTo(6);
        for (int i = 12; i >= 7; i--) {
            LocalDate måned = stp.minusMonths(i);
            assertThat(perioder.get(12 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(12 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }

    @Test
    void skal_finne_de_siste_6_mnd_av_de_siste_12_før_stp() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        LocalDate stp = LocalDate.of(2020, 3, 1);
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
            .medInntekt(BigDecimal.TEN)
            .medPeriode(Periode.of(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 8, 31)))
            .build());

        // Act
        List<Periode> perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder.size()).isEqualTo(6);
        for (int i = 6; i >= 1; i--) {
            LocalDate måned = stp.minusMonths(i);
            assertThat(perioder.get(6 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(6 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }

    @Test
    void skal_finne_6_mnd_når_annenhver_mnd_på_siste_12_har_ytelse() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        LocalDate stp = LocalDate.of(2020, 3, 1);
        inntektsgrunnlag.leggTilPeriodeinntekt(byggYtelse(Periode.of(LocalDate.of(2020, 2, 1), LocalDate.of(2020, 2, 29))));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggYtelse(Periode.of(LocalDate.of(2019, 12, 1), LocalDate.of(2019, 12, 31))));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggYtelse(Periode.of(LocalDate.of(2019, 10, 1), LocalDate.of(2019, 10, 31))));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggYtelse(Periode.of(LocalDate.of(2019, 8, 1), LocalDate.of(2019, 8, 31))));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggYtelse(Periode.of(LocalDate.of(2019, 6, 1), LocalDate.of(2019, 6, 30))));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggYtelse(Periode.of(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 30))));

        // Act
        List<Periode> perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder.size()).isEqualTo(6);
        for (int i = 6; i >= 1; i--) {
            LocalDate måned = stp.minusMonths(i*2);
            assertThat(perioder.get(6 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(6 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }

    @Test
    void skal_finne_6_mnd_når_10_mnd_av_siste_12_mnd_har_ytelse() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        LocalDate stp = LocalDate.of(2020, 3, 1);
        inntektsgrunnlag.leggTilPeriodeinntekt(byggYtelse(Periode.of(LocalDate.of(2019, 5, 20), LocalDate.of(2020, 2, 12))));

        // Act
        List<Periode> perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder.size()).isEqualTo(6);
        for (int i = 16; i >= 11; i--) {
            LocalDate måned = stp.minusMonths(i);
            assertThat(perioder.get(16 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(16 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }

    @Test
    void skal_finne_6_mnd_når_alle_13_siste_mnd_har_ytelse() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        LocalDate stp = LocalDate.of(2020, 3, 1);
        inntektsgrunnlag.leggTilPeriodeinntekt(byggYtelse(Periode.of(LocalDate.of(2019, 1, 15), LocalDate.of(2020, 2, 12))));

        // Act
        List<Periode> perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder.size()).isEqualTo(6);
        for (int i = 20; i >= 15; i--) {
            LocalDate måned = stp.minusMonths(i);
            assertThat(perioder.get(20 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(20 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }

    @Test
    void skal_finne_6_måneder_når_de_30_andre_er_med_ytelse() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        LocalDate stp = LocalDate.of(2020, 3, 1);
        inntektsgrunnlag.leggTilPeriodeinntekt(byggYtelse(Periode.of(LocalDate.of(2017, 9, 30), LocalDate.of(2020, 2, 12))));

        // Act
        List<Periode> perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder.size()).isEqualTo(6);
        for (int i = 36; i >= 31; i--) {
            LocalDate måned = stp.minusMonths(i);
            assertThat(perioder.get(36 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(36 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }

    private Periodeinntekt byggYtelse(Periode periode) {
        return Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
            .medInntekt(BigDecimal.TEN)
            .medPeriode(periode)
            .build();
    }
}
