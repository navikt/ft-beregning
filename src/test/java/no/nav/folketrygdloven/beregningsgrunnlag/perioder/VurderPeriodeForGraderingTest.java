package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGraderingImpl;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
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

    @Test
    public void skalLageToPerioderNårBruttoForAndelSnErNullOgMedOpphørAvGraderingISammePeriode() {
        // Arrange
        Periode graderingsPeriode = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.MARCH, 8));
        Gradering gradering = new Gradering(graderingsPeriode, BigDecimal.TEN);

        AndelGraderingImpl andelGradering = AndelGraderingImpl.builder()
            .medAktivitetStatus(AktivitetStatusV2.SN)
            .medGraderinger(List.of(gradering))
            .medEksisterendeAktivitetFraDato(graderingsPeriode.getFom())
            .build();
        BruttoBeregningsgrunnlag bruttoBeregningsgrunnlag = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.SN, null, BigDecimal.ZERO);

        PeriodisertBruttoBeregningsgrunnlag periodisertBruttoBeregningsgrunnlag = PeriodisertBruttoBeregningsgrunnlag.builder()
            .leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag)
            .medPeriode(new Periode(LocalDate.of(2019, Month.JANUARY, 20), null))
            .build();

        PeriodeModell input = PeriodeModell.builder()
            .medAndelGraderinger(List.of(andelGradering))
            .medPeriodisertBruttoBeregningsgrunnlag(List.of(periodisertBruttoBeregningsgrunnlag))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, graderingsPeriode);

        // Assert
        assertGraderingFom(resultatList, List.of(graderingsPeriode.getFom(), graderingsPeriode.getTom().plusDays(1)));
    }

    @Test
    public void skalIkkeLagePerioderNårBruttoForAndelSnIkkeErNullOgMedOpphørAvGraderingISammePeriode() {
        // Arrange
        Periode graderingsPeriode = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.MARCH, 8));
        Gradering gradering = new Gradering(graderingsPeriode, BigDecimal.TEN);
        AndelGraderingImpl andelGradering = AndelGraderingImpl.builder()
            .medAktivitetStatus(AktivitetStatusV2.SN)
            .medGraderinger(List.of(gradering))
            .medEksisterendeAktivitetFraDato(graderingsPeriode.getFom())
            .build();

        BruttoBeregningsgrunnlag bruttoBeregningsgrunnlag = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.SN, null, BigDecimal.valueOf(200_000));
        PeriodisertBruttoBeregningsgrunnlag periodisertBruttoBeregningsgrunnlag = PeriodisertBruttoBeregningsgrunnlag.builder()
            .leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag)
            .medPeriode(new Periode(LocalDate.of(2019, Month.JANUARY, 20), null))
            .build();

        PeriodeModell input = PeriodeModell.builder()
            .medAndelGraderinger(List.of(andelGradering))
            .medPeriodisertBruttoBeregningsgrunnlag(List.of(periodisertBruttoBeregningsgrunnlag))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, graderingsPeriode);

        // Assert
        assertThat(resultatList).isEmpty();
    }

    @Test
    public void skalLageToPerioderNårBruttoForAndelAtErNullOgToArbeidsforholdHosPrivatpersonOgMedOpphørAvGraderingISammePeriode() {
        // Arrange
        Periode graderingsPeriode = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.MARCH, 8));
        Gradering gradering = new Gradering(graderingsPeriode, BigDecimal.TEN);

        Arbeidsforhold arbeidsforhold1 = Arbeidsforhold.nyttArbeidsforholdHosPrivatperson("5454", "1111");
        Arbeidsforhold arbeidsforhold2 = Arbeidsforhold.nyttArbeidsforholdHosPrivatperson("5454", "2222");
        ArbeidsforholdOgInntektsmelding andelMedGradering = lagArbeidsforholdOgInntektsmelding(1L, List.of(), List.of(gradering), arbeidsforhold1);
        ArbeidsforholdOgInntektsmelding andelUtenGradering = lagArbeidsforholdOgInntektsmelding(2L, List.of(), List.of(), arbeidsforhold2);
        BruttoBeregningsgrunnlag bruttoBeregningsgrunnlag1 = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.AT, arbeidsforhold1, BigDecimal.ZERO);
        BruttoBeregningsgrunnlag bruttoBeregningsgrunnlag2 = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.AT, arbeidsforhold2, BigDecimal.valueOf(100000));

        PeriodisertBruttoBeregningsgrunnlag periodisertBruttoBeregningsgrunnlag = PeriodisertBruttoBeregningsgrunnlag.builder()
            .leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag1)
            .leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag2)
            .medPeriode(new Periode(LocalDate.of(2019, Month.JANUARY, 20), null))
            .build();

        PeriodeModell input = PeriodeModell.builder()
            .medInntektsmeldinger(List.of(andelUtenGradering, andelMedGradering))
            .medPeriodisertBruttoBeregningsgrunnlag(List.of(periodisertBruttoBeregningsgrunnlag))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> resultatList = VurderPeriodeForGradering.vurder(input, andelMedGradering, graderingsPeriode);

        // Assert
        assertGraderingFom(resultatList, List.of(graderingsPeriode.getFom(), graderingsPeriode.getTom().plusDays(1)));
    }

    @Test
    public void skalLageToPerioderNårBruttoForAndelAtErNullOgBrukerAnsattIOrganisasjonOgMedOpphørAvGraderingISammePeriode() {
        // Arrange
        Periode graderingsPeriode = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.MARCH, 8));
        Gradering gradering = new Gradering(graderingsPeriode, BigDecimal.TEN);

        Arbeidsforhold arbeidsforhold1 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("5454", "1111");
        Arbeidsforhold arbeidsforhold2 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("5454", "2222");
        ArbeidsforholdOgInntektsmelding andelMedGradering = lagArbeidsforholdOgInntektsmelding(1L, List.of(), List.of(gradering), arbeidsforhold1);
        ArbeidsforholdOgInntektsmelding andelUtenGradering = lagArbeidsforholdOgInntektsmelding(2L, List.of(), List.of(), arbeidsforhold2);
        BruttoBeregningsgrunnlag bruttoBeregningsgrunnlag1 = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.AT, arbeidsforhold1, BigDecimal.ZERO);
        BruttoBeregningsgrunnlag bruttoBeregningsgrunnlag2 = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.AT, arbeidsforhold2, BigDecimal.valueOf(100000));

        PeriodisertBruttoBeregningsgrunnlag periodisertBruttoBeregningsgrunnlag = PeriodisertBruttoBeregningsgrunnlag.builder()
            .leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag1)
            .leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag2)
            .medPeriode(new Periode(LocalDate.of(2019, Month.JANUARY, 20), null))
            .build();

        PeriodeModell input = PeriodeModell.builder()
            .medInntektsmeldinger(List.of(andelUtenGradering, andelMedGradering))
            .medPeriodisertBruttoBeregningsgrunnlag(List.of(periodisertBruttoBeregningsgrunnlag))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> resultatList = VurderPeriodeForGradering.vurder(input, andelMedGradering, graderingsPeriode);

        // Assert
        assertGraderingFom(resultatList, List.of(graderingsPeriode.getFom(), graderingsPeriode.getTom().plusDays(1)));
    }

    @Test
    public void skalLageEnPerioderNårBruttoForAndelFlErNullOgMedOpphørAvGraderingIAnnenPeriode() {
        // Arrange
        Periode graderingsPeriode = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.MARCH, 8));
        Gradering gradering = new Gradering(graderingsPeriode, BigDecimal.TEN);
        AndelGraderingImpl andelGradering = AndelGraderingImpl.builder()
            .medAktivitetStatus(AktivitetStatusV2.FL)
            .medGraderinger(List.of(gradering))
            .medEksisterendeAktivitetFraDato(graderingsPeriode.getFom())
            .build();

        BruttoBeregningsgrunnlag bruttoBeregningsgrunnlagBruttoErNull = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.FL, null, BigDecimal.ZERO);
        BruttoBeregningsgrunnlag bruttoBeregningsgrunnlagBruttoIkkeNull = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.FL, null, BigDecimal.valueOf(100_000));

        PeriodisertBruttoBeregningsgrunnlag periodisertBruttoBeregningsgrunnlag1 = PeriodisertBruttoBeregningsgrunnlag.builder()
            .leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlagBruttoErNull)
            .medPeriode(new Periode(LocalDate.of(2019, Month.JANUARY, 20), graderingsPeriode.getFom().plusDays(2)))
            .build();

        PeriodisertBruttoBeregningsgrunnlag periodisertBruttoBeregningsgrunnlag2 = PeriodisertBruttoBeregningsgrunnlag.builder()
            .leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlagBruttoIkkeNull)
            .medPeriode(new Periode(graderingsPeriode.getFom().plusDays(3), null))
            .build();

        PeriodeModell input = PeriodeModell.builder()
            .medAndelGraderinger(List.of(andelGradering))
            .medPeriodisertBruttoBeregningsgrunnlag(List.of(periodisertBruttoBeregningsgrunnlag1, periodisertBruttoBeregningsgrunnlag2))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        // Act
        List<PeriodeSplittData> resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, graderingsPeriode);

        // Assert
        assertGraderingFom(resultatList, List.of(graderingsPeriode.getFom()));
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

    private ArbeidsforholdOgInntektsmelding lagArbeidsforholdOgInntektsmelding(Long andelsnr, List<Refusjonskrav> refusjonskrav, List<Gradering> gradering, Arbeidsforhold arbeidsforhold){
        return ArbeidsforholdOgInntektsmelding.builder()
            .medRefusjonskrav(refusjonskrav)
            .medGyldigeRefusjonskrav(refusjonskrav)
            .medGraderinger(gradering)
            .medAndelsnr(andelsnr)
            .medArbeidsforhold(arbeidsforhold)
            .build();
    }

    private BruttoBeregningsgrunnlag lagBruttoBeregningsgrunnlag(AktivitetStatusV2 aktivitetStatus, Arbeidsforhold arbeidsforhold, BigDecimal bruttoBeregningsgrunnlag){
        return BruttoBeregningsgrunnlag.builder()
            .medAktivitetStatus(aktivitetStatus)
            .medArbeidsforhold(arbeidsforhold)
            .medBruttoPrÅr(bruttoBeregningsgrunnlag)
            .build();
    }
}
