package no.nav.folketrygdloven.beregningsgrunnlag.perioder.utbetalingsgrad;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.AndelUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.Utbetalingsgrad;

class IdentifiserPerioderForEndringISøktYtelseTest {


	@Test
	void skal_støtte_hull_i_utbetalingsperioder() {
		// Arrange
        var fom = LocalDate.now();
        var tom = fom.plusMonths(1);

        var fom2 = tom.plusDays(2);
        var tom2 = fom2.plusMonths(1);
		var andelGradering = AndelUtbetalingsgrad.builder()
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("123"))
				.medUtbetalingsgrader(List.of(
						new Utbetalingsgrad(Periode.of(fom, tom), BigDecimal.valueOf(50)),
						new Utbetalingsgrad(Periode.of(fom2, tom2), BigDecimal.valueOf(50))))
				.build();

		// Act
        var periodesplitter = IdentifiserPerioderForEndringISøktYtelse.identifiser(andelGradering);

		// Assert
		assertThat(periodesplitter).hasSize(4);
        var iterator = periodesplitter.iterator();
        var første = iterator.next();
		assertThat(første.getFom()).isEqualTo(fom);
		assertThat(første.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);

        var andre = iterator.next();
		assertThat(andre.getFom()).isEqualTo(tom.plusDays(1));
		assertThat(andre.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);

        var tredje = iterator.next();
		assertThat(tredje.getFom()).isEqualTo(fom2);
		assertThat(tredje.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);

        var fjerde = iterator.next();
		assertThat(fjerde.getFom()).isEqualTo(tom2.plusDays(1));
		assertThat(fjerde.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
	}


	@Test
	void fårSplittForFørstePeriode() {
		// Arrange
        var fom = LocalDate.now();
        var tom = fom.plusMonths(1);
		var andelGradering = AndelUtbetalingsgrad.builder()
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("123"))
				.medUtbetalingsgrader(List.of(new Utbetalingsgrad(Periode.of(fom, tom), BigDecimal.valueOf(50))))
				.build();

		// Act
        var periodesplitter = IdentifiserPerioderForEndringISøktYtelse.identifiser(andelGradering);

		// Assert
		assertThat(periodesplitter).hasSize(2);
        var iterator = periodesplitter.iterator();
        var første = iterator.next();
		assertThat(første.getFom()).isEqualTo(fom);
		assertThat(første.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);

        var andre = iterator.next();
		assertThat(andre.getFom()).isEqualTo(tom.plusDays(1));
		assertThat(andre.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
	}

	@Test
	void fårSplittForPerioderMedForskjelligUtbetaling() {
		// Arrange
        var fom = LocalDate.now();
        var tom1 = fom.plusMonths(1);
        var p1 = Periode.of(fom, tom1);
        var fom2 = tom1.plusDays(1);
        var tom2 = fom.plusMonths(2);
        var p2 = Periode.of(fom2, tom2);
		var andelGradering = AndelUtbetalingsgrad.builder()
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("123"))
				.medUtbetalingsgrader(List.of(
						new Utbetalingsgrad(p1, BigDecimal.valueOf(50)),
						new Utbetalingsgrad(p2, BigDecimal.valueOf(40))
				))
				.build();

		// Act
        var periodesplitter = IdentifiserPerioderForEndringISøktYtelse.identifiser(andelGradering);

