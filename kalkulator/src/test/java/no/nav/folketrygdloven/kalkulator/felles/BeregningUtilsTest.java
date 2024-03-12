package no.nav.folketrygdloven.kalkulator.felles;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

public class BeregningUtilsTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019,1,1);
    @Test
    public void skal_finne_ytelse_med_korrekt_ytelsetype() {
        YtelseDto aapYtelse = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1)).build();
        YtelseDto dpYtelse = lagYtelse(YtelseType.DAGPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(1)).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(aapYtelse, dpYtelse));

        Optional<YtelseDto> ytelse = MeldekortUtils.sisteVedtakFørStpForType(filter, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelse).isPresent();
        assertThat(ytelse.get()).isEqualTo(aapYtelse);
    }

    @Test
    public void skal_finne_ytelse_med_vedtak_nærmest_skjæringstidspunkt() {
        YtelseDto aapYtelseGammel = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1)).build();
        YtelseDto aapYtelseNy = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(1)).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(aapYtelseNy, aapYtelseGammel));

        Optional<YtelseDto> ytelse = MeldekortUtils.sisteVedtakFørStpForType(filter, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelse).isPresent();
        assertThat(ytelse.get()).isEqualTo(aapYtelseNy);
    }

    @Test
    public void skal_ikke_ta_med_vedtak_med_fom_etter_skjæringstidspunkt() {
        YtelseDto aapYtelseGammel = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1)).build();
        YtelseDto aapYtelseNy = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15)).build();
        YtelseDto aapYtelseEtterStp = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.plusDays(1), SKJÆRINGSTIDSPUNKT.plusMonths(1)).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(aapYtelseNy, aapYtelseGammel, aapYtelseEtterStp));

        Optional<YtelseDto> ytelse = MeldekortUtils.sisteVedtakFørStpForType(filter, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelse).isPresent();
        assertThat(ytelse.get()).isEqualTo(aapYtelseNy);
    }

    @Test
    public void skal_finne_korrekt_meldekort_når_det_tilhører_nyeste_vedtak() {
        YtelseDtoBuilder aapGammelBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        YtelseDtoBuilder aapYtelseNyBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15));

        YtelseAnvistDto nyttMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusDays(45), SKJÆRINGSTIDSPUNKT.minusDays(31));
        YtelseAnvistDto gammeltMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusDays(60), SKJÆRINGSTIDSPUNKT.minusDays(46));

        YtelseDto gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(gammeltMeldekort).build();
        YtelseDto nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(nyttMeldekort).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse));
        Optional<YtelseAnvistDto> ytelseAnvist = MeldekortUtils.sisteHeleMeldekortFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelseAnvist).isPresent();
        assertThat(ytelseAnvist.get()).isEqualTo(nyttMeldekort);
    }

    @Test
    public void skal_finne_korrekt_meldekort_når_det_tilhører_eldste_vedtak() {
        YtelseDtoBuilder aapGammelBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        YtelseDtoBuilder aapYtelseNyBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15));

        YtelseAnvistDto nyttMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusDays(45), SKJÆRINGSTIDSPUNKT.minusDays(31));
        YtelseAnvistDto gammeltMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusDays(60), SKJÆRINGSTIDSPUNKT.minusDays(46));

        YtelseDto gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(nyttMeldekort).build();
        YtelseDto nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(gammeltMeldekort).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse));
        Optional<YtelseAnvistDto> ytelseAnvist = MeldekortUtils.sisteHeleMeldekortFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelseAnvist).isPresent();
        assertThat(ytelseAnvist.get()).isEqualTo(nyttMeldekort);
    }

    @Test
    public void skal_finne_meldekort_fra_nyeste_vedtak_når_to_vedtak_har_meldekort_med_samme_periode() {
        YtelseDtoBuilder aapGammelBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        YtelseDtoBuilder aapYtelseNyBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15));

        YtelseAnvistDto meldekortHundre = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusDays(45), SKJÆRINGSTIDSPUNKT.minusDays(31));
        YtelseAnvistDto meldekortFemti = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusDays(45), SKJÆRINGSTIDSPUNKT.minusDays(31));

        YtelseDto gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(meldekortHundre).build();
        YtelseDto nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(meldekortFemti).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse));
        Optional<YtelseAnvistDto> ytelseAnvist = MeldekortUtils.sisteHeleMeldekortFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelseAnvist).isPresent();
        assertThat(ytelseAnvist.get()).isEqualTo(meldekortFemti);
    }

    @Test
    public void skal_ikke_ta_med_meldekort_fra_vedtak_etter_stp() {
        YtelseDtoBuilder aapGammelBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        YtelseDtoBuilder aapYtelseNyBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15));
        YtelseDtoBuilder aapYtelseEtterStpBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15));

        YtelseAnvistDto meldekortGammel = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusDays(45), SKJÆRINGSTIDSPUNKT.minusDays(31));
        YtelseAnvistDto meldekortNytt = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusDays(30), SKJÆRINGSTIDSPUNKT.minusDays(16));
        YtelseAnvistDto meldekortNyest = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusDays(2), SKJÆRINGSTIDSPUNKT.plusDays(12));

        YtelseDto gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(meldekortGammel).build();
        YtelseDto nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(meldekortNytt).build();
        YtelseDto ytelseEtterStp = aapYtelseEtterStpBuilder.leggTilYtelseAnvist(meldekortNyest).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse, ytelseEtterStp));
        Optional<YtelseAnvistDto> ytelseAnvist = MeldekortUtils.sisteHeleMeldekortFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

        assertThat(ytelseAnvist).isPresent();
        assertThat(ytelseAnvist.get()).isEqualTo(meldekortNytt);
    }

    @Test
    public void skalFinneMeldekortVedGittDatoNårMeldekortetFinnes() {
        YtelseDtoBuilder aapGammelBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(5), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        YtelseDtoBuilder aapYtelseNyBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.plusMonths(1));

        YtelseAnvistDto meldekort1 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(6), SKJÆRINGSTIDSPUNKT.minusWeeks(4));
        YtelseAnvistDto meldekort2 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(4), SKJÆRINGSTIDSPUNKT.minusWeeks(2));
        YtelseAnvistDto meldekort3 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(2), SKJÆRINGSTIDSPUNKT.minusWeeks(0));
        YtelseAnvistDto meldekort4 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(10), SKJÆRINGSTIDSPUNKT.minusWeeks(8));
        YtelseAnvistDto gammeltMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(10), SKJÆRINGSTIDSPUNKT.minusWeeks(8));

        YtelseDto gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(gammeltMeldekort).build();
        YtelseDto nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(meldekort1)
                .leggTilYtelseAnvist(meldekort2)
                .leggTilYtelseAnvist(meldekort3)
                .leggTilYtelseAnvist(meldekort4).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse));
        Optional<YtelseAnvistDto> ytelseAnvist = MeldekortUtils.finnMeldekortSomInkludererGittDato(filter, nyYtelse, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER), SKJÆRINGSTIDSPUNKT);

        assertThat(ytelseAnvist).isPresent();
        assertThat(ytelseAnvist.get()).isEqualTo(meldekort3);
    }

    @Test
    public void skalIkkeFinneMeldekortNårMeldekortVedGittDatoIkkeFinnes() {
        YtelseDtoBuilder aapGammelBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(5), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        YtelseDtoBuilder aapYtelseNyBuilder = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.plusMonths(1));

        YtelseAnvistDto meldekort1 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(6), SKJÆRINGSTIDSPUNKT.minusWeeks(4));
        YtelseAnvistDto meldekort2 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(4), SKJÆRINGSTIDSPUNKT.minusWeeks(2));
        YtelseAnvistDto meldekort3 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(2), SKJÆRINGSTIDSPUNKT.minusWeeks(0));
        YtelseAnvistDto meldekort4 = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(10), SKJÆRINGSTIDSPUNKT.minusWeeks(8));
        YtelseAnvistDto gammeltMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusWeeks(10), SKJÆRINGSTIDSPUNKT.minusWeeks(8));

        YtelseDto gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(gammeltMeldekort).build();
        YtelseDto nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(meldekort1)
                .leggTilYtelseAnvist(meldekort2)
                .leggTilYtelseAnvist(meldekort3)
                .leggTilYtelseAnvist(meldekort4).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse));
        Optional<YtelseAnvistDto> ytelseAnvist = MeldekortUtils.finnMeldekortSomInkludererGittDato(filter, nyYtelse, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER), SKJÆRINGSTIDSPUNKT.plusDays(1));

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
