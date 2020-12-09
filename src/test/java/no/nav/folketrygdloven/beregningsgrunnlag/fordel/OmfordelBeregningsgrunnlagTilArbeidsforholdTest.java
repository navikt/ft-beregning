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
import no.nav.fpsak.nare.ServiceArgument;

public class OmfordelBeregningsgrunnlagTilArbeidsforholdTest {

    private static final LocalDate STP = LocalDate.now();
    private static final String ORGNR1 = "995";
    private static final String ORGNR2 = "910";

    @Test
    public void skal_flytte_beregningsgrunnlag_fra_SN_til_arbeidsforhold_uten_rest_uten_å_ta_fra_arbeidsforhold() {
        // Arrange
        BigDecimal refusjonskrav1 = BigDecimal.valueOf(200_000);
        BigDecimal beregnetPrÅr1 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

        BigDecimal refusjonskrav2 = BigDecimal.ZERO;
        BigDecimal beregnetPrÅr2 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

        BeregningsgrunnlagPrStatus atfl = lagATFL(List.of(a1, a2));

        BigDecimal beregnetPrÅrSN = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrStatus SN = lagSN(beregnetPrÅrSN);

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(SN)
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        // Act
        kjørRegel(a1, periode);

        // Assert
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        assertThat(arbeidsforhold.size()).isEqualTo(3);
        assertThat(a1.getFordeltPrÅr()).isNull();
        assertThat(a1.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
        var andelFraSN = arbeidsforhold.stream().filter(a -> a.getArbeidsforhold().getOrgnr().equals(ORGNR1) &&
            a.getInntektskategori().equals(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)).findFirst().get();
        assertThat(andelFraSN.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
        assertThat(a2.getFordeltPrÅr()).isNull();
        assertThat(SN.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_flytte_beregningsgrunnlag_fra_SN_til_arbeidsforhold_uten_rest_og_skal_ta_fra_arbeidsforhold() {
        // Arrange
        BigDecimal refusjonskrav1 = BigDecimal.valueOf(200_000);
        BigDecimal beregnetPrÅr1 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

        BigDecimal refusjonskrav2 = BigDecimal.ZERO;
        BigDecimal beregnetPrÅr2 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

        BeregningsgrunnlagPrStatus atfl = lagATFL(List.of(a1, a2));

        BigDecimal beregnetPrÅrSN = BigDecimal.valueOf(50_000);
        BeregningsgrunnlagPrStatus SN = lagSN(beregnetPrÅrSN);

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(SN)
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        // Act
        kjørRegel(a1, periode);

        // Assert
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        assertThat(arbeidsforhold.size()).isEqualTo(3);
        assertThat(a1.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
        var andelFraSN = arbeidsforhold.stream().filter(a -> a.getArbeidsforhold().getOrgnr().equals(ORGNR1) &&
            a.getInntektskategori().equals(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)).findFirst().get();
        assertThat(andelFraSN.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
        assertThat(a2.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
        assertThat(SN.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_flytte_beregningsgrunnlag_fra_SN_til_arbeidsforhold_uten_rest_og_skal_ta_fra_FL() {
        // Arrange
        BigDecimal refusjonskrav1 = BigDecimal.valueOf(200_000);
        BigDecimal beregnetPrÅr1 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

        BigDecimal refusjonskrav2 = BigDecimal.ZERO;
        BigDecimal beregnetPrÅr2 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

        BigDecimal beregnetPrÅrFL = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold frilans = lagFLArbeidsforhold(beregnetPrÅrFL);

        BeregningsgrunnlagPrStatus atfl = lagATFL(List.of(a1, a2, frilans));

        BigDecimal beregnetPrÅrSN = BigDecimal.valueOf(50_000);
        BeregningsgrunnlagPrStatus SN = lagSN(beregnetPrÅrSN);

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(SN)
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        // Act
        kjørRegel(a1, periode);

        // Assert
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        assertThat(arbeidsforhold.size()).isEqualTo(5);
        assertThat(a1.getFordeltPrÅr()).isNull();
        var andelFraSN = arbeidsforhold.stream().filter(a -> ORGNR1.equals(a.getArbeidsforhold().getArbeidsgiverId()) &&
            a.getInntektskategori().equals(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)).findFirst().get();
        assertThat(andelFraSN.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(50_000));

        var andelFraFL = arbeidsforhold.stream().filter(a -> ORGNR1.equals(a.getArbeidsforhold().getArbeidsgiverId()) &&
            a.getInntektskategori().equals(Inntektskategori.FRILANSER)).findFirst().get();
        assertThat(andelFraFL.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(50_000));

        assertThat(a2.getFordeltPrÅr()).isNull();
        assertThat(frilans.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
        assertThat(SN.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_flytte_beregningsgrunnlag_fra_SN_og_FL_til_arbeidsforhold_uten_rest_og_ta_fra_arbeidsforhold() {
        // Arrange
        BigDecimal refusjonskrav1 = BigDecimal.valueOf(200_000);
        BigDecimal beregnetPrÅr1 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

        BigDecimal refusjonskrav2 = BigDecimal.ZERO;
        BigDecimal beregnetPrÅr2 = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

        BigDecimal beregnetPrÅrFL = BigDecimal.valueOf(25_000);
        BeregningsgrunnlagPrArbeidsforhold frilans = lagFLArbeidsforhold(beregnetPrÅrFL);

        BeregningsgrunnlagPrStatus atfl = lagATFL(List.of(a1, a2, frilans));

        BigDecimal beregnetPrÅrSN = BigDecimal.valueOf(50_000);
        BeregningsgrunnlagPrStatus SN = lagSN(beregnetPrÅrSN);

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(SN)
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        // Act
        kjørRegel(a1, periode);

        // Assert
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        assertThat(arbeidsforhold.size()).isEqualTo(5);
        assertThat(a1.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(125_000));
        var andelFraSN = arbeidsforhold.stream().filter(a -> ORGNR1.equals(a.getArbeidsforhold().getArbeidsgiverId()) &&
            a.getInntektskategori().equals(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)).findFirst().get();
        assertThat(andelFraSN.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(50_000));

        var andelFraFL = arbeidsforhold.stream().filter(a -> ORGNR1.equals(a.getArbeidsforhold().getArbeidsgiverId()) &&
            a.getInntektskategori().equals(Inntektskategori.FRILANSER)).findFirst().get();
        assertThat(andelFraFL.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(25_000));

        assertThat(a2.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(75_000));
        assertThat(frilans.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(SN.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private void kjørRegel(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPeriode periode) {
        OmfordelBeregningsgrunnlagTilArbeidsforhold regel = new OmfordelBeregningsgrunnlagTilArbeidsforhold();
        regel.medServiceArgument(new ServiceArgument("arbeidsforhold", arbeidsforhold));
        regel.getSpecification().evaluate(periode);
    }

    private BeregningsgrunnlagPrStatus lagATFL(List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdList) {
        BeregningsgrunnlagPrStatus.Builder builder = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL);
        arbeidsforholdList.forEach(builder::medArbeidsforhold);
        return builder.build();
    }

    private BeregningsgrunnlagPrArbeidsforhold lagArbeidsforhold(BigDecimal refusjonskravPrÅr, BigDecimal beregnetPrÅr, Long andelsnr, String orgnr) {
        return BeregningsgrunnlagPrArbeidsforhold.builder()
            .medAndelNr(andelsnr)
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr, null))
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medGjeldendeRefusjonPrÅr(refusjonskravPrÅr)
            .medBeregnetPrÅr(beregnetPrÅr)
            .build();
    }

    private BeregningsgrunnlagPrArbeidsforhold lagFLArbeidsforhold(BigDecimal beregnetPrÅr) {
        return BeregningsgrunnlagPrArbeidsforhold.builder()
            .medAndelNr(3L)
            .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
            .medInntektskategori(Inntektskategori.FRILANSER)
            .medBeregnetPrÅr(beregnetPrÅr)
            .build();
    }

    private BeregningsgrunnlagPrStatus lagSN(BigDecimal beregnetPrÅr1) {
        return BeregningsgrunnlagPrStatus.builder()
            .medAndelNr(2L)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SN)
            .medBeregnetPrÅr(beregnetPrÅr1).build();
    }



}
