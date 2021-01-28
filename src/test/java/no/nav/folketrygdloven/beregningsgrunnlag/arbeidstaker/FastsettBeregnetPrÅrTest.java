package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.fp.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;

class FastsettBeregnetPrÅrTest {


    @Test
    void skal_sette_riktig_hjemmel_for_omp_flsn() {
        // Arrange
        Beregningsgrunnlag regelBg = lagBeregningsgrunnlagFLSNOMP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_FRILANSER_OG_SELVSTENDIG);
    }

    @Test
    void skal_sette_riktig_hjemmel_for_fp_flsn() {
        // Arrange
        Beregningsgrunnlag regelBg = lagBeregningsgrunnlagFLSNFP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K14_HJEMMEL_FRILANSER_OG_SELVSTENDIG);
    }



    @Test
    void skal_sette_riktig_hjemmel_for_omp_fl() {
        // Arrange
        Beregningsgrunnlag regelBg = lagBeregningsgrunnlagFLOMP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_FRILANSER);
    }

    @Test
    void skal_sette_riktig_hjemmel_for_fp_fl() {
        // Arrange
        Beregningsgrunnlag regelBg = lagBeregningsgrunnlagFLFP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER);
    }

    @Test
    void skal_sette_riktig_hjemmel_for_omp_atflsn() {
        // Arrange
        Beregningsgrunnlag regelBg = lagBeregningsgrunnlagATFLSNOMP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER_OG_SELVSTENDIG);
    }

    @Test
    void skal_sette_riktig_hjemmel_for_fp_atflsn() {
        // Arrange
        Beregningsgrunnlag regelBg = lagBeregningsgrunnlagATFLSNFP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER_OG_SELVSTENDIG);
    }

    @Test
    void skal_sette_riktig_hjemmel_for_omp_atfl() {
        // Arrange
        Beregningsgrunnlag regelBg = lagBeregningsgrunnlagATFLOMP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER);
    }


    @Test
    void skal_sette_riktig_hjemmel_for_fp_atfl() {
        // Arrange
        Beregningsgrunnlag regelBg = lagBeregningsgrunnlagATFLFP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER);
    }


    @Test
    void skal_sette_riktig_hjemmel_for_omp_arbeidstaker_full_refusjon() {
        // Arrange
        BigDecimal bgPrÅr = BigDecimal.valueOf(100_000);
        Beregningsgrunnlag regelBg = lagBeregningsgrunnlagArbeidstakerOMP(bgPrÅr, bgPrÅr);

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_ARBEIDSTAKER_REFUSJON);
    }

    @Test
    void skal_sette_riktig_hjemmel_for_omp_arbeidstaker_direkte_utbetaling() {
        // Arrange
        BigDecimal bgPrÅr = BigDecimal.valueOf(100_000);
        Beregningsgrunnlag regelBg = lagBeregningsgrunnlagArbeidstakerOMP(bgPrÅr, BigDecimal.ZERO);

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_ARBEIDSTAKER_MED_AVVIKSVURDERING);
    }

    @Test
    void skal_sette_riktig_hjemmel_for_fp_arbeidstaker() {
        // Arrange
        BigDecimal bgPrÅr = BigDecimal.valueOf(100_000);
        Beregningsgrunnlag regelBg = lagBeregningsgrunnlagArbeidstakerFP(bgPrÅr);

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER);
    }


    private Beregningsgrunnlag lagBeregningsgrunnlagFLSNOMP() {
        BeregningsgrunnlagPrStatus atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        BeregningsgrunnlagPrStatus snStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.SN)
            .medAndelNr(3L)
            .medBeregnetPrÅr(BigDecimal.ZERO)
            .build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .medBeregningsgrunnlagPrStatus(snStatus)
            .build();
        return lagBgOMP(periode, BigDecimal.ZERO, AktivitetStatus.ATFL_SN);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagFLSNFP() {
        BeregningsgrunnlagPrStatus atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        BeregningsgrunnlagPrStatus snStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.SN)
            .medAndelNr(3L)
            .medBeregnetPrÅr(BigDecimal.ZERO)
            .build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .medBeregningsgrunnlagPrStatus(snStatus)
            .build();
        return lagBgFP(periode, AktivitetStatus.ATFL_SN);
    }



    private Beregningsgrunnlag lagBeregningsgrunnlagFLOMP() {
        BeregningsgrunnlagPrStatus atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .build();
        return lagBgOMP(periode, BigDecimal.ZERO, AktivitetStatus.ATFL);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagFLFP() {
        BeregningsgrunnlagPrStatus atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .build();
        return lagBgFP(periode, AktivitetStatus.ATFL);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagATFLSNOMP() {
        BeregningsgrunnlagPrStatus atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.TEN)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("5367712635"))
                .build())
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(2L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        BeregningsgrunnlagPrStatus snStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.SN)
            .medAndelNr(3L)
            .medBeregnetPrÅr(BigDecimal.ZERO)
            .build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .medBeregningsgrunnlagPrStatus(snStatus)
            .build();
        return lagBgOMP(periode, BigDecimal.ZERO, AktivitetStatus.ATFL_SN);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagATFLSNFP() {
        BeregningsgrunnlagPrStatus atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.TEN)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("5367712635"))
                .build())
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(2L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        BeregningsgrunnlagPrStatus snStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.SN)
            .medAndelNr(3L)
            .medBeregnetPrÅr(BigDecimal.ZERO)
            .build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .medBeregningsgrunnlagPrStatus(snStatus)
            .build();
        return lagBgFP(periode, AktivitetStatus.ATFL_SN);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagATFLOMP() {
        BeregningsgrunnlagPrStatus atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.TEN)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("5367712635"))
                .build())
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(2L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .build();
        return lagBgOMP(periode, BigDecimal.ZERO, AktivitetStatus.ATFL);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagATFLFP() {
        BeregningsgrunnlagPrStatus atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.TEN)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("5367712635"))
                .build())
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(2L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .build();
        return lagBgFP(periode, AktivitetStatus.ATFL);
    }


    private Beregningsgrunnlag lagBgOMP(BeregningsgrunnlagPeriode periode, BigDecimal zero, AktivitetStatus aktivitetStatus) {
        return Beregningsgrunnlag.builder()
            .medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(aktivitetStatus, null)))
            .medGrunnbeløp(BigDecimal.valueOf(99000))
            .medGrunnbeløpSatser(List.of(new Grunnbeløp(LocalDate.now(), LocalDate.now(), 99000L, 99000L)))
            .medSkjæringstidspunkt(LocalDate.now())
            .medInntektsgrunnlag(new Inntektsgrunnlag())
            .medBeregningsgrunnlagPeriode(periode)
            .medYtelsesSpesifiktGrunnlag(new OmsorgspengerGrunnlag(zero, false))
            .build();
    }


    private Beregningsgrunnlag lagBeregningsgrunnlagArbeidstakerOMP(BigDecimal bgPrÅr, BigDecimal totalRefusjon) {
        BeregningsgrunnlagPrStatus atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(bgPrÅr)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("5367712635"))
                .build())
            .build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .build();
        return lagBgOMP(periode, totalRefusjon, AktivitetStatus.ATFL);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagArbeidstakerFP(BigDecimal bgPrÅr) {
        BeregningsgrunnlagPrStatus atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(bgPrÅr)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("5367712635"))
                .build())
            .build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .build();
        return lagBgFP(periode, AktivitetStatus.ATFL);
    }

    private Beregningsgrunnlag lagBgFP(BeregningsgrunnlagPeriode periode, AktivitetStatus status) {
        return Beregningsgrunnlag.builder()
            .medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(status, null)))
            .medGrunnbeløp(BigDecimal.valueOf(99000))
            .medGrunnbeløpSatser(List.of(new Grunnbeløp(LocalDate.now(), LocalDate.now(), 99000L, 99000L)))
            .medSkjæringstidspunkt(LocalDate.now())
            .medInntektsgrunnlag(new Inntektsgrunnlag())
            .medBeregningsgrunnlagPeriode(periode)
            .medYtelsesSpesifiktGrunnlag(new ForeldrepengerGrunnlag(false))
            .build();
    }

    private void kjørRegel(BeregningsgrunnlagPeriode periode) {
        new FastsettBeregnetPrÅr().evaluate(periode);
    }
}
