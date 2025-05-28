package no.nav.folketrygdloven.kalkulator.felles;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

class BeregningUtilsTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019,1,1);
    @Test
    void skal_finne_ytelse_med_korrekt_ytelsetype() {
        var aapYtelse = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1)).build();
        var dpYtelse = lagYtelse(YtelseType.DAGPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(1)).build();

        var filter = new YtelseFilterDto(Arrays.asList(aapYtelse, dpYtelse));

        var ytelse = MeldekortUtils.sisteVedtakFørStpForType(filter, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelse).isPresent();
        assertThat(ytelse.get()).isEqualTo(aapYtelse);
    }

    @Test
    void skal_finne_ytelse_med_vedtak_nærmest_skjæringstidspunkt() {
        var aapYtelseGammel = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1)).build();
        var aapYtelseNy = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(1)).build();

        var filter = new YtelseFilterDto(Arrays.asList(aapYtelseNy, aapYtelseGammel));

        var ytelse = MeldekortUtils.sisteVedtakFørStpForType(filter, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelse).isPresent();
        assertThat(ytelse.get()).isEqualTo(aapYtelseNy);
    }

    @Test
    void skal_ikke_ta_med_vedtak_med_fom_etter_skjæringstidspunkt() {
        var aapYtelseGammel = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1)).build();
        var aapYtelseNy = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15)).build();
        var aapYtelseEtterStp = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.plusDays(1), SKJÆRINGSTIDSPUNKT.plusMonths(1)).build();

        var filter = new YtelseFilterDto(Arrays.asList(aapYtelseNy, aapYtelseGammel, aapYtelseEtterStp));

        var ytelse = MeldekortUtils.sisteVedtakFørStpForType(filter, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelse).isPresent();
        assertThat(ytelse.get()).isEqualTo(aapYtelseNy);
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
        var ytelseAnvist = MeldekortUtils.sisteHeleMeldekortFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelseAnvist).isPresent();
        assertThat(ytelseAnvist.get()).isEqualTo(nyttMeldekort);
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
        var ytelseAnvist = MeldekortUtils.sisteHeleMeldekortFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelseAnvist).isPresent();
        assertThat(ytelseAnvist.get()).isEqualTo(nyttMeldekort);
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
        var ytelseAnvist = MeldekortUtils.sisteHeleMeldekortFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelseAnvist).isPresent();
        assertThat(ytelseAnvist.get()).isEqualTo(meldekortFemti);
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
        var ytelseAnvist = MeldekortUtils.sisteHeleMeldekortFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelseAnvist).isPresent();
        assertThat(ytelseAnvist.get()).isEqualTo(meldekortNytt);
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
        var ytelseAnvist = MeldekortUtils.finnMeldekortSomInkludererGittDato(filter, nyYtelse, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER), SKJÆRINGSTIDSPUNKT);

        assertThat(ytelseAnvist).isPresent();
        assertThat(ytelseAnvist.get()).isEqualTo(meldekort3);
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
        var ytelseAnvist = MeldekortUtils.finnMeldekortSomInkludererGittDato(filter, nyYtelse, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER), SKJÆRINGSTIDSPUNKT.plusDays(1));

        assertThat(ytelseAnvist).isEmpty();
    }

    private YtelseAnvistDto lagMeldekort(BigDecimal utbetalingsgrad, LocalDate fom, LocalDate tom) {
        return YtelseDtoBuilder.ny().getAnvistBuilder()
            .medAnvistPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
            .medUtbetalingsgradProsent(Stillingsprosent.fra(utbetalingsgrad)).build();
    }


    private YtelseDtoBuilder lagYtelse(YtelseType ytelsetype, LocalDate fom, LocalDate tom) {
        return YtelseDtoBuilder.ny()
                .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medYtelseType(ytelsetype);
    }


}
