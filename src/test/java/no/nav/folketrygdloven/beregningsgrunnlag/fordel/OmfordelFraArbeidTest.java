package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;

public class OmfordelFraArbeidTest {

    private static final LocalDate STP = LocalDate.now();
    private static final String ORGNR1 = "995";
    private static final String ORGNR2 = "910";    private static final String ORGNR3 = "973";


    @Test
    public void skal_flytte_beregningsgrunnlag_fra_et_annet_arbeidsforhold() {
        // Arrange
        BigDecimal refusjonskrav1 = BigDecimal.valueOf(200_000);
        BigDecimal beregnetPrÅr1 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

        BigDecimal refusjonskrav2 = BigDecimal.ZERO;
        BigDecimal beregnetPrÅr2 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

        BeregningsgrunnlagPrStatus atfl = lagATFL(List.of(a1, a2));

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        // Act
        kjørRegel(a1, periode);

        // Assert
        assertThat(a1.getFordeltPrÅr()).isEqualByComparingTo(refusjonskrav1);
        assertThat(a2.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_flytte_deler_av_beregningsgrunnlag_fra_et_annet_arbeidsforhold() {
        // Arrange
        BigDecimal refusjonskrav1 = BigDecimal.valueOf(200_000);
        BigDecimal beregnetPrÅr1 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

        BigDecimal refusjonskrav2 = BigDecimal.ZERO;
        BigDecimal beregnetPrÅr2 = BigDecimal.valueOf(150_000);
        BeregningsgrunnlagPrArbeidsforhold a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

        BeregningsgrunnlagPrStatus atfl = lagATFL(List.of(a1, a2));

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        // Act
        kjørRegel(a1, periode);

        // Assert
        assertThat(a1.getFordeltPrÅr()).isEqualByComparingTo(refusjonskrav1);
        assertThat(a2.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
    }

    @Test
    public void skal_flytte_beregningsgrunnlag_fra_to_arbeidsforhold() {
        // Arrange
        BigDecimal refusjonskrav1 = BigDecimal.valueOf(300_000);
        BigDecimal beregnetPrÅr1 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

        BigDecimal refusjonskrav2 = BigDecimal.ZERO;
        BigDecimal beregnetPrÅr2 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

        BigDecimal refusjonskrav3 = BigDecimal.ZERO;
        BigDecimal beregnetPrÅr3 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a3 = lagArbeidsforhold(refusjonskrav3, beregnetPrÅr3, 3L, ORGNR3);

        BeregningsgrunnlagPrStatus atfl = lagATFL(List.of(a1, a2, a3));

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        // Act
        kjørRegel(a1, periode);

        // Assert
        assertThat(a1.getFordeltPrÅr()).isEqualByComparingTo(refusjonskrav1);
        assertThat(a2.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(a3.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_kun_flytte_beregningsgrunnlag_fra_arbeidsforhold_som_har_lavere_refusjon_enn_beregningsgrunnlag() {
        // Arrange
        BigDecimal refusjonskrav1 = BigDecimal.valueOf(300_000);
        BigDecimal beregnetPrÅr1 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

        BigDecimal refusjonskrav2 = BigDecimal.valueOf(100_000);
        BigDecimal beregnetPrÅr2 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

        BigDecimal refusjonskrav3 = BigDecimal.ZERO;
        BigDecimal beregnetPrÅr3 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a3 = lagArbeidsforhold(refusjonskrav3, beregnetPrÅr3, 3L, ORGNR3);

        BeregningsgrunnlagPrStatus atfl = lagATFL(List.of(a1, a2, a3));

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        // Act
        kjørRegel(a1, periode);

        // Assert
        assertThat(a1.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(200_000));
        assertThat(a2.getFordeltPrÅr()).isNull();
        assertThat(a2.getBruttoInkludertNaturalytelsePrÅr().get()).isEqualByComparingTo(beregnetPrÅr2);
        assertThat(a3.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
    }


    private BeregningsgrunnlagPrArbeidsforhold lagArbeidsforhold(BigDecimal refusjonskravPrÅr, BigDecimal beregnetPrÅr, Long andelsnr, String orgnr) {
        return BeregningsgrunnlagPrArbeidsforhold.builder()
            .medAndelNr(andelsnr)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr, null))
            .medRefusjonskravPrÅr(refusjonskravPrÅr)
            .medBeregnetPrÅr(beregnetPrÅr)
            .build();
    }

    private BeregningsgrunnlagPrStatus lagATFL(List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdList) {
        BeregningsgrunnlagPrStatus.Builder builder = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL);
        arbeidsforholdList.forEach(builder::medArbeidsforhold);
        return builder.build();
    }


    private void kjørRegel(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPeriode periode) {
        OmfordelFraArbeid regel = new OmfordelFraArbeid(arbeidsforhold);
        regel.evaluate(periode);
    }
}
