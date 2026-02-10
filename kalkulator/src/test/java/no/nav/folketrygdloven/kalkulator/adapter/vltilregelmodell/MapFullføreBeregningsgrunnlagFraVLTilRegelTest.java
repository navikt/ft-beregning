package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.MINUS_DAYS_10;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.MINUS_DAYS_5;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.MINUS_YEARS_1;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.MINUS_YEARS_2;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.MINUS_YEARS_3;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.NOW;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.buildSammenligningsgrunnlagPrStatus;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.buildVLBGAktivitetStatus;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.buildVLBGPStatus;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.buildVLBGPStatusForSN;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.buildVLBGPeriode;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.buildVLBeregningsgrunnlag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.felles.MeldekortUtils;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OpptjeningsnøkkelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.testutilities.TestHjelper;
import no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

class MapFullføreBeregningsgrunnlagFraVLTilRegelTest {

	private static final int MELDEKORTSATS1 = 1000;
	private static final int MELDEKORTSATS2 = 1100;
	private static final int SIGRUN_2015 = 500000;
	private static final int SIGRUN_2016 = 600000;
	private static final int SIGRUN_2017 = 700000;
	private static final int TOTALINNTEKT_SIGRUN = SIGRUN_2015 + SIGRUN_2016 + SIGRUN_2017;

	private static final LocalDate FIRST_DAY_PREVIOUS_MONTH = LocalDate.now().minusMonths(1).withDayOfMonth(1);
	private static final Integer INNTEKT_BELOP = 25000;
	private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

	private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
	private YrkesaktivitetDtoBuilder yrkesaktivitetBuilder;
	private static final String VIRKSOMHET_A = "42";
	private static final String VIRKSOMHET_B = "47";

	private InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseBuilder;
	private final MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel();
	private final Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

