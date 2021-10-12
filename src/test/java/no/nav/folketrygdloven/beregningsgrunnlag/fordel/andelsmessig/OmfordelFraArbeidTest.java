package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;

public class OmfordelFraArbeidTest {

    private static final LocalDate STP = LocalDate.now();
    private static final String ORGNR1 = "995";
    private static final String ORGNR2 = "910";    private static final String ORGNR3 = "973";


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
        assertThat(arbeidMedBortfaltNatYtelsePrÅr.getFordeltPrÅr().orElseThrow()).isEqualTo(BigDecimal.ZERO);
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
        assertThat(arbeidMedBortfaltNatYtelsePrÅr.getNaturalytelseBortfaltPrÅr().orElseThrow()).isEqualTo(BigDecimal.ZERO);
        assertThat(arbeidMedBortfaltNatYtelsePrÅr.getFordeltPrÅr().orElseThrow()).isEqualTo(BigDecimal.ZERO);
        assertThat(aktivitet.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
    }


    @Test
    public void skal_flytte_beregningsgrunnlag_fra_et_annet_arbeidsforhold() {
        // Arrange
	    var refusjonskrav1 = BigDecimal.valueOf(200_000);
	    var beregnetPrÅr1 = BigDecimal.valueOf(100_000);
	    var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

	    var refusjonskrav2 = BigDecimal.ZERO;
	    var beregnetPrÅr2 = BigDecimal.valueOf(100_000);
	    var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

	    var periode = new FordelPeriodeModell(Periode.of(STP, null), Arrays.asList(a1, a2));

        // Act
        kjørRegel(a1, periode);

        // Assert
        assertThat(a1.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(refusjonskrav1);
        assertThat(a2.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_flytte_deler_av_beregningsgrunnlag_fra_et_annet_arbeidsforhold() {
        // Arrange
        var refusjonskrav1 = BigDecimal.valueOf(200_000);
        var beregnetPrÅr1 = BigDecimal.valueOf(100_000);
        var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

        var refusjonskrav2 = BigDecimal.ZERO;
        var beregnetPrÅr2 = BigDecimal.valueOf(150_000);
        var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

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
        var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

        var refusjonskrav2 = BigDecimal.ZERO;
        var beregnetPrÅr2 = BigDecimal.valueOf(100_000);
        var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

        var refusjonskrav3 = BigDecimal.ZERO;
        var beregnetPrÅr3 = BigDecimal.valueOf(100_000);
        var a3 = lagArbeidsforhold(refusjonskrav3, beregnetPrÅr3, 3L, ORGNR3);

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
	    var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

	    var refusjonskrav2 = BigDecimal.valueOf(100_000);
	    var beregnetPrÅr2 = BigDecimal.valueOf(100_000);
	    var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

	    var refusjonskrav3 = BigDecimal.ZERO;
	    var beregnetPrÅr3 = BigDecimal.valueOf(100_000);
	    var a3 = lagArbeidsforhold(refusjonskrav3, beregnetPrÅr3, 3L, ORGNR3);

	    var periode = new FordelPeriodeModell(Periode.of(STP, null), Arrays.asList(a1, a2, a3));

	    // Act
        kjørRegel(a1, periode);

        // Assert
        assertThat(a1.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(200_000));
        assertThat(a2.getFordeltPrÅr()).isEmpty();
        assertThat(a2.getBruttoInkludertNaturalytelsePrÅr().orElseThrow()).isEqualByComparingTo(beregnetPrÅr2);
        assertThat(a3.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
    }


    private FordelAndelModell lagArbeidsforhold(BigDecimal refusjonskravPrÅr, BigDecimal beregnetPrÅr, Long andelsnr, String orgnr) {
        return FordelAndelModell.builder()
            .medAndelNr(andelsnr)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
	        .medAktivitetStatus(AktivitetStatus.AT)
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr, null))
            .medGjeldendeRefusjonPrÅr(refusjonskravPrÅr)
            .medForeslåttPrÅr(beregnetPrÅr)
            .build();
    }

    private void kjørRegel(FordelAndelModell arbeidsforhold, FordelPeriodeModell periode) {
        OmfordelFraArbeid regel = new OmfordelFraArbeid(arbeidsforhold);
        regel.evaluate(new FordelModell(periode));
    }
}
