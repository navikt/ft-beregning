package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;

public class OmfordelFraAktiviteterUtenArbeidsforholdTest {

    private static final LocalDate STP = LocalDate.now();
    private static final String ORGNR = "995";

    @Test
    public void skal_flytte_beregningsgrunnlag_fra_SN_til_arbeidsforhold_uten_rest() {
        BigDecimal refusjonskravPrÅr = BigDecimal.valueOf(200_000);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);
        BeregningsgrunnlagPrStatus atfl = lagATFL(arbeidsforhold);

        BigDecimal beregnetPrÅrSN = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrStatus SN = lagSN(beregnetPrÅrSN);

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(SN)
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        kjørRegel(arbeidsforhold, periode);

        assertThat(arbeidsforhold.getFordeltPrÅr()).isEqualByComparingTo(refusjonskravPrÅr);
        assertThat(SN.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_flytte_beregningsgrunnlag_fra_SN_til_arbeidsforhold_med_resterende_beløp_å_flytte() {
        BigDecimal refusjonskravPrÅr = BigDecimal.valueOf(200_000);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);
        BeregningsgrunnlagPrStatus atfl = lagATFL(arbeidsforhold);

        BigDecimal beregnetPrÅrSN = BigDecimal.valueOf(50_000);
        BeregningsgrunnlagPrStatus SN = lagSN(beregnetPrÅrSN);

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(SN)
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        kjørRegel(arbeidsforhold, periode);

        assertThat(arbeidsforhold.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
        assertThat(SN.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_flytte_beregningsgrunnlag_fra_SN_til_arbeidsforhold_med_resterende_beløp_på_SN_andel() {
        BigDecimal refusjonskravPrÅr = BigDecimal.valueOf(200_000);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);
        BeregningsgrunnlagPrStatus atfl = lagATFL(arbeidsforhold);

        BigDecimal beregnetPrÅrSN = BigDecimal.valueOf(150_000);
        BeregningsgrunnlagPrStatus SN = lagSN(beregnetPrÅrSN);

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(SN)
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        kjørRegel(arbeidsforhold, periode);

        assertThat(arbeidsforhold.getFordeltPrÅr()).isEqualByComparingTo(refusjonskravPrÅr);
        assertThat(SN.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
    }

    @Test
    public void beregningsgrunnlag_med_SN_DP_og_ARBEID_flytter_fra_SN() {
        BigDecimal refusjonskravPrÅr = BigDecimal.valueOf(200_000);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);
        BeregningsgrunnlagPrStatus atfl = lagATFL(arbeidsforhold);

        BigDecimal beregnetPrÅrSN = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrStatus SN = lagSN(beregnetPrÅrSN);

        BigDecimal beregnetPrÅrDP = BigDecimal.valueOf(50_000);
        BeregningsgrunnlagPrStatus DP = lagDP(beregnetPrÅrDP);

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(SN)
            .medBeregningsgrunnlagPrStatus(DP)
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        kjørRegel(arbeidsforhold, periode);

        assertThat(arbeidsforhold.getFordeltPrÅr()).isEqualByComparingTo(refusjonskravPrÅr);
        assertThat(SN.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(DP.getFordeltPrÅr()).isNull();
        assertThat(DP.getBruttoPrÅr()).isEqualByComparingTo(beregnetPrÅrDP);
    }

    @Test
    public void beregningsgrunnlag_med_SN_DP_og_ARBEID_flytter_fra_SN_før_DP() {
        BigDecimal refusjonskravPrÅr = BigDecimal.valueOf(225_000);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);
        BeregningsgrunnlagPrStatus atfl = lagATFL(arbeidsforhold);

        BigDecimal beregnetPrÅrSN = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrStatus SN = lagSN(beregnetPrÅrSN);

        BigDecimal beregnetPrÅrDP = BigDecimal.valueOf(50_000);
        BeregningsgrunnlagPrStatus DP = lagDP(beregnetPrÅrDP);

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(SN)
            .medBeregningsgrunnlagPrStatus(DP)
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        kjørRegel(arbeidsforhold, periode);

        assertThat(arbeidsforhold.getFordeltPrÅr()).isEqualByComparingTo(refusjonskravPrÅr);
        assertThat(SN.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(DP.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(25_000));
    }

    @Test
    public void beregningsgrunnlag_med_SN_DP_AAP_og_ARBEID_flytter_fra_AAP_før_DP() {
        BigDecimal refusjonskravPrÅr = BigDecimal.valueOf(225_000);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr);
        BeregningsgrunnlagPrStatus atfl = lagATFL(arbeidsforhold);

        BigDecimal beregnetPrÅrSN = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrStatus SN = lagSN(beregnetPrÅrSN);

        BigDecimal beregnetPrÅrDP = BigDecimal.valueOf(50_000);
        BeregningsgrunnlagPrStatus DP = lagDP(beregnetPrÅrDP);

        BigDecimal beregnetPrÅrAAP = BigDecimal.valueOf(50_000);
        BeregningsgrunnlagPrStatus AAP = lagAAP(beregnetPrÅrAAP);

        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(SN)
            .medBeregningsgrunnlagPrStatus(DP)
            .medBeregningsgrunnlagPrStatus(AAP)
            .medBeregningsgrunnlagPrStatus(atfl)
            .medPeriode(Periode.of(STP, null))
            .build();

        kjørRegel(arbeidsforhold, periode);

        assertThat(arbeidsforhold.getFordeltPrÅr()).isEqualByComparingTo(refusjonskravPrÅr);
        assertThat(SN.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(AAP.getFordeltPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(25_000));
        assertThat(DP.getFordeltPrÅr()).isNull();
        assertThat(DP.getBruttoPrÅr()).isEqualByComparingTo(beregnetPrÅrDP);
    }

    private BeregningsgrunnlagPrStatus lagATFL(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        return BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.ATFL)
                .medArbeidsforhold(arbeidsforhold).build();
    }

    private BeregningsgrunnlagPrStatus lagSN(BigDecimal beregnetPrÅr1) {
        return BeregningsgrunnlagPrStatus.builder()
                .medAndelNr(2L)
                .medAktivitetStatus(AktivitetStatus.SN)
                .medBeregnetPrÅr(beregnetPrÅr1).build();
    }

    private BeregningsgrunnlagPrStatus lagDP(BigDecimal beregnetPrÅr1) {
        return BeregningsgrunnlagPrStatus.builder()
            .medAndelNr(3L)
            .medAktivitetStatus(AktivitetStatus.DP)
            .medBeregnetPrÅr(beregnetPrÅr1).build();
    }

    private BeregningsgrunnlagPrStatus lagAAP(BigDecimal beregnetPrÅr1) {
        return BeregningsgrunnlagPrStatus.builder()
            .medAndelNr(4L)
            .medAktivitetStatus(AktivitetStatus.AAP)
            .medBeregnetPrÅr(beregnetPrÅr1).build();
    }


    private BeregningsgrunnlagPrArbeidsforhold lagArbeidsforhold(BigDecimal refusjonskravPrÅr, BigDecimal beregnetPrÅr) {
        return BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR, null))
                .medRefusjonskravPrÅr(refusjonskravPrÅr)
                .medBeregnetPrÅr(beregnetPrÅr)
                .build();
    }

    private void kjørRegel(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPeriode periode) {
        OmfordelFraAktiviteterUtenArbeidsforhold regel = new OmfordelFraAktiviteterUtenArbeidsforhold(arbeidsforhold);
        regel.evaluate(periode);
    }
}
