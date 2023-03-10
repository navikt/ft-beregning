package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;

public class OmfordelFraAktiviteterUtenArbeidsforholdTest {

    private static final LocalDate STP = LocalDate.now();
    private static final String ORGNR = "995";

    @Test
    void skal_flytte_beregningsgrunnlag_fra_SN_til_arbeidsforhold_uten_rest() {
        var refusjonskravPrÅr = BigDecimal.valueOf(200_000);
        var beregnetPrÅr = BigDecimal.valueOf(100_000);
        var arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);

        var beregnetPrÅrSN = BigDecimal.valueOf(100_000);
        var sn = lagSN(beregnetPrÅrSN);

	    var periode = new FordelPeriodeModell(Periode.of(STP, null), Arrays.asList(arbeidsforhold, sn));

        kjørRegel(arbeidsforhold, periode);

        assertThat(arbeidsforhold.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(refusjonskravPrÅr);
        assertThat(arbeidsforhold.getInntektskategori()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(sn.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void skal_flytte_beregningsgrunnlag_fra_SN_til_arbeidsforhold_med_resterende_beløp_å_flytte() {
        var refusjonskravPrÅr = BigDecimal.valueOf(200_000);
        var beregnetPrÅr = BigDecimal.valueOf(100_000);
        var arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);

        var beregnetPrÅrSN = BigDecimal.valueOf(50_000);
        var sn = lagSN(beregnetPrÅrSN);

	    var periode = new FordelPeriodeModell(Periode.of(STP, null), Arrays.asList(arbeidsforhold, sn));

        kjørRegel(arbeidsforhold, periode);

        assertThat(arbeidsforhold.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
        assertThat(arbeidsforhold.getInntektskategori()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(sn.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void skal_flytte_beregningsgrunnlag_fra_SN_til_arbeidsforhold_med_resterende_beløp_på_SN_andel() {
        var refusjonskravPrÅr = BigDecimal.valueOf(200_000);
        var beregnetPrÅr = BigDecimal.valueOf(100_000);
        var arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);

	    var beregnetPrÅrSN = BigDecimal.valueOf(150_000);
	    var sn = lagSN(beregnetPrÅrSN);

	    var periode = new FordelPeriodeModell(Periode.of(STP, null), Arrays.asList(arbeidsforhold, sn));

        kjørRegel(arbeidsforhold, periode);

        assertThat(arbeidsforhold.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(refusjonskravPrÅr);
        assertThat(arbeidsforhold.getInntektskategori()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(sn.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
    }

    @Test
    void beregningsgrunnlag_med_SN_DP_og_ARBEID_flytter_fra_SN() {
	    var refusjonskravPrÅr = BigDecimal.valueOf(200_000);
	    var beregnetPrÅr = BigDecimal.valueOf(100_000);
	    var arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);

	    var beregnetPrÅrSN = BigDecimal.valueOf(100_000);
	    var sn = lagSN(beregnetPrÅrSN);

	    var beregnetPrÅrDP = BigDecimal.valueOf(50_000);
	    var dp = lagDP(beregnetPrÅrDP);

	    var periode = new FordelPeriodeModell(Periode.of(STP, null), Arrays.asList(arbeidsforhold, sn, dp));

        kjørRegel(arbeidsforhold, periode);

        assertThat(arbeidsforhold.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(refusjonskravPrÅr);
        assertThat(arbeidsforhold.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO)).isEqualByComparingTo(refusjonskravPrÅr);
        assertThat(arbeidsforhold.getInntektskategori()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(sn.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dp.getFordeltPrÅr()).isEmpty();
        assertThat(dp.getBruttoPrÅr().orElse(BigDecimal.ZERO)).isEqualByComparingTo(beregnetPrÅrDP);
    }

    @Test
    void beregningsgrunnlag_med_SN_DP_og_ARBEID_flytter_fra_SN_før_DP() {
	    var refusjonskravPrÅr = BigDecimal.valueOf(225_000);
	    var beregnetPrÅr = BigDecimal.valueOf(100_000);
	    var arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);

	    var beregnetPrÅrSN = BigDecimal.valueOf(100_000);
	    var sn = lagSN(beregnetPrÅrSN);

	    var beregnetPrÅrDP = BigDecimal.valueOf(50_000);
	    var dp = lagDP(beregnetPrÅrDP);

	    var periode = new FordelPeriodeModell(Periode.of(STP, null), new ArrayList<>(Arrays.asList(arbeidsforhold, sn, dp)));

        kjørRegel(arbeidsforhold, periode);

	    var alleArbeidsforhold = periode.getAlleAndelerForStatus(AktivitetStatus.AT);

	    assertThat(alleArbeidsforhold.size()).isEqualTo(2);
        assertThat(totalBrutto(alleArbeidsforhold)).isEqualByComparingTo(refusjonskravPrÅr);
        var flyttetFraSN = alleArbeidsforhold.stream().filter(a -> a.getInntektskategori().equals(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)).findFirst().orElseThrow();
        assertThat(flyttetFraSN.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(beregnetPrÅrSN.add(beregnetPrÅr));
        assertThat(flyttetFraSN.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO)).isEqualByComparingTo(beregnetPrÅrSN.add(beregnetPrÅr));
        var flyttetFraDP = alleArbeidsforhold.stream().filter(a -> a.getInntektskategori().equals(Inntektskategori.DAGPENGER)).findFirst().orElseThrow();
        assertThat(flyttetFraDP.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(25_000));
        assertThat(flyttetFraDP.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.valueOf(25_000));
        assertThat(sn.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dp.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(25_000));
    }

	private BigDecimal totalBrutto(List<FordelAndelModell> alleArbeidsforhold) {
		return alleArbeidsforhold.stream().map(a -> a.getBruttoPrÅr().orElse(BigDecimal.ZERO)).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
	}

	@Test
    void beregningsgrunnlag_med_SN_DP_AAP_og_ARBEID_flytter_fra_AAP_før_DP() {
	    var refusjonskravPrÅr = BigDecimal.valueOf(225_000);
	    var beregnetPrÅr = BigDecimal.valueOf(100_000);
	    var arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);

	    var beregnetPrÅrSN = BigDecimal.valueOf(100_000);
	    var sn = lagSN(beregnetPrÅrSN);

	    var beregnetPrÅrDP = BigDecimal.valueOf(50_000);
	    var dp = lagDP(beregnetPrÅrDP);

	    var beregnetPrÅrAAP = BigDecimal.valueOf(50_000);
	    var aap = lagAAP(beregnetPrÅrAAP);

		var periode = new FordelPeriodeModell(Periode.of(STP, null), new ArrayList<>(Arrays.asList(arbeidsforhold, sn, dp, aap)));

        kjørRegel(arbeidsforhold, periode);

		var alleArbeidsforhold = periode.getAlleAndelerForStatus(AktivitetStatus.AT);

        assertThat(alleArbeidsforhold.size()).isEqualTo(2);
        assertThat(totalBrutto(alleArbeidsforhold)).isEqualByComparingTo(refusjonskravPrÅr);
        var flyttetFraSN = alleArbeidsforhold.stream().filter(a -> a.getInntektskategori().equals(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)).findFirst().orElseThrow();
        assertThat(flyttetFraSN.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(beregnetPrÅrSN.add(beregnetPrÅr));
        assertThat(flyttetFraSN.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO)).isEqualByComparingTo(beregnetPrÅrSN.add(beregnetPrÅr));
        var flyttetFraAAP = alleArbeidsforhold.stream().filter(a -> a.getInntektskategori().equals(Inntektskategori.ARBEIDSAVKLARINGSPENGER)).findFirst().orElseThrow();
        assertThat(flyttetFraAAP.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(25_000));
        assertThat(flyttetFraAAP.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.valueOf(25_000));

        assertThat(sn.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(aap.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(25_000));
        assertThat(dp.getFordeltPrÅr()).isEmpty();
        assertThat(dp.getBruttoPrÅr().orElse(BigDecimal.ZERO)).isEqualByComparingTo(beregnetPrÅrDP);
    }

    private FordelAndelModell lagSN(BigDecimal beregnetPrÅr1) {
        return FordelAndelModell.builder()
            .medAndelNr(2L)
            .medAktivitetStatus(AktivitetStatus.SN)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medForeslåttPrÅr(beregnetPrÅr1).build();
    }

    private FordelAndelModell lagDP(BigDecimal beregnetPrÅr1) {
        return FordelAndelModell.builder()
            .medAndelNr(3L)
            .medAktivitetStatus(AktivitetStatus.DP)
            .medInntektskategori(Inntektskategori.DAGPENGER)
            .medForeslåttPrÅr(beregnetPrÅr1).build();
    }

    private FordelAndelModell lagAAP(BigDecimal beregnetPrÅr1) {
        return FordelAndelModell.builder()
            .medAndelNr(4L)
            .medAktivitetStatus(AktivitetStatus.AAP)
            .medInntektskategori(Inntektskategori.ARBEIDSAVKLARINGSPENGER)
            .medForeslåttPrÅr(beregnetPrÅr1).build();
    }


    private FordelAndelModell lagArbeidsforhold(BigDecimal refusjonskravPrÅr, BigDecimal beregnetPrÅr) {
        return FordelAndelModell.builder()
            .medAndelNr(1L)
	        .medAktivitetStatus(AktivitetStatus.AT)
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR, null))
            .medGjeldendeRefusjonPrÅr(refusjonskravPrÅr)
            .medForeslåttPrÅr(beregnetPrÅr)
            .build();
    }

    private void kjørRegel(FordelAndelModell arbeidsforhold, FordelPeriodeModell periode) {
        OmfordelFraAktiviteterUtenArbeidsforhold regel = new OmfordelFraAktiviteterUtenArbeidsforhold(arbeidsforhold);
	    var modell = new FordelModell(periode);
	    regel.evaluate(modell);
		String s = "";
    }
}
