package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektskomponenten;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

public class BeregnPrArbeidsforholdFraAOrdningenTest {

	private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2021, 10, 15);
	private Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(SKJÆRINGSTIDSPUNKT.minusYears(2), "12345");

	@Test
	void skalKasteExceptionNårBeregningperiodeErNull() {
		//Arrange
		Beregningsgrunnlag grunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(SKJÆRINGSTIDSPUNKT, BigDecimal.valueOf(35000), BigDecimal.ZERO, false);
		BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0)).medBeregningsperiode(null);
		BeregningsgrunnlagPrArbeidsforhold arbeidstakerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		//Act
		BeregnPrArbeidsforholdFraAOrdningen beregnPrArbeidsforholdFraAOrdningen = new BeregnPrArbeidsforholdFraAOrdningen(arbeidstakerStatus);
		Assertions.assertThrows(IllegalStateException.class, () -> beregnPrArbeidsforholdFraAOrdningen.evaluate(periode));
	}

	@Test
	void skalBeregneSnittAvInntekterIBeregningperioden() {
		//Arrange
		Periode beregningsperiode = Periode.of(SKJÆRINGSTIDSPUNKT.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));
		Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(31452)).medMåned(beregningsperiode.getFom()).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(48739)).medMåned(beregningsperiode.getFom().plusMonths(1)).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(44810)).medMåned(beregningsperiode.getTom()).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		//Inntekt utenfor beregningsperioden - skal ikke tas med
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(999999)).medMåned(beregningsperiode.getFom().minusMonths(1)).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());

		Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), inntektsgrunnlag, List.of(AktivitetStatus.ATFL), List.of(arbeidsforhold));
		BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPrArbeidsforhold arbeidstakerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(arbeidstakerStatus).medBeregningsperiode(beregningsperiode);
		//Act
		Evaluation resultat = new BeregnPrArbeidsforholdFraAOrdningen(arbeidstakerStatus).evaluate(periode);
		//Assert
		assertThat(resultat.result()).isEqualTo(Resultat.JA);
		assertThat(arbeidstakerStatus.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(500004));
	}

	@Test
	void skalBeregneSnittAvInntekterMedToHeleMånederOgEnHalv() {
		//Arrange
		LocalDate startArbeidsforhold = SKJÆRINGSTIDSPUNKT.minusMonths(3).withDayOfMonth(15);
		Periode beregningsperiode = Periode.of(startArbeidsforhold, SKJÆRINGSTIDSPUNKT.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));
		Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(31452)).medMåned(beregningsperiode.getFom()).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(48739)).medMåned(beregningsperiode.getFom().plusMonths(1)).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(44810)).medMåned(beregningsperiode.getTom()).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		//Inntekt utenfor beregningsperioden - skal ikke tas med
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(999999)).medMåned(beregningsperiode.getFom().minusMonths(1)).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());

		Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), inntektsgrunnlag, List.of(AktivitetStatus.ATFL), List.of(arbeidsforhold));
		BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPrArbeidsforhold arbeidstakerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(arbeidstakerStatus).medBeregningsperiode(beregningsperiode);
		//Act
		Evaluation resultat = new BeregnPrArbeidsforholdFraAOrdningen(arbeidstakerStatus).evaluate(periode);
		//Assert
		assertThat(resultat.result()).isEqualTo(Resultat.JA);
		assertThat(arbeidstakerStatus.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(561294));
	}

	@Test
	void skalBeregneSnittAvInntekterMedEnHelMånedOgEnHalv() {
		//Arrange
		LocalDate startArbeidsforhold = SKJÆRINGSTIDSPUNKT.minusMonths(2).withDayOfMonth(15);
		Periode beregningsperiode = Periode.of(startArbeidsforhold, SKJÆRINGSTIDSPUNKT.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));
		Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(48739)).medMåned(beregningsperiode.getFom()).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(44810)).medMåned(beregningsperiode.getTom()).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		//Inntekt utenfor beregningsperioden - skal ikke tas med
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(999999)).medMåned(beregningsperiode.getFom().minusMonths(1)).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());

		Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), inntektsgrunnlag, List.of(AktivitetStatus.ATFL), List.of(arbeidsforhold));
		BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPrArbeidsforhold arbeidstakerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(arbeidstakerStatus).medBeregningsperiode(beregningsperiode);
		//Act
		Evaluation resultat = new BeregnPrArbeidsforholdFraAOrdningen(arbeidstakerStatus).evaluate(periode);
		//Assert
		assertThat(resultat.result()).isEqualTo(Resultat.JA);
		assertThat(arbeidstakerStatus.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(537720));
	}

	@Test
	void skalBeregneSnittAvInntekterMedEnHalvMåned() {
		//Arrange
		// 12 Virkedager gjenstående
		LocalDate startArbeidsforhold = SKJÆRINGSTIDSPUNKT.minusMonths(1).withDayOfMonth(15);
		Arbeidsforhold arbeidsforhold = Arbeidsforhold.builder()
				.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
				.medOrgnr("12345")
				.medStartdato(startArbeidsforhold)
				.build();
		Periode beregningsperiode = Periode.of(startArbeidsforhold, SKJÆRINGSTIDSPUNKT.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));
		Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(20000)).medMåned(beregningsperiode.getTom()).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		//Inntekt utenfor beregningsperioden - skal ikke tas med
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(999999)).medMåned(beregningsperiode.getFom().minusMonths(1)).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());

		Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), inntektsgrunnlag, List.of(AktivitetStatus.ATFL), List.of(arbeidsforhold));
		BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPrArbeidsforhold arbeidstakerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(arbeidstakerStatus).medBeregningsperiode(beregningsperiode);
		//Act
		Evaluation resultat = new BeregnPrArbeidsforholdFraAOrdningen(arbeidstakerStatus).evaluate(periode);
		//Assert
		assertThat(resultat.result()).isEqualTo(Resultat.JA);
		assertThat(arbeidstakerStatus.getBeregnetPrÅr()).isCloseTo(BigDecimal.valueOf(440000), within(BigDecimal.valueOf(0.01)));
	}

	@Test
	void skalBeregneInntektNårLønnsendringISisteMånedFørStp() {
		/*
				personen har lønn på 46332, og får lønnsøkning til 69498 2021-09-20.
				utbetalt lønn i 2021-09 er 46332
				utbetalt lønn i 2021-10 er 55809 (13 virkedager med 46332, 9 virkedager med 69498)
		 */

		LocalDate stp = LocalDate.of(2021, 10, 15);
		LocalDate lønnsendringdato = LocalDate.of(2021, 9, 20);
		Periode beregningsperiode = Periode.of(lønnsendringdato, stp.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));

		Arbeidsforhold arbeidsforhold = Arbeidsforhold.builder()
				.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
				.medOrgnr("12345")
				.medStartdato(LocalDate.of(2020, 1, 1))
				.build();

		Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(46332)).medMåned(YearMonth.of(2021, 8)).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(55809)).medMåned(YearMonth.of(2021, 9)).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());

		Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), inntektsgrunnlag, List.of(AktivitetStatus.ATFL), List.of(arbeidsforhold));
		BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPrArbeidsforhold arbeidstakerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(arbeidstakerStatus).medBeregningsperiode(beregningsperiode);
		//Act
		Evaluation resultat = new BeregnPrArbeidsforholdFraAOrdningen(arbeidstakerStatus).evaluate(periode);
		//Assert
		assertThat(resultat.result()).isEqualTo(Resultat.JA);
		assertThat(arbeidstakerStatus.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(69498 * 12));
	}

	@Test
	void OMS_Skal_gi_fordele_restinntekt_fra_aordningen_til_arbeidsforhold_uten_inntektsmelding() {
		//Arrange
		LocalDate arbeidsforholStartdato = SKJÆRINGSTIDSPUNKT.minusYears(2);
		var arbeidsforholdMedInntektsmelding = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, "12345", "ARB1");
		var arbeidsforholdUtenInntektsmelding = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato,"12345", "ARB2");

		Periode beregningsperiode = Periode.of(SKJÆRINGSTIDSPUNKT.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));
		Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(31452)).medMåned(beregningsperiode.getFom())
				.medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(48739)).medMåned(beregningsperiode.getFom().plusMonths(1)).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(44810)).medMåned(beregningsperiode.getTom()).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		//Inntekt utenfor beregningsperioden - skal ikke tas med
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(999999)).medMåned(beregningsperiode.getFom().minusMonths(1)).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());

		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(10000))
				.medArbeidsgiver(arbeidsforholdMedInntektsmelding).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING).build());


		Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(),
				inntektsgrunnlag,
				List.of(AktivitetStatus.ATFL),
				List.of(arbeidsforholdMedInntektsmelding, arbeidsforholdUtenInntektsmelding),
				Optional.of(new OmsorgspengerGrunnlag(false, false)));

		BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPrArbeidsforhold arbeidstakerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(1);
		BeregningsgrunnlagPrArbeidsforhold.builder(arbeidstakerStatus).medBeregningsperiode(beregningsperiode);
		//Act
		Evaluation resultat = new BeregnPrArbeidsforholdFraAOrdningen(arbeidstakerStatus).evaluate(periode);
		//Assert
		assertThat(resultat.result()).isEqualTo(Resultat.JA);
		assertThat(arbeidstakerStatus.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(380004));
	}

	@Test
	void OMS_Skal_gi_fordele_restinntekt_fra_aordningen_til_to_arbeidsforhold_uten_inntektsmelding() {
		//Arrange
		LocalDate arbeidsforholStartdato = SKJÆRINGSTIDSPUNKT.minusYears(2);
		var arbeidsforholdMedInntektsmelding = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, "12345", "ARB1");
		var arbeidsforholdUtenInntektsmelding = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, "12345", "ARB2");
		var arbeidsforholdUtenInntektsmelding2 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, "12345", "ARB3");

		Periode beregningsperiode = Periode.of(SKJÆRINGSTIDSPUNKT.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));
		Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(31452)).medMåned(beregningsperiode.getFom())
				.medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(48739)).medMåned(beregningsperiode.getFom().plusMonths(1)).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(44810)).medMåned(beregningsperiode.getTom()).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());

		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(10000))
				.medArbeidsgiver(arbeidsforholdMedInntektsmelding).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING).build());


		Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(),
				inntektsgrunnlag,
				List.of(AktivitetStatus.ATFL),
				List.of(arbeidsforholdMedInntektsmelding, arbeidsforholdUtenInntektsmelding, arbeidsforholdUtenInntektsmelding2),
				Optional.of(new OmsorgspengerGrunnlag(false, false)));

		BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPrArbeidsforhold arbeidstakerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(1);
		BeregningsgrunnlagPrArbeidsforhold.builder(arbeidstakerStatus).medBeregningsperiode(beregningsperiode);
		//Act
		Evaluation resultat = new BeregnPrArbeidsforholdFraAOrdningen(arbeidstakerStatus).evaluate(periode);
		//Assert
		assertThat(resultat.result()).isEqualTo(Resultat.JA);
		assertThat(arbeidstakerStatus.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(190002));
	}

	@Test
	void OMS_Skal_gi_fordele_restinntekt_fra_aordningen_til_to_arbeidsforhold_uten_inntektsmelding_med_to_arbeidsforhold_med_inntektsmelding() {
		//Arrange
		LocalDate arbeidsforholStartdato = SKJÆRINGSTIDSPUNKT.minusYears(2);
		var arbeidsforholdMedInntektsmelding = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, "12345", "ARB1");
		var arbeidsforholdMedInntektsmelding2 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, "12345", "ARB4");
		var arbeidsforholdUtenInntektsmelding = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, "12345", "ARB2");
		var arbeidsforholdUtenInntektsmelding2 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, "12345", "ARB3");

		Periode beregningsperiode = Periode.of(SKJÆRINGSTIDSPUNKT.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));
		Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(31452)).medMåned(beregningsperiode.getFom())
				.medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(48739)).medMåned(beregningsperiode.getFom().plusMonths(1)).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(44810)).medMåned(beregningsperiode.getTom()).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());

		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(5000))
				.medArbeidsgiver(arbeidsforholdMedInntektsmelding).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING).build());
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(5000))
				.medArbeidsgiver(arbeidsforholdMedInntektsmelding2).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING).build());

		Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(),
				inntektsgrunnlag,
				List.of(AktivitetStatus.ATFL),
				List.of(arbeidsforholdMedInntektsmelding, arbeidsforholdMedInntektsmelding2, arbeidsforholdUtenInntektsmelding, arbeidsforholdUtenInntektsmelding2),
				Optional.of(new OmsorgspengerGrunnlag(false, false)));

		BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPrArbeidsforhold arbeidstakerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(1);
		BeregningsgrunnlagPrArbeidsforhold.builder(arbeidstakerStatus).medBeregningsperiode(beregningsperiode);
		//Act
		Evaluation resultat = new BeregnPrArbeidsforholdFraAOrdningen(arbeidstakerStatus).evaluate(periode);
		//Assert
		assertThat(resultat.result()).isEqualTo(Resultat.JA);
		assertThat(arbeidstakerStatus.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(190002));
	}

}
