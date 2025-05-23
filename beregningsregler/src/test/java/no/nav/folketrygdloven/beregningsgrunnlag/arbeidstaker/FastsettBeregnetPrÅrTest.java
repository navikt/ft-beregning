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
        var regelBg = lagBeregningsgrunnlagFLSNOMP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_FRILANSER_OG_SELVSTENDIG);
    }

    @Test
    void skal_sette_riktig_hjemmel_for_fp_flsn() {
        // Arrange
        var regelBg = lagBeregningsgrunnlagFLSNFP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K14_HJEMMEL_FRILANSER_OG_SELVSTENDIG);
    }



    @Test
    void skal_sette_riktig_hjemmel_for_omp_fl() {
        // Arrange
        var regelBg = lagBeregningsgrunnlagFLOMP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_FRILANSER);
    }

    @Test
    void skal_sette_riktig_hjemmel_for_fp_fl() {
        // Arrange
        var regelBg = lagBeregningsgrunnlagFLFP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER);
    }

    @Test
    void skal_sette_riktig_hjemmel_for_omp_atflsn() {
        // Arrange
        var regelBg = lagBeregningsgrunnlagATFLSNOMP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER_OG_SELVSTENDIG);
    }

    @Test
    void skal_sette_riktig_hjemmel_for_fp_atflsn() {
        // Arrange
        var regelBg = lagBeregningsgrunnlagATFLSNFP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER_OG_SELVSTENDIG);
    }

    @Test
    void skal_sette_riktig_hjemmel_for_omp_atfl() {
        // Arrange
        var regelBg = lagBeregningsgrunnlagATFLOMP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER);
    }


    @Test
    void skal_sette_riktig_hjemmel_for_fp_atfl() {
        // Arrange
        var regelBg = lagBeregningsgrunnlagATFLFP();

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER);
    }


    @Test
    void skal_sette_riktig_hjemmel_for_omp_arbeidstaker_full_refusjon() {
        // Arrange
        var bgPrÅr = BigDecimal.valueOf(100_000);
        var harBrukerSøkt = false;
        var regelBg = lagBeregningsgrunnlagArbeidstakerOMP(bgPrÅr, harBrukerSøkt);

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_ARBEIDSTAKER_REFUSJON);
    }

    @Test
    void skal_sette_riktig_hjemmel_for_omp_arbeidstaker_direkte_utbetaling() {
        // Arrange
        var bgPrÅr = BigDecimal.valueOf(100_000);
        var regelBg = lagBeregningsgrunnlagArbeidstakerOMP(bgPrÅr, true);

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_ARBEIDSTAKER_MED_AVVIKSVURDERING);
    }

    @Test
    void skal_sette_riktig_hjemmel_for_fp_arbeidstaker() {
        // Arrange
        var bgPrÅr = BigDecimal.valueOf(100_000);
        var regelBg = lagBeregningsgrunnlagArbeidstakerFP(bgPrÅr);

        // Act
        kjørRegel(regelBg.getBeregningsgrunnlagPerioder().get(0));

        // Assert
        assertThat(regelBg.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER);
    }


    private Beregningsgrunnlag lagBeregningsgrunnlagFLSNOMP() {
        var atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        var snStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.SN)
            .medAndelNr(3L)
            .medBeregnetPrÅr(BigDecimal.ZERO)
            .build();
        var periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .medBeregningsgrunnlagPrStatus(snStatus)
            .build();
        return lagBgOMP(periode, AktivitetStatus.ATFL_SN, true);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagFLSNFP() {
        var atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        var snStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.SN)
            .medAndelNr(3L)
            .medBeregnetPrÅr(BigDecimal.ZERO)
            .build();
        var periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .medBeregningsgrunnlagPrStatus(snStatus)
            .build();
        return lagBgFP(periode, AktivitetStatus.ATFL_SN);
    }



    private Beregningsgrunnlag lagBeregningsgrunnlagFLOMP() {
        var atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        var periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .build();
        return lagBgOMP(periode, AktivitetStatus.ATFL, true);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagFLFP() {
        var atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        var periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .build();
        return lagBgFP(periode, AktivitetStatus.ATFL);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagATFLSNOMP() {
        var atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.TEN)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999"))
                .build())
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(2L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        var snStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.SN)
            .medAndelNr(3L)
            .medBeregnetPrÅr(BigDecimal.ZERO)
            .build();
        var periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .medBeregningsgrunnlagPrStatus(snStatus)
            .build();
        return lagBgOMP(periode, AktivitetStatus.ATFL_SN, true);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagATFLSNFP() {
        var atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.TEN)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999"))
                .build())
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(2L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        var snStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.SN)
            .medAndelNr(3L)
            .medBeregnetPrÅr(BigDecimal.ZERO)
            .build();
        var periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .medBeregningsgrunnlagPrStatus(snStatus)
            .build();
        return lagBgFP(periode, AktivitetStatus.ATFL_SN);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagATFLOMP() {
        var atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.TEN)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999"))
                .build())
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(2L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        var periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .build();
        return lagBgOMP(periode, AktivitetStatus.ATFL, true);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagATFLFP() {
        var atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.TEN)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999"))
                .build())
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(2L)
                .medBeregnetPrÅr(BigDecimal.ONE)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .build())
            .build();
        var periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .build();
        return lagBgFP(periode, AktivitetStatus.ATFL);
    }


    private Beregningsgrunnlag lagBgOMP(BeregningsgrunnlagPeriode periode, AktivitetStatus aktivitetStatus, boolean harBrukerSøkt) {
        return Beregningsgrunnlag.builder()
            .medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(aktivitetStatus, null)))
            .medGrunnbeløp(BigDecimal.valueOf(99000))
            .medGrunnbeløpSatser(List.of(new Grunnbeløp(LocalDate.now(), LocalDate.now(), 99000L, 99000L)))
            .medSkjæringstidspunkt(LocalDate.now())
            .medInntektsgrunnlag(new Inntektsgrunnlag())
            .medBeregningsgrunnlagPeriode(periode)
            .medYtelsesSpesifiktGrunnlag(new OmsorgspengerGrunnlag(false, harBrukerSøkt))
            .build();
    }


    private Beregningsgrunnlag lagBeregningsgrunnlagArbeidstakerOMP(BigDecimal bgPrÅr, boolean harBrukerSøkt) {
        var atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(bgPrÅr)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999"))
                .build())
            .build();
        var periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medBeregningsgrunnlagPrStatus(atflStatus)
            .build();
        return lagBgOMP(periode, AktivitetStatus.ATFL, harBrukerSøkt);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagArbeidstakerFP(BigDecimal bgPrÅr) {
        var atflStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medAndelNr(1L)
                .medBeregnetPrÅr(bgPrÅr)
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999"))
                .build())
            .build();
        var periode = BeregningsgrunnlagPeriode.builder()
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
