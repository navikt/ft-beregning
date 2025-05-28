package no.nav.folketrygdloven.beregningsgrunnlag.perioder.naturalytelse;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.NaturalytelserPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodeModellNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.fpsak.tidsserie.LocalDateInterval;

class FastsettPerioderNaturalytelseRegelTest {
	private static final LocalDate STP = LocalDate.of(2023,5,1);

	@Test
	void skal_periodisere_en_naturalytelse() {
		// Arrange
		var arbfor = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999");
		var periode = lagEksisterendePeriode(STP, etterStp(50), lagEksisterendeAndel(arbfor, 1L));
		var naturalytelse = lagNaturalytelse(arbfor, 1L, new NaturalYtelse(BigDecimal.valueOf(5000), STP, etterStp(20)));

		var modell = PeriodeModellNaturalytelse.builder()
				.medEksisterendePerioder(Arrays.asList(periode))
				.medGrunnbeløp(BigDecimal.valueOf(100000))
				.medSkjæringstidspunkt(STP)
				.medInntektsmeldinger(Arrays.asList(naturalytelse))
				.build();

		// Act
		var regel = new FastsettPerioderNaturalytelseRegel();
		var splittedePerioder = new ArrayList<SplittetPeriode>();
		regel.evaluer(modell, splittedePerioder);

		// Assert
		assertThat(splittedePerioder).hasSize(2);
		assertPeriode(splittedePerioder, STP, etterStp(20));
		assertPeriode(splittedePerioder, etterStp(21), LocalDateInterval.TIDENES_ENDE);
	}
	@Test
	void skal_periodisere_tre_naturalytelser() {
		// Arrange
		var arbfor = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999");
		var periode = lagEksisterendePeriode(STP, etterStp(50), lagEksisterendeAndel(arbfor, 1L));
		var naturalytelse = lagNaturalytelse(arbfor, 1L, new NaturalYtelse(BigDecimal.valueOf(5000), STP, etterStp(20)),
				new NaturalYtelse(BigDecimal.valueOf(5000), STP, etterStp(30)),
				new NaturalYtelse(BigDecimal.valueOf(5000), STP, etterStp(40)));

		var modell = PeriodeModellNaturalytelse.builder()
				.medEksisterendePerioder(Arrays.asList(periode))
				.medGrunnbeløp(BigDecimal.valueOf(100000))
				.medSkjæringstidspunkt(STP)
				.medInntektsmeldinger(Arrays.asList(naturalytelse))
				.build();

		// Act
		var regel = new FastsettPerioderNaturalytelseRegel();
		var splittedePerioder = new ArrayList<SplittetPeriode>();
		regel.evaluer(modell, splittedePerioder);

		// Assert
		assertThat(splittedePerioder).hasSize(4);
		assertPeriode(splittedePerioder, STP, etterStp(20));
		assertPeriode(splittedePerioder, etterStp(21), etterStp(30));
		assertPeriode(splittedePerioder, etterStp(31), etterStp(40));
		assertPeriode(splittedePerioder, etterStp(41), LocalDateInterval.TIDENES_ENDE);
	}

	private void assertPeriode(ArrayList<SplittetPeriode> splittedePerioder, LocalDate fom, LocalDate tom) {
		var match = splittedePerioder.stream().filter(p -> p.getPeriode().getFom().equals(fom) && p.getPeriode().getTom().equals(tom)).findFirst();
		assertThat(match).isPresent();
		assertThat(match.get().getEksisterendePeriodeAndeler()).hasSize(1);
	}

	private NaturalytelserPrArbeidsforhold lagNaturalytelse(Arbeidsforhold arbfor, long andelsnr, NaturalYtelse... nat) {
		return NaturalytelserPrArbeidsforhold.builder()
				.medNaturalytelser(Arrays.asList(nat))
				.medArbeidsforhold(arbfor)
				.medAndelsnr(andelsnr)
				.build();

	}


	private SplittetPeriode lagEksisterendePeriode(LocalDate fom, LocalDate tom, EksisterendeAndel... andel) {
		return SplittetPeriode.builder()
				.medPeriode(Periode.of(fom, tom))
				.medPeriodeÅrsaker(Collections.emptyList())
				.medFørstePeriodeAndeler(Arrays.asList(andel))
				.build();
	}

	private LocalDate etterStp(int dager) {
		return STP.plusDays(dager);
	}

	private EksisterendeAndel lagEksisterendeAndel(Arbeidsforhold arbeidsforhold, Long andelsnr) {
		return EksisterendeAndel.builder()
				.medArbeidsforhold(arbeidsforhold)
				.medAndelNr(andelsnr)
				.build();
	}

}
