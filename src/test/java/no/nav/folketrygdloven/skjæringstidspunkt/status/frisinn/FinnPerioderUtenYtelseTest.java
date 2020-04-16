package no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.internal.cglib.core.Local;
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
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        LocalDate stp = LocalDate.of(2020, 3, 1);

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
    void skal_finne_de_første_6_mnd_av_de_siste_12_før_stp() {
        // Arrange
        Map<String, Object> res = new HashMap<>();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        LocalDate stp = LocalDate.of(2020, 3, 1);
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.YTELSER)
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
            .medInntektskildeOgPeriodeType(Inntektskilde.YTELSER)
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

}
