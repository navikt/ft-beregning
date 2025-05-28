package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelingTilfelle;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagArbeidsforholdDto;

class NyAktivitetMedSøktYtelseFordelingTest {

    public static final LocalDate STP = LocalDate.now();
    public static final Arbeidsgiver VIRKSOMHET = Arbeidsgiver.virksomhet("421648712");

    @Test
    void skal_lage_periode_for_nytt_arbeidsforhold() {
        // Arrange
        var utbetalingsgradArbeidsforhold = new AktivitetDto(VIRKSOMHET, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        var fom = STP;
        var tom = STP.plusDays(10);
        var utbetalingsgradPrAktivitetList = List.of(
                new UtbetalingsgradPrAktivitetDto(utbetalingsgradArbeidsforhold, List.of(lagPeriode(fom, tom, BigDecimal.TEN)))
        );
        var svangerskapspengerGrunnlag = new SvangerskapspengerGrunnlag(utbetalingsgradPrAktivitetList);

        var andel = BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(VIRKSOMHET).medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef()))
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medAndelsnr(1L)
                .build();

        // Act
        var nyPeriodeDtos = NyAktivitetMedSøktYtelseFordeling.lagPerioderForNyAktivitetMedSøktYtelse(svangerskapspengerGrunnlag, FordelingTilfelle.NY_AKTIVITET, andel, new FordelBeregningsgrunnlagArbeidsforholdDto());

        // Assert
        assertThat(nyPeriodeDtos).hasSize(1);
        assertThat(nyPeriodeDtos.get(0).getFom()).isEqualTo(fom);
        assertThat(nyPeriodeDtos.get(0).getTom()).isEqualTo(tom);
        assertThat(nyPeriodeDtos.get(0).isErSøktYtelse()).isTrue();
    }

    @Test
    void skal_lage_periode_for_ny_næring() {
        // Arrange
        var utbetalingsgradArbeidsforhold = new AktivitetDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        var fom = STP;
        var tom = STP.plusDays(10);
        var utbetalingsgradPrAktivitetList = List.of(
                new UtbetalingsgradPrAktivitetDto(utbetalingsgradArbeidsforhold, List.of(lagPeriode(fom, tom, BigDecimal.TEN)))
        );
        var svangerskapspengerGrunnlag = new SvangerskapspengerGrunnlag(utbetalingsgradPrAktivitetList);

        var andel = BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medAndelsnr(1L)
                .build();

        // Act
        var nyPeriodeDtos = NyAktivitetMedSøktYtelseFordeling.lagPerioderForNyAktivitetMedSøktYtelse(svangerskapspengerGrunnlag, FordelingTilfelle.NY_AKTIVITET, andel, new FordelBeregningsgrunnlagArbeidsforholdDto());

        // Assert
        assertThat(nyPeriodeDtos).hasSize(1);
        assertThat(nyPeriodeDtos.get(0).getFom()).isEqualTo(fom);
        assertThat(nyPeriodeDtos.get(0).getTom()).isEqualTo(tom);
        assertThat(nyPeriodeDtos.get(0).isErSøktYtelse()).isTrue();
    }

    private PeriodeMedUtbetalingsgradDto lagPeriode(LocalDate fom, LocalDate tom, BigDecimal utbetalingsgrad) {
        return new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(fom, tom), Utbetalingsgrad.fra(utbetalingsgrad));
    }
}
