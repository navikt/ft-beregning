package no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;

class FinnPerioderUtenYtelseTest {

    @Test
    void skal_finne_12_mnd_før_stp_uten_ytelse() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        var inntektsgrunnlag = new Inntektsgrunnlag();
        var stp = LocalDate.of(2020, 3, 15);

        // Act
        var perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder).hasSize(12);
        for (var i = 12; i >= 1; i--) {
            var måned = stp.minusMonths(i);
            assertThat(perioder.get(12 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(12 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }

    @Test
    void skal_finne_de_første_7_mnd_av_de_siste_12_før_stp() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        var inntektsgrunnlag = new Inntektsgrunnlag();
        var stp = LocalDate.of(2020, 3, 15);
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
            .medInntekt(BigDecimal.TEN)
            .medPeriode(Periode.of(LocalDate.of(2019, 10, 1), LocalDate.of(2020, 2, 29)))
            .build());

        // Act
        var perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder).hasSize(7);
        for (var i = 12; i >= 6; i--) {
            var måned = stp.minusMonths(i);
            assertThat(perioder.get(12 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(12 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }


    @Test
    void skal_finne_de_første_6_mnd_av_de_siste_12_før_stp() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        var inntektsgrunnlag = new Inntektsgrunnlag();
        var stp = LocalDate.of(2020, 3, 15);
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
            .medInntekt(BigDecimal.TEN)
            .medPeriode(Periode.of(LocalDate.of(2019, 9, 1), LocalDate.of(2020, 2, 29)))
            .build());

        // Act
        var perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder).hasSize(6);
        for (var i = 12; i >= 7; i--) {
            var måned = stp.minusMonths(i);
            assertThat(perioder.get(12 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(12 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }

    @Test
    void skal_finne_de_siste_6_mnd_av_de_siste_12_før_stp() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        var inntektsgrunnlag = new Inntektsgrunnlag();
        var stp = LocalDate.of(2020, 3, 1);
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
            .medInntekt(BigDecimal.TEN)
            .medPeriode(Periode.of(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 8, 31)))
            .build());

        // Act
        var perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder).hasSize(6);
        for (var i = 6; i >= 1; i--) {
            var måned = stp.minusMonths(i);
            assertThat(perioder.get(6 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(6 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }

    @Test
    void skal_finne_6_mnd_når_annenhver_mnd_på_siste_12_har_ytelse() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        var inntektsgrunnlag = new Inntektsgrunnlag();
        var stp = LocalDate.of(2020, 3, 1);
        inntektsgrunnlag.leggTilPeriodeinntekt(byggDPEllerAAPYtelse(Periode.of(LocalDate.of(2020, 2, 1), LocalDate.of(2020, 2, 29))));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggDPEllerAAPYtelse(Periode.of(LocalDate.of(2019, 12, 1), LocalDate.of(2019, 12, 31))));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggDPEllerAAPYtelse(Periode.of(LocalDate.of(2019, 10, 1), LocalDate.of(2019, 10, 31))));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggDPEllerAAPYtelse(Periode.of(LocalDate.of(2019, 8, 1), LocalDate.of(2019, 8, 31))));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggDPEllerAAPYtelse(Periode.of(LocalDate.of(2019, 6, 1), LocalDate.of(2019, 6, 30))));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggDPEllerAAPYtelse(Periode.of(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 30))));

        // Act
        var perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder).hasSize(6);
        for (var i = 6; i >= 1; i--) {
            var måned = stp.minusMonths(i* 2L);
            assertThat(perioder.get(6 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(6 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }

    @Test
    void skal_finne_6_mnd_når_10_mnd_av_siste_12_mnd_har_ytelse() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        var inntektsgrunnlag = new Inntektsgrunnlag();
        var stp = LocalDate.of(2020, 3, 1);
        inntektsgrunnlag.leggTilPeriodeinntekt(byggDPEllerAAPYtelse(Periode.of(LocalDate.of(2019, 5, 20), LocalDate.of(2020, 2, 12))));

        // Act
        var perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder).hasSize(6);
        for (var i = 16; i >= 11; i--) {
            var måned = stp.minusMonths(i);
            assertThat(perioder.get(16 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(16 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }

    @Test
    void skal_finne_6_mnd_når_alle_13_siste_mnd_har_ytelse() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        var inntektsgrunnlag = new Inntektsgrunnlag();
        var stp = LocalDate.of(2020, 3, 1);
        inntektsgrunnlag.leggTilPeriodeinntekt(byggDPEllerAAPYtelse(Periode.of(LocalDate.of(2019, 1, 15), LocalDate.of(2020, 2, 12))));

        // Act
        var perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder).hasSize(6);
        for (var i = 20; i >= 15; i--) {
            var måned = stp.minusMonths(i);
            assertThat(perioder.get(20 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(20 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }

    @Test
    void skal_finne_6_måneder_når_de_30_andre_er_med_ytelse() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        var inntektsgrunnlag = new Inntektsgrunnlag();
        var stp = LocalDate.of(2020, 3, 1);
        inntektsgrunnlag.leggTilPeriodeinntekt(byggAnnenYtelse(Periode.of(LocalDate.of(2017, 9, 30), LocalDate.of(2020, 2, 12))));

        // Act
        var perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder).hasSize(6);
        for (var i = 36; i >= 31; i--) {
            var måned = stp.minusMonths(i);
            assertThat(perioder.get(36 - i).getFom()).isEqualTo(måned.withDayOfMonth(1));
            assertThat(perioder.get(36 - i).getTom()).isEqualTo(måned.withDayOfMonth(måned.lengthOfMonth()));
        }
    }

    @Test
    void skal_finne_ingen_måneder_når_det_er_DP_eller_AAP_til_2017() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        var inntektsgrunnlag = new Inntektsgrunnlag();
        var stp = LocalDate.of(2020, 3, 1);
        inntektsgrunnlag.leggTilPeriodeinntekt(byggDPEllerAAPYtelse(Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2020, 2, 12))));

        // Act
        var perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder).isEmpty();
    }

    @Test
    void skal_finne_6_måneder_når_det_er_DP_eller_AAP_til_februar_2018() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        var inntektsgrunnlag = new Inntektsgrunnlag();
        var stp = LocalDate.of(2020, 3, 1);
        inntektsgrunnlag.leggTilPeriodeinntekt(byggDPEllerAAPYtelse(Periode.of(LocalDate.of(2018, 2, 1), LocalDate.of(2020, 2, 12))));

        // Act
        var perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder).hasSize(6);
    }

    @Test
    void skal_finne_6_måneder_når_det_er_ytelse_til_2017_men_ikke_AAP_eller_DP() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        var inntektsgrunnlag = new Inntektsgrunnlag();
        var stp = LocalDate.of(2020, 3, 1);
        inntektsgrunnlag.leggTilPeriodeinntekt(byggAnnenYtelse(Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2020, 2, 12))));

        // Act
        var perioder = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, stp, res);

        // Assert
        assertThat(perioder).hasSize(6);
    }

    private Periodeinntekt byggDPEllerAAPYtelse(Periode periode) {
        return Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
            .medInntekt(BigDecimal.TEN)
            .medPeriode(periode)
            .build();
    }


    private Periodeinntekt byggAnnenYtelse(Periode periode) {
        return Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.ANNEN_YTELSE)
            .medInntekt(BigDecimal.TEN)
            .medPeriode(periode)
            .build();
    }
}
