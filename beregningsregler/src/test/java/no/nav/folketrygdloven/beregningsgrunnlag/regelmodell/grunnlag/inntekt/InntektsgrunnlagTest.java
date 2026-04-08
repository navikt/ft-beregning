package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;


class InntektsgrunnlagTest {

    private static final LocalDate STP = LocalDate.of(2018, Month.JANUARY, 15);
    private static final BigDecimal ANTALL_VIRKEDAGER_I_ÅR = BigDecimal.valueOf(260);

    private Inntektsgrunnlag inntektsgrunnlag;

    @BeforeEach
    void setUp() {
        inntektsgrunnlag = new Inntektsgrunnlag();
    }

    @Test
    void skal_beregne_for_DP_fra_ytelse_med_og_uten_faktor() {
        var dagsats = BigDecimal.valueOf(1200);
        var utbetalingsfaktor = BigDecimal.valueOf(0.8);
        leggTilPeriodeinntektForDPfraYtelse(dagsats, utbetalingsfaktor);

        var resultatMedFaktor = inntektsgrunnlag.regnUtSnittInntektForDPellerAAP(AktivitetStatus.SP_AV_DP, STP, true);
        var resultatUtenFaktor = inntektsgrunnlag.regnUtSnittInntektForDPellerAAP(AktivitetStatus.SP_AV_DP, STP, false);

        assertThat(resultatMedFaktor.dagsats()).isEqualByComparingTo(dagsats);
        assertThat(resultatMedFaktor.beregnetPrÅr()).isEqualByComparingTo(dagsats.multiply(ANTALL_VIRKEDAGER_I_ÅR).multiply(utbetalingsfaktor));
        assertThat(resultatUtenFaktor.dagsats()).isEqualByComparingTo(dagsats);
        assertThat(resultatUtenFaktor.beregnetPrÅr()).isEqualByComparingTo(dagsats.multiply(ANTALL_VIRKEDAGER_I_ÅR));
    }

    @Test
    void skal_bruke_enkeltdager_når_DP_fra_ytelse_men_ingen_ytelse_vedtak() {
        leggTilPeriodeinntektFraYtelseEnkeltdag(1000, førStp(3));
        leggTilPeriodeinntektFraYtelseEnkeltdag(1000, førStp(4));
        leggTilPeriodeinntektFraYtelseEnkeltdag(1000, førStp(5));
        leggTilPeriodeinntektFraYtelseEnkeltdag(1000, førStp(10));
        leggTilPeriodeinntektFraYtelseEnkeltdag(1000, førStp(11));

        var resultat = inntektsgrunnlag.regnUtSnittInntektForDPellerAAP(AktivitetStatus.SP_AV_DP, STP, false);

        assertThat(resultat.dagsats()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(resultat.beregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(500).multiply(ANTALL_VIRKEDAGER_I_ÅR));
    }

    @Test
    void skal_kun_bruke_inntekter_innenfor_beregningsperioden() {
        leggTilPeriodeinntektFraYtelseEnkeltdag(20, førStp(17));
        leggTilPeriodeinntektFraYtelseEnkeltdag(20, førStp(18));

        leggTilPeriodeinntektFraYtelseEnkeltdag(2000, førStp(3));
        leggTilPeriodeinntektFraYtelseEnkeltdag(2000, førStp(4));
        leggTilPeriodeinntektFraYtelseEnkeltdag(2000, førStp(5));
        leggTilPeriodeinntektFraYtelseEnkeltdag(2000, førStp(10));
        leggTilPeriodeinntektFraYtelseEnkeltdag(2000, førStp(11));

        leggTilPeriodeinntektFraYtelseEnkeltdag(9999, STP.plusDays(1));
        leggTilPeriodeinntektFraYtelseEnkeltdag(9999, STP.plusDays(2));

        var resultat = inntektsgrunnlag.regnUtSnittInntektForDPellerAAP(AktivitetStatus.DP, STP, false);

        assertThat(resultat.dagsats()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(resultat.beregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(260000));
    }

    @Test
    void skal_beregne_snitt_med_varierende_dagsats_og_utbetalingsfaktor() {
        leggTilPeriodeinntektFraYtelseEnkeltdag(1611, førStp(3), 1);
        leggTilPeriodeinntektFraYtelseEnkeltdag(1611, førStp(4), 1);
        leggTilPeriodeinntektFraYtelseEnkeltdag(1611, førStp(5), 0.5);
        leggTilPeriodeinntektFraYtelseEnkeltdag(2147, førStp(10), 0.5);
        leggTilPeriodeinntektFraYtelseEnkeltdag(2147, førStp(11), 0.5);

        var resultat = inntektsgrunnlag.regnUtSnittInntektForDPellerAAP(AktivitetStatus.DP, STP, true);

        assertThat(resultat.dagsats()).isEqualByComparingTo(BigDecimal.valueOf(912.7));
        assertThat(resultat.beregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(83055.7));
    }

    @Test
    void skal_kaste_exception_når_ingen_inntekter_finnes() {
        assertThatThrownBy(() -> inntektsgrunnlag.regnUtSnittInntektForDPellerAAP(AktivitetStatus.DP, STP, false)).isInstanceOf(
            NoSuchElementException.class);
    }

    private LocalDate førStp(int dager) {
        return STP.minusDays(dager);
    }

    private void leggTilPeriodeinntektForDPfraYtelse(BigDecimal dagsats, BigDecimal utbetalingsfaktor) {
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.YTELSE_VEDTAK)
            .medDag(STP)
            .medInntekt(dagsats)
            .medUtbetalingsfaktor(utbetalingsfaktor)
            .medInntektskategori(Inntektskategori.DAGPENGER)
            .build());
    }

    private void leggTilPeriodeinntektFraYtelseEnkeltdag(int dagsats, LocalDate dag) {
        leggTilPeriodeinntektFraYtelseEnkeltdag(dagsats, dag, 1);
    }

    private void leggTilPeriodeinntektFraYtelseEnkeltdag(int dagsats, LocalDate dag, double utbetalingsfaktor) {
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
            .medDag(dag)
            .medInntekt(BigDecimal.valueOf(dagsats))
            .medUtbetalingsfaktor(BigDecimal.valueOf(utbetalingsfaktor))
            .medInntektskategori(Inntektskategori.DAGPENGER)
            .build());
    }
}
