package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

class ErHøyerePrioriterteAndelerBruttoMinst6GTest {
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90_000);
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, Month.FEBRUARY, 14);

    private static final BigDecimal MÅNEDSBELØP_UNDER_6G = BigDecimal.valueOf(40_000);
    private static final BigDecimal MÅNEDSBELØP_PÅ_6G = BigDecimal.valueOf(45_000);
    private static final BigDecimal MÅNEDSBELØP_OVER_6G = BigDecimal.valueOf(70_000);

    @Test
    void gradert_SN_selvstendigErLaverePrioritertEnnFL() {
        // Arrange
        var periodisertBg = PeriodisertBruttoBeregningsgrunnlag.builder()
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, DateUtil.TIDENES_ENDE))
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.SN)
                .medBruttoPrÅr(MÅNEDSBELØP_UNDER_6G.multiply(BigDecimal.valueOf(12)))
                .build())
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.FL)
                .medBruttoPrÅr(MÅNEDSBELØP_OVER_6G.multiply(BigDecimal.valueOf(12)))
                .build())
            .build();

        var gradering = new Gradering(new Periode(LocalDate.of(2019, Month.MARCH, 1), DateUtil.TIDENES_ENDE));
        var andelGradering = AndelGradering.builder()
            .medAktivitetStatus(AktivitetStatusV2.SN)
            .medGraderinger(List.of(gradering))
            .build();

        // Act
        var resultat = ErHøyerePrioriterteAndelerBruttoMinst6G.vurder(GRUNNBELØP, periodisertBg, andelGradering);

        // Assert
        assertThat(resultat).isTrue();
    }

    @Test
    void gradert_frilans_frilansErHøyerePrioritertEnnSN() {
        // Arrange
        var periodisertBg = PeriodisertBruttoBeregningsgrunnlag.builder()
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, DateUtil.TIDENES_ENDE))
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.SN)
                .medBruttoPrÅr(MÅNEDSBELØP_UNDER_6G.multiply(BigDecimal.valueOf(12)))
                .build())
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.FL)
                .medBruttoPrÅr(MÅNEDSBELØP_PÅ_6G.multiply(BigDecimal.valueOf(12)))
                .build())
            .build();

        var gradering = new Gradering(new Periode(LocalDate.of(2019, Month.MARCH, 1), DateUtil.TIDENES_ENDE));
        var andelGradering = AndelGradering.builder()
            .medAktivitetStatus(AktivitetStatusV2.FL)
            .medGraderinger(List.of(gradering))
            .build();

        // Act
        var resultat = ErHøyerePrioriterteAndelerBruttoMinst6G.vurder(GRUNNBELØP, periodisertBg, andelGradering);

        // Assert
        assertThat(resultat).isFalse();
    }

    @Test
    void gradert_frilans_frilansErLaverePrioritertEnnAT() {
        // Arrange
        var periodisertBg = PeriodisertBruttoBeregningsgrunnlag.builder()
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, DateUtil.TIDENES_ENDE))
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.AT)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("orgnr"))
                .medBruttoPrÅr(MÅNEDSBELØP_PÅ_6G.multiply(BigDecimal.valueOf(12)))
                .build())
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.FL)
                .medBruttoPrÅr(MÅNEDSBELØP_UNDER_6G.multiply(BigDecimal.valueOf(12)))
                .build())
            .build();

        var gradering = new Gradering(new Periode(LocalDate.of(2019, Month.MARCH, 1), DateUtil.TIDENES_ENDE));
        var andelGradering = AndelGradering.builder()
            .medAktivitetStatus(AktivitetStatusV2.FL)
            .medGraderinger(List.of(gradering))
            .build();

        // Act
        var resultat = ErHøyerePrioriterteAndelerBruttoMinst6G.vurder(GRUNNBELØP, periodisertBg, andelGradering);

        // Assert
        assertThat(resultat).isTrue();
    }

    @Test
    void gradert_frilans_BGUnder6G() {
        // Arrange
        var periodisertBg = PeriodisertBruttoBeregningsgrunnlag.builder()
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, DateUtil.TIDENES_ENDE))
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.AT)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("orgnr"))
                .medBruttoPrÅr(MÅNEDSBELØP_UNDER_6G.multiply(BigDecimal.valueOf(12)))
                .build())
            .leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag.builder()
                .medAktivitetStatus(AktivitetStatusV2.FL)
                .medBruttoPrÅr(MÅNEDSBELØP_UNDER_6G.multiply(BigDecimal.valueOf(12)))
                .build())
            .build();

        var gradering = new Gradering(new Periode(LocalDate.of(2019, Month.MARCH, 1), DateUtil.TIDENES_ENDE));
        var andelGradering = AndelGradering.builder()
            .medAktivitetStatus(AktivitetStatusV2.FL)
            .medGraderinger(List.of(gradering))
            .build();

        // Act
        var resultat = ErHøyerePrioriterteAndelerBruttoMinst6G.vurder(GRUNNBELØP, periodisertBg, andelGradering);

        // Assert
        assertThat(resultat).isFalse();
    }
}
