package no.nav.folketrygdloven.beregningsgrunnlag.perioder.utbetalingsgrad;


import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.AndelUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.PeriodeModellUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.Utbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

class FastsettPeriodeUtbetalingsgradRegelTest {

	private static final LocalDate MANDAG = LocalDate.of(2021, 11, 8);
	private static final LocalDate SKJÆRINGSTIDSPUNKT = MANDAG;
	private static final LocalDate FREDAG = LocalDate.of(2021, 11, 12);
	private static final LocalDate PÅFØLGENDE_MANDAG = LocalDate.of(2021, 11, 15);
	private static final String ORGNR = "999999999";
	private static final String ORGNR2 = "999999998";

	@Test
	void skalLageNyAndelForSVPForArbeidsforholdMedSøktYtelseFraSTP() {

		Arbeidsforhold arbeidsforhold2 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr(ORGNR2)
				.medAnsettelsesPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(12))).build();
		PeriodeModellUtbetalingsgrad inputMedGraderingFraStartForNyttArbeid = lagPeriodeInputMedEnAndelFraStart()
				.medEndringISøktYtelse(lagGraderingFraSkjæringstidspunkt(arbeidsforhold2))
				.build();
		List<SplittetPeriode> perioder = new ArrayList<>();
		kjørRegel(inputMedGraderingFraStartForNyttArbeid, perioder);
		assertThat(perioder).hasSize(2);
		assertThat(perioder.get(0).getNyeAndeler()).hasSize(1);
		assertThat(perioder.get(1).getNyeAndeler()).isEmpty();
	}

	@Test
	void skalHaNyAndelIHelgMellomToPerioderMedUtbetaling() {

		Arbeidsforhold arbeidsforhold2 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr(ORGNR2)
				.medAnsettelsesPeriode(Periode.of(MANDAG, MANDAG.plusMonths(12))).build();
		var utbetalingsgrader = List.of(AndelUtbetalingsgrad.builder().medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(arbeidsforhold2)
				.medNyAktivitetFraDato(MANDAG)
				.medUtbetalingsgrader(List.of(new Utbetalingsgrad(Periode.of(MANDAG, FREDAG), BigDecimal.valueOf(50)),
						new Utbetalingsgrad(Periode.of(PÅFØLGENDE_MANDAG, PÅFØLGENDE_MANDAG.plusDays(1)), BigDecimal.valueOf(50)))).build());

		PeriodeModellUtbetalingsgrad inputMedGraderingFraStartForNyttArbeid = lagPeriodeInputMedEnAndelFraStart()
				.medEndringISøktYtelse(utbetalingsgrader)
				.build();
		List<SplittetPeriode> perioder = new ArrayList<>();
		kjørRegel(inputMedGraderingFraStartForNyttArbeid, perioder);
		assertThat(perioder).hasSize(4);
		assertThat(perioder.get(0).getNyeAndeler()).hasSize(1);
		assertThat(perioder.get(1).getNyeAndeler()).hasSize(1);
		assertThat(perioder.get(2).getNyeAndeler()).hasSize(1);
		assertThat(perioder.get(3).getNyeAndeler()).isEmpty();
	}


	@Test
	void skalHaNyAndelIPeriodeMedUtbetalingUtenRefusjonMedEksisterendePeriode() {

		Arbeidsforhold arbeidsforhold2 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr(ORGNR2)
				.medAnsettelsesPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(12))).build();
		var utbetalingsgrader = List.of(AndelUtbetalingsgrad.builder().medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(arbeidsforhold2)
				.medNyAktivitetFraDato(SKJÆRINGSTIDSPUNKT)
				.medUtbetalingsgrader(List.of(new Utbetalingsgrad(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(3)), BigDecimal.valueOf(50)))).build());

		PeriodeModellUtbetalingsgrad inputMedGraderingFraStartForNyttArbeid = lagPeriodeInputMedEnAndelFraStart()
				.medEksisterendePerioder(List.of(
						SplittetPeriode.builder().medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1))).medPeriodeÅrsaker(List.of(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET)).build(),
						SplittetPeriode.builder().medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT.plusMonths(2), TIDENES_ENDE)).medPeriodeÅrsaker(List.of(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET)).build()))
				.medEndringISøktYtelse(utbetalingsgrader)
				.build();
		List<SplittetPeriode> perioder = new ArrayList<>();
		kjørRegel(inputMedGraderingFraStartForNyttArbeid, perioder);
		assertThat(perioder).hasSize(3);
		assertThat(perioder.get(0).getNyeAndeler()).hasSize(1);
		assertThat(perioder.get(1).getNyeAndeler()).hasSize(1);
		assertThat(perioder.get(2).getNyeAndeler()).isEmpty();
	}

	@Test
	void skalHaNyAndelIPeriodeMedUtbetalingEnDag() {

		Arbeidsforhold arbeidsforhold2 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr(ORGNR2)
				.medAnsettelsesPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(12))).build();
		var utbetalingsgrader = List.of(AndelUtbetalingsgrad.builder().medAktivitetStatus(AktivitetStatusV2.AT)
				.medArbeidsforhold(arbeidsforhold2)
				.medNyAktivitetFraDato(SKJÆRINGSTIDSPUNKT)
				.medUtbetalingsgrader(List.of(new Utbetalingsgrad(Periode.of(SKJÆRINGSTIDSPUNKT.plusMonths(3), SKJÆRINGSTIDSPUNKT.plusMonths(3)), BigDecimal.valueOf(50)))).build());

		PeriodeModellUtbetalingsgrad inputMedGraderingFraStartForNyttArbeid = lagPeriodeInputMedEnAndelFraStart()
				.medEksisterendePerioder(List.of(
						SplittetPeriode.builder().medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1))).medPeriodeÅrsaker(List.of(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET)).build(),
						SplittetPeriode.builder().medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT.plusMonths(2), TIDENES_ENDE)).medPeriodeÅrsaker(List.of(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET)).build()))
				.medEndringISøktYtelse(utbetalingsgrader)
				.build();
		List<SplittetPeriode> perioder = new ArrayList<>();
		kjørRegel(inputMedGraderingFraStartForNyttArbeid, perioder);
		assertThat(perioder).hasSize(4);
		assertThat(perioder.get(0).getNyeAndeler()).isEmpty();
		assertThat(perioder.get(1).getNyeAndeler()).isEmpty();
		assertThat(perioder.get(2).getNyeAndeler()).hasSize(1);
		assertThat(perioder.get(3).getNyeAndeler()).isEmpty();
	}


	private void kjørRegel(PeriodeModellUtbetalingsgrad inputMedGraderingFraStartForNyttArbeid, List<SplittetPeriode> perioder) {
		new FastsettPerioderForUtbetalingsgradRegel().evaluerRegel(inputMedGraderingFraStartForNyttArbeid, perioder);
	}

	private List<AndelUtbetalingsgrad> lagGraderingFraSkjæringstidspunkt(Arbeidsforhold arbeidsforhold2) {
		return List.of(AndelUtbetalingsgrad.builder().medAktivitetStatus(AktivitetStatusV2.AT)
				.medNyAktivitetFraDato(SKJÆRINGSTIDSPUNKT)
				.medArbeidsforhold(arbeidsforhold2)
				.medUtbetalingsgrader(List.of(new Utbetalingsgrad(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(1)), BigDecimal.valueOf(50)))).build());
	}

	private PeriodeModellUtbetalingsgrad.Builder lagPeriodeInputMedEnAndelFraStart() {
		Arbeidsforhold arbeidsforhold = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
				.medAnsettelsesPeriode(Periode.of(SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.plusMonths(12))).medOrgnr(ORGNR).build();
		BruttoBeregningsgrunnlag bruttoBeregningsgrunnlag = BruttoBeregningsgrunnlag.builder().medAktivitetStatus(AktivitetStatusV2.AT).medBruttoPrÅr(BigDecimal.valueOf(500_000)).medArbeidsforhold(arbeidsforhold).build();
		PeriodisertBruttoBeregningsgrunnlag periodisertBruttoBeregningsgrunnlag = PeriodisertBruttoBeregningsgrunnlag.builder()
				.medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
				.leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag)
				.build();
		return PeriodeModellUtbetalingsgrad.builder()
				.medGrunnbeløp(BigDecimal.valueOf(90_000))
				.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
				.medPeriodisertBruttoBeregningsgrunnlag(List.of(periodisertBruttoBeregningsgrunnlag))
				.medEksisterendePerioder(List.of(SplittetPeriode.builder()
						.medPeriodeÅrsaker(List.of())
						.medFørstePeriodeAndeler(List.of(EksisterendeAndel.builder().medArbeidsforhold(arbeidsforhold).medAndelNr(1L).build()))
						.medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
						.build()));
	}
}
