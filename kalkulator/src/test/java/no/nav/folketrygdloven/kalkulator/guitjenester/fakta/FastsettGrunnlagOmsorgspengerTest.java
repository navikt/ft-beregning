package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
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
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

class FastsettGrunnlagOmsorgspengerTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);

    private static final Inntektskategori INNTEKTSKATEGORI = Inntektskategori.ARBEIDSTAKER;
    private static final Beløp AVKORTET_PR_AAR = Beløp.fra(150000);
    private static final Beløp BRUTTO_PR_AAR = Beløp.fra(300000);
    private static final Beløp REDUSERT_PR_AAR = Beløp.fra(500000);
    private static final LocalDate ANDEL_FOM = LocalDate.now().minusDays(100);
    private static final LocalDate ANDEL_TOM = LocalDate.now();
    private static final String ORGNR = "973093681";

    private static final Beløp RAPPORTERT_PR_AAR = Beløp.fra(300000);
    private static final BigDecimal AVVIK_OVER_25_PROSENT = BigDecimal.valueOf(500L);
    private static final BigDecimal AVVIK_UNDER_25_PROSENT = BigDecimal.valueOf(30L);
    private static final LocalDate SAMMENLIGNING_FOM = LocalDate.now().minusDays(100);
    private static final LocalDate SAMMENLIGNING_TOM = LocalDate.now();

    private BeregningsgrunnlagGrunnlagDto grunnlag;

    @Test
    void skalIkkeFastsetteGrunnlagNårYtelseErOmsorgspengerMedAvvikIBeregningOgFullRefusjon() {
        //Arange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        var Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25ProsentMedKunSammenligningsgrunnlag();
        var bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelAt(bgPeriode, arbeidsgiver, 1L);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver);
        var periodeFom = SKJÆRINGSTIDSPUNKT;
        var periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(periodeFom,
                periodeFom.plusMonths(1)), Utbetalingsgrad.valueOf(100));
        var aktivitetDto = new AktivitetDto(arbeidsgiver,
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        var utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(aktivitetDto, List.of(periodeMedUtbetalingsgradDto));
        var omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto), List.of());
        var inntektsmelding = InntektsmeldingDtoBuilder.builder()
                .medRefusjon(Beløp.fra(BRUTTO_PR_AAR.verdi().divide(KonfigTjeneste.getMånederIÅr())))
                .medBeløp(Beløp.fra(BRUTTO_PR_AAR.verdi().divide(KonfigTjeneste.getMånederIÅr())))
                .medArbeidsgiver(arbeidsgiver)
                .build();
        var skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT)
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
        var koblingReferanse = KoblingReferanse.fra(FagsakYtelseType.OMSORGSPENGER, AktørId.dummy(), 1L, UUID.randomUUID(), Optional.empty(), skjæringstidspunkt);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of(inntektsmelding)).build();
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, iayGrunnlag, List.of(), omsorgspengerGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        var fastsettGrunnlagOmsorgspenger = new FastsettGrunnlagOmsorgspenger();
        //Act
        var skalGrunnlagFastsettes = fastsettGrunnlagOmsorgspenger.skalGrunnlagFastsettes(input,
                grunnlag.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0));
        // Assert
        assertThat(skalGrunnlagFastsettes).isFalse();
    }

	@Test
	void skalFastsetteGrunnlagNårYtelseErOmsorgspengerMedAvvikIBeregningOgFullRefusjonDersomBrukerHarSøkt() {
		//Arange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        var Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25ProsentMedKunSammenligningsgrunnlag();
        var bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
		byggAndelAt(bgPeriode, arbeidsgiver, 1L);
		lagBehandling(Beregningsgrunnlag, arbeidsgiver);
        var periodeFom = SKJÆRINGSTIDSPUNKT;
        var periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(periodeFom,
				periodeFom.plusMonths(1)), Utbetalingsgrad.valueOf(100));
        var aktivitetDto = new AktivitetDto(arbeidsgiver,
				InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        var utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(aktivitetDto, List.of(periodeMedUtbetalingsgradDto));
        var omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto), List.of(Intervall.fraOgMedTilOgMed(periodeFom, periodeFom.plusMonths(1))));
        var inntektsmelding = InntektsmeldingDtoBuilder.builder()
				.medRefusjon(Beløp.fra(BRUTTO_PR_AAR.verdi().divide(KonfigTjeneste.getMånederIÅr())))
				.medBeløp(Beløp.fra(BRUTTO_PR_AAR.verdi().divide(KonfigTjeneste.getMånederIÅr())))
				.medArbeidsgiver(arbeidsgiver)
				.build();
        var skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT)
				.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
        var koblingReferanse = KoblingReferanse.fra(FagsakYtelseType.OMSORGSPENGER, AktørId.dummy(), 1L, UUID.randomUUID(), Optional.empty(), skjæringstidspunkt);
		var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of(inntektsmelding)).build();
		var input = new BeregningsgrunnlagGUIInput(koblingReferanse, iayGrunnlag, List.of(), omsorgspengerGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        var fastsettGrunnlagOmsorgspenger = new FastsettGrunnlagOmsorgspenger();
		//Act
		var skalGrunnlagFastsettes = fastsettGrunnlagOmsorgspenger.skalGrunnlagFastsettes(input,
				grunnlag.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0));
		// Assert
		assertThat(skalGrunnlagFastsettes).isTrue();
	}


	@Test
    void skalIkkeFastsetteGrunnlagNårYtelseErOmsorgspengerMedAvvikIBeregningOgRefusjonEr6G() {
        //Arange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        var Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25ProsentMedKunSammenligningsgrunnlag();
        var bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        var bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
                .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(INNTEKTSKATEGORI)
                .medAndelsnr(1L)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
                .medBeregnetPrÅr(Beløp.fra(1_200_000))
                .build(bgPeriode);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver);
        var periodeFom = SKJÆRINGSTIDSPUNKT;
        var periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(periodeFom,
                periodeFom.plusMonths(1)), Utbetalingsgrad.valueOf(100));
        var aktivitetDto = new AktivitetDto(arbeidsgiver,
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        var utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(aktivitetDto, List.of(periodeMedUtbetalingsgradDto));
        var omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto), List.of());
        var inntektsmelding = InntektsmeldingDtoBuilder.builder()
                .medRefusjon(Beløp.fra(60_000))
                .medBeløp(Beløp.fra(1_200_000).map(v -> v.divide(KonfigTjeneste.getMånederIÅr())))
                .medArbeidsgiver(arbeidsgiver)
                .build();
        var skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT)
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
        var koblingReferanse = KoblingReferanse.fra(FagsakYtelseType.OMSORGSPENGER, AktørId.dummy(), 1L, UUID.randomUUID(), Optional.empty(), skjæringstidspunkt);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of(inntektsmelding)).build();
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, iayGrunnlag, List.of(), omsorgspengerGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        var fastsettGrunnlagOmsorgspenger = new FastsettGrunnlagOmsorgspenger();
        //Act
        var skalGrunnlagFastsettes = fastsettGrunnlagOmsorgspenger.skalGrunnlagFastsettes(input,
                grunnlag.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0));
        // Assert
        assertThat(skalGrunnlagFastsettes).isFalse();
    }

    @Test
    void skalIkkeFastsetteGrunnlagNårYtelseErOmsorgspengerUtenAvvikIBeregning() {
        //Arange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        var Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikUnder25ProsentMedKunSammenligningsgrunnlag();
        var bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelAt(bgPeriode, arbeidsgiver, 1L);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver);
        var PeriodeFom =LocalDate.of(2020,01,26);
        var periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(PeriodeFom,
                PeriodeFom.plusMonths(1)), Utbetalingsgrad.valueOf(100));
        var aktivitetDto = new AktivitetDto(arbeidsgiver,
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        var utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(aktivitetDto, List.of(periodeMedUtbetalingsgradDto));
        var omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto), List.of());
        var inntektsmelding = InntektsmeldingDtoBuilder.builder()
                .medBeløp(Beløp.fra(BRUTTO_PR_AAR.verdi().divide(KonfigTjeneste.getMånederIÅr())))
                .medArbeidsgiver(arbeidsgiver)
                .build();
        var skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT)
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
        var koblingReferanse = KoblingReferanse.fra(FagsakYtelseType.OMSORGSPENGER, AktørId.dummy(), 1L, UUID.randomUUID(), Optional.empty(), skjæringstidspunkt);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of(inntektsmelding)).build();
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, iayGrunnlag, List.of(), omsorgspengerGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        var fastsettGrunnlagOmsorgspenger = new FastsettGrunnlagOmsorgspenger();
        //Act
        var skalGrunnlagFastsettes = fastsettGrunnlagOmsorgspenger.skalGrunnlagFastsettes(input,
                grunnlag.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0));
        // Assert
        assertThat(skalGrunnlagFastsettes).isFalse();
    }

    @Test
    void skalFastsetteGrunnlagNårYtelseErOmsorgspengerMedAvvikIBeregningOgIkkeFullRefusjon() {
        //Arange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        var Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25ProsentMedKunSammenligningsgrunnlag();
        var bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelAt(bgPeriode, arbeidsgiver, 1L);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver);
        var PeriodeFom =ANDEL_FOM;
        var periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(PeriodeFom,
                PeriodeFom.plusMonths(1)), Utbetalingsgrad.valueOf(100));
        var aktivitetDto = new AktivitetDto(arbeidsgiver,
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        var utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(aktivitetDto, List.of(periodeMedUtbetalingsgradDto));
        var omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto), List.of());
        var inntektsmelding = InntektsmeldingDtoBuilder.builder()
                .medRefusjon(Beløp.fra(BRUTTO_PR_AAR.verdi().divide(BigDecimal.valueOf(30), RoundingMode.HALF_EVEN)))
                .medBeløp(Beløp.fra(BRUTTO_PR_AAR.verdi().divide(KonfigTjeneste.getMånederIÅr(), RoundingMode.HALF_EVEN)))
                .medArbeidsgiver(arbeidsgiver)
                .build();
        var skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(ANDEL_FOM)
                .medSkjæringstidspunktOpptjening(ANDEL_FOM).build();
        var koblingReferanse = KoblingReferanse.fra(FagsakYtelseType.OMSORGSPENGER, AktørId.dummy(), 1L, UUID.randomUUID(), Optional.empty(), skjæringstidspunkt);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of(inntektsmelding)).build();
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, iayGrunnlag, List.of(), omsorgspengerGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        var fastsettGrunnlagOmsorgspenger = new FastsettGrunnlagOmsorgspenger();
        //Act
        var skalGrunnlagFastsettes = fastsettGrunnlagOmsorgspenger.skalGrunnlagFastsettes(input,
                grunnlag.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0));
        // Assert
        assertThat(skalGrunnlagFastsettes).isTrue();
    }

    @Test
    void skalReturnereFalseNårForeslåBeregningIkkeErKjørt() {
        //Arange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        var beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25ProsentMedKunSammenligningsgrunnlag();
        var bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag);
        var bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
                .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(INNTEKTSKATEGORI)
                .medAndelsnr(1L)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
                .medBeregnetPrÅr(null)
                .build(bgPeriode);
        var beregningAktiviteter = lagBeregningAktiviteter(arbeidsgiver);
        var beregningsgrunnlagGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktiviteter)
                .medBeregningsgrunnlag(beregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPRETTET);
        this.grunnlag = beregningsgrunnlagGrunnlag;

        var PeriodeFom =LocalDate.of(2020,01,26);
        var periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(PeriodeFom,
                PeriodeFom.plusMonths(1)), Utbetalingsgrad.valueOf(100));
        var aktivitetDto = new AktivitetDto(arbeidsgiver,
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        var utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(aktivitetDto, List.of(periodeMedUtbetalingsgradDto));
        var omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto), List.of());
        var inntektsmelding = InntektsmeldingDtoBuilder.builder()
                .medRefusjon(Beløp.fra(10_000))
                .medBeløp(Beløp.fra(20_000))
                .medArbeidsgiver(arbeidsgiver)
                .build();
        var skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT)
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
        var koblingReferanse = KoblingReferanse.fra(FagsakYtelseType.OMSORGSPENGER, AktørId.dummy(), 1L, UUID.randomUUID(), Optional.empty(), skjæringstidspunkt);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of(inntektsmelding)).build();
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, iayGrunnlag, List.of(), omsorgspengerGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        var fastsettGrunnlagOmsorgspenger = new FastsettGrunnlagOmsorgspenger();
        //Act
        var skalGrunnlagFastsettes = fastsettGrunnlagOmsorgspenger.skalGrunnlagFastsettes(input,
                grunnlag.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0));
        // Assert
        assertThat(skalGrunnlagFastsettes).isFalse();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedAvvikOver25ProsentMedKunSammenligningsgrunnlag() {
        var sammenligningsgrunnlagDto = SammenligningsgrunnlagPrStatusDto.builder()
                .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM)
                .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                .medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL)
                .medAvvikPromilleNy(AVVIK_OVER_25_PROSENT)
                .build();

        return BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagDto)
                .medGrunnbeløp(Beløp.fra(99_858))
                .build();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedAvvikUnder25ProsentMedKunSammenligningsgrunnlag() {
        var sammenligningsgrunnlagDto = SammenligningsgrunnlagPrStatusDto.builder()
                .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM)
                .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                .medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL)
                .medAvvikPromilleNy(AVVIK_UNDER_25_PROSENT)
                .build();

        return BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagDto)
                .medGrunnbeløp(Beløp.fra(99_858))
                .build();
    }

    private void byggAndelAt(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, Arbeidsgiver arbeidsgiver, Long andelsNr) {
        var bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
                .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(INNTEKTSKATEGORI)
                .medAndelsnr(andelsNr)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
                .medBeregnetPrÅr(BRUTTO_PR_AAR)
                .medAvkortetPrÅr(AVKORTET_PR_AAR)
                .medRedusertPrÅr(REDUSERT_PR_AAR)
                .build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagDto lagBehandling(BeregningsgrunnlagDto beregningsgrunnlag, Arbeidsgiver arbeidsgiver) {
        var beregningAktiviteter = lagBeregningAktiviteter(arbeidsgiver);
        var beregningsgrunnlagGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktiviteter)
                .medBeregningsgrunnlag(beregningsgrunnlag).build(BeregningsgrunnlagTilstand.FORESLÅTT);

        this.grunnlag = beregningsgrunnlagGrunnlag;
        return beregningsgrunnlag;
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

    private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode(BeregningsgrunnlagDto beregningsgrunnlag) {
        return BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(ANDEL_FOM, null)
                .build(beregningsgrunnlag);
    }

}
