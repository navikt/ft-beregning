package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;

public class OmfordelFraFrilansTest {

    private static final LocalDate STP = LocalDate.now();
    private static final String ORGNR = "995";

    @Test
    public void skal_ikkje_flytte_om_det_ikkje_finnes_frilans() {
        // Arrange
        BigDecimal refusjonskravPrÅr = BigDecimal.valueOf(200_000);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);
        BeregningsgrunnlagPrStatus atfl = lagATFL(arbeidsforhold);

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        // Act
        kjørRegel(arbeidsforhold, periode);

        // Assert
        assertThat(arbeidsforhold.getFordeltPrÅr()).isNull();
        assertThat(arbeidsforhold.getBruttoInkludertNaturalytelsePrÅr().get()).isEqualByComparingTo(beregnetPrÅr);
    }

    @Test
    public void skal_flytte_fra_frilans_til_arbeid_frilans_avkortet_til_0() {
        // Arrange
        BigDecimal refusjonskravPrÅr = BigDecimal.valueOf(200_000);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);
        BigDecimal beregnetPrÅrFL = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold frilans = lagFLArbeidsforhold(beregnetPrÅrFL);
        BeregningsgrunnlagPrStatus atfl = lagATFLMedFrilans(arbeidsforhold, frilans);

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        // Act
        kjørRegel(arbeidsforhold, periode);

        // Assert
        assertThat(atfl.getArbeidsforhold().size()).isEqualTo(3);
        assertThat(atfl.getBruttoPrÅr()).isEqualByComparingTo(refusjonskravPrÅr);
        var flyttetFraFL = atfl.getArbeidsforhold().stream().filter(a ->
            a.getArbeidsforhold().equals(arbeidsforhold.getArbeidsforhold())
            && a.getInntektskategori().equals(Inntektskategori.FRILANSER)).findFirst().orElseThrow();
        assertThat(flyttetFraFL.getFordeltPrÅr()).isEqualByComparingTo(beregnetPrÅrFL);
        assertThat(flyttetFraFL.getRefusjonskravPrÅr().get()).isEqualByComparingTo(beregnetPrÅrFL);

        assertThat(arbeidsforhold.getBruttoPrÅr().get()).isEqualByComparingTo(beregnetPrÅr);
        assertThat(arbeidsforhold.getRefusjonskravPrÅr().get()).isEqualByComparingTo(refusjonskravPrÅr.subtract(beregnetPrÅrFL));
        assertThat(frilans.getRefusjonskravPrÅr()).isEmpty();
        assertThat(frilans.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_flytte_fra_frilans_til_arbeid_med_restbeløp_å_flytte() {
        // Arrange
        BigDecimal refusjonskravPrÅr = BigDecimal.valueOf(200_000);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);
        BigDecimal beregnetPrÅrFL = BigDecimal.valueOf(50_000);
        BeregningsgrunnlagPrArbeidsforhold frilans = lagFLArbeidsforhold(beregnetPrÅrFL);
        BeregningsgrunnlagPrStatus atfl = lagATFLMedFrilans(arbeidsforhold, frilans);

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        // Act
        kjørRegel(arbeidsforhold, periode);

        // Assert
        assertThat(atfl.getArbeidsforhold().size()).isEqualTo(3);
        assertThat(atfl.getBruttoPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
        var flyttetFraFL = atfl.getArbeidsforhold().stream()
            .filter(a -> a.getArbeidsforhold().equals(arbeidsforhold.getArbeidsforhold()) && a.getInntektskategori().equals(Inntektskategori.FRILANSER)).findFirst().orElseThrow();
        assertThat(flyttetFraFL.getFordeltPrÅr()).isEqualByComparingTo(beregnetPrÅrFL);
        assertThat(flyttetFraFL.getRefusjonskravPrÅr().get()).isEqualByComparingTo(beregnetPrÅrFL);

        assertThat(arbeidsforhold.getBruttoPrÅr().get()).isEqualByComparingTo(beregnetPrÅr);
        assertThat(arbeidsforhold.getRefusjonskravPrÅr().get()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
        assertThat(frilans.getRefusjonskravPrÅr()).isEmpty();
        assertThat(frilans.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    public void skal_flytte_fra_frilans_til_arbeid_med_restbeløp_på_frilans() {
        // Arrange
        BigDecimal refusjonskravPrÅr = BigDecimal.valueOf(200_000);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);
        BigDecimal beregnetPrÅrFL = BigDecimal.valueOf(150_000);
        BeregningsgrunnlagPrArbeidsforhold frilans = lagFLArbeidsforhold(beregnetPrÅrFL);
        BeregningsgrunnlagPrStatus atfl = lagATFLMedFrilans(arbeidsforhold, frilans);

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        // Act
        kjørRegel(arbeidsforhold, periode);

        // Assert
        assertThat(atfl.getArbeidsforhold().size()).isEqualTo(3);
        assertThat(atfl.getBruttoPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(250_000));
        var flyttetFraFL = atfl.getArbeidsforhold().stream()
            .filter(a -> a.getArbeidsforhold().equals(arbeidsforhold.getArbeidsforhold()) && a.getInntektskategori().equals(Inntektskategori.FRILANSER)).findFirst().orElseThrow();
        assertThat(flyttetFraFL.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
        assertThat(flyttetFraFL.getRefusjonskravPrÅr().get()).isEqualByComparingTo(BigDecimal.valueOf(100_000));

        assertThat(arbeidsforhold.getBruttoPrÅr().get()).isEqualByComparingTo(beregnetPrÅr);
        assertThat(arbeidsforhold.getRefusjonskravPrÅr().get()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
        assertThat(frilans.getRefusjonskravPrÅr()).isEmpty();
        assertThat(frilans.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
    }

    private BeregningsgrunnlagPrArbeidsforhold lagArbeidsforhold(BigDecimal refusjonskravPrÅr, BigDecimal beregnetPrÅr) {
        return BeregningsgrunnlagPrArbeidsforhold.builder()
            .medAndelNr(1L)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR, null))
            .medRefusjonskravPrÅr(refusjonskravPrÅr)
            .medBeregnetPrÅr(beregnetPrÅr)
            .build();
    }

    private BeregningsgrunnlagPrArbeidsforhold lagFLArbeidsforhold(BigDecimal beregnetPrÅr) {
        return BeregningsgrunnlagPrArbeidsforhold.builder()
            .medAndelNr(2L)
            .medInntektskategori(Inntektskategori.FRILANSER)
            .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
            .medBeregnetPrÅr(beregnetPrÅr)
            .build();
    }

    private BeregningsgrunnlagPrStatus lagATFL(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        return BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(arbeidsforhold).build();
    }

    private BeregningsgrunnlagPrStatus lagATFLMedFrilans(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPrArbeidsforhold frilans) {
        return BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(arbeidsforhold)
            .medArbeidsforhold(frilans)
            .build();
    }

    private void kjørRegel(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPeriode periode) {
        OmfordelFraFrilans regel = new OmfordelFraFrilans(arbeidsforhold);
        regel.evaluate(periode);
    }

}
