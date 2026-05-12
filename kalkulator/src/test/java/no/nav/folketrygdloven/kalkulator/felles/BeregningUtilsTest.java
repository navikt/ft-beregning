package no.nav.folketrygdloven.kalkulator.felles;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

class BeregningUtilsTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019,1,1);
    @Test
    void skal_finne_ytelse_med_korrekt_ytelsetype() {
        var aapYtelse = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1)).build();
        var dpYtelse = lagYtelse(YtelseType.DAGPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(1)).build();

        var filter = new YtelseFilterDto(Arrays.asList(aapYtelse, dpYtelse));

        var ytelse = MeldekortUtils.sisteVedtakFørStpForType(filter, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelse).isPresent().contains(aapYtelse);
    }

    @Test
    void skal_finne_ytelse_med_vedtak_nærmest_skjæringstidspunkt() {
        var aapYtelseGammel = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1)).build();
        var aapYtelseNy = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(1)).build();

        var filter = new YtelseFilterDto(Arrays.asList(aapYtelseNy, aapYtelseGammel));

        var ytelse = MeldekortUtils.sisteVedtakFørStpForType(filter, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelse).isPresent().contains(aapYtelseNy);
    }

    @Test
    void skal_ikke_ta_med_vedtak_med_fom_etter_skjæringstidspunkt() {
        var aapYtelseGammel = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1)).build();
        var aapYtelseNy = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15)).build();
        var aapYtelseEtterStp = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.plusDays(1), SKJÆRINGSTIDSPUNKT.plusMonths(1)).build();

        var filter = new YtelseFilterDto(Arrays.asList(aapYtelseNy, aapYtelseGammel, aapYtelseEtterStp));

        var ytelse = MeldekortUtils.sisteVedtakFørStpForType(filter, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelse).isPresent().contains(aapYtelseNy);
    }

    @Test
    void skal_finne_korrekt_meldekort_når_det_tilhører_nyeste_vedtak() {
        var aapGammelBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        var aapYtelseNyBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15));

        var nyttMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusDays(45), SKJÆRINGSTIDSPUNKT.minusDays(31));
        var gammeltMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusDays(60), SKJÆRINGSTIDSPUNKT.minusDays(46));

        var gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(gammeltMeldekort).build();
        var nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(nyttMeldekort).build();

        var filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse));
        var snittUtbetaling = MeldekortUtils.snittUtbetalingsgradSistePeriodeFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(snittUtbetaling).isPresent().hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.valueOf(0.5)));
    }

    @Test
    void skal_finne_korrekt_meldekort_når_det_tilhører_eldste_vedtak() {
        var aapGammelBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        var aapYtelseNyBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15));

        var nyttMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusDays(45), SKJÆRINGSTIDSPUNKT.minusDays(31));
        var gammeltMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusDays(60), SKJÆRINGSTIDSPUNKT.minusDays(46));

        var gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(nyttMeldekort).build();
        var nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(gammeltMeldekort).build();

        var filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse));
        var snittUtbetaling = MeldekortUtils.snittUtbetalingsgradSistePeriodeFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(snittUtbetaling).isPresent().hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.valueOf(0.5)));
    }

    @Test
    void skal_finne_meldekort_fra_nyeste_vedtak_når_to_vedtak_har_meldekort_med_samme_periode() {
        var aapGammelBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        var aapYtelseNyBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15));

        var meldekortHundre = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusDays(45), SKJÆRINGSTIDSPUNKT.minusDays(31));
        var meldekortFemti = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusDays(45), SKJÆRINGSTIDSPUNKT.minusDays(31));

        var gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(meldekortHundre).build();
        var nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(meldekortFemti).build();

        var filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse));
        var snittUtbetaling = MeldekortUtils.snittUtbetalingsgradSistePeriodeFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(snittUtbetaling).isPresent().hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.valueOf(0.25)));
    }

    @Test
    void skal_ikke_ta_med_meldekort_fra_vedtak_etter_stp() {
        var aapGammelBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        var aapYtelseNyBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15));
        var aapYtelseEtterStpBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15));

        var meldekortGammel = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusDays(45), SKJÆRINGSTIDSPUNKT.minusDays(31));
        var meldekortNytt = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusDays(30), SKJÆRINGSTIDSPUNKT.minusDays(16));
        var meldekortNyest = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusDays(2), SKJÆRINGSTIDSPUNKT.plusDays(12));

        var gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(meldekortGammel).build();
        var nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(meldekortNytt).build();
        var ytelseEtterStp = aapYtelseEtterStpBuilder.leggTilYtelseAnvist(meldekortNyest).build();

        var filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse, ytelseEtterStp));
        var snittUtbetaling = MeldekortUtils.snittUtbetalingsgradSistePeriodeFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(snittUtbetaling).isPresent().hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.valueOf(0.25)));
    }

    @Test
    void skalFinneMeldekortVedGittDatoNårMeldekortetFinnes() {
        var aapGammelBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(5), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        var aapYtelseNyBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.plusMonths(1));

        var meldekort1 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(6), SKJÆRINGSTIDSPUNKT.minusWeeks(4));
        var meldekort2 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(4), SKJÆRINGSTIDSPUNKT.minusWeeks(2));
        var meldekort3 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(2), SKJÆRINGSTIDSPUNKT.minusWeeks(0));
        var meldekort4 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(10), SKJÆRINGSTIDSPUNKT.minusWeeks(8));
        var gammeltMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(10), SKJÆRINGSTIDSPUNKT.minusWeeks(8));

        var gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(gammeltMeldekort).build();
        var nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(meldekort1)
                .leggTilYtelseAnvist(meldekort2)
                .leggTilYtelseAnvist(meldekort3)
                .leggTilYtelseAnvist(meldekort4).build();

        var filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse));
        var ytelseAnvist = MeldekortUtils.finnesMeldekortSomInkludererGittDato(filter, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER), SKJÆRINGSTIDSPUNKT);

        assertThat(ytelseAnvist).isTrue();
    }

    @Test
    void skalIkkeFinneMeldekortNårMeldekortVedGittDatoIkkeFinnes() {
        var aapGammelBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(5), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        var aapYtelseNyBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.plusMonths(1));

        var meldekort1 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(6), SKJÆRINGSTIDSPUNKT.minusWeeks(4));
        var meldekort2 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(4), SKJÆRINGSTIDSPUNKT.minusWeeks(2));
        var meldekort3 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(2), SKJÆRINGSTIDSPUNKT.minusWeeks(0));
        var meldekort4 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(10), SKJÆRINGSTIDSPUNKT.minusWeeks(8));
        var gammeltMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(10), SKJÆRINGSTIDSPUNKT.minusWeeks(8));

        var gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(gammeltMeldekort).build();
        var nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(meldekort1)
                .leggTilYtelseAnvist(meldekort2)
                .leggTilYtelseAnvist(meldekort3)
                .leggTilYtelseAnvist(meldekort4).build();

        var filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse));
        var ytelseAnvist = MeldekortUtils.finnesMeldekortSomInkludererGittDato(filter, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER), SKJÆRINGSTIDSPUNKT.plusDays(1));

        assertThat(ytelseAnvist).isFalse();
    }

    private YtelseAnvistDto lagMeldekort(BigDecimal utbetalingsgrad, LocalDate fom, LocalDate tom) {
        return YtelseDtoBuilder.ny().getAnvistBuilder()
            .medAnvistPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
            .medUtbetalingsgradProsent(Stillingsprosent.fra(utbetalingsgrad)).build();
    }


    private YtelseDtoBuilder lagYtelse(YtelseType ytelsetype, LocalDate fom, LocalDate tom) {
        return YtelseDtoBuilder.ny()
                .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medYtelseType(ytelsetype)
                .medYtelseKilde(YtelseKilde.ARENA);
    }

    @Test
    void skalFinneRiktigUtbetalingsprosentForSisteHelePeriodeFastProsent() {
        var aapYtelseBuilder = YtelseDtoBuilder.ny().medYtelseType(YtelseType.ARBEIDSAVKLARINGSPENGER)
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(5), SKJÆRINGSTIDSPUNKT.plusMonths(1)))
            .medYtelseKilde(YtelseKilde.KELVIN);

        var meldekort1 = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusWeeks(6), SKJÆRINGSTIDSPUNKT.minusWeeks(4));
        var meldekort2 = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusWeeks(4), SKJÆRINGSTIDSPUNKT.minusWeeks(2));
        var meldekort3 = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusWeeks(2), SKJÆRINGSTIDSPUNKT.minusWeeks(0));
        var meldekort4 = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusWeeks(10), SKJÆRINGSTIDSPUNKT.minusWeeks(8));

        var nyYtelse = aapYtelseBuilder.leggTilYtelseAnvist(meldekort1)
            .leggTilYtelseAnvist(meldekort2)
            .leggTilYtelseAnvist(meldekort3)
            .leggTilYtelseAnvist(meldekort4).build();

        var filter = new YtelseFilterDto(List.of(nyYtelse));
        var utbetalingsgrad = MeldekortUtils.snittUtbetalingsgradSistePeriodeFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(utbetalingsgrad).isPresent().hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.valueOf(0.5)));
    }

    @Test
    void skalFinneRiktigUtbetalingsprosentForSisteHelePeriodeVariabelProsent() {
        var aapYtelseBuilder = YtelseDtoBuilder.ny().medYtelseType(YtelseType.ARBEIDSAVKLARINGSPENGER)
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(5), SKJÆRINGSTIDSPUNKT.plusMonths(1)))
            .medYtelseKilde(YtelseKilde.KELVIN);

        var meldekort1 = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusWeeks(6), SKJÆRINGSTIDSPUNKT.minusWeeks(4));
        var meldekort2 = lagMeldekort(BigDecimal.valueOf(40), SKJÆRINGSTIDSPUNKT.minusWeeks(4), SKJÆRINGSTIDSPUNKT.minusWeeks(2));
        var meldekort3 = lagMeldekort(BigDecimal.valueOf(60), SKJÆRINGSTIDSPUNKT.minusWeeks(2), SKJÆRINGSTIDSPUNKT);
        var meldekort4 = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusWeeks(10), SKJÆRINGSTIDSPUNKT.minusWeeks(8));

        var nyYtelse = aapYtelseBuilder.leggTilYtelseAnvist(meldekort1)
            .leggTilYtelseAnvist(meldekort2)
            .leggTilYtelseAnvist(meldekort3)
            .leggTilYtelseAnvist(meldekort4).build();

        var filter = new YtelseFilterDto(List.of(nyYtelse));
        var utbetalingsgrad = MeldekortUtils.snittUtbetalingsgradSistePeriodeFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));
        // Forventer 9 dager * 60% og 1 dag 40% = 580 / 10 = 58%
        assertThat(utbetalingsgrad).isPresent().hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.valueOf(0.58)));
    }

    @Test
    void skalFinneRiktigUtbetalingsprosentForSisteHelePeriodeLangPeriodeOverStp() {
        var aapYtelseBuilder = YtelseDtoBuilder.ny().medYtelseType(YtelseType.ARBEIDSAVKLARINGSPENGER)
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(5), SKJÆRINGSTIDSPUNKT.plusMonths(1)))
            .medYtelseKilde(YtelseKilde.KELVIN);

        var meldekort1 = lagMeldekort(BigDecimal.valueOf(75), SKJÆRINGSTIDSPUNKT.minusWeeks(3), SKJÆRINGSTIDSPUNKT.plusWeeks(1));

        var nyYtelse = aapYtelseBuilder.leggTilYtelseAnvist(meldekort1).build();

        var filter = new YtelseFilterDto(List.of(nyYtelse));
        var utbetalingsgrad = MeldekortUtils.snittUtbetalingsgradSistePeriodeFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(utbetalingsgrad).isPresent().hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.valueOf(0.75)));
    }

    @Test
    void skalFinneRiktigUtbetalingsprosentForSisteHelePeriodeVariabelPeriodeSamtProsent() {
        var aapYtelseBuilder = YtelseDtoBuilder.ny().medYtelseType(YtelseType.ARBEIDSAVKLARINGSPENGER)
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(5), SKJÆRINGSTIDSPUNKT.plusMonths(1)))
            .medYtelseKilde(YtelseKilde.KELVIN);

        var meldekort1 = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusWeeks(6), SKJÆRINGSTIDSPUNKT.minusWeeks(4));
        var meldekort2 = lagMeldekort(BigDecimal.valueOf(40), SKJÆRINGSTIDSPUNKT.minusWeeks(4), SKJÆRINGSTIDSPUNKT.minusWeeks(3));
        var meldekort3 = lagMeldekort(BigDecimal.valueOf(80), SKJÆRINGSTIDSPUNKT.minusWeeks(3), SKJÆRINGSTIDSPUNKT.minusWeeks(2));
        var meldekort4 = lagMeldekort(BigDecimal.valueOf(30), SKJÆRINGSTIDSPUNKT.minusWeeks(2), SKJÆRINGSTIDSPUNKT.minusWeeks(1));
        var meldekort5 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(1), SKJÆRINGSTIDSPUNKT.plusDays(6));

        var nyYtelse = aapYtelseBuilder.leggTilYtelseAnvist(meldekort1)
            .leggTilYtelseAnvist(meldekort2)
            .leggTilYtelseAnvist(meldekort3)
            .leggTilYtelseAnvist(meldekort4)
            .leggTilYtelseAnvist(meldekort5)
            .build();

        var filter = new YtelseFilterDto(List.of(nyYtelse));
        var utbetalingsgrad = MeldekortUtils.snittUtbetalingsgradSistePeriodeFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));
        // Skal finne 4 dager a 100%, 4 dager a 30%, 2 dager a 80% = 680 / 10 / 100 = 0.68
        assertThat(utbetalingsgrad).isPresent().hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.valueOf(0.68)));
    }

}
