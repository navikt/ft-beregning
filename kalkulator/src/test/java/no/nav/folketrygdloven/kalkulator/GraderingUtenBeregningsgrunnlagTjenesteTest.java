package no.nav.folketrygdloven.kalkulator;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.ytelse.fp.GraderingUtenBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

class GraderingUtenBeregningsgrunnlagTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final String ORGNR = "915933149";

    private Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);

    private BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag();


    @Test
    void skalIkkeFåAvklaringsbehovArbeidstakerMedBG() {
        // Arrange
        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, BigDecimal.TEN);


        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(AktivitetGradering.INGEN_GRADERING);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isFalse();
    }

    @Test
    void skalIkkeFåAvklaringsbehovArbeidstakerUtenGradering() {
        // Arrange
        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.ARBEIDSTAKER, BigDecimal.ZERO);


        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(AktivitetGradering.INGEN_GRADERING);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isFalse();
    }

    @Test
    void skalFåAvklaringsbehovArbeidstakerNårGraderingOgHarIkkeBG() {
        // Arrange
        var skjæringstidspunkt = SKJÆRINGSTIDSPUNKT;
        var graderingFom = skjæringstidspunkt;
        var graderingTom = skjæringstidspunkt.plusMonths(4);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medArbeidsgiver(arbeidsgiver)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(skjæringstidspunkt, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.ARBEIDSTAKER, BigDecimal.ZERO, SKJÆRINGSTIDSPUNKT.minusYears(1), TIDENES_ENDE);

        // Act

        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(aktivitetGradering);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isTrue();
    }

    @Test
    void skalFåAvklaringsbehovSelvstendigNårGraderingOgHarIkkeBG() {
        // Arrange
        var skjæringstidspunkt = SKJÆRINGSTIDSPUNKT;
        var graderingFom = skjæringstidspunkt;
        var graderingTom = skjæringstidspunkt.plusMonths(4);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, BigDecimal.ZERO);

        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(aktivitetGradering);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isTrue();
    }

    @Test
    void skalIkkeFåAvklaringsbehovSelvstendigNårGraderingOgHarBG() {
        // Arrange
        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, BigDecimal.TEN);


        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(AktivitetGradering.INGEN_GRADERING);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isFalse();
    }

    @Test
    void skalIkkeFåAvklaringsbehovSelvstendigNårIkkeGradering() {
        // Arrange
        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, BigDecimal.ZERO);


        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(AktivitetGradering.INGEN_GRADERING);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isFalse();
    }

    @Test
    void skalFåAvklaringsbehovFrilanserNårGraderingOgHarIkkeBG() {
        // Arrange
        var skjæringstidspunkt = SKJÆRINGSTIDSPUNKT;
        var graderingFom = skjæringstidspunkt;
        var graderingTom = skjæringstidspunkt.plusMonths(6);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.FRILANSER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.FRILANSER, BigDecimal.ZERO);

        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(aktivitetGradering);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isTrue();
    }

    @Test
    void skalIkkeFåAvklaringsbehovFrilanserNårGraderingOgHarBG() {
        // Arrange
        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.FRILANSER, BigDecimal.TEN);


        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(AktivitetGradering.INGEN_GRADERING);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isFalse();
    }

    @Test
    void skalIkkeFåAvklaringsbehovFrilanserNårIkkeGradering() {
        // Arrange
        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.FRILANSER, BigDecimal.ZERO);


        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(AktivitetGradering.INGEN_GRADERING);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isFalse();
    }

    @Test
    void skalIkkeFåAvklaringsbehovFrilanserNårGraderingUtenforPeriodeUtenBeregningsgrunnlag() {
        // Arrange
        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(3));
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.FRILANSER, BigDecimal.ZERO);


        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(AktivitetGradering.INGEN_GRADERING);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isFalse();
    }

    @Test
    void skalIkkeFåAvklaringsbehovSNNårGraderingUtenforPeriodeUtenBeregningsgrunnlag() {
        // Arrange
        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT.plusMonths(2), null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.FRILANSER, BigDecimal.ZERO);


        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(AktivitetGradering.INGEN_GRADERING);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isFalse();
    }

    @Test
    void skalIkkeFåAvklaringsbehovArbeidstakerNårGraderingUtenforPeriodeUtenBeregningsgrunnlag2() {
        // Arrange
        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(3));
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.ARBEIDSTAKER, BigDecimal.ZERO);


        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(AktivitetGradering.INGEN_GRADERING);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isFalse();
    }

    @Test
    void skalIkkeFåAvklaringsbehovNårAAP() {
        // Arrange
        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.ARBEIDSAVKLARINGSPENGER, BigDecimal.ZERO);

        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(AktivitetGradering.INGEN_GRADERING);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isFalse();
    }

    @Test
    void skal_ikke_finne_andel_når_det_er_sn_med_gradering_med_inntekt_på_grunnlag() {
        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, BigDecimal.TEN);


        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(AktivitetGradering.INGEN_GRADERING);
        var andeler = finnAndelerMedGraderingUtenBG(beregningsgrunnlag, AktivitetGradering.INGEN_GRADERING);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isFalse();
        assertThat(andeler).isEmpty();
    }

    @Test
    void skal_ikke_finne_andel_når_det_er_gradering_men_ikke_fastsatt_redusert_pr_år() {
        // Arrange
        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.ARBEIDSTAKER, null);


        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(AktivitetGradering.INGEN_GRADERING);
        var andeler = finnAndelerMedGraderingUtenBG(beregningsgrunnlag, AktivitetGradering.INGEN_GRADERING);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isFalse();
        assertThat(andeler).isEmpty();
    }

    @Test
    void skal_ikke_finne_andel_når_det_er_gradering_men_fastsatt_grunnlag_over_0_redusert_pr_år() {
        // Arrange
        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.ARBEIDSTAKER, BigDecimal.TEN);


        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(AktivitetGradering.INGEN_GRADERING);
        var andeler = finnAndelerMedGraderingUtenBG(beregningsgrunnlag, AktivitetGradering.INGEN_GRADERING);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isFalse();
        assertThat(andeler).isEmpty();
    }

    @Test
    void skal_finne_andel_når_det_er_gradering() {
        // Arrange
        var skjæringstidspunkt = SKJÆRINGSTIDSPUNKT;
        var graderingFom = skjæringstidspunkt;
        var graderingTom = skjæringstidspunkt.plusMonths(4);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medArbeidsgiver(arbeidsgiver)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.ARBEIDSTAKER, BigDecimal.ZERO, SKJÆRINGSTIDSPUNKT.minusYears(1), TIDENES_ENDE);

        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(aktivitetGradering);
        var andeler = finnAndelerMedGraderingUtenBG(beregningsgrunnlag, aktivitetGradering);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isTrue();
        assertThat(andeler).hasSize(1);
    }

    @Test
    void skal_finne_andel_når_det_er_sn_med_gradering_uten_inntekt_på_grunnlag() {
        // Arrange
        var skjæringstidspunkt = SKJÆRINGSTIDSPUNKT;
        var graderingFom = skjæringstidspunkt;
        var graderingTom = skjæringstidspunkt.plusMonths(6);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(skjæringstidspunkt, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, BigDecimal.ZERO);

        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(aktivitetGradering);
        var andeler = finnAndelerMedGraderingUtenBG(beregningsgrunnlag, aktivitetGradering);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isTrue();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    @Test
    void skal_finne_riktig_andel_når_det_er_flere_med_gradering_men_kun_en_mangler_inntekt() {
        // Arrange
        var skjæringstidspunkt = SKJÆRINGSTIDSPUNKT;
        var graderingFom = skjæringstidspunkt;
        var graderingTom = skjæringstidspunkt.plusMonths(4);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, BigDecimal.ZERO);

        // Arrange AT andel
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.ARBEIDSTAKER, BigDecimal.TEN);


        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(aktivitetGradering);
        var andeler = finnAndelerMedGraderingUtenBG(beregningsgrunnlag, aktivitetGradering);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isTrue();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    @Test
    void skal_gi_false_når_to_andeler_i_graderingsperiode_men_ikke_0_på_andel_som_skal_graderes() {
        // Arrange
        var skjæringstidspunkt = SKJÆRINGSTIDSPUNKT;
        var graderingFom = skjæringstidspunkt;
        var graderingTom = skjæringstidspunkt.plusMonths(4);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        var beregningsgrunnlagPeriode = lagBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null);
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, BigDecimal.TEN);

        // Arrange AT andel
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, AktivitetStatus.FRILANSER, BigDecimal.ZERO);


        // Act
        var harAndelerMedGraderingUtenGrunnlag = harAndelerMedGraderingUtenGrunnlag(aktivitetGradering);

        // Assert
        assertThat(harAndelerMedGraderingUtenGrunnlag).isFalse();
    }

    private List<BeregningsgrunnlagPrStatusOgAndelDto> finnAndelerMedGraderingUtenBG(BeregningsgrunnlagDto beregningsgrunnlag, AktivitetGradering aktivitetGradering) {
        return GraderingUtenBeregningsgrunnlagTjeneste.finnAndelerMedGraderingUtenBG(beregningsgrunnlag, aktivitetGradering);
    }

    private boolean harAndelerMedGraderingUtenGrunnlag(AktivitetGradering aktivitetGradering) {
        return !GraderingUtenBeregningsgrunnlagTjeneste.finnAndelerMedGraderingUtenBG(beregningsgrunnlag, aktivitetGradering).isEmpty();
    }

    private BeregningsgrunnlagPeriodeDto lagBeregningsgrunnlagPeriode(LocalDate periodeFom, LocalDate periodeTom) {
        return BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(periodeFom, periodeTom).build(beregningsgrunnlag);
    }

    private void lagBeregningsgrunnlagAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, AktivitetStatus aktivitetStatus, BigDecimal redusertPrÅr) {
        lagBeregningsgrunnlagAndel(beregningsgrunnlagPeriode, aktivitetStatus, redusertPrÅr, null, null);
    }

    private void lagBeregningsgrunnlagAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, AktivitetStatus aktivitetStatus, BigDecimal redusertPrÅr, LocalDate arbeidsperiodeFom, LocalDate arbeidsperiodeTom) {

        BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(aktivitetStatus).build(beregningsgrunnlag);
        var bgAndelBuilder = BGAndelArbeidsforholdDto.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsperiodeFom(arbeidsperiodeFom)
                .medArbeidsperiodeTom(arbeidsperiodeTom);
        var andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(aktivitetStatus)
                .medRedusertPrÅr(Beløp.fra(redusertPrÅr));

        if (aktivitetStatus.erArbeidstaker()) {
            andelBuilder.medBGAndelArbeidsforhold(bgAndelBuilder);
        }

        andelBuilder.build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        return BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .build();
    }

}
