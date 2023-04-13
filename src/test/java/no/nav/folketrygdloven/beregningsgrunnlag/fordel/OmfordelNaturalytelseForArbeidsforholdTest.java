package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;


import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

class OmfordelNaturalytelseForArbeidsforholdTest {


    public static final Arbeidsforhold ARBEID1 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr("12343543543").build();
    public static final Arbeidsforhold ARBEID2 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr("087435984").build();

    @Test
    void skal_omfordele_naturalytelse() {
        // Arrange
	    var aktivitet = FordelAndelModell.builder()
			    .medGjeldendeRefusjonPrÅr(BigDecimal.valueOf(100_000))
			    .medAndelNr(1L)
			    .medArbeidsforhold(ARBEID1)
			    .medFordeltPrÅr(BigDecimal.valueOf(50_000))
			    .medAktivitetStatus(AktivitetStatus.AT)
			    .build();
	    var arbeidMedBortfaltNatYtelsePrÅr = FordelAndelModell.builder()
            .medNaturalytelseBortfaltPrÅr(BigDecimal.valueOf(50_000))
            .medAndelNr(2L)
            .medFordeltPrÅr(BigDecimal.ZERO)
            .medArbeidsforhold(ARBEID2)
		    .medAktivitetStatus(AktivitetStatus.AT)
		    .build();
	    var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), LocalDate.now().plusMonths(1)), Arrays.asList(aktivitet, arbeidMedBortfaltNatYtelsePrÅr));

        // Act
        new OmfordelNaturalytelseForArbeidsforhold(new FordelModell(periode)).omfordelForArbeidsforhold(aktivitet, (periode1) -> Optional.of(arbeidMedBortfaltNatYtelsePrÅr));

        // Assert
        assertThat(arbeidMedBortfaltNatYtelsePrÅr.getNaturalytelseBortfaltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(aktivitet.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
    }

}
