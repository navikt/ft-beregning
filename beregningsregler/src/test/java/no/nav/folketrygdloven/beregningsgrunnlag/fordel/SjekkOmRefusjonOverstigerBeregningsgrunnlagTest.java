package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;


import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

class SjekkOmRefusjonOverstigerBeregningsgrunnlagTest {

    private static final LocalDate STP = LocalDate.now();
    private static final String ORGNR = "995";

    @Test
    void skal_gi_at_refusjon_overstiger_bg() {
        // Arrange
        var refusjonskravPrÅr = BigDecimal.valueOf(200_000);
        var beregnetPrÅr = BigDecimal.valueOf(100_000);
        var andel = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);

	    var periode = new FordelPeriodeModell(Periode.of(STP, null), Collections.singletonList(andel));

        // Act
        var evaluering = kjørRegel(andel, periode);

        // Assert
        assertThat(evaluering.result()).isEqualTo(Resultat.JA);
    }

    @Test
    void skal_gi_at_refusjon_ikkje_overstiger_bg() {
        // Arrange
	    var refusjonskravPrÅr = BigDecimal.valueOf(200_000);
	    var beregnetPrÅr = BigDecimal.valueOf(250_000);
        var andel = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);
	    var periode = new FordelPeriodeModell(Periode.of(STP, null), Collections.singletonList(andel));

        // Act
        var evaluering = kjørRegel(andel, periode);

        // Assert
        assertThat(evaluering.result()).isEqualTo(Resultat.NEI);
    }

    private FordelAndelModell lagArbeidsforhold(BigDecimal refusjonskravPrÅr, BigDecimal beregnetPrÅr) {
        return FordelAndelModell.builder()
            .medAndelNr(1L)
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR, null))
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
	        .medAktivitetStatus(AktivitetStatus.AT)
            .medGjeldendeRefusjonPrÅr(refusjonskravPrÅr)
            .medForeslåttPrÅr(beregnetPrÅr)
            .build();
    }

    private Evaluation kjørRegel(FordelAndelModell arbeidsforhold, FordelPeriodeModell periode) {
        SjekkOmRefusjonOverstigerBeregningsgrunnlag regel = new SjekkOmRefusjonOverstigerBeregningsgrunnlag(arbeidsforhold);
        return regel.evaluate(new FordelModell(periode));
    }

}