		// Assert
		assertThat(periodesplitter)
				.hasSize(3)
				.anySatisfy(splitt -> {
					assertThat(splitt.getFom()).isEqualTo(p1.getFom());
					assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
				})
				.anySatisfy(splitt -> {
					assertThat(splitt.getFom()).isEqualTo(p2.getFom());
					assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
				}).anySatisfy(splitt -> {
					assertThat(splitt.getFom()).isEqualTo(p2.getTom().plusDays(1));
					assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
				});
	}

	@Test
	void fårBareSplittForFørstePeriodeMedLikUtbetaling() {
		// Arrange
        var fom = LocalDate.now();
        var p0 = Periode.of(fom.minusDays(14), fom.minusDays(1));
        var p1 = Periode.of(fom, fom.plusMonths(1));
        var p2 = Periode.of(fom.plusMonths(1).plusDays(1), fom.plusMonths(2));
		var andelGradering = AndelUtbetalingsgrad.builder()
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("123"))
				.medUtbetalingsgrader(List.of(
						new Utbetalingsgrad(p0, BigDecimal.valueOf(0)),
						new Utbetalingsgrad(p1, BigDecimal.valueOf(50)),
						new Utbetalingsgrad(p2, BigDecimal.valueOf(50))
				))
				.build();

		// Act
        var periodesplitter = IdentifiserPerioderForEndringISøktYtelse.identifiser(andelGradering);

		// Assert
		assertThat(periodesplitter).hasSize(2)
				.anySatisfy(splitt -> {
					assertThat(splitt.getFom()).isEqualTo(p1.getFom());
					assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
				}).anySatisfy(splitt -> {
					assertThat(splitt.getFom()).isEqualTo(p2.getTom().plusDays(1));
					assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
				});
	}


	@Test
	void fårSplittForFlerePerioderMedUlikUtbetaling() {
		// Arrange
        var fom = LocalDate.now();
        var p1 = Periode.of(fom, fom.plusMonths(1));
        var p2 = Periode.of(fom.plusMonths(1).plusDays(1), fom.plusMonths(2));
        var p3 = Periode.of(fom.plusMonths(2).plusDays(1), fom.plusMonths(3));
        var p4 = Periode.of(fom.plusMonths(3).plusDays(1), fom.plusMonths(4));
        var p5 = Periode.of(fom.plusMonths(4).plusDays(1), fom.plusMonths(5));
        var p6 = Periode.of(fom.plusMonths(4).plusDays(1), fom.plusMonths(5));
		var andelGradering = AndelUtbetalingsgrad.builder()
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("123"))
				.medUtbetalingsgrader(List.of(
						new Utbetalingsgrad(p1, BigDecimal.valueOf(50)),
						new Utbetalingsgrad(p2, BigDecimal.valueOf(50)),
						new Utbetalingsgrad(p3, BigDecimal.valueOf(51)),
						new Utbetalingsgrad(p4, BigDecimal.valueOf(0)),
						new Utbetalingsgrad(p5, BigDecimal.valueOf(100)),
						new Utbetalingsgrad(p6, BigDecimal.valueOf(100))
				))
				.build();

		// Act
        var periodesplitter = IdentifiserPerioderForEndringISøktYtelse.identifiser(andelGradering);

		// Assert
		assertThat(periodesplitter).hasSize(5)
				.anySatisfy(splitt -> {
					assertThat(splitt.getFom()).isEqualTo(p1.getFom());
					assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
				}).anySatisfy(splitt -> {
					assertThat(splitt.getFom()).isEqualTo(p3.getFom());
					assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
				}).anySatisfy(splitt -> {
					assertThat(splitt.getFom()).isEqualTo(p4.getFom());
					assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
				}).anySatisfy(splitt -> {
					assertThat(splitt.getFom()).isEqualTo(p5.getFom());
					assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
				}).anySatisfy(splitt -> {
					assertThat(splitt.getFom()).isEqualTo(p6.getTom().plusDays(1));
					assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
				});
	}


	@Test
	void ingen_splitt_med_kun_null() {
		// Arrange
        var fom = LocalDate.now();
        var tom = fom.plusMonths(1);
		var andelGradering = AndelUtbetalingsgrad.builder()
				.medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("123"))
				.medUtbetalingsgrader(List.of(new Utbetalingsgrad(Periode.of(fom, tom), BigDecimal.valueOf(0))))
				.build();

		// Act
        var periodesplitter = IdentifiserPerioderForEndringISøktYtelse.identifiser(andelGradering);

		// Assert
		assertThat(periodesplitter).isEmpty();
	}

}
