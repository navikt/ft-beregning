package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

class FastsettAndelLikBruttoBGTest {

	private static final LocalDate STP = LocalDate.now();
	private static final String ORGNR = "995";
	private static final String ORGNR2 = "32423";

	@Test
	void skal_sette_andelsmessig_før_gradering_for_alle_arbeidsforhold() {
		// Arrange
		var arbeidsforholdSøktFor = lagArbeidsforhold(BigDecimal.valueOf(100_000), BigDecimal.valueOf(100), ORGNR, 1L);
		var arbeidsforholdIkkeSøktFor = lagArbeidsforhold(BigDecimal.valueOf(50_000), BigDecimal.valueOf(0),
				ORGNR2, 2L);

		var atflStatus = lagATFL(List.of(arbeidsforholdSøktFor, arbeidsforholdIkkeSøktFor));

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medBeregningsgrunnlagPrStatus(atflStatus)
				.medPeriode(Periode.of(STP, null))
				.build();


		// Act
		new FastsettAndelLikBruttoBG().evaluate(periode);


		// Assert
		assertThat(arbeidsforholdIkkeSøktFor.getAndelsmessigFørGraderingPrAar()).isEqualTo(BigDecimal.valueOf(50_000));
		assertThat(arbeidsforholdSøktFor.getAndelsmessigFørGraderingPrAar()).isEqualTo(BigDecimal.valueOf(100_000));
	}

	private BeregningsgrunnlagPrArbeidsforhold lagArbeidsforhold(BigDecimal beregnetPrÅr,
	                                                             BigDecimal utbetalingsgrad,
	                                                             String orgnr,
	                                                             long andelNr) {
		return BeregningsgrunnlagPrArbeidsforhold.builder()
				.medAndelNr(andelNr)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr, null))
				.medBruttoPrÅr(beregnetPrÅr)
				.medUtbetalingsprosent(utbetalingsgrad)
				.build();
	}

	private BeregningsgrunnlagPrStatus lagATFL(List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold) {
		var builder = BeregningsgrunnlagPrStatus.builder()
				.medAktivitetStatus(AktivitetStatus.ATFL);
		arbeidsforhold.forEach(builder::medArbeidsforhold);
		return builder.build();
	}

}