package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

class VurderPeriodeForGraderingTest {

	// månedsbeløp tilsvarende 5,3G
	private static final BigDecimal MÅNEDSBELØP_5_3G = BigDecimal.valueOf(40_000);
	private static final BigDecimal MÅNEDSBELØP_OVER_6G = BigDecimal.valueOf(70_000);
	private static final BigDecimal ÅRSBBELØP_OVER_6G = MÅNEDSBELØP_OVER_6G.multiply(BigDecimal.valueOf(12));
	private static final BigDecimal ÅRSBBELØP_OVER_3G = MÅNEDSBELØP_5_3G.multiply(BigDecimal.valueOf(12));
	public static final String ORGNR1 = "999999999";
	public static final String ORGNR2 = "999999998";

	@Test
	void totalRefusjonOver6GEksisterendeAktivitetIngenRefusjonPåDato() {
		// Arrange
        var gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), TIDENES_ENDE);
        var andelGradering = AndelGradering.builder()
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2))
				.medEksisterendeAktivitetFraDato(gradering.getFom()) // eksisterende aktivitet
				.build();
        var input = PeriodeModellGradering.builder()
				.medPeriodisertBruttoBeregningsgrunnlag(lagPeriodisertBg(gradering,
						Map.of(ORGNR1, ÅRSBBELØP_OVER_6G, ORGNR2, BigDecimal.TEN),
						Map.of(ORGNR1, ÅRSBBELØP_OVER_6G)))
				.medAndelGraderinger(List.of(andelGradering))
				.medGrunnbeløp(BigDecimal.valueOf(90000))
				.build();

		// Act
        var splitResultat = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

		// Assert
		assertGraderingFom(splitResultat, List.of(gradering.getFom()));
	}

	private List<PeriodisertBruttoBeregningsgrunnlag> lagPeriodisertBg(Periode gradering,
	                                                                   Map<String, BigDecimal> orgnrBruttoMap,
	                                                                   Map<String, BigDecimal> orgnrRefusjonMap) {
        var builder = PeriodisertBruttoBeregningsgrunnlag.builder()
				.medPeriode(gradering);
		orgnrBruttoMap.forEach((key, value) -> {
            var br = lagBruttoBg(key, value, orgnrRefusjonMap.get(key));
			builder.leggTilBruttoBeregningsgrunnlag(br);
		});
		return List.of(builder.build());
	}

	@Test
	void totalRefusjonOver6GEksisterendeAktivitetIngenRefusjonPåDatoAvsluttetGradering() {
		// Arrange
        var gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1),
				LocalDate.of(2019, Month.JULY, 1));
        var andelGradering = AndelGradering.builder()
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2))
				.medEksisterendeAktivitetFraDato(gradering.getFom()) // eksisterende aktivitet
				.build();
        var input = PeriodeModellGradering.builder()
				.medPeriodisertBruttoBeregningsgrunnlag(lagPeriodisertBg(Periode.of(gradering.getFom(), gradering.getTom().plusDays(1)),
						Map.of(ORGNR1, ÅRSBBELØP_OVER_6G, ORGNR2, BigDecimal.TEN),
						Map.of(ORGNR1, ÅRSBBELØP_OVER_6G)))
				.medAndelGraderinger(List.of(andelGradering))
				.medGrunnbeløp(BigDecimal.valueOf(90000))
				.build();

		// Act
        var splitResultat = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

		// Assert
		assertGraderingFom(splitResultat, List.of(gradering.getFom(), gradering.getTom().plusDays(1)));
	}

	@Test
	void totalRefusjonOver6GEksisterendeAktivitetIngenRefusjonPåDatoAvsluttetGraderingMedTotalRefusjonUnder6GVedEndtGradering() {
		// Arrange
        var periode1Builder = PeriodisertBruttoBeregningsgrunnlag.builder()
				.medPeriode(new Periode(DateUtil.TIDENES_BEGYNNELSE, LocalDate.of(2019, Month.APRIL, 1)));
		periode1Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR1, ÅRSBBELØP_OVER_6G, ÅRSBBELØP_OVER_6G));
		periode1Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR2, ÅRSBBELØP_OVER_6G, null));

        var periode2Builder = PeriodisertBruttoBeregningsgrunnlag.builder()
				.medPeriode(new Periode(LocalDate.of(2019, Month.APRIL, 1).plusDays(1), TIDENES_ENDE));
		periode2Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR1, ÅRSBBELØP_OVER_6G, null));
		periode2Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR2, ÅRSBBELØP_OVER_6G, null));

        var gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.JULY, 1));
        var andelGradering = AndelGradering.builder()
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2))
				.medEksisterendeAktivitetFraDato(gradering.getFom()) // eksisterende aktivitet
				.build();
        var input = PeriodeModellGradering.builder()
				.medAndelGraderinger(List.of(andelGradering))
				.medPeriodisertBruttoBeregningsgrunnlag(List.of(periode1Builder.build(), periode2Builder.build()))
				.medGrunnbeløp(BigDecimal.valueOf(90000))
				.build();

		// Act
        var splitResultat = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

		// Assert
		assertGraderingFom(splitResultat, List.of(gradering.getFom()));
	}

	private BruttoBeregningsgrunnlag lagBruttoBg(String orgnr, BigDecimal bruttoBg, BigDecimal refusjon) {
		return BruttoBeregningsgrunnlag.builder()
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr))
				.medBruttoPrÅr(bruttoBg)
				.medRefusjonPrÅr(refusjon).build();
	}

	@Test
	void totalRefusjonOver6GEksisterendeAktivitetIngenRefusjonPåDatoAvsluttetGraderingMedRefusjonskravVedEndtGradering() {
		// Arrange
        var gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.JULY, 1));

        var periode1Builder = PeriodisertBruttoBeregningsgrunnlag.builder()
				.medPeriode(new Periode(DateUtil.TIDENES_BEGYNNELSE, LocalDate.of(2019, Month.APRIL, 1)));
		periode1Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR1, ÅRSBBELØP_OVER_6G, ÅRSBBELØP_OVER_6G));
		periode1Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR2, ÅRSBBELØP_OVER_6G, null));

        var periode2Builder = PeriodisertBruttoBeregningsgrunnlag.builder()
				.medPeriode(new Periode(gradering.getFom().plusMonths(1), TIDENES_ENDE));
		periode2Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR1, ÅRSBBELØP_OVER_6G, ÅRSBBELØP_OVER_6G));
		periode2Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR2, ÅRSBBELØP_OVER_6G, BigDecimal.TEN));

        var andelGradering = AndelGradering.builder()
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2))
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medEksisterendeAktivitetFraDato(gradering.getFom()) // eksisterende aktivitet
				.build();
        var input = PeriodeModellGradering.builder()
				.medAndelGraderinger(List.of(andelGradering))
				.medPeriodisertBruttoBeregningsgrunnlag(List.of(periode1Builder.build(), periode2Builder.build()))
				.medGrunnbeløp(BigDecimal.valueOf(90000))
				.build();

		// Act
        var splitResultat = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

		// Assert
		assertGraderingFom(splitResultat, List.of(gradering.getFom()));
	}

	@Test
	void totalRefusjonUnder6GEksisterendeAktivitetIngenRefusjonPåDato() {
		// Arrange
        var periode1Builder = PeriodisertBruttoBeregningsgrunnlag.builder()
				.medPeriode(new Periode(DateUtil.TIDENES_BEGYNNELSE, TIDENES_ENDE));
		periode1Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR1, ÅRSBBELØP_OVER_3G, ÅRSBBELØP_OVER_3G));
		periode1Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR2, ÅRSBBELØP_OVER_3G, null));


        var gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), TIDENES_ENDE);
        var andelGradering = AndelGradering.builder()
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2))
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medEksisterendeAktivitetFraDato(gradering.getFom()) // eksisterende aktivitet
				.build();
        var input = PeriodeModellGradering.builder()
				.medAndelGraderinger(List.of(andelGradering))
				.medPeriodisertBruttoBeregningsgrunnlag(List.of(periode1Builder.build()))
				.medGrunnbeløp(BigDecimal.valueOf(90000))
				.build();

		// Act
        var resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

		// Assert
		assertThat(resultatList).isEmpty();
	}

	@Test
	void totalRefusjonOver6GEksisterendeAktivitetHarRefusjonPåDato() {
		// Arrange
        var periode1Builder = PeriodisertBruttoBeregningsgrunnlag.builder()
				.medPeriode(new Periode(DateUtil.TIDENES_BEGYNNELSE, TIDENES_ENDE));
		periode1Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR1, ÅRSBBELØP_OVER_3G, ÅRSBBELØP_OVER_3G));
		periode1Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR2, ÅRSBBELØP_OVER_3G, ÅRSBBELØP_OVER_3G));

        var gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), TIDENES_ENDE);
        var andelGradering = AndelGradering.builder()
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2))
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medEksisterendeAktivitetFraDato(gradering.getFom()) // eksisterende aktivitet
				.build();
        var input = PeriodeModellGradering.builder()
				.medAndelGraderinger(List.of(andelGradering))
				.medPeriodisertBruttoBeregningsgrunnlag(List.of(periode1Builder.build()))
				.medGrunnbeløp(BigDecimal.valueOf(90000))
				.build();

		// Act
        var resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

		// Assert
		assertThat(resultatList).isEmpty();
	}

	@Test
	void totalRefusjonUnder6GEksisterendeAktivitetHarRefusjonPåDato() {
		// Arrange
        var grunnbeløp = BigDecimal.valueOf(90000);

        var periode1Builder = PeriodisertBruttoBeregningsgrunnlag.builder()
				.medPeriode(new Periode(DateUtil.TIDENES_BEGYNNELSE, TIDENES_ENDE));
		periode1Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR1, BigDecimal.valueOf(2).multiply(grunnbeløp), BigDecimal.valueOf(2).multiply(grunnbeløp)));
		periode1Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR2, BigDecimal.valueOf(2).multiply(grunnbeløp), BigDecimal.valueOf(2).multiply(grunnbeløp)));


        var gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), TIDENES_ENDE);
        var andelGradering = AndelGradering.builder()
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2))
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medEksisterendeAktivitetFraDato(gradering.getFom()) // eksisterende aktivitet
				.build();
        var input = PeriodeModellGradering.builder()
				.medAndelGraderinger(List.of(andelGradering))
				.medPeriodisertBruttoBeregningsgrunnlag(List.of(periode1Builder.build()))
				.medGrunnbeløp(grunnbeløp)
				.build();

		// Act
        var resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

		// Assert
		assertThat(resultatList).isEmpty();
	}

	@Test
	void totalRefusjonOver6GNyAktivitetIngenRefusjonPåDato() {
		// Arrange
        var grunnbeløp = BigDecimal.valueOf(90000);
        var periode1Builder = PeriodisertBruttoBeregningsgrunnlag.builder()
				.medPeriode(new Periode(DateUtil.TIDENES_BEGYNNELSE, TIDENES_ENDE));
		periode1Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR1, BigDecimal.valueOf(7).multiply(grunnbeløp), BigDecimal.valueOf(7).multiply(grunnbeløp)));


        var gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), TIDENES_ENDE);
        var andelGradering = AndelGradering.builder()
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2))
				.medNyAktivitetFraDato(gradering.getFom()) // ny aktivitet
				.build();
        var input = PeriodeModellGradering.builder()
				.medAndelGraderinger(List.of(andelGradering))
				.medPeriodisertBruttoBeregningsgrunnlag(List.of(periode1Builder.build()))
				.medGrunnbeløp(grunnbeløp)
				.build();

		// Act
        var resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

		// Assert
		assertGraderingFom(resultatList, List.of(gradering.getFom()));
	}

	@Test
	void totalRefusjonUnder6GNyAktivitetIngenRefusjonPåDato() {
		// Arrange
        var grunnbeløp = BigDecimal.valueOf(90000);
        var periode1Builder = PeriodisertBruttoBeregningsgrunnlag.builder()
				.medPeriode(new Periode(DateUtil.TIDENES_BEGYNNELSE, TIDENES_ENDE));
		periode1Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR1, BigDecimal.valueOf(5).multiply(grunnbeløp), BigDecimal.valueOf(5).multiply(grunnbeløp)));


        var gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), TIDENES_ENDE);
        var andelGradering = AndelGradering.builder()
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2))
				.medNyAktivitetFraDato(gradering.getFom()) // ny aktivitet
				.build();
        var input = PeriodeModellGradering.builder()
				.medAndelGraderinger(List.of(andelGradering))
				.medPeriodisertBruttoBeregningsgrunnlag(List.of(periode1Builder.build()))
				.medGrunnbeløp(grunnbeløp)
				.build();

		// Act
        var resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

		// Assert
		assertGraderingFom(resultatList, List.of(gradering.getFom()));
	}


	@Test
	void totalRefusjonUnder6GNyAktivitetHarIkkeRefusjonPåDato() {
		// Arrange
        var grunnbeløp = BigDecimal.valueOf(90000);

        var periode1Builder = PeriodisertBruttoBeregningsgrunnlag.builder()
				.medPeriode(new Periode(DateUtil.TIDENES_BEGYNNELSE, TIDENES_ENDE));
		periode1Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR1, BigDecimal.valueOf(5).multiply(grunnbeløp), BigDecimal.valueOf(5).multiply(grunnbeløp)));
		periode1Builder.leggTilBruttoBeregningsgrunnlag(lagBruttoBg(ORGNR2, BigDecimal.valueOf(5).multiply(grunnbeløp), null));


        var gradering = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.MARCH, 8));
        var andelGradering = AndelGradering.builder()
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2))
				.medNyAktivitetFraDato(gradering.getFom()) // ny aktivitet
				.build();
        var input = PeriodeModellGradering.builder()
				.medAndelGraderinger(List.of(andelGradering))
				.medPeriodisertBruttoBeregningsgrunnlag(List.of(periode1Builder.build()))
				.medGrunnbeløp(grunnbeløp)
				.build();

		// Act
        var resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, gradering);

		// Assert
		assertGraderingFom(resultatList, List.of(gradering.getFom(), gradering.getTom().plusDays(1)));
	}

	@Test
	void skalLageToPerioderNårBruttoForAndelSnErNullOgMedOpphørAvGraderingISammePeriode() {
		// Arrange
        var graderingsPeriode = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.MARCH, 8));
        var gradering = new Gradering(graderingsPeriode);

        var andelGradering = AndelGradering.builder()
				.medAktivitetStatus(AktivitetStatusV2.SN)
				.medGraderinger(List.of(gradering))
				.medEksisterendeAktivitetFraDato(graderingsPeriode.getFom())
				.build();
        var bruttoBeregningsgrunnlag = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.SN, null, BigDecimal.ZERO);

        var periodisertBruttoBeregningsgrunnlag = PeriodisertBruttoBeregningsgrunnlag.builder()
				.leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag)
				.medPeriode(new Periode(LocalDate.of(2019, Month.JANUARY, 20), null))
				.build();

        var input = PeriodeModellGradering.builder()
				.medAndelGraderinger(List.of(andelGradering))
				.medPeriodisertBruttoBeregningsgrunnlag(List.of(periodisertBruttoBeregningsgrunnlag))
				.medGrunnbeløp(BigDecimal.valueOf(90000))
				.build();

		// Act
        var resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, graderingsPeriode);

		// Assert
		assertGraderingFom(resultatList, List.of(graderingsPeriode.getFom(), graderingsPeriode.getTom().plusDays(1)));
	}

	@Test
	void skalIkkeLagePerioderNårBruttoForAndelSnIkkeErNullOgMedOpphørAvGraderingISammePeriode() {
		// Arrange
        var graderingsPeriode = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.MARCH, 8));
        var gradering = new Gradering(graderingsPeriode);
        var andelGradering = AndelGradering.builder()
				.medAktivitetStatus(AktivitetStatusV2.SN)
				.medGraderinger(List.of(gradering))
				.medEksisterendeAktivitetFraDato(graderingsPeriode.getFom())
				.build();

        var bruttoBeregningsgrunnlag = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.SN, null, BigDecimal.valueOf(200_000));
        var periodisertBruttoBeregningsgrunnlag = PeriodisertBruttoBeregningsgrunnlag.builder()
				.leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag)
				.medPeriode(new Periode(LocalDate.of(2019, Month.JANUARY, 20), null))
				.build();

        var input = PeriodeModellGradering.builder()
				.medAndelGraderinger(List.of(andelGradering))
				.medPeriodisertBruttoBeregningsgrunnlag(List.of(periodisertBruttoBeregningsgrunnlag))
				.medGrunnbeløp(BigDecimal.valueOf(90000))
				.build();

		// Act
        var resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, graderingsPeriode);

		// Assert
		assertThat(resultatList).isEmpty();
	}

	@Test
	void skalLageToPerioderNårBruttoForAndelAtErNullOgToArbeidsforholdHosPrivatpersonOgMedOpphørAvGraderingISammePeriode() {
		// Arrange
        var graderingsPeriode = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.MARCH, 8));
        var gradering = new Gradering(graderingsPeriode);

        var arbeidsforhold1 = Arbeidsforhold.nyttArbeidsforholdHosPrivatperson("5454", "1111");
        var arbeidsforhold2 = Arbeidsforhold.nyttArbeidsforholdHosPrivatperson("5454", "2222");
        var andelMedGradering = AndelGradering.builder()
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medGraderinger(List.of(gradering))
				.medEksisterendeAktivitetFraDato(graderingsPeriode.getFom())
				.medArbeidsforhold(arbeidsforhold1)
				.build();
        var bruttoBeregningsgrunnlag1 = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.AT, arbeidsforhold1, BigDecimal.ZERO);
        var bruttoBeregningsgrunnlag2 = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.AT, arbeidsforhold2, BigDecimal.valueOf(100000));

        var periodisertBruttoBeregningsgrunnlag = PeriodisertBruttoBeregningsgrunnlag.builder()
				.leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag1)
				.leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag2)
				.medPeriode(new Periode(LocalDate.of(2019, Month.JANUARY, 20), null))
				.build();

        var input = PeriodeModellGradering.builder()
				.medAndelGraderinger(List.of(andelMedGradering))
				.medPeriodisertBruttoBeregningsgrunnlag(List.of(periodisertBruttoBeregningsgrunnlag))
				.medGrunnbeløp(BigDecimal.valueOf(90000))
				.build();

		// Act
        var resultatList = VurderPeriodeForGradering.vurder(input, andelMedGradering, graderingsPeriode);

		// Assert
		assertGraderingFom(resultatList, List.of(graderingsPeriode.getFom(), graderingsPeriode.getTom().plusDays(1)));
	}

	@Test
	void skalLageToPerioderNårBruttoForAndelAtErNullOgBrukerAnsattIOrganisasjonOgMedOpphørAvGraderingISammePeriode() {
		// Arrange
        var graderingsPeriode = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.MARCH, 8));
        var gradering = new Gradering(graderingsPeriode);

        var arbeidsforhold1 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("5454", "1111");
        var arbeidsforhold2 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("5454", "2222");
        var andelMedGradering = AndelGradering.builder()
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medGraderinger(List.of(gradering))
				.medEksisterendeAktivitetFraDato(graderingsPeriode.getFom())
				.medArbeidsforhold(arbeidsforhold1)
				.build();
        var bruttoBeregningsgrunnlag1 = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.AT, arbeidsforhold1, BigDecimal.ZERO);
        var bruttoBeregningsgrunnlag2 = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.AT, arbeidsforhold2, BigDecimal.valueOf(100000));

        var periodisertBruttoBeregningsgrunnlag = PeriodisertBruttoBeregningsgrunnlag.builder()
				.leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag1)
				.leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag2)
				.medPeriode(new Periode(LocalDate.of(2019, Month.JANUARY, 20), null))
				.build();

        var input = PeriodeModellGradering.builder()
				.medAndelGraderinger(List.of(andelMedGradering))
				.medPeriodisertBruttoBeregningsgrunnlag(List.of(periodisertBruttoBeregningsgrunnlag))
				.medGrunnbeløp(BigDecimal.valueOf(90000))
				.build();

		// Act
        var resultatList = VurderPeriodeForGradering.vurder(input, andelMedGradering, graderingsPeriode);

		// Assert
		assertGraderingFom(resultatList, List.of(graderingsPeriode.getFom(), graderingsPeriode.getTom().plusDays(1)));
	}

	@Test
	void skalLageEnPerioderNårBruttoForAndelFlErNullOgMedOpphørAvGraderingIAnnenPeriode() {
		// Arrange
        var graderingsPeriode = new Periode(LocalDate.of(2019, Month.MARCH, 1), LocalDate.of(2019, Month.MARCH, 8));
        var gradering = new Gradering(graderingsPeriode);
        var andelGradering = AndelGradering.builder()
				.medAktivitetStatus(AktivitetStatusV2.FL)
				.medGraderinger(List.of(gradering))
				.medEksisterendeAktivitetFraDato(graderingsPeriode.getFom())
				.build();

        var bruttoBeregningsgrunnlagBruttoErNull = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.FL, null, BigDecimal.ZERO);
        var bruttoBeregningsgrunnlagBruttoIkkeNull = lagBruttoBeregningsgrunnlag(AktivitetStatusV2.FL, null, BigDecimal.valueOf(100_000));

        var periodisertBruttoBeregningsgrunnlag1 = PeriodisertBruttoBeregningsgrunnlag.builder()
				.leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlagBruttoErNull)
				.medPeriode(new Periode(LocalDate.of(2019, Month.JANUARY, 20), graderingsPeriode.getFom().plusDays(2)))
				.build();

        var periodisertBruttoBeregningsgrunnlag2 = PeriodisertBruttoBeregningsgrunnlag.builder()
				.leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlagBruttoIkkeNull)
				.medPeriode(new Periode(graderingsPeriode.getFom().plusDays(3), null))
				.build();

        var input = PeriodeModellGradering.builder()
				.medAndelGraderinger(List.of(andelGradering))
				.medPeriodisertBruttoBeregningsgrunnlag(List.of(periodisertBruttoBeregningsgrunnlag1, periodisertBruttoBeregningsgrunnlag2))
				.medGrunnbeløp(BigDecimal.valueOf(90000))
				.build();

		// Act
        var resultatList = VurderPeriodeForGradering.vurder(input, andelGradering, graderingsPeriode);

		// Assert
		assertGraderingFom(resultatList, List.of(graderingsPeriode.getFom()));
	}

	private void assertGraderingFom(List<PeriodeSplittData> resultatList, List<LocalDate> foms) {
		assertThat(resultatList).hasSameSizeAs(foms);
		for (var i = 0; i < resultatList.size(); i++) {
            var resultat = resultatList.get(i);
			assertThat(resultat.getFom()).as("fom").isEqualTo(foms.get(i));
			if (i == 0) {
				assertThat(resultat.getPeriodeÅrsak()).as("periodeÅrsak").isEqualTo(PeriodeÅrsak.GRADERING);
			} else {
				assertThat(resultat.getPeriodeÅrsak()).as("periodeÅrsak").isEqualTo(PeriodeÅrsak.GRADERING_OPPHØRER);
			}
		}
	}

	private BruttoBeregningsgrunnlag lagBruttoBeregningsgrunnlag(AktivitetStatusV2 aktivitetStatus, Arbeidsforhold arbeidsforhold, BigDecimal bruttoBeregningsgrunnlag) {
		return BruttoBeregningsgrunnlag.builder()
				.medAktivitetStatus(aktivitetStatus)
				.medArbeidsforhold(arbeidsforhold)
				.medBruttoPrÅr(bruttoBeregningsgrunnlag)
				.build();
	}
}
