package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
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
        var refusjonskrav1 = BigDecimal.valueOf(200_000);
        var beregnetPrÅr1 = BigDecimal.valueOf(100_000);
        var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

        var refusjonskrav2 = BigDecimal.ZERO;
        var beregnetPrÅr2 = BigDecimal.valueOf(100_000);
        var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

        var beregnetPrÅrSN = BigDecimal.valueOf(100_000);
        var sn = lagSN(beregnetPrÅrSN);

	    var periode = new FordelPeriodeModell(Periode.of(STP, null), new ArrayList<>(Arrays.asList(a1, a2, sn)));

	    // Act
        kjørRegel(a1, periode);

        // Assert
        List<FordelAndelModell> arbeidsforhold = periode.getAlleAndelerForStatus(AktivitetStatus.AT);
        assertThat(arbeidsforhold.size()).isEqualTo(3);
        assertThat(a1.getFordeltPrÅr()).isEmpty();
        assertThat(a1.getBruttoPrÅr().orElse(BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.valueOf(100_000));
        var andelFraSN = arbeidsforhold.stream()
		        .filter(a -> matcherOrgnr(a, ORGNR1) && a.getInntektskategori().equals(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE))
		        .findFirst().orElseThrow();
        assertThat(andelFraSN.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
        assertThat(a2.getFordeltPrÅr()).isEmpty();
        assertThat(sn.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_flytte_beregningsgrunnlag_fra_SN_til_arbeidsforhold_uten_rest_og_skal_ta_fra_arbeidsforhold() {
        // Arrange
        var refusjonskrav1 = BigDecimal.valueOf(200_000);
        var beregnetPrÅr1 = BigDecimal.valueOf(100_000);
        var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

        var refusjonskrav2 = BigDecimal.ZERO;
        var beregnetPrÅr2 = BigDecimal.valueOf(100_000);
        var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

        var beregnetPrÅrSN = BigDecimal.valueOf(50_000);
        var sn = lagSN(beregnetPrÅrSN);
	    var periode = new FordelPeriodeModell(Periode.of(STP, null), new ArrayList<>(Arrays.asList(a1, a2, sn)));

        // Act
        kjørRegel(a1, periode);

        // Assert
        List<FordelAndelModell> arbeidsforhold = periode.getAlleAndelerForStatus(AktivitetStatus.AT);
        assertThat(arbeidsforhold.size()).isEqualTo(3);
        assertThat(a1.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
        var andelFraSN = arbeidsforhold.stream()
		        .filter(a -> matcherOrgnr(a, ORGNR1) && a.getInntektskategori().equals(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE))
		        .findFirst().orElseThrow();
        assertThat(andelFraSN.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
        assertThat(a2.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
        assertThat(sn.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
    }

	@Test
    public void skal_flytte_beregningsgrunnlag_fra_SN_til_arbeidsforhold_uten_rest_og_skal_ta_fra_FL() {
        // Arrange
        var refusjonskrav1 = BigDecimal.valueOf(200_000);
        var beregnetPrÅr1 = BigDecimal.valueOf(100_000);
        var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

        var refusjonskrav2 = BigDecimal.ZERO;
        var beregnetPrÅr2 = BigDecimal.valueOf(100_000);
        var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

        var beregnetPrÅrFL = BigDecimal.valueOf(100_000);
        var frilans = lagFLArbeidsforhold(beregnetPrÅrFL);

        var beregnetPrÅrSN = BigDecimal.valueOf(50_000);
        var sn = lagSN(beregnetPrÅrSN);

		var periode = new FordelPeriodeModell(Periode.of(STP, null), new ArrayList<>(Arrays.asList(a1, a2, sn, frilans)));

        // Act
        kjørRegel(a1, periode);

        // Assert
        List<FordelAndelModell> arbeidsforhold = periode.getAlleAndelerForStatus(AktivitetStatus.AT);
		assertThat(periode.getEnesteAndelForStatus(AktivitetStatus.FL)).isPresent();
		assertThat(periode.getEnesteAndelForStatus(AktivitetStatus.SN)).isPresent();
		assertThat(arbeidsforhold.size()).isEqualTo(4);
        assertThat(a1.getFordeltPrÅr()).isEmpty();
        var andelFraSN = arbeidsforhold.stream()
		        .filter(a -> matcherOrgnr(a, ORGNR1) && a.getInntektskategori().equals(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE))
		        .findFirst().orElseThrow();
        assertThat(andelFraSN.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(50_000));

        var andelFraFL = arbeidsforhold.stream()
		        .filter(a -> matcherOrgnr(a, ORGNR1) && a.getInntektskategori().equals(Inntektskategori.FRILANSER))
		        .findFirst().orElseThrow();
        assertThat(andelFraFL.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(50_000));

        assertThat(a2.getFordeltPrÅr()).isEmpty();
        assertThat(frilans.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
        assertThat(sn.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_flytte_beregningsgrunnlag_fra_SN_og_FL_til_arbeidsforhold_uten_rest_og_ta_fra_arbeidsforhold() {
        // Arrange
        var refusjonskrav1 = BigDecimal.valueOf(200_000);
        var beregnetPrÅr1 = BigDecimal.valueOf(100_000);
        var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

        var refusjonskrav2 = BigDecimal.ZERO;
        var beregnetPrÅr2 = BigDecimal.valueOf(100_000);
        var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

        var beregnetPrÅrFL = BigDecimal.valueOf(25_000);
        var frilans = lagFLArbeidsforhold(beregnetPrÅrFL);

        var beregnetPrÅrSN = BigDecimal.valueOf(50_000);
        var sn = lagSN(beregnetPrÅrSN);

	    var periode = new FordelPeriodeModell(Periode.of(STP, null), new ArrayList<>(Arrays.asList(a1, a2, sn, frilans)));

        // Act
        kjørRegel(a1, periode);

        // Assert
        List<FordelAndelModell> arbeidsforhold = periode.getAlleAndelerForStatus(AktivitetStatus.AT);
        assertThat(arbeidsforhold.size()).isEqualTo(4);
        assertThat(a1.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(125_000));
        var andelFraSN = arbeidsforhold.stream()
		        .filter(a -> matcherOrgnr(a, ORGNR1) && a.getInntektskategori().equals(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE))
		        .findFirst().orElseThrow();
        assertThat(andelFraSN.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(50_000));

        var andelFraFL = arbeidsforhold.stream()
		        .filter(a -> matcherOrgnr(a, ORGNR1) && a.getInntektskategori().equals(Inntektskategori.FRILANSER))
		        .findFirst().orElseThrow();
        assertThat(andelFraFL.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(25_000));

        assertThat(a2.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(75_000));
        assertThat(frilans.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(sn.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private void kjørRegel(FordelAndelModell arbeidsforhold, FordelPeriodeModell periode) {
        OmfordelBeregningsgrunnlagTilArbeidsforhold regel = new OmfordelBeregningsgrunnlagTilArbeidsforhold();
        regel.medServiceArgument(new ServiceArgument("arbeidsforhold", arbeidsforhold));
        regel.getSpecification().evaluate(new FordelModell(periode));
    }

    private FordelAndelModell lagArbeidsforhold(BigDecimal refusjonskravPrÅr, BigDecimal beregnetPrÅr, Long andelsnr, String orgnr) {
        return FordelAndelModell.builder()
            .medAndelNr(andelsnr)
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr, null))
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
	        .medAktivitetStatus(AktivitetStatus.AT)
            .medGjeldendeRefusjonPrÅr(refusjonskravPrÅr)
            .medForeslåttPrÅr(beregnetPrÅr)
            .build();
    }

	private boolean matcherOrgnr(FordelAndelModell andel, String orgnr) {
		return andel.getArbeidsforhold().map(af -> af.getOrgnr().equals(orgnr)).orElse(false);
	}

    private FordelAndelModell lagFLArbeidsforhold(BigDecimal beregnetPrÅr) {
        return FordelAndelModell.builder()
            .medAndelNr(3L)
            .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
            .medInntektskategori(Inntektskategori.FRILANSER)
            .medForeslåttPrÅr(beregnetPrÅr)
	        .medAktivitetStatus(AktivitetStatus.FL)
	        .build();
    }

    private FordelAndelModell lagSN(BigDecimal beregnetPrÅr1) {
        return FordelAndelModell.builder()
            .medAndelNr(2L)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SN)
            .medForeslåttPrÅr(beregnetPrÅr1).build();
    }



}
