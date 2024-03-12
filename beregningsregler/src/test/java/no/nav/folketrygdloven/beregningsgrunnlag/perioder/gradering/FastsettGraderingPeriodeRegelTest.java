package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

class FastsettGraderingPeriodeRegelTest {

	private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2021, 11, 8);
	private static final String ORGNR = "999999999";
	private static final String ORGNR2 = "999999998";

	private void kjørRegel(PeriodeModellGradering inputMedGraderingFraStartForNyttArbeid, List<SplittetPeriode> perioder) {
		new FastsettPerioderGraderingRegel().evaluerRegel(inputMedGraderingFraStartForNyttArbeid, perioder);
	}

	@Test
	void skalLageNyAndelIPeriodeEtterGraderingperiodeMedNyAndel() {
		Arbeidsforhold arbeidsforhold2 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr(ORGNR2)
				.medAnsettelsesPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(12))).build();
		PeriodeModellGradering inputMedGraderingFraStartForNyttArbeid = lagPeriodeInputMedEnAndelFraStart()
				.medAndelGraderinger(lagGraderingFraSTP(arbeidsforhold2))
				.build();
		List<SplittetPeriode> perioder = new ArrayList<>();
		kjørRegel(inputMedGraderingFraStartForNyttArbeid, perioder);
		assertThat(perioder).hasSize(2);
		assertThat(perioder.get(0).getNyeAndeler()).hasSize(1);
		assertThat(perioder.get(1).getNyeAndeler()).hasSize(1);
	}


	private List<AndelGradering> lagGraderingFraSTP(Arbeidsforhold arbeidsforhold2) {
		return List.of(AndelGradering.builder()
				.medArbeidsforhold(Arbeidsforhold.builder(arbeidsforhold2)
						.medAnsettelsesPeriode(Periode.of(SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.plusMonths(12)))
						.build())
						.medNyAktivitetFraDato(SKJÆRINGSTIDSPUNKT)
				.medGraderinger(List.of(new Gradering(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(1))))).build());
	}

	private PeriodeModellGradering.Builder lagPeriodeInputMedEnAndelFraStart() {
		Arbeidsforhold arbeidsforhold = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
				.medAnsettelsesPeriode(Periode.of(SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.plusMonths(12))).medOrgnr(ORGNR).build();
		BruttoBeregningsgrunnlag bruttoBeregningsgrunnlag = BruttoBeregningsgrunnlag.builder().medAktivitetStatus(AktivitetStatusV2.AT).medBruttoPrÅr(BigDecimal.valueOf(500_000)).medArbeidsforhold(arbeidsforhold).build();
		PeriodisertBruttoBeregningsgrunnlag periodisertBruttoBeregningsgrunnlag = PeriodisertBruttoBeregningsgrunnlag.builder()
				.medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
				.leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag)
				.build();
		return PeriodeModellGradering.builder()
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