	private InntektArbeidYtelseAggregatBuilder opprettForBehandling(InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {
        var fraOgMed = MINUS_YEARS_1.withDayOfMonth(1);
        var tilOgMed = fraOgMed.plusYears(1);
		inntektArbeidYtelseBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(iayGrunnlagBuilder.getKladd().getRegisterVersjon(), VersjonTypeDto.REGISTER);
		lagAktørArbeid(inntektArbeidYtelseBuilder, Arbeidsgiver.virksomhet(VIRKSOMHET_A), fraOgMed, tilOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, Optional.empty());
		for (var dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
			lagInntekt(inntektArbeidYtelseBuilder, VIRKSOMHET_A, dt, dt.plusMonths(1));
		}
		return inntektArbeidYtelseBuilder;
	}

	private void lagIAYforTilstøtendeYtelser(InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder, BeregningsgrunnlagDto beregningsgrunnlag) {
        var skjæring = beregningsgrunnlag.getSkjæringstidspunkt();
        var iayBuilder = opprettForBehandling(iayGrunnlagBuilder);
        var aktørYtelseBuilder = iayBuilder.getAktørYtelseBuilder();
        var ytelse = lagYtelse(YtelseType.DAGPENGER,
				skjæring.minusMonths(1).plusDays(1),
				skjæring.plusMonths(6),
				Beløp.fra(MELDEKORTSATS1),
				MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG_ARENA,
				skjæring.minusMonths(1).plusDays(2),
				skjæring.minusMonths(1).plusDays(16),
				YtelseKilde.ARENA);
		aktørYtelseBuilder.leggTilYtelse(ytelse);
		ytelse = lagYtelse(YtelseType.DAGPENGER,
				skjæring.minusMonths(3),
				skjæring.minusMonths(1),
				Beløp.fra(MELDEKORTSATS2),
				new BigDecimal(100),
				skjæring.minusMonths(1).minusDays(13),
				skjæring.minusMonths(1).plusDays(1),
				YtelseKilde.ARENA);
		aktørYtelseBuilder.leggTilYtelse(ytelse);
		iayBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
		iayGrunnlagBuilder.medData(iayBuilder);
	}

	private void lagIAYKelvin(InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder, BeregningsgrunnlagDto beregningsgrunnlag) {
        var skjæring = beregningsgrunnlag.getSkjæringstidspunkt();
        var iayBuilder = opprettForBehandling(iayGrunnlagBuilder);
        var aktørYtelseBuilder = iayBuilder.getAktørYtelseBuilder();
        var ytelse = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER,
				skjæring.minusMonths(1).plusDays(1),
				skjæring.plusMonths(6),
				Beløp.fra(MELDEKORTSATS1),
				MeldekortUtils.MAX_UTBETALING_PROSENT_KELVIN_DP_SAK,
				skjæring.minusMonths(1).plusDays(2),
				skjæring.minusMonths(1).plusDays(16),
				YtelseKilde.KELVIN);
		aktørYtelseBuilder.leggTilYtelse(ytelse);
		ytelse = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER,
				skjæring.minusMonths(3),
				skjæring.minusMonths(1),
				Beløp.fra(MELDEKORTSATS2),
				MeldekortUtils.MAX_UTBETALING_PROSENT_KELVIN_DP_SAK,
				skjæring.minusMonths(1).minusDays(13),
				skjæring.minusMonths(1).plusDays(1),
				YtelseKilde.KELVIN);
		aktørYtelseBuilder.leggTilYtelse(ytelse);
		iayBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
		iayGrunnlagBuilder.medData(iayBuilder);
	}

	private KoblingReferanse lagIAYforTilstøtendeYtelserForMarginalTilfelle(InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder, BeregningsgrunnlagDto beregningsgrunnlag) {
        var skjæring = beregningsgrunnlag.getSkjæringstidspunkt();
        var iayBuilder = opprettForBehandling(iayGrunnlagBuilder);
        var aktørYtelseBuilder = iayBuilder.getAktørYtelseBuilder();
        var ytelse = lagYtelse(YtelseType.DAGPENGER,
				skjæring.minusWeeks(2),
				skjæring.plusMonths(6),
				Beløp.fra(MELDEKORTSATS1),
				MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG_ARENA.subtract(BigDecimal.TEN),
				skjæring.minusDays(5),
				skjæring.plusDays(9),
				YtelseKilde.ARENA);
		aktørYtelseBuilder.leggTilYtelse(ytelse);
		iayBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
		iayGrunnlagBuilder.medData(iayBuilder);
		return koblingReferanse;
	}

	private YtelseDtoBuilder lagYtelse(YtelseType relatertYtelseType,
	                                   LocalDate fom, LocalDate tom, Beløp beløp, BigDecimal utbetalingsgrad,
	                                   LocalDate meldekortFom, LocalDate meldekortTom, YtelseKilde ytelseKilde) {
        var ytelselseBuilder = YtelseDtoBuilder.ny().medPeriode(Intervall.fraOgMedTilOgMed(fom, tom)).medYtelseType(relatertYtelseType).medYtelseKilde(ytelseKilde);
		return ytelselseBuilder.medYtelseType(relatertYtelseType)
				.medVedtaksDagsats(beløp)
				.leggTilYtelseAnvist(ytelselseBuilder.getAnvistBuilder()
						.medAnvistPeriode(Intervall.fraOgMedTilOgMed(meldekortFom, meldekortTom))
						.medDagsats(beløp)
						.medUtbetalingsgradProsent(Stillingsprosent.fra(utbetalingsgrad))
						.build());
	}

	private AktørArbeidDto lagAktørArbeid(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
	                                      Arbeidsgiver arbeidsgiver, LocalDate fom, LocalDate tom, ArbeidType arbeidType, Optional<InternArbeidsforholdRefDto> arbeidsforholdRef) {
        var aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder
				.getAktørArbeidBuilder();

        var opptjeningsnøkkel = arbeidsforholdRef.map(behandlingReferanse -> new OpptjeningsnøkkelDto(behandlingReferanse, arbeidsgiver)).
				orElseGet(() -> OpptjeningsnøkkelDto.forOrgnummer(arbeidsgiver.getIdentifikator()));
		yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(opptjeningsnøkkel, arbeidType);
        var aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        var aktivitetsAvtale = aktivitetsAvtaleBuilder.medPeriode(Intervall.fraOgMedTilOgMed(fom, tom)).medErAnsettelsesPeriode(false);
		yrkesaktivitetBuilder.leggTilAktivitetsAvtale(aktivitetsAvtale)
				.medArbeidType(arbeidType)
				.medArbeidsgiver(arbeidsgiver);

		yrkesaktivitetBuilder.medArbeidsforholdId(arbeidsforholdRef.orElse(null));
		aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
		inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
		return aktørArbeidBuilder.build();
	}

	private void lagInntekt(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, String virksomhetOrgnr,
	                        LocalDate fom, LocalDate tom) {
        var opptjeningsnøkkel = OpptjeningsnøkkelDto.forOrgnummer(virksomhetOrgnr);

        var aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder();

		Stream.of(InntektskildeType.INNTEKT_BEREGNING, InntektskildeType.INNTEKT_SAMMENLIGNING).forEach(kilde -> {
            var inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
            var inntektspost = InntektspostDtoBuilder.ny()
					.medBeløp(Beløp.fra(INNTEKT_BELOP))
					.medPeriode(fom, tom)
					.medInntektspostType(InntektspostType.LØNN);
			inntektBuilder.leggTilInntektspost(inntektspost).medArbeidsgiver(yrkesaktivitetBuilder.build().getArbeidsgiver());
			aktørInntektBuilder.leggTilInntekt(inntektBuilder);
			inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
		});
	}

	@Test
	void skalMapBGForSN() {
		//Arrange
        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var register = opprettForBehandling(iayGrunnlagBuilder);
		iayGrunnlagBuilder.medData(register);
        var beregningsgrunnlag = BeregningsgrunnlagDto.builder(buildVLBeregningsgrunnlag()).leggTilSammenligningsgrunnlag(buildSammenligningsgrunnlagPrStatus(SammenligningsgrunnlagType.SAMMENLIGNING_SN)).build();
		buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER);
		var aktørInntektBuilder = leggTilInntekterFraSigrun();
        var registerVersjon = iayGrunnlagBuilder.getKladd().getRegisterVersjon();
		InntektArbeidYtelseAggregatBuilder.oppdatere(registerVersjon, VersjonTypeDto.REGISTER)
				.leggTilAktørInntekt(aktørInntektBuilder);
        var bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
		buildVLBGPStatusForSN(bgPeriode);

		//Act
        var grunnlag = lagGrunnlag(beregningsgrunnlag);
		final var resultatBG = map(koblingReferanse, grunnlag, iayGrunnlagBuilder);

		//Assert
		assertThat(resultatBG).isNotNull();
		verifiserInntekterFraSigrun(resultatBG, TOTALINNTEKT_SIGRUN);
		assertThat(resultatBG.getSkjæringstidspunkt()).isEqualTo(MINUS_DAYS_5);
		assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
		final var resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().getFirst();
		assertThat(resultatBGP.getBeregningsgrunnlagPeriode().getFom()).isEqualTo(resultatBG.getSkjæringstidspunkt());
		assertThat(resultatBGP.getBeregningsgrunnlagPeriode().getTom()).isEqualTo(resultatBG.getSkjæringstidspunkt().plusYears(3));
		assertThat(resultatBGP.getBruttoPrÅr().doubleValue()).isEqualTo(4444432.32, within(0.01));
		assertThat(resultatBGP.getBeregningsgrunnlagPrStatus()).hasSize(1);
		resultatBGP.getBeregningsgrunnlagPrStatus().forEach(resultatBGPS -> {
					assertThat(resultatBGPS.getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.SN);
					assertThat(resultatBGPS.getBeregningsperiode().getFom()).isEqualTo(MINUS_DAYS_10);
					assertThat(resultatBGPS.getBeregningsperiode().getTom()).isEqualTo(MINUS_DAYS_5);
					assertThat(resultatBGPS.getArbeidsforhold()).isEmpty();
					assertThat(resultatBGPS.getBeregnetPrÅr().doubleValue()).isEqualTo(1000.01, within(0.01));
					assertThat(resultatBGPS.samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isZero();
				}
		);
	}

	@Test
	void skal_mappe_utbetalingsgrad_fra_kelvin() {
		//Arrange
        var beregningsgrunnlag = buildVLBeregningsgrunnlag();
        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
		lagIAYKelvin(iayGrunnlagBuilder, beregningsgrunnlag);
		buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.ARBEIDSAVKLARINGSPENGER);
        var bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
		buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSAVKLARINGSPENGER, Inntektskategori.ARBEIDSAVKLARINGSPENGER, MINUS_DAYS_10, NOW);


		final var resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);

        var aapInntekter = resultatBG.getInntektsgrunnlag().getPeriodeinntekter().stream()
				.filter(mi -> mi.getInntektskilde().equals(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP))
				.toList();
		assertThat(aapInntekter).hasSize(1);
        var dagsats = BigDecimal.valueOf(MELDEKORTSATS1);
		assertThat(aapInntekter.getFirst().getInntekt()).isEqualByComparingTo(dagsats);
		assertThat(aapInntekter.getFirst().getUtbetalingsfaktor()).hasValueSatisfying(utbg ->
				assertThat(utbg).isEqualByComparingTo(BigDecimal.ONE));
	}

	private no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag map(KoblingReferanse koblingReferanse, BeregningsgrunnlagGrunnlagDto grunnlag, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {
		var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(inntektsmeldinger).build();
        var grunnlagDtoBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag);
        var input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(koblingReferanse, grunnlagDtoBuilder, grunnlag.getBeregningsgrunnlagTilstand(), iayGrunnlag);
		return mapBeregningsgrunnlagFraVLTilRegel.map(input, grunnlag);
	}

	private InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder leggTilInntekterFraSigrun() {
        var builder = inntektArbeidYtelseBuilder.getAktørInntektBuilder();
        var inntektBuilder = builder.getInntektBuilder(InntektskildeType.SIGRUN, OpptjeningsnøkkelDto.forType(AktørId.dummy().getId(), OpptjeningsnøkkelDto.Type.AKTØR_ID));
		inntektBuilder.leggTilInntektspost(opprettInntektspostForSigrun(2015, SIGRUN_2015));
		inntektBuilder.leggTilInntektspost(opprettInntektspostForSigrun(2016, SIGRUN_2016));
		inntektBuilder.leggTilInntektspost(opprettInntektspostForSigrun(2017, SIGRUN_2017));
		builder.leggTilInntekt(inntektBuilder);
		return builder;
	}

	private InntektspostDtoBuilder opprettInntektspostForSigrun(int år, int inntekt) {
		return InntektspostDtoBuilder.ny()
				.medBeløp(Beløp.fra(inntekt))
				.medPeriode(LocalDate.of(år, Month.JANUARY, 1), LocalDate.of(år, Month.DECEMBER, 31))
				.medInntektspostType(InntektspostType.LØNN);
	}

	private void verifiserInntekterFraSigrun(final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG, int totalinntektSigrun) {
		assertThat(resultatBG.getInntektsgrunnlag()).isNotNull();
        var ig = resultatBG.getInntektsgrunnlag();
        var fraSigrun = ig.getPeriodeinntekter().stream().filter(mi -> mi.getInntektskilde().equals(Inntektskilde.SIGRUN)).toList();
		assertThat(fraSigrun).isNotEmpty();
        var total = fraSigrun.stream().map(Periodeinntekt::getInntekt).mapToInt(BigDecimal::intValue).sum();
		assertThat(total).isEqualTo(totalinntektSigrun);
	}

	@Test
	void skalMapBGForArebidstakerMedFlereBGPStatuser() {
		//Arrange
        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var register = opprettForBehandling(iayGrunnlagBuilder);
		iayGrunnlagBuilder.medData(register);
        var beregningsgrunnlag = buildVLBeregningsgrunnlag();
		buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER);
        var bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
		buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_2, MINUS_YEARS_1, Arbeidsgiver.virksomhet(VIRKSOMHET_A), OpptjeningAktivitetType.ARBEID);
		buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1, NOW, Arbeidsgiver.virksomhet(VIRKSOMHET_B), OpptjeningAktivitetType.ARBEID);
		TestHjelper.lagAktørArbeid(register, Arbeidsgiver.virksomhet(VIRKSOMHET_A), MINUS_YEARS_3, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
		TestHjelper.lagAktørArbeid(register, Arbeidsgiver.virksomhet(VIRKSOMHET_B), MINUS_YEARS_3, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

		//Act
		final var resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);

		//Assert
		assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
		final var resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().getFirst();
		assertThat(resultatBGP.getBeregningsgrunnlagPrStatus()).hasSize(1);
		resultatBGP.getBeregningsgrunnlagPrStatus().forEach(resultatBGPS -> {
					assertThat(resultatBGPS.getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL);
					assertThat(resultatBGPS.getBeregningsperiode()).isNull();
					assertThat(resultatBGPS.getBeregnetPrÅr().doubleValue()).isEqualTo(2000.02, within(0.01));
					assertThat(resultatBGPS.samletNaturalytelseBortfaltMinusTilkommetPrÅr().doubleValue()).isEqualTo(6464.64, within(0.01));
					assertThat(resultatBGPS.getArbeidsforhold()).hasSize(2);
					assertThat(resultatBGPS.getArbeidsforhold().get(0).getArbeidsgiverId()).isEqualTo("42");
					assertArbeidforhold(resultatBGPS.getArbeidsforhold().get(0), MINUS_YEARS_2, MINUS_YEARS_1);
					assertThat(resultatBGPS.getArbeidsforhold().get(1).getArbeidsgiverId()).isEqualTo("47");
					assertArbeidforhold(resultatBGPS.getArbeidsforhold().get(1), MINUS_YEARS_1, NOW);
            var startdatoer = resultatBGP.getBeregningsgrunnlagPrStatus().stream().flatMap(s -> s.getArbeidsforhold().stream()).map(a -> a.getArbeidsforhold().getStartdato()).distinct().toList();
					assertThat(startdatoer).containsOnly(MINUS_YEARS_3);
				}
		);
	}

	@Test
	void skal_mappe_bg_for_arbeidstaker_hos_privatperson_og_virksomhet() {
		//Arrange
        var aktørId = AktørId.dummy();
        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var register = opprettForBehandling(iayGrunnlagBuilder);
		TestHjelper.lagAktørArbeid(register, Arbeidsgiver.person(aktørId), MINUS_YEARS_3, ArbeidType.FORENKLET_OPPGJØRSORDNING);
		TestHjelper.lagAktørArbeid(register, Arbeidsgiver.virksomhet(VIRKSOMHET_B), MINUS_YEARS_3, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
		iayGrunnlagBuilder.medData(register);
        var beregningsgrunnlag = buildVLBeregningsgrunnlag();
		buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER);
        var bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
		buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1, NOW, Arbeidsgiver.person(aktørId), OpptjeningAktivitetType.ARBEID);
		buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_2, MINUS_YEARS_1, Arbeidsgiver.virksomhet(VIRKSOMHET_B), OpptjeningAktivitetType.ARBEID);
		lagAktørArbeid(register, Arbeidsgiver.virksomhet(VIRKSOMHET_B), MINUS_DAYS_10, LocalDate.MAX, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, Optional.empty());
		lagAktørArbeid(register, Arbeidsgiver.person(aktørId), MINUS_DAYS_10, LocalDate.MAX, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, Optional.empty());
		//Act

		final var resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);

		//Assert
		assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
		final var resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().getFirst();
		assertThat(resultatBGP.getBeregningsgrunnlagPrStatus()).hasSize(1);
		resultatBGP.getBeregningsgrunnlagPrStatus().forEach(resultatBGPS -> {
			assertThat(resultatBGPS.getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL);
			assertThat(resultatBGPS.getBeregningsperiode()).isNull();
			assertThat(resultatBGPS.getBeregnetPrÅr().doubleValue()).isEqualTo(2000.02, within(0.01));
			assertThat(resultatBGPS.samletNaturalytelseBortfaltMinusTilkommetPrÅr().doubleValue()).isEqualTo(6464.64, within(0.01));
			assertThat(resultatBGPS.getArbeidsforhold()).hasSize(2);
			assertThat(resultatBGPS.getArbeidsforhold().get(0).getArbeidsforhold().getAktørId()).isEqualTo(aktørId.getId());
			assertArbeidforhold(resultatBGPS.getArbeidsforhold().get(0), MINUS_YEARS_1, NOW);
			assertThat(resultatBGPS.getArbeidsforhold().get(1).getArbeidsgiverId()).isEqualTo("47");
			assertArbeidforhold(resultatBGPS.getArbeidsforhold().get(1), MINUS_YEARS_2, MINUS_YEARS_1);
		});
	}

	@Test
	void skalMapBGForATogSNBeregeningGPStatuser() {
		//Arrange
        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var register = opprettForBehandling(iayGrunnlagBuilder);
		TestHjelper.lagAktørArbeid(register, Arbeidsgiver.virksomhet(VIRKSOMHET_A), MINUS_YEARS_3, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
		iayGrunnlagBuilder.medData(register);
        var beregningsgrunnlag = buildVLBeregningsgrunnlag();
		buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER);
        var bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
		buildVLBGPStatusForSN(bgPeriode);
		buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1, NOW, Arbeidsgiver.virksomhet(VIRKSOMHET_A), OpptjeningAktivitetType.ARBEID);

		//Act

		final var resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);
		//Assert
		assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
		final var resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().getFirst();
		assertThat(resultatBGP.getBeregningsgrunnlagPrStatus()).hasSize(2);
		resultatBGP.getBeregningsgrunnlagPrStatus().forEach(resultatBGPStatus -> {
			if (resultatBGPStatus.getAktivitetStatus().equals(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.SN)) {
				assertThat(resultatBGPStatus.getArbeidsforhold()).isEmpty();
			} else {
				assertThat(resultatBGPStatus.getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL);
				assertThat(resultatBGPStatus.getArbeidsforhold()).hasSize(1);
			}
		});
	}

	@Test
	void skalMapBGForArbeidstakerMedInntektsgrunnlag() {
		// Arrange
        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var register = opprettForBehandling(iayGrunnlagBuilder);
		TestHjelper.lagAktørArbeid(register, Arbeidsgiver.virksomhet(VIRKSOMHET_A), MINUS_YEARS_3, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
		iayGrunnlagBuilder.medData(register);
        var beregningsgrunnlag = buildVLBeregningsgrunnlag();
		buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER);
        var bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
		buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1, NOW, Arbeidsgiver.virksomhet(VIRKSOMHET_A), OpptjeningAktivitetType.ARBEID);

		// Act

		final var resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);

		// Assert
		assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
		final var resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().getFirst();

        var månedsinntekter = resultatBG.getInntektsgrunnlag().getPeriodeinntekter();
		assertThat(antallMånedsinntekter(månedsinntekter, INNTEKTSKOMPONENTEN_BEREGNING)).isEqualTo(12);
		assertThat(antallMånedsinntekter(månedsinntekter, INNTEKTSKOMPONENTEN_SAMMENLIGNING)).isEqualTo(12);
		assertThat(månedsinntekter).hasSize(24);
        var inntektBeregning = resultatBGP.getInntektsgrunnlag().getPeriodeinntekt(INNTEKTSKOMPONENTEN_BEREGNING, FIRST_DAY_PREVIOUS_MONTH);
        var inntektSammenligning = resultatBGP.getInntektsgrunnlag().getPeriodeinntekt(INNTEKTSKOMPONENTEN_SAMMENLIGNING, FIRST_DAY_PREVIOUS_MONTH);
		assertInntektsgrunnlag(inntektBeregning);
		assertInntektsgrunnlag(inntektSammenligning);
	}

	@Test
	void skalMappeTilstøtendeYtelserDPogAAP() {
		//Arrange
        var beregningsgrunnlag = buildVLBeregningsgrunnlag();
        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
		lagIAYforTilstøtendeYtelser(iayGrunnlagBuilder, beregningsgrunnlag);
		buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.DAGPENGER);
        var bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
		buildVLBGPStatus(bgPeriode, AktivitetStatus.DAGPENGER, Inntektskategori.DAGPENGER, MINUS_DAYS_10, NOW);


		final var resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);

        var dpMånedsInntekter = resultatBG.getInntektsgrunnlag().getPeriodeinntekter().stream()
				.filter(mi -> mi.getInntektskilde().equals(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP))
				.toList();
		assertThat(dpMånedsInntekter).hasSize(1);
        var dagsats = BigDecimal.valueOf(MELDEKORTSATS1);
		assertThat(dpMånedsInntekter.getFirst().getInntekt()).isEqualByComparingTo(dagsats);
		assertThat(dpMånedsInntekter.getFirst().getUtbetalingsfaktor()).hasValueSatisfying(utbg ->
				assertThat(utbg).isEqualByComparingTo(BigDecimal.ONE));
	}

	@Test
	void skalMappeTilstøtendeYtelserDPogAAPMarginalTilfelle() {
		//Arrange
        var beregningsgrunnlag = buildVLBeregningsgrunnlag();
        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
		koblingReferanse = lagIAYforTilstøtendeYtelserForMarginalTilfelle(iayGrunnlagBuilder, beregningsgrunnlag);
		buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.DAGPENGER);
        var bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
		buildVLBGPStatus(bgPeriode, AktivitetStatus.DAGPENGER, Inntektskategori.DAGPENGER, MINUS_DAYS_10, NOW);


		final var resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);

        var dpMånedsInntekter = resultatBG.getInntektsgrunnlag().getPeriodeinntekter().stream()
				.filter(mi -> mi.getInntektskilde().equals(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP))
				.toList();
		assertThat(dpMånedsInntekter).hasSize(1);
        var dagsats = BigDecimal.valueOf(MELDEKORTSATS1);
		assertThat(dpMånedsInntekter.getFirst().getInntekt()).isEqualByComparingTo(dagsats);
		assertThat(dpMånedsInntekter.getFirst().getUtbetalingsfaktor()).hasValueSatisfying(utbg ->
				assertThat(utbg).isEqualByComparingTo(BigDecimal.ONE));
	}

	@Test
	void skalMappeTilRegelNårBrukerErFrilanser() {
		//Arrange
        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var register = opprettForBehandling(iayGrunnlagBuilder);
		TestHjelper.lagAktørArbeid(register, Arbeidsgiver.virksomhet(VIRKSOMHET_A), MINUS_YEARS_3, ArbeidType.FRILANSER);
		iayGrunnlagBuilder.medData(register);
        var beregningsgrunnlag = BeregningsgrunnlagDto.builder(buildVLBeregningsgrunnlag()).leggTilSammenligningsgrunnlag(buildSammenligningsgrunnlagPrStatus(SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL)).build();
		buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.FRILANSER);
        var bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
		buildVLBGPStatus(bgPeriode, AktivitetStatus.FRILANSER, Inntektskategori.FRILANSER, MINUS_YEARS_2, MINUS_YEARS_1, null, OpptjeningAktivitetType.FRILANS);
		//Act

		final var resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);
		//Assert
		assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
		final var resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().getFirst();
		assertThat(resultatBGP.getBeregningsgrunnlagPrStatus()).hasSize(1);
		resultatBGP.getBeregningsgrunnlagPrStatus().forEach(resultatBGPS -> {
					assertThat(resultatBGPS.getBeregningsperiode()).isNull();
					assertThat(resultatBGPS.getBeregnetPrÅr().doubleValue()).isEqualTo(1000.01, within(0.01));
				}
		);
	}

	@Test
	void skalMappeTilRegelNårBrukerErArbeidstaker() {
		//Arrange
        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var register = opprettForBehandling(iayGrunnlagBuilder);
		iayGrunnlagBuilder.medData(register);
        var beregningsgrunnlag = BeregningsgrunnlagDto.builder(buildVLBeregningsgrunnlag()).leggTilSammenligningsgrunnlag(buildSammenligningsgrunnlagPrStatus(SammenligningsgrunnlagType.SAMMENLIGNING_AT)).build();
		buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER);
        var bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
		buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_2, MINUS_YEARS_1, Arbeidsgiver.virksomhet(VIRKSOMHET_A), OpptjeningAktivitetType.ARBEID);
		buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1, NOW, Arbeidsgiver.virksomhet(VIRKSOMHET_B), OpptjeningAktivitetType.ARBEID);
		TestHjelper.lagAktørArbeid(register, Arbeidsgiver.virksomhet(VIRKSOMHET_A), MINUS_YEARS_3, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
		TestHjelper.lagAktørArbeid(register, Arbeidsgiver.virksomhet(VIRKSOMHET_B), MINUS_YEARS_3, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
		//Act
		final var resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);
		//Assert
		assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
		final var resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().getFirst();
		assertThat(resultatBGP.getBeregningsgrunnlagPrStatus()).hasSize(1);
		resultatBGP.getBeregningsgrunnlagPrStatus().forEach(resultatBGPS -> {
					assertThat(resultatBGPS.getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL);
					assertThat(resultatBGPS.getBeregningsperiode()).isNull();
					assertThat(resultatBGPS.getBeregnetPrÅr().doubleValue()).isEqualTo(2000.02, within(0.01));
					assertThat(resultatBGPS.samletNaturalytelseBortfaltMinusTilkommetPrÅr().doubleValue()).isEqualTo(6464.64, within(0.01));
					assertThat(resultatBGPS.getArbeidsforhold()).hasSize(2);
					assertThat(resultatBGPS.getArbeidsforhold().get(0).getArbeidsgiverId()).isEqualTo("42");
					assertArbeidforhold(resultatBGPS.getArbeidsforhold().get(0), MINUS_YEARS_2, MINUS_YEARS_1);
					assertThat(resultatBGPS.getArbeidsforhold().get(1).getArbeidsgiverId()).isEqualTo("47");
					assertArbeidforhold(resultatBGPS.getArbeidsforhold().get(1), MINUS_YEARS_1, NOW);
				}
		);
	}

	private BeregningsgrunnlagGrunnlagDto lagGrunnlag(BeregningsgrunnlagDto beregningsgrunnlag) {
		return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
				.medBeregningsgrunnlag(beregningsgrunnlag)
				.medRegisterAktiviteter(BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT, OpptjeningAktivitetType.ARBEID))
				.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
	}

	private long antallMånedsinntekter(List<Periodeinntekt> månedsinntekter, Inntektskilde inntektskomponentenBeregning) {
		return månedsinntekter.stream().filter(m -> m.getInntektskilde().equals(inntektskomponentenBeregning)).count();
	}

	private void assertInntektsgrunnlag(Optional<Periodeinntekt> inntektBeregning) {
		assertThat(inntektBeregning).isPresent();
		assertThat(inntektBeregning).hasValueSatisfying(månedsinntekt -> {
			assertThat(månedsinntekt.getInntekt().intValue()).isEqualTo(INNTEKT_BELOP);
			assertThat(månedsinntekt.getFom()).isEqualTo(FIRST_DAY_PREVIOUS_MONTH);
			assertThat(månedsinntekt.fraInntektsmelding()).isFalse();
		});
	}

	private void assertArbeidforhold(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, LocalDate fom, LocalDate tom) {
		assertThat(arbeidsforhold.getBeregnetPrÅr().doubleValue()).isEqualTo(1000.01, within(0.01));
		assertThat(arbeidsforhold.getNaturalytelseBortfaltPrÅr()).hasValueSatisfying(naturalYtelseBortfalt ->
				assertThat(naturalYtelseBortfalt.doubleValue()).isEqualTo(3232.32, within(0.01))
		);
		assertThat(arbeidsforhold.getBeregningsperiode()).isEqualTo(Periode.of(fom, tom));
	}
}
