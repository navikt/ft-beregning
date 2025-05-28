package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;

class OmfordelFraFrilansTest {

    private static final LocalDate STP = LocalDate.now();
    private static final String ORGNR = "995";

    @Test
    void skal_ikkje_flytte_om_det_ikkje_finnes_frilans() {
        // Arrange
        var refusjonskravPrÅr = BigDecimal.valueOf(200_000);
        var beregnetPrÅr = BigDecimal.valueOf(100_000);
        var arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);
	    var periode = new FordelPeriodeModell(Periode.of(STP, null), Collections.singletonList(arbeidsforhold));

        // Act
        kjørRegel(arbeidsforhold, periode);

        // Assert
        assertThat(arbeidsforhold.getFordeltPrÅr()).isEmpty();
        assertThat(arbeidsforhold.getBruttoInkludertNaturalytelsePrÅr().orElseThrow()).isEqualByComparingTo(beregnetPrÅr);
    }

    @Test
    void skal_flytte_fra_frilans_til_arbeid_frilans_avkortet_til_0() {
        // Arrange
        var refusjonskravPrÅr = BigDecimal.valueOf(200_000);
        var beregnetPrÅr = BigDecimal.valueOf(100_000);
        var arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);
        var beregnetPrÅrFL = BigDecimal.valueOf(100_000);
        var frilans = lagFLArbeidsforhold(beregnetPrÅrFL);
	    var periode = new FordelPeriodeModell(Periode.of(STP, null),  new ArrayList<>(Arrays.asList(arbeidsforhold, frilans)));

        // Act
        kjørRegel(arbeidsforhold, periode);

        // Assert
	    var alleArbeidsforhold = periode.getAlleAndelerForStatus(AktivitetStatus.AT);
	    assertThat(alleArbeidsforhold).hasSize(2);
        assertThat(totalBrutto(alleArbeidsforhold)).isEqualByComparingTo(refusjonskravPrÅr);
        var flyttetFraFL = alleArbeidsforhold.stream().filter(a ->
            a.getArbeidsforhold().equals(arbeidsforhold.getArbeidsforhold())
            && a.getInntektskategori().equals(Inntektskategori.FRILANSER)).findFirst().orElseThrow();
        assertThat(flyttetFraFL.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(beregnetPrÅrFL);
        assertThat(flyttetFraFL.getGjeldendeRefusjonPrÅr().orElseThrow()).isEqualByComparingTo(beregnetPrÅrFL);

        assertThat(arbeidsforhold.getBruttoPrÅr().orElseThrow()).isEqualByComparingTo(beregnetPrÅr);
        assertThat(arbeidsforhold.getGjeldendeRefusjonPrÅr().orElseThrow()).isEqualByComparingTo(refusjonskravPrÅr.subtract(beregnetPrÅrFL));
        assertThat(frilans.getGjeldendeRefusjonPrÅr()).isEmpty();
        assertThat(frilans.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void skal_flytte_fra_frilans_til_arbeid_med_restbeløp_å_flytte() {
        // Arrange
        var refusjonskravPrÅr = BigDecimal.valueOf(200_000);
	    var beregnetPrÅr = BigDecimal.valueOf(100_000);
	    var arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);
	    var beregnetPrÅrFL = BigDecimal.valueOf(50_000);
	    var frilans = lagFLArbeidsforhold(beregnetPrÅrFL);

	    var periode = new FordelPeriodeModell(Periode.of(STP, null),  new ArrayList<>(Arrays.asList(arbeidsforhold, frilans)));

        // Act
        kjørRegel(arbeidsforhold, periode);

	    var alleArbeidsforhold = periode.getAlleAndelerForStatus(AktivitetStatus.AT);

        // Assert
        assertThat(alleArbeidsforhold).hasSize(2);
        assertThat(totalBrutto(alleArbeidsforhold)).isEqualByComparingTo(BigDecimal.valueOf(150_000));
        var flyttetFraFL = alleArbeidsforhold.stream()
            .filter(a -> a.getArbeidsforhold().equals(arbeidsforhold.getArbeidsforhold()) && a.getInntektskategori().equals(Inntektskategori.FRILANSER)).findFirst().orElseThrow();
        assertThat(flyttetFraFL.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(beregnetPrÅrFL);
        assertThat(flyttetFraFL.getGjeldendeRefusjonPrÅr().orElseThrow()).isEqualByComparingTo(beregnetPrÅrFL);

        assertThat(arbeidsforhold.getBruttoPrÅr().orElseThrow()).isEqualByComparingTo(beregnetPrÅr);
        assertThat(arbeidsforhold.getGjeldendeRefusjonPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
        assertThat(frilans.getGjeldendeRefusjonPrÅr()).isEmpty();
        assertThat(frilans.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void skal_flytte_fra_frilans_til_arbeid_med_restbeløp_på_frilans() {
        // Arrange
	    var refusjonskravPrÅr = BigDecimal.valueOf(200_000);
	    var beregnetPrÅr = BigDecimal.valueOf(100_000);
	    var arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);
	    var beregnetPrÅrFL = BigDecimal.valueOf(150_000);
	    var frilans = lagFLArbeidsforhold(beregnetPrÅrFL);

	    var periode = new FordelPeriodeModell(Periode.of(STP, null), new ArrayList<>(Arrays.asList(arbeidsforhold, frilans)));

        // Act
        kjørRegel(arbeidsforhold, periode);

        // Assert
	    var alleArbeidsforhold = periode.getAlleAndelerForStatus(AktivitetStatus.AT);
	    assertThat(alleArbeidsforhold).hasSize(2);
        assertThat(totalBrutto(alleArbeidsforhold)).isEqualByComparingTo(BigDecimal.valueOf(200_000));
        var flyttetFraFL = alleArbeidsforhold.stream()
            .filter(a -> a.getArbeidsforhold().equals(arbeidsforhold.getArbeidsforhold()) && a.getInntektskategori().equals(Inntektskategori.FRILANSER)).findFirst().orElseThrow();
        assertThat(flyttetFraFL.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
        assertThat(flyttetFraFL.getGjeldendeRefusjonPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(100_000));

        assertThat(arbeidsforhold.getBruttoPrÅr().orElseThrow()).isEqualByComparingTo(beregnetPrÅr);
        assertThat(arbeidsforhold.getGjeldendeRefusjonPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
        assertThat(frilans.getGjeldendeRefusjonPrÅr()).isEmpty();
        assertThat(frilans.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
    }

    private FordelAndelModell lagArbeidsforhold(BigDecimal refusjonskravPrÅr, BigDecimal beregnetPrÅr) {
        return FordelAndelModell.builder()
            .medAndelNr(1L)
	        .medAktivitetStatus(AktivitetStatus.AT)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR, null))
            .medGjeldendeRefusjonPrÅr(refusjonskravPrÅr)
            .medForeslåttPrÅr(beregnetPrÅr)
            .build();
    }

	private BigDecimal totalBrutto(List<FordelAndelModell> alleArbeidsforhold) {
		return alleArbeidsforhold.stream().map(a -> a.getBruttoPrÅr().orElse(BigDecimal.ZERO)).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
	}

    private FordelAndelModell lagFLArbeidsforhold(BigDecimal beregnetPrÅr) {
        return FordelAndelModell.builder()
            .medAndelNr(2L)
	        .medAktivitetStatus(AktivitetStatus.FL)
	        .medInntektskategori(Inntektskategori.FRILANSER)
            .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
            .medForeslåttPrÅr(beregnetPrÅr)
            .build();
    }

    private void kjørRegel(FordelAndelModell arbeidsforhold, FordelPeriodeModell periode) {
        var regel = new OmfordelFraFrilans(arbeidsforhold);
        regel.evaluate(new FordelModell(periode));
    }

}
