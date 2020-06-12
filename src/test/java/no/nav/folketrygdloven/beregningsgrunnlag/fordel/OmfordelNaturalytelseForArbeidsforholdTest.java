package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;

class OmfordelNaturalytelseForArbeidsforholdTest {


    public static final Arbeidsforhold ARBEID1 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr("12343543543").build();
    public static final Arbeidsforhold ARBEID2 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr("087435984").build();

    @Test
    void skal_omfordele_naturalytelse() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold aktivitet = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medRefusjonskravPrÅr(BigDecimal.valueOf(100_000))
            .medAndelNr(1L)
            .medArbeidsforhold(ARBEID1)
            .medFordeltPrÅr(BigDecimal.valueOf(50_000))
            .build();
        BeregningsgrunnlagPrArbeidsforhold arbeidMedBortfaltNatYtelsePrÅr = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medNaturalytelseBortfaltPrÅr(BigDecimal.valueOf(50_000))
            .medAndelNr(2L)
            .medFordeltPrÅr(BigDecimal.ZERO)
            .medArbeidsforhold(ARBEID2)
            .build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now().plusMonths(1)))
            .medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.ATFL)
                .medArbeidsforhold(arbeidMedBortfaltNatYtelsePrÅr)
                .medArbeidsforhold(aktivitet)
                .build())
            .build();

        // Act
        new OmfordelNaturalytelseForArbeidsforhold(periode).omfordelForArbeidsforhold(aktivitet, (periode1) -> Optional.of(arbeidMedBortfaltNatYtelsePrÅr));

        // Assert
        assertThat(arbeidMedBortfaltNatYtelsePrÅr.getNaturalytelseBortfaltPrÅr().get()).isEqualTo(BigDecimal.ZERO);
        assertThat(aktivitet.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
    }

}
