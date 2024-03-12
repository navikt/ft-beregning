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
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.NyPeriodeDto;

class NyAktivitetMedSøktYtelseFordelingTest {

    public static final LocalDate STP = LocalDate.now();
    public static final Arbeidsgiver VIRKSOMHET = Arbeidsgiver.virksomhet("421648712");

    @Test
    void skal_lage_periode_for_nytt_arbeidsforhold() {
        // Arrange
        AktivitetDto utbetalingsgradArbeidsforhold = new AktivitetDto(VIRKSOMHET, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        LocalDate fom = STP;
        LocalDate tom = STP.plusDays(10);
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitetList = List.of(
                new UtbetalingsgradPrAktivitetDto(utbetalingsgradArbeidsforhold, List.of(lagPeriode(fom, tom, BigDecimal.TEN)))
        );
        SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = new SvangerskapspengerGrunnlag(utbetalingsgradPrAktivitetList);

        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(VIRKSOMHET).medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef()))
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medAndelsnr(1L)
                .build();

        // Act
        List<NyPeriodeDto> nyPeriodeDtos = NyAktivitetMedSøktYtelseFordeling.lagPerioderForNyAktivitetMedSøktYtelse(svangerskapspengerGrunnlag, FordelingTilfelle.NY_AKTIVITET, andel, new FordelBeregningsgrunnlagArbeidsforholdDto());

        // Assert
        assertThat(nyPeriodeDtos).hasSize(1);
        assertThat(nyPeriodeDtos.get(0).getFom()).isEqualTo(fom);
        assertThat(nyPeriodeDtos.get(0).getTom()).isEqualTo(tom);
        assertThat(nyPeriodeDtos.get(0).isErSøktYtelse()).isEqualTo(true);
    }

    @Test
    void skal_lage_periode_for_ny_næring() {
        // Arrange
        AktivitetDto utbetalingsgradArbeidsforhold = new AktivitetDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        LocalDate fom = STP;
        LocalDate tom = STP.plusDays(10);
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitetList = List.of(
                new UtbetalingsgradPrAktivitetDto(utbetalingsgradArbeidsforhold, List.of(lagPeriode(fom, tom, BigDecimal.TEN)))
        );
        SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = new SvangerskapspengerGrunnlag(utbetalingsgradPrAktivitetList);

        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medAndelsnr(1L)
                .build();

        // Act
        List<NyPeriodeDto> nyPeriodeDtos = NyAktivitetMedSøktYtelseFordeling.lagPerioderForNyAktivitetMedSøktYtelse(svangerskapspengerGrunnlag, FordelingTilfelle.NY_AKTIVITET, andel, new FordelBeregningsgrunnlagArbeidsforholdDto());

        // Assert
        assertThat(nyPeriodeDtos).hasSize(1);
        assertThat(nyPeriodeDtos.get(0).getFom()).isEqualTo(fom);
        assertThat(nyPeriodeDtos.get(0).getTom()).isEqualTo(tom);
        assertThat(nyPeriodeDtos.get(0).isErSøktYtelse()).isEqualTo(true);
    }

    private PeriodeMedUtbetalingsgradDto lagPeriode(LocalDate fom, LocalDate tom, BigDecimal utbetalingsgrad) {
        return new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(fom, tom), Utbetalingsgrad.fra(utbetalingsgrad));
    }
}
