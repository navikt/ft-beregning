package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.fpsak.nare.ServiceArgument;

public class OmfordelFraArbeidTest {

	private static final LocalDate STP = LocalDate.now();
	private static final String ORGNR1 = "995";
	private static final String ORGNR2 = "910";
	private static final String ORGNR3 = "973";


	@Test
	void skal_omfordele_inntekt_før_naturalytelse() {
		// Arrange
		var aktivitet = FordelAndelModell.builder()
				.medGjeldendeRefusjonPrÅr(BigDecimal.valueOf(100_000))
				.medAndelNr(1L)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR1, null))
				.medFordeltPrÅr(BigDecimal.valueOf(50_000))
				.medAktivitetStatus(AktivitetStatus.AT)
				.build();
		var arbeidMedBortfaltNatYtelsePrÅr = FordelAndelModell.builder()
				.medNaturalytelseBortfaltPrÅr(BigDecimal.valueOf(50_000))
				.medAndelNr(2L)
				.medForeslåttPrÅr(BigDecimal.valueOf(50_000))
				.medAktivitetStatus(AktivitetStatus.AT)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2, null))
				.build();

		var periode = new FordelPeriodeModell(Periode.of(STP, null), Arrays.asList(aktivitet, arbeidMedBortfaltNatYtelsePrÅr));

		// Act
		kjørRegel(aktivitet, periode);

		// Assert
		assertThat(arbeidMedBortfaltNatYtelsePrÅr.getNaturalytelseBortfaltPrÅr().orElseThrow()).isEqualTo(BigDecimal.valueOf(50_000));
		assertThat(arbeidMedBortfaltNatYtelsePrÅr.getFordeltPrÅr().orElseThrow().compareTo(BigDecimal.ZERO) == 0).isTrue();
		assertThat(aktivitet.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
	}

	@Test
	void skal_omfordele_både_inntekt_og_naturalytelse() {
		// Arrange
		var aktivitet = FordelAndelModell.builder()
				.medGjeldendeRefusjonPrÅr(BigDecimal.valueOf(150_000))
				.medAndelNr(1L)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR1, null))
				.medAktivitetStatus(AktivitetStatus.AT)
				.medFordeltPrÅr(BigDecimal.valueOf(50_000))
				.build();
		var arbeidMedBortfaltNatYtelsePrÅr = FordelAndelModell.builder()
				.medNaturalytelseBortfaltPrÅr(BigDecimal.valueOf(50_000))
				.medAndelNr(2L)
				.medForeslåttPrÅr(BigDecimal.valueOf(50_000))
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2, null))
				.medAktivitetStatus(AktivitetStatus.AT)
				.build();

		var periode = new FordelPeriodeModell(Periode.of(STP, null), Arrays.asList(aktivitet, arbeidMedBortfaltNatYtelsePrÅr));

		// Act
		kjørRegel(aktivitet, periode);

		// Assert
		assertThat(arbeidMedBortfaltNatYtelsePrÅr.getNaturalytelseBortfaltPrÅr().orElseThrow().compareTo(BigDecimal.ZERO) == 0 ).isTrue();
		assertThat(arbeidMedBortfaltNatYtelsePrÅr.getFordeltPrÅr().orElseThrow().compareTo(BigDecimal.ZERO) == 0).isTrue();
		assertThat(aktivitet.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
	}


	@Test
	public void skal_flytte_beregningsgrunnlag_fra_et_annet_arbeidsforhold() {
		// Arrange
		var refusjonskrav1 = BigDecimal.valueOf(200_000);
		var beregnetPrÅr1 = BigDecimal.valueOf(100_000);
		var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1, BigDecimal.valueOf(100));

		var refusjonskrav2 = BigDecimal.ZERO;
		var beregnetPrÅr2 = BigDecimal.valueOf(100_000);
		var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2, BigDecimal.valueOf(100));

		var periode = new FordelPeriodeModell(Periode.of(STP, null), Arrays.asList(a1, a2));

		// Act
		kjørRegel(a1, periode);

		// Assert
		assertThat(a1.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(refusjonskrav1);
		assertThat(a2.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	public void skal_flytte_beregningsgrunnlag_fra_et_annet_arbeidsforhold_med_ulik_utbetalingsgrad() {
		// slik regner vi det ut:
		// Kompensert grunnlag for A2 er 100_000 * 0,25 = 25_0000
		// Dette grunnlaget flyttes til A1 og skaleres opp slik at skalert_grunnlag * utbetalingsgrad_A1 = 25_0000
		// Dette gir skalert_grunnlag = 25_000/0,5 = 50_0000
		// Så tar vi summen av dette og eksisterende grunnlag på A1: 100_000 + 50_0000 = 150_000

		// Arrange
		var refusjonskrav1 = BigDecimal.valueOf(200_000);
		var beregnetPrÅr1 = BigDecimal.valueOf(100_000);
		var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1, BigDecimal.valueOf(50));

		var refusjonskrav2 = BigDecimal.ZERO;
		var beregnetPrÅr2 = BigDecimal.valueOf(100_000);
		var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2, BigDecimal.valueOf(25));

		var periode = new FordelPeriodeModell(Periode.of(STP, null), Arrays.asList(a1, a2));

		// Act
		kjørRegel(a1, periode);

		// Assert
		assertThat(a1.getFordeltPrÅr().orElseThrow().compareTo(BigDecimal.valueOf(150_000)) == 0).isTrue();
		assertThat(a2.getFordeltPrÅr().orElseThrow().compareTo(BigDecimal.ZERO) == 0).isTrue();
	}


	@Test
	public void skal_flytte_deler_av_beregningsgrunnlag_fra_et_annet_arbeidsforhold() {
		// Arrange
		var refusjonskrav1 = BigDecimal.valueOf(200_000);
		var beregnetPrÅr1 = BigDecimal.valueOf(100_000);
		var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1, BigDecimal.valueOf(100));

		var refusjonskrav2 = BigDecimal.ZERO;
		var beregnetPrÅr2 = BigDecimal.valueOf(150_000);
		var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2, BigDecimal.valueOf(100));

		var periode = new FordelPeriodeModell(Periode.of(STP, null), Arrays.asList(a1, a2));

		// Act
		kjørRegel(a1, periode);

		// Assert
		assertThat(a1.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(refusjonskrav1);
		assertThat(a2.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
	}

	@Test
	public void skal_flytte_beregningsgrunnlag_fra_to_arbeidsforhold() {
		// Arrange
		var refusjonskrav1 = BigDecimal.valueOf(300_000);
		var beregnetPrÅr1 = BigDecimal.valueOf(100_000);
		var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1, BigDecimal.valueOf(100));

		var refusjonskrav2 = BigDecimal.ZERO;
		var beregnetPrÅr2 = BigDecimal.valueOf(100_000);
		var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2, BigDecimal.valueOf(100));

		var refusjonskrav3 = BigDecimal.ZERO;
		var beregnetPrÅr3 = BigDecimal.valueOf(100_000);
		var a3 = lagArbeidsforhold(refusjonskrav3, beregnetPrÅr3, 3L, ORGNR3, BigDecimal.valueOf(100));

		var periode = new FordelPeriodeModell(Periode.of(STP, null), Arrays.asList(a1, a2, a3));

		// Act
		kjørRegel(a1, periode);

		// Assert
		assertThat(a1.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(refusjonskrav1);
		assertThat(a2.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(a3.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	public void skal_kun_flytte_beregningsgrunnlag_fra_arbeidsforhold_som_har_lavere_refusjon_enn_beregningsgrunnlag() {
		// Arrange
		var refusjonskrav1 = BigDecimal.valueOf(300_000);
		var beregnetPrÅr1 = BigDecimal.valueOf(100_000);
		var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1, BigDecimal.valueOf(100));

		var refusjonskrav2 = BigDecimal.valueOf(100_000);
		var beregnetPrÅr2 = BigDecimal.valueOf(100_000);
		var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2, BigDecimal.valueOf(100));

		var refusjonskrav3 = BigDecimal.ZERO;
		var beregnetPrÅr3 = BigDecimal.valueOf(100_000);
		var a3 = lagArbeidsforhold(refusjonskrav3, beregnetPrÅr3, 3L, ORGNR3, BigDecimal.valueOf(100));

		var periode = new FordelPeriodeModell(Periode.of(STP, null), Arrays.asList(a1, a2, a3));

		// Act
		kjørRegel(a1, periode);

		// Assert
		assertThat(a1.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(200_000));
		assertThat(a2.getFordeltPrÅr()).isEmpty();
		assertThat(a2.getBruttoInkludertNaturalytelsePrÅr().orElseThrow()).isEqualByComparingTo(beregnetPrÅr2);
		assertThat(a3.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
	}


	private FordelAndelModell lagArbeidsforhold(BigDecimal refusjonskravPrÅr, BigDecimal beregnetPrÅr, Long andelsnr, String orgnr, BigDecimal utbetalingsgrad) {
		return FordelAndelModell.builder()
				.medAndelNr(andelsnr)
				.medInntektskategori(Inntektskategori.ARBEIDSTAKER)
				.medAktivitetStatus(AktivitetStatus.AT)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr, null))
				.medGjeldendeRefusjonPrÅr(refusjonskravPrÅr)
				.medForeslåttPrÅr(beregnetPrÅr)
				.medUtbetalingsgrad(utbetalingsgrad)
				.build();
	}

	private void kjørRegel(FordelAndelModell arbeidsforhold, FordelPeriodeModell periode) {
		OmfordelFraArbeid regel = new OmfordelFraArbeid();
		regel.evaluate(new FordelModell(periode), new ServiceArgument("arbeidsforhold", arbeidsforhold));
	}
}
