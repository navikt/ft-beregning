package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

public class VurderPeriodeForGraderingTest {

    // månedsbeløp tilsvarende 5,3G
    private static final BigDecimal MÅNEDSBELØP_5_3G = BigDecimal.valueOf(40_000);
    private static final BigDecimal MÅNEDSBELØP_OVER_6G = BigDecimal.valueOf(70_000);
    private static final Refusjonskrav REFUSJONSKRAV_UNDER_6G = new Refusjonskrav(MÅNEDSBELØP_5_3G, DateUtil.TIDENES_BEGYNNELSE, DateUtil.TIDENES_ENDE);
    private static final Refusjonskrav REFUSJONSKRAV_OVER_6G = new Refusjonskrav(MÅNEDSBELØP_OVER_6G, DateUtil.TIDENES_BEGYNNELSE, DateUtil.TIDENES_ENDE);

    @Test
    public void totalRefusjonOver6GEksisterendeAktivitetIngenRefusjonPåDato() {
        // Arrange
        List<Refusjonskrav> refusjonskrav = List.of(REFUSJONSKRAV_OVER_6G);
        ArbeidsforholdOgInntektsmelding andel6G = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(refusjonskrav)
            .medGyldigeRefusjonskrav(refusjonskrav)
            .medAndelsnr(1L) // eksisterende aktivitet
            .build();

        Periode gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), DateUtil.TIDENES_ENDE);
        ArbeidsforholdOgInntektsmelding andelGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of())
            .medAndelsnr(2L) // eksisterende aktivitet
            .build();
        PeriodeModell input = PeriodeModell.builder()
            .medInntektsmeldinger(List.of(andel6G, andelGradering))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> splitResultat = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

        // Assert
        assertGraderingFom(splitResultat, List.of(gradering.getFom()));
    }

    @Test
    public void totalRefusjonOver6GEksisterendeAktivitetIngenRefusjonPåDatoAvsluttetGradering() {
        // Arrange
        List<Refusjonskrav> refusjonskrav = List.of(REFUSJONSKRAV_OVER_6G);
        ArbeidsforholdOgInntektsmelding andel6G = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(refusjonskrav)
            .medGyldigeRefusjonskrav(refusjonskrav)
            .medAndelsnr(1L) // eksisterende aktivitet
            .build();

        Periode gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.JULY, 1));
        ArbeidsforholdOgInntektsmelding andelGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of())
            .medAndelsnr(2L) // eksisterende aktivitet
            .build();
        PeriodeModell input = PeriodeModell.builder()
            .medInntektsmeldinger(List.of(andel6G, andelGradering))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> splitResultat = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

        // Assert
        assertGraderingFom(splitResultat, List.of(gradering.getFom(), gradering.getTom().plusDays(1)));
    }

    @Test
    public void totalRefusjonOver6GEksisterendeAktivitetIngenRefusjonPåDatoAvsluttetGraderingMedTotalRefusjonUnder6GVedEndtGradering() {
        // Arrange
        Refusjonskrav REFUSJONSKRAV_OVER_6G = new Refusjonskrav(MÅNEDSBELØP_OVER_6G, DateUtil.TIDENES_BEGYNNELSE, LocalDate.of(2019, Month.APRIL, 1));
        List<Refusjonskrav> refusjonskrav = List.of(REFUSJONSKRAV_OVER_6G);
        ArbeidsforholdOgInntektsmelding andel6G = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(refusjonskrav)
            .medGyldigeRefusjonskrav(refusjonskrav)
            .medAndelsnr(1L) // eksisterende aktivitet
            .build();

        Periode gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.JULY, 1));
        ArbeidsforholdOgInntektsmelding andelGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of())
            .medAndelsnr(2L) // eksisterende aktivitet
            .build();
        PeriodeModell input = PeriodeModell.builder()
            .medInntektsmeldinger(List.of(andel6G, andelGradering))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> splitResultat = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

        // Assert
        assertGraderingFom(splitResultat, List.of(gradering.getFom()));
    }

    @Test
    public void totalRefusjonOver6GEksisterendeAktivitetIngenRefusjonPåDatoAvsluttetGraderingMedRefusjonskravVedEndtGradering() {
        // Arrange
        List<Refusjonskrav> refusjonskrav = List.of(REFUSJONSKRAV_OVER_6G);
        ArbeidsforholdOgInntektsmelding andel6G = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(refusjonskrav)
            .medGyldigeRefusjonskrav(refusjonskrav)
            .medAndelsnr(1L) // eksisterende aktivitet
            .build();

        Refusjonskrav REFUSJONSKRAV_UNDER_6G = new Refusjonskrav(MÅNEDSBELØP_5_3G, LocalDate.of(2019, Month.MAY, 1), DateUtil.TIDENES_ENDE);
        Periode gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.JULY, 1));
        ArbeidsforholdOgInntektsmelding andelGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medGyldigeRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medAndelsnr(2L) // eksisterende aktivitet
            .build();
        PeriodeModell input = PeriodeModell.builder()
            .medInntektsmeldinger(List.of(andel6G, andelGradering))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> splitResultat = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

        // Assert
        assertGraderingFom(splitResultat, List.of(gradering.getFom()));
    }

    @Test
    public void totalRefusjonUnder6GEksisterendeAktivitetIngenRefusjonPåDato() {
        // Arrange
        ArbeidsforholdOgInntektsmelding andel6G = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medGyldigeRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medAndelsnr(1L) // eksisterende aktivitet
            .build();

        Periode gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), DateUtil.TIDENES_ENDE);
        ArbeidsforholdOgInntektsmelding andelGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of())
            .medAndelsnr(2L) // eksisterende aktivitet
            .build();
        PeriodeModell input = PeriodeModell.builder()
            .medInntektsmeldinger(List.of(andel6G, andelGradering))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

        // Assert
        assertThat(resultatList).isEmpty();
    }

    @Test
    public void totalRefusjonOver6GEksisterendeAktivitetHarRefusjonPåDato() {
        // Arrange
        List<Refusjonskrav> refusjonskrav = List.of(REFUSJONSKRAV_OVER_6G);
        ArbeidsforholdOgInntektsmelding andelUtenGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(refusjonskrav)
            .medGyldigeRefusjonskrav(refusjonskrav)
            .medAndelsnr(1L) // eksisterende aktivitet
            .build();

        Periode gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), DateUtil.TIDENES_ENDE);
        ArbeidsforholdOgInntektsmelding andelGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medGyldigeRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medAndelsnr(2L) // eksisterende aktivitet
            .build();
        PeriodeModell input = PeriodeModell.builder()
            .medInntektsmeldinger(List.of(andelUtenGradering, andelGradering))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

        // Assert
        assertThat(resultatList).isEmpty();
    }

    @Test
    public void totalRefusjonUnder6GEksisterendeAktivitetHarRefusjonPåDato() {
        // Arrange
        ArbeidsforholdOgInntektsmelding andelUtenGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medGyldigeRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medAndelsnr(1L) // eksisterende aktivitet
            .build();

        Periode gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), DateUtil.TIDENES_ENDE);
        ArbeidsforholdOgInntektsmelding andelGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medGyldigeRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medAndelsnr(2L) // eksisterende aktivitet
            .build();
        PeriodeModell input = PeriodeModell.builder()
            .medInntektsmeldinger(List.of(andelUtenGradering, andelGradering))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

        // Assert
        assertThat(resultatList).isEmpty();
    }

    @Test
    public void totalRefusjonOver6GNyAktivitetIngenRefusjonPåDato() {
        // Arrange
        ArbeidsforholdOgInntektsmelding andel6G = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of(REFUSJONSKRAV_OVER_6G))
            .medGyldigeRefusjonskrav(List.of(REFUSJONSKRAV_OVER_6G))
            .medAndelsnr(1L) // eksisterende aktivitet
            .build();

        Periode gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), DateUtil.TIDENES_ENDE);
        ArbeidsforholdOgInntektsmelding andelGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of())
            .medAndelsnr(null) // ny aktivitet
            .build();
        PeriodeModell input = PeriodeModell.builder()
            .medInntektsmeldinger(List.of(andel6G, andelGradering))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

        // Assert
        assertGraderingFom(resultatList, List.of(gradering.getFom()));
    }

    @Test
    public void totalRefusjonUnder6GNyAktivitetIngenRefusjonPåDato() {
        // Arrange
        List<Refusjonskrav> refusjonskrav = List.of(REFUSJONSKRAV_UNDER_6G);
        ArbeidsforholdOgInntektsmelding andel6G = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(refusjonskrav)
            .medAndelsnr(1L) // eksisterende aktivitet
            .build();

        Periode gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), DateUtil.TIDENES_ENDE);
        ArbeidsforholdOgInntektsmelding andelGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of())
            .medAndelsnr(null) // ny aktivitet
            .build();
        PeriodeModell input = PeriodeModell.builder()
            .medInntektsmeldinger(List.of(andel6G, andelGradering))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

        // Assert
        assertGraderingFom(resultatList, List.of(gradering.getFom()));
    }

    @Test
    public void totalRefusjonOver6GNyAktivitetHarRefusjonPåDato() {
        // Arrange
        List<Refusjonskrav> refusjonskrav = List.of(REFUSJONSKRAV_OVER_6G);
        ArbeidsforholdOgInntektsmelding andelUtenGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(refusjonskrav)
            .medGyldigeRefusjonskrav(refusjonskrav)
            .medAndelsnr(1L) // eksisterende aktivitet
            .build();

        Periode gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), DateUtil.TIDENES_ENDE);
        ArbeidsforholdOgInntektsmelding andelGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medGyldigeRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medAndelsnr(null) // ny aktivitet
            .build();
        PeriodeModell input = PeriodeModell.builder()
            .medInntektsmeldinger(List.of(andelUtenGradering, andelGradering))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

        // Assert
        assertThat(resultatList).isEmpty();
    }

    @Test
    public void totalRefusjonUnder6GNyAktivitetHarRefusjonPåDato() {
        // Arrange
        ArbeidsforholdOgInntektsmelding andelUtenGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medGyldigeRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medAndelsnr(1L) // eksisterende aktivitet
            .build();

        Periode gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), DateUtil.TIDENES_ENDE);
        ArbeidsforholdOgInntektsmelding andelGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medGyldigeRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medAndelsnr(null) // ny aktivitet
            .build();
        PeriodeModell input = PeriodeModell.builder()
            .medInntektsmeldinger(List.of(andelUtenGradering, andelGradering))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

        // Assert
        assertThat(resultatList).isEmpty();
    }

    @Test
    public void totalRefusjonUnder6GNyAktivitetHarIkkeRefusjonPåDato() {
        // Arrange
        ArbeidsforholdOgInntektsmelding andelUtenGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medGyldigeRefusjonskrav(List.of(REFUSJONSKRAV_UNDER_6G))
            .medAndelsnr(1L) // eksisterende aktivitet
            .build();

        Periode gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.MARCH, 8));
        ArbeidsforholdOgInntektsmelding andelGradering = ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(List.of())
            .medAndelsnr(null) // ny aktivitet
            .build();
        PeriodeModell input = PeriodeModell.builder()
            .medInntektsmeldinger(List.of(andelUtenGradering, andelGradering))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

        // Assert
        assertGraderingFom(resultatList, List.of(gradering.getFom(), gradering.getTom().plusDays(1)));
    }

    private void assertGraderingFom(List<PeriodeSplittData> resultatList, List<LocalDate> foms) {
        assertThat(resultatList.size()).isEqualTo(foms.size());
        for (int i = 0; i < resultatList.size(); i++) {
            PeriodeSplittData resultat = resultatList.get(i);
            assertThat(resultat.getFom()).as("fom").isEqualTo(foms.get(i));
            if (i == 0) {
                assertThat(resultat.getPeriodeÅrsak()).as("periodeÅrsak").isEqualTo(PeriodeÅrsak.GRADERING);
            } else {
                assertThat(resultat.getPeriodeÅrsak()).as("periodeÅrsak").isEqualTo(PeriodeÅrsak.GRADERING_OPPHØRER);
            }
        }
    }
}
