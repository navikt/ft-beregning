package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.AndelMedBeløpDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.KunYtelseDto;

public class KunYtelseDtoTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    private static final Beløp BRUTTO_PR_ÅR = Beløp.fra(10000);
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_OPPTJENING);

    private KunYtelseDtoTjeneste kunYtelseDtoTjeneste;

    @BeforeEach
    public void setUp() {
        this.kunYtelseDtoTjeneste = new KunYtelseDtoTjeneste();
    }

    @Test
    public void fødende_kvinne_uten_dagpenger() {
        // Arrange
        Intervall periode = Intervall.fraOgMedTilOgMed(
                SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(8),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING, periode, OpptjeningAktivitetType.SYKEPENGER);
        var beregningsgrunnlagGrunnlag = lagBeregningsgrunnlag(beregningAktivitetAggregat);
        ForeldrepengerGrunnlag medBesteberegning = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);

        // Act
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), List.of(), medBesteberegning)
                .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);

        KunYtelseDto kunytelse = kunYtelseDtoTjeneste.lagKunYtelseDto(input);

        // Assert
        assertAndel(kunytelse);
        assertThat(kunytelse.isFodendeKvinneMedDP()).isFalse();
        assertThat(kunytelse.getErBesteberegning()).isNull();
    }

    @Test
    public void skal_ikke_feile_hvis_svpgrunnlag() {
        Intervall periode = Intervall.fraOgMedTilOgMed(
                SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(8),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING, periode, OpptjeningAktivitetType.SYKEPENGER);
        var beregningsgrunnlagGrunnlag = lagBeregningsgrunnlag(beregningAktivitetAggregat);
        SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = new SvangerskapspengerGrunnlag(List.of());

        // Act
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), List.of(), svangerskapspengerGrunnlag)
                .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);

        KunYtelseDto kunytelse = kunYtelseDtoTjeneste.lagKunYtelseDto(input);

        // Assert
        assertAndel(kunytelse);
        assertThat(kunytelse.isFodendeKvinneMedDP()).isFalse();
        assertThat(kunytelse.getErBesteberegning()).isNull();
    }

    @Test
    public void fødende_kvinne_med_dagpenger() {
        // Arrange
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = beregningAktivitetSykepengerOgDagpenger();
        var beregningsgrunnlagGrunnlag = lagBeregningsgrunnlag(beregningAktivitetAggregat);
        ForeldrepengerGrunnlag medBesteberegning = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, true);

        // Act
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), List.of(), medBesteberegning)
                .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);

        KunYtelseDto kunytelse = kunYtelseDtoTjeneste.lagKunYtelseDto(input);

        // Assert
        assertAndel(kunytelse);
        assertThat(kunytelse.isFodendeKvinneMedDP()).isTrue();
        assertThat(kunytelse.getErBesteberegning()).isNull();
    }

    @Test
    public void adopsjon_kvinne_med_dagpenger() {
        // Arrange
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = beregningAktivitetSykepengerOgDagpenger();

        var beregningsgrunnlagGrunnlag = lagBeregningsgrunnlag(beregningAktivitetAggregat);
        ForeldrepengerGrunnlag utenBesteberegning = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);

        // Act
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), List.of(), utenBesteberegning)
                .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);

        KunYtelseDto kunytelse = kunYtelseDtoTjeneste.lagKunYtelseDto(input);

        // Assert
        assertAndel(kunytelse);
        assertThat(kunytelse.isFodendeKvinneMedDP()).isFalse();
        assertThat(kunytelse.getErBesteberegning()).isNull();
    }

    private BeregningAktivitetAggregatDto beregningAktivitetSykepengerOgDagpenger() {
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder()
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        builder.leggTilAktivitet(BeregningAktivitetDto.builder()
                .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(8), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1)))
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.SYKEPENGER)
                .build());
        builder.leggTilAktivitet(BeregningAktivitetDto.builder()
                .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(12), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(8).minusDays(1)))
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.DAGPENGER)
                .build());
        return builder.build();
    }

    @Test
    public void mann_med_dagpenger() {
        // Arrange
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = beregningAktivitetSykepengerOgDagpenger();
        var beregningsgrunnlagGrunnlag = lagBeregningsgrunnlag(beregningAktivitetAggregat);
        ForeldrepengerGrunnlag utenBesteberegning = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);

        // Act
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), List.of(), utenBesteberegning)
                .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);

        KunYtelseDto kunytelse = kunYtelseDtoTjeneste.lagKunYtelseDto(input);

        // Assert
        assertAndel(kunytelse);
        assertThat(kunytelse.isFodendeKvinneMedDP()).isFalse();
        assertThat(kunytelse.getErBesteberegning()).isNull();
    }

    @Test
    public void skal_sette_verdier_om_forrige_grunnlag_var_kun_ytelse() {
        // Arrange
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = beregningAktivitetSykepengerOgDagpenger();
        var beregningsgrunnlagGrunnlag = lagForrigeBeregningsgrunnlagMedLagtTilAndel(beregningAktivitetAggregat);
        ForeldrepengerGrunnlag utenBesteberegning = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        // Act
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), List.of(), utenBesteberegning)
                .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);

        KunYtelseDto kunytelse = kunYtelseDtoTjeneste.lagKunYtelseDto(input);

        // Assert
        List<AndelMedBeløpDto> andeler = kunytelse.getAndeler();
        assertThat(andeler).hasSize(2);
        assertThat(andeler.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.BRUKERS_ANDEL);
        assertThat(andeler.get(0).getAndelsnr()).isEqualTo(1L);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(andeler.get(1).getAktivitetStatus()).isEqualTo(AktivitetStatus.BRUKERS_ANDEL);
        assertThat(andeler.get(1).getAndelsnr()).isEqualTo(2L);
        assertThat(andeler.get(1).getInntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
    }


    @Test
    public void skal_sette_verdier_fra_forrige_med_besteberegning() {
        // Arrange
        Intervall periode = Intervall.fraOgMedTilOgMed(
                SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(8),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING, periode, OpptjeningAktivitetType.SYKEPENGER);
        var beregningsgrunnlagGrunnlag = lagForrigeBeregningsgrunnlag(true, beregningAktivitetAggregat);
        ForeldrepengerGrunnlag medBesteberegning = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, true);

        // Act
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), List.of(), medBesteberegning)
                .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);

        KunYtelseDto kunytelse = kunYtelseDtoTjeneste.lagKunYtelseDto(input);


        // Assert
        List<AndelMedBeløpDto> andeler = kunytelse.getAndeler();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getFastsattBelopPrMnd()).isNotEqualByComparingTo(ModellTyperMapper.beløpTilDto(BRUTTO_PR_ÅR));
        assertThat(andeler.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.BRUKERS_ANDEL);
        assertThat(andeler.get(0).getAndelsnr()).isEqualTo(1L);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.UDEFINERT);
        assertThat(kunytelse.getErBesteberegning()).isTrue();
        assertThat(kunytelse.isFodendeKvinneMedDP()).isTrue();
    }

    @Test
    public void skal_sette_verdier_fra_forrige_uten_besteberegning() {
        // Arrange
        Intervall periode = Intervall.fraOgMedTilOgMed(
                SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(8),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING, periode, OpptjeningAktivitetType.SYKEPENGER);
        var beregningsgrunnlagGrunnlag = lagForrigeBeregningsgrunnlag(false, beregningAktivitetAggregat);
        ForeldrepengerGrunnlag utenBesteberegning = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);

        // Act
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), List.of(), utenBesteberegning)
                .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);

        KunYtelseDto kunytelse = kunYtelseDtoTjeneste.lagKunYtelseDto(input);


        // Assert
        List<AndelMedBeløpDto> andeler = kunytelse.getAndeler();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getFastsattBelopPrMnd()).isNotEqualByComparingTo(ModellTyperMapper.beløpTilDto(BRUTTO_PR_ÅR));
        assertThat(andeler.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.BRUKERS_ANDEL);
        assertThat(andeler.get(0).getAndelsnr()).isEqualTo(1L);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.UDEFINERT);
        assertThat(kunytelse.getErBesteberegning()).isFalse();
        assertThat(kunytelse.isFodendeKvinneMedDP()).isFalse();
    }


    private void assertAndel(KunYtelseDto kunytelse) {
        List<AndelMedBeløpDto> andeler = kunytelse.getAndeler();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getFastsattBelopPrMnd()).isNull();
        assertThat(andeler.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.BRUKERS_ANDEL);
        assertThat(andeler.get(0).getAndelsnr()).isEqualTo(1L);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.UDEFINERT);
    }

    private BeregningsgrunnlagGrunnlagDto lagForrigeBeregningsgrunnlag(boolean medBesteberegning, BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
                .medGrunnbeløp(Beløp.fra(90000))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.KUN_YTELSE))
                .build();
        BeregningsgrunnlagPeriodeDto periode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBesteberegningPrÅr(medBesteberegning ? BRUTTO_PR_ÅR : null)
                .medBeregnetPrÅr(BRUTTO_PR_ÅR)
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.BRUKERS_ANDEL)
                .build(periode1);

        var builder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(bg)
                .medRegisterAktiviteter(beregningAktivitetAggregat);

        return builder.build(BeregningsgrunnlagTilstand.KOFAKBER_UT);
    }

    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlag(BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
                .medGrunnbeløp(Beløp.fra(90000))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.KUN_YTELSE))
                .build();
        BeregningsgrunnlagPeriodeDto periode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.BRUKERS_ANDEL)
                .build(periode1);

        var builder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(bg)
                .medRegisterAktiviteter(beregningAktivitetAggregat);

        return builder.build(BeregningsgrunnlagTilstand.OPPRETTET);
    }

    private BeregningsgrunnlagGrunnlagDto lagForrigeBeregningsgrunnlagMedLagtTilAndel(BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
                .medGrunnbeløp(Beløp.fra(90000))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.KUN_YTELSE))
                .build();
        BeregningsgrunnlagPeriodeDto periode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.BRUKERS_ANDEL)
                .medInntektskategori(no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori.ARBEIDSTAKER)
                .build(periode1);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.BRUKERS_ANDEL)
                .medInntektskategori(no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori.FRILANSER)
                .build(periode1);

        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(bg)
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .build(BeregningsgrunnlagTilstand.OPPRETTET);
    }

}
