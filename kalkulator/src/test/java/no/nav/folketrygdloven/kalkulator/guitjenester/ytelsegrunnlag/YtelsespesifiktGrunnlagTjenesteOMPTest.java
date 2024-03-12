package no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.OmsorgspengeGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.YtelsespesifiktGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public class YtelsespesifiktGrunnlagTjenesteOMPTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);
    private BigDecimal GRUNNBELØP = BigDecimal.valueOf(99_888);
    private static final LocalDate ANDEL_FOM = LocalDate.now().minusDays(100);
    private static final LocalDate ANDEL_TOM = LocalDate.now();
    private static final String ORGNR = "973093681";
    private static final Long ANDELSNR = 1L;
    private BeregningsgrunnlagGrunnlagDto grunnlag;
    private BeregningAktivitetAggregatDto beregningAktiviteter;

    @Test
    public void skalSetteRiktigYtelsesspesifikkInformasjonNårOmsorgspengerOgAtOgDirekteUtbetalingTilBruker() {
        //Arrange
        BigDecimal beregnet = BigDecimal.valueOf(20_000);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(240_000);
        BigDecimal refusjon = BigDecimal.valueOf(15_000);
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        this.beregningAktiviteter = lagBeregningAktiviteter(arbeidsgiver);
        var beregningsgrunnlag = no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .medGrunnbeløp(Beløp.fra(GRUNNBELØP))
                .build();
        BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag);
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori.ARBEIDSTAKER)
                .medAndelsnr(ANDELSNR)
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(Beløp.fra(beregnetPrÅr))
                .build(bgPeriode);
        this.grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktiviteter)
                .medBeregningsgrunnlag(beregningsgrunnlag).build(BeregningsgrunnlagTilstand.FORESLÅTT);

        LocalDate PeriodeFom =ANDEL_FOM;
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(PeriodeFom,
                PeriodeFom.plusMonths(1)), Utbetalingsgrad.valueOf(100));
        AktivitetDto aktivitetDto = new AktivitetDto(arbeidsgiver,
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(aktivitetDto, List.of(periodeMedUtbetalingsgradDto));
        OmsorgspengerGrunnlag omsorgspengeGrunnlagDto = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto), List.of());
        InntektsmeldingDto inntektsmelding = InntektsmeldingDtoBuilder.builder()
                .medRefusjon(Beløp.fra(refusjon))
                .medBeløp(Beløp.fra(beregnet))
                .medArbeidsgiver(arbeidsgiver)
                .build();
        Skjæringstidspunkt skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(ANDEL_FOM)
                .medSkjæringstidspunktOpptjening(ANDEL_FOM).build();
        KoblingReferanse koblingReferanse = KoblingReferanse.fra(FagsakYtelseType.OMSORGSPENGER, AktørId.dummy(), 1L, UUID.randomUUID(), Optional.empty(), skjæringstidspunkt);

        var input = new BeregningsgrunnlagGUIInput(lagReferanseMedStp(koblingReferanse),
                InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of(inntektsmelding)).build(), List.of(),  omsorgspengeGrunnlagDto).medBeregningsgrunnlagGrunnlag(grunnlag);
        YtelsespesifiktGrunnlagTjenesteOMP ytelsespesifiktGrunnlagTjenesteOMP = new YtelsespesifiktGrunnlagTjenesteOMP();
        // Act
        Optional<YtelsespesifiktGrunnlagDto> resultat = ytelsespesifiktGrunnlagTjenesteOMP.map(input);
        // Assert
        assertThat(resultat.get()).isInstanceOf(OmsorgspengeGrunnlagDto.class);
        OmsorgspengeGrunnlagDto omsorgspengeGrunnlagDtoResultat = (OmsorgspengeGrunnlagDto) resultat.get();
        assertThat(omsorgspengeGrunnlagDtoResultat.getSkalAvviksvurdere()).isEqualTo(true);
    }

    @Test
    public void skalSetteRiktigYtelsesspesifikkInformasjonNårOmsorgspengerOgAtOgFullRefusjon() {
        //Arrange
        BigDecimal beregnet = BigDecimal.valueOf(20_000);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(240_000);
        BigDecimal refusjon = beregnet;
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        this.beregningAktiviteter = lagBeregningAktiviteter(arbeidsgiver);
        var beregningsgrunnlag = no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .medGrunnbeløp(Beløp.fra(GRUNNBELØP))
                .build();
        BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag);
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori.ARBEIDSTAKER)
                .medAndelsnr(ANDELSNR)
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(Beløp.fra(beregnetPrÅr))
                .build(bgPeriode);
        this.grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktiviteter)
                .medBeregningsgrunnlag(beregningsgrunnlag).build(BeregningsgrunnlagTilstand.FORESLÅTT);

        LocalDate PeriodeFom =SKJÆRINGSTIDSPUNKT;
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(PeriodeFom,
                PeriodeFom.plusMonths(1)), Utbetalingsgrad.valueOf(100));
        AktivitetDto aktivitetDto = new AktivitetDto(arbeidsgiver,
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(aktivitetDto, List.of(periodeMedUtbetalingsgradDto));
        OmsorgspengerGrunnlag omsorgspengeGrunnlagDto = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto), List.of());
        InntektsmeldingDto inntektsmelding = InntektsmeldingDtoBuilder.builder()
                .medRefusjon(Beløp.fra(refusjon))
                .medBeløp(Beløp.fra(beregnet))
                .medArbeidsgiver(arbeidsgiver)
                .build();

        Skjæringstidspunkt skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT)
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
        KoblingReferanse koblingReferanse = KoblingReferanse.fra(FagsakYtelseType.OMSORGSPENGER, AktørId.dummy(), 1L, UUID.randomUUID(), Optional.empty(), skjæringstidspunkt);
        var input = new BeregningsgrunnlagGUIInput(lagReferanseMedStp(koblingReferanse),
                InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of(inntektsmelding)).build(), List.of(),  omsorgspengeGrunnlagDto).medBeregningsgrunnlagGrunnlag(grunnlag);
        YtelsespesifiktGrunnlagTjenesteOMP ytelsespesifiktGrunnlagTjenesteOMP = new YtelsespesifiktGrunnlagTjenesteOMP();
        // Act
        Optional<YtelsespesifiktGrunnlagDto> resultat = ytelsespesifiktGrunnlagTjenesteOMP.map(input);
        // Assert
        assertThat(resultat.get()).isInstanceOf(OmsorgspengeGrunnlagDto.class);
        OmsorgspengeGrunnlagDto omsorgspengeGrunnlagDtoResultat = (OmsorgspengeGrunnlagDto) resultat.get();
        assertThat(omsorgspengeGrunnlagDtoResultat.getSkalAvviksvurdere()).isEqualTo(false);
    }

    @Test
    public void skalSetteRiktigYtelsesspesifikkInformasjonNårOmsorgspengerOgFlOgAtOgFullRefusjon() {
        //Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        this.beregningAktiviteter = lagBeregningAktiviteter(arbeidsgiver);
        var beregningsgrunnlag = no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .build();
        BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag);
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(Inntektskategori.FRILANSER)
                .medAndelsnr(ANDELSNR)
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .build(bgPeriode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAndelsnr(ANDELSNR+1)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(bgPeriode);
        this.grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktiviteter)
                .medBeregningsgrunnlag(beregningsgrunnlag).build(BeregningsgrunnlagTilstand.FORESLÅTT);

        Skjæringstidspunkt skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT)
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
        KoblingReferanse koblingReferanse = KoblingReferanse.fra(FagsakYtelseType.OMSORGSPENGER, AktørId.dummy(), 1L, UUID.randomUUID(), Optional.empty(), skjæringstidspunkt);
        BeregningsgrunnlagGUIInput input = new BeregningsgrunnlagGUIInput(koblingReferanse, null, List.of(), null).medBeregningsgrunnlagGrunnlag(grunnlag);
        YtelsespesifiktGrunnlagTjenesteOMP ytelsespesifiktGrunnlagTjenesteOMP = new YtelsespesifiktGrunnlagTjenesteOMP();
        // Act
        Optional<YtelsespesifiktGrunnlagDto> resultat = ytelsespesifiktGrunnlagTjenesteOMP.map(input);
        // Assert
        assertThat(resultat.get()).isInstanceOf(OmsorgspengeGrunnlagDto.class);
        OmsorgspengeGrunnlagDto omsorgspengeGrunnlagDtoResultat = (OmsorgspengeGrunnlagDto) resultat.get();
        assertThat(omsorgspengeGrunnlagDtoResultat.getSkalAvviksvurdere()).isEqualTo(true);
    }

    @Test
    public void skalSetteRiktigYtelsesspesifikkInformasjonNårOmsorgspengerOgSnOgAtOgFullRefusjon() {
        //Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        this.beregningAktiviteter = lagBeregningAktiviteter(arbeidsgiver);
        var beregningsgrunnlag = no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .build();
        BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag);
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medAndelsnr(ANDELSNR)
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .build(bgPeriode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAndelsnr(ANDELSNR+1)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(bgPeriode);
        this.grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktiviteter)
                .medBeregningsgrunnlag(beregningsgrunnlag).build(BeregningsgrunnlagTilstand.FORESLÅTT);

        Skjæringstidspunkt skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT)
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
        KoblingReferanse koblingReferanse = KoblingReferanse.fra(FagsakYtelseType.OMSORGSPENGER, AktørId.dummy(), 1L, UUID.randomUUID(), Optional.empty(), skjæringstidspunkt);
        BeregningsgrunnlagGUIInput input = new BeregningsgrunnlagGUIInput(koblingReferanse, null, List.of(), null).medBeregningsgrunnlagGrunnlag(grunnlag);
        YtelsespesifiktGrunnlagTjenesteOMP ytelsespesifiktGrunnlagTjenesteOMP = new YtelsespesifiktGrunnlagTjenesteOMP();
        // Act
        Optional<YtelsespesifiktGrunnlagDto> resultat = ytelsespesifiktGrunnlagTjenesteOMP.map(input);
        // Assert
        assertThat(resultat.get()).isInstanceOf(OmsorgspengeGrunnlagDto.class);
        OmsorgspengeGrunnlagDto omsorgspengeGrunnlagDtoResultat = (OmsorgspengeGrunnlagDto) resultat.get();
        assertThat(omsorgspengeGrunnlagDtoResultat.getSkalAvviksvurdere()).isEqualTo(true);
    }


    private KoblingReferanse lagReferanseMedStp(KoblingReferanse koblingReferanse) {
        Skjæringstidspunkt skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT)
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
        return koblingReferanse.medSkjæringstidspunkt(skjæringstidspunkt);
    }

    private BeregningAktivitetAggregatDto lagBeregningAktiviteter(Arbeidsgiver arbeidsgiver) {
        return lagBeregningAktiviteter(BeregningAktivitetAggregatDto.builder(), arbeidsgiver);
    }

    private BeregningAktivitetAggregatDto lagBeregningAktiviteter(BeregningAktivitetAggregatDto.Builder builder, Arbeidsgiver arbeidsgiver) {
        return builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT)
                .leggTilAktivitet(BeregningAktivitetDto.builder()
                        .medArbeidsgiver(arbeidsgiver)
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                        .medPeriode(Intervall.fraOgMedTilOgMed(ANDEL_FOM, ANDEL_TOM))
                        .build())
                .build();
    }

    private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
        return BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(ANDEL_FOM, null)
                .build(beregningsgrunnlag);
    }
}
