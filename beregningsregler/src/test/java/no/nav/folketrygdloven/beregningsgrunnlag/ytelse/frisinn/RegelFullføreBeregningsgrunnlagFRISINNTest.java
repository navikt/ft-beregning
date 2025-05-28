package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2019;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

class RegelFullføreBeregningsgrunnlagFRISINNTest {
    public static final String ORGNR = "999999999";
    public static final BigDecimal SEKS_G = BigDecimal.valueOf(GRUNNBELØP_2019 * 6);
    private static final BigDecimal DEKNINGSGRAD_80 = BigDecimal.valueOf(0.8);

    private static final LocalDate skjæringstidspunkt = LocalDate.of(2020, Month.MARCH, 15);


    @Test
    void kun_sn_under_6G_inntekt_ingen_avkorting_100_prosent_utbetaling() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(100_000d, null, null, 100d, 0d);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        assertThat(regelResultat).isNotNull();
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), BigDecimal.valueOf(100_000), BigDecimal.valueOf(80_000));
    }
    @Test
    void kun_sn_under_6G_inntekt_ingen_avkorting_50_prosent_utbetaling() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(100_000d, null, null, 50d, 0d);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        assertThat(regelResultat).isNotNull();
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), BigDecimal.valueOf(50_000), BigDecimal.valueOf(40_000));
    }
    @Test
    void kun_sn_over_6G_inntekt_ingen_avkorting_100_prosent_utbetaling() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(800_000d, null, null, 100d, 0d);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        assertThat(regelResultat).isNotNull();
        var avkortet = SEKS_G;
        var redusert = avkortet.multiply(DEKNINGSGRAD_80);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortet, redusert);
    }

    @Test
    void kun_sn_over_6G_inntekt_ingen_avkorting_50_prosent_utbetaling() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(800_000d, null, null, 50d, 0d);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        assertThat(regelResultat).isNotNull();
        var avkortet = SEKS_G.subtract(BigDecimal.valueOf(400_000));
        var redusert = avkortet.multiply(DEKNINGSGRAD_80);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortet, redusert);
    }

    @Test
    void fl_sn_over_under_6G_inntekt_ingen_avkorting_100_prosent_utbetaling() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(200_000d, 200_000d, null, 100d, 100d);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        assertThat(regelResultat).isNotNull();
        var avkortet = BigDecimal.valueOf(200_000);
        var redusert = avkortet.multiply(DEKNINGSGRAD_80);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortet, redusert);
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortet, redusert);
    }

    @Test
    void fl_sn_over_over_6G_med_ingen_avkorting_100_prosent_utbetaling() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(400_000d, 400_000d, null, 100d, 100d);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        var avkortetFL = BigDecimal.valueOf(400_000);
        var redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        var avkortetSN = SEKS_G.subtract(avkortetFL);
        var redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
    }

    @Test
    void fl_sn_over_over_6G_med_ingen_avkorting_delvis_utbetaling_fl_full_sn() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(400_000d, 400_000d, null, 100d, 50d);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        var avkortetFL = BigDecimal.valueOf(200_000);
        var redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        var løpendeFL = BigDecimal.valueOf(200_000);
        var avkortetSN = SEKS_G.subtract(avkortetFL).subtract(løpendeFL);
        var redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
    }

    @Test
    void at_sn_under_6G_med_ingen_avkorting_full_utb_sn() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(300_000d, null, 200_000d, 100d, null);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        var avkortetSN = BigDecimal.valueOf(300_000);
        var redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
    }

    @Test
    void at_sn_over_6G_med_avkorting_delvis_utb_sn() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(600_000d, null, 500_000d, 50d, null);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        var avkortetSN = BigDecimal.ZERO;
        var redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
    }

    @Test
    void at_sn_over_6G_med_avkorting_full_utb_sn() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(600_000d, null, 500_000d, 100d, null);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        var bruttoAT = BigDecimal.valueOf(500_000);
        var avkortetSN = SEKS_G.subtract(bruttoAT);
        var redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
    }

    @Test
    void at_sn_under_6G_uten_avkorting_delvis_utb_sn() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(100_000d, null, 400_000d, 50d, null);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        var avkortetSN = BigDecimal.valueOf(100_000).divide(BigDecimal.valueOf(2), RoundingMode.HALF_EVEN);
        var redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
    }

    @Test
    void at_fl_sn_under_6G_uten_avkorting_full_utb_sn_og_fl() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(100_000d, 50_000d, 200_000d, 100d, 100d);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        var avkortetFL = BigDecimal.valueOf(50_000);
        var redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        var avkortetSN = BigDecimal.valueOf(100_000);
        var redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
        assertIngenUtbetalingAT(bgPeriode.getBeregningsgrunnlagPrStatus());
    }

    @Test
    void at_fl_sn_under_6G_uten_avkorting_delvis_utb_fl() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(100_000d, 50_000d, 200_000d, 100d, 50d);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        var avkortetFL = BigDecimal.valueOf(25_000);
        var redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        var avkortetSN = BigDecimal.valueOf(100_000);
        var redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
        assertIngenUtbetalingAT(bgPeriode.getBeregningsgrunnlagPrStatus());
    }

    @Test
    void at_fl_sn_over_6G_med_avkorting_av_sn_full_utb_fl_sn() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(200_000d, 200_000d, 400_000d, 100d, 100d);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        var bruttoAT = BigDecimal.valueOf(400_000);
        var avkortetFL = SEKS_G.subtract(bruttoAT);
        var redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        var avkortetSN = SEKS_G.subtract(bruttoAT).subtract(avkortetFL).max(BigDecimal.ZERO);
        var redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
        assertIngenUtbetalingAT(bgPeriode.getBeregningsgrunnlagPrStatus());
    }

    @Test
    void at_fl_sn_over_6G_uten_avkorting_delvis_utb_fl_sn() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(200_000d, 200_000d, 400_000d, 100d, 10d);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        var bruttoAT = BigDecimal.valueOf(400_000);
        var løpedeFL = BigDecimal.valueOf(180_000);
        var avkortetFL = SEKS_G.subtract(bruttoAT).subtract(løpedeFL);
        var redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        var avkortetSN = SEKS_G.subtract(bruttoAT).subtract(avkortetFL).subtract(løpedeFL).max(BigDecimal.ZERO);
        var redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
        assertIngenUtbetalingAT(bgPeriode.getBeregningsgrunnlagPrStatus());
    }

    @Test
    void at_fl_sn_over_6G_uten_avkorting_ingen_utb_fl() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(200_000d, 200_000d, 400_000d, 100d, 0d);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        var bruttoAT = BigDecimal.valueOf(400_000);
        var løpedeFL = BigDecimal.valueOf(200_000);
        var avkortetFL = SEKS_G.subtract(bruttoAT).subtract(løpedeFL).max(BigDecimal.ZERO);
        var redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        var avkortetSN = SEKS_G.subtract(bruttoAT).subtract(avkortetFL).subtract(løpedeFL).max(BigDecimal.ZERO);
        var redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
        assertIngenUtbetalingAT(bgPeriode.getBeregningsgrunnlagPrStatus());
    }

    @Test
    void at_fl_sn_over_6G_uten_avkorting_ingen_utb_fl_utb_sn() {
        var beregningsgrunnlag = lagBeregningsgrunnlag(200_000d, 100_000d, 400_000d, 100d, 0d);
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var regelResultat = kjørRegel(bgPeriode);

        var bruttoAT = BigDecimal.valueOf(400_000);
        var løpedeFL = BigDecimal.valueOf(100_000);
        var avkortetFL = BigDecimal.ZERO;
        var redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        var avkortetSN = SEKS_G.subtract(bruttoAT).subtract(avkortetFL).subtract(løpedeFL).max(BigDecimal.ZERO);
        var redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
        assertIngenUtbetalingAT(bgPeriode.getBeregningsgrunnlagPrStatus());
    }


    private void assertIngenUtbetalingAT(Collection<BeregningsgrunnlagPrStatus> andeler) {
        var atflAndel = andeler.stream().filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.ATFL)).findFirst().orElse(null);
        assertThat(atflAndel).isNotNull();
        var finnesArbforMedUtbetaling = atflAndel.getArbeidsforholdIkkeFrilans().stream().anyMatch(a -> a.getRedusertPrÅr().compareTo(BigDecimal.ZERO) != 0);
        assertThat(finnesArbforMedUtbetaling).isFalse();
    }


    private void assertFLAndel(Collection<BeregningsgrunnlagPrStatus> andeler, BigDecimal avkortet, BigDecimal redusert) {
        var atflAndel = andeler.stream().filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.ATFL)).findFirst().orElse(null);
        assertThat(atflAndel).isNotNull();
        var flAndel = atflAndel.getFrilansArbeidsforhold().orElse(null);
        assertThat(flAndel).isNotNull();
        assertThat(flAndel.getAvkortetPrÅr()).isEqualByComparingTo(avkortet);
        assertThat(flAndel.getRedusertPrÅr()).isEqualByComparingTo(redusert);
    }

    private void assertSNAndel(Collection<BeregningsgrunnlagPrStatus> andeler, BigDecimal avkortet, BigDecimal redusert) {
        var snAndel = andeler.stream().filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.SN)).findFirst().orElse(null);
        assertThat(snAndel).isNotNull();
        assertThat(snAndel.getAvkortetPrÅr()).isEqualByComparingTo(avkortet);
        assertThat(snAndel.getRedusertPrÅr()).isEqualByComparingTo(redusert);
    }

    private RegelResultat kjørRegel(BeregningsgrunnlagPeriode grunnlag) {
		new RegelFinnGrenseverdiFRISINN().evaluerRegel(grunnlag);
        return new RegelFullføreBeregningsgrunnlagFRISINN().evaluerRegel(grunnlag);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag(Double snInntektPrÅr, Double frilansInntektPrÅr, Double arbeidsinntektPrÅr, Double snUtbetalingsgrad, Double flUtbetalingsgrad) {
        var periodeBuilder = BeregningsgrunnlagPeriode.builder()
		        .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .medPeriode(new Periode(skjæringstidspunkt, null));
        byggSN(snInntektPrÅr, periodeBuilder, snUtbetalingsgrad);
        byggATFL(frilansInntektPrÅr, arbeidsinntektPrÅr, periodeBuilder, flUtbetalingsgrad);
        var periode = periodeBuilder.build();
        return Beregningsgrunnlag.builder()
            .medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2019))
            .medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL_SN, null)))
            .medBeregningsgrunnlagPeriode(periode)
            .build();
    }

    private void byggSN(Double snInntektPrÅr, BeregningsgrunnlagPeriode.Builder periodeBuilder, Double utbetalingsgrad) {
        if (snInntektPrÅr != null) {
            periodeBuilder.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.SN)
                .medAndelNr(1L)
                .medUtbetalingsprosent(BigDecimal.valueOf(utbetalingsgrad))
                .medBruttoPrÅr(BigDecimal.valueOf(snInntektPrÅr))
                .build());
        }
    }

    private void byggATFL(Double frilansInntektPrÅr, Double arbeidsinntektPrÅr, BeregningsgrunnlagPeriode.Builder periodeBuilder, Double flUtbetalingsgrad) {
        if (frilansInntektPrÅr != null || arbeidsinntektPrÅr != null) {
            var atflStatusBuilder = BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.ATFL);
            if (frilansInntektPrÅr != null) {
                var flAndel = BeregningsgrunnlagPrArbeidsforhold.builder()
                    .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                    .medBruttoPrÅr(BigDecimal.valueOf(frilansInntektPrÅr))
                    .medAndelNr(2L)
                    .medUtbetalingsprosent(BigDecimal.valueOf(flUtbetalingsgrad))
                    .build();
                flAndel.setErSøktYtelseFor(true);
                atflStatusBuilder.medArbeidsforhold(flAndel);
            }
            if (arbeidsinntektPrÅr != null) {
                var arbfor = BeregningsgrunnlagPrArbeidsforhold.builder()
                    .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR))
                    .medBruttoPrÅr(BigDecimal.valueOf(arbeidsinntektPrÅr))
                    .medAndelNr(3L)
                    .build();
                arbfor.setErSøktYtelseFor(false);
                atflStatusBuilder.medArbeidsforhold(arbfor);
            }
            periodeBuilder.medBeregningsgrunnlagPrStatus(atflStatusBuilder.build());
        }
    }
}
