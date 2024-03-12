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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.felles.MeldekortUtils;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
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
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

@ExtendWith(MockitoExtension.class)
public class MapFullføreBeregningsgrunnlagFraVLTilRegelTest {

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
    private String virksomhetA = "42";
    private String virksomhetB = "47";

    private InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseBuilder;
    private MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel();
    private Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

    private InntektArbeidYtelseAggregatBuilder opprettForBehandling(InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {
        LocalDate fraOgMed = MINUS_YEARS_1.withDayOfMonth(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);
        inntektArbeidYtelseBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(iayGrunnlagBuilder.getKladd().getRegisterVersjon(), VersjonTypeDto.REGISTER);
        lagAktørArbeid(inntektArbeidYtelseBuilder, Arbeidsgiver.virksomhet(virksomhetA), fraOgMed, tilOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, Optional.empty());
        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            lagInntekt(inntektArbeidYtelseBuilder, virksomhetA, dt, dt.plusMonths(1));
        }
        return inntektArbeidYtelseBuilder;
    }

    private void lagIAYforTilstøtendeYtelser(InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder, BeregningsgrunnlagDto beregningsgrunnlag) {
        LocalDate skjæring = beregningsgrunnlag.getSkjæringstidspunkt();
        InntektArbeidYtelseAggregatBuilder iayBuilder = opprettForBehandling(iayGrunnlagBuilder);
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = iayBuilder.getAktørYtelseBuilder();
        YtelseDtoBuilder ytelse = lagYtelse(YtelseType.DAGPENGER,
                skjæring.minusMonths(1).plusDays(1),
                skjæring.plusMonths(6),
                Beløp.fra(MELDEKORTSATS1),
                MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG,
                skjæring.minusMonths(1).plusDays(2),
                skjæring.minusMonths(1).plusDays(16));
        aktørYtelseBuilder.leggTilYtelse(ytelse);
        ytelse = lagYtelse(YtelseType.DAGPENGER,
                skjæring.minusMonths(3),
                skjæring.minusMonths(1),
                Beløp.fra(MELDEKORTSATS2),
                new BigDecimal(100),
                skjæring.minusMonths(1).minusDays(13),
                skjæring.minusMonths(1).plusDays(1));
        aktørYtelseBuilder.leggTilYtelse(ytelse);
        iayBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
        iayGrunnlagBuilder.medData(iayBuilder);
    }

    private KoblingReferanse lagIAYforTilstøtendeYtelserForMarginalTilfelle(InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder, BeregningsgrunnlagDto beregningsgrunnlag) {
        LocalDate skjæring = beregningsgrunnlag.getSkjæringstidspunkt();
        InntektArbeidYtelseAggregatBuilder iayBuilder = opprettForBehandling(iayGrunnlagBuilder);
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = iayBuilder.getAktørYtelseBuilder();
        YtelseDtoBuilder ytelse = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER,
                skjæring.minusWeeks(2),
                skjæring.plusMonths(6),
                Beløp.fra(MELDEKORTSATS1),
                MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG.subtract(BigDecimal.TEN),
                skjæring.minusDays(5),
                skjæring.plusDays(9));
        aktørYtelseBuilder.leggTilYtelse(ytelse);
        iayBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
        iayGrunnlagBuilder.medData(iayBuilder);
        return koblingReferanse;
    }

    private YtelseDtoBuilder lagYtelse(YtelseType relatertYtelseType,
                                       LocalDate fom, LocalDate tom, Beløp beløp, BigDecimal utbetalingsgrad,
                                       LocalDate meldekortFom, LocalDate meldekortTom) {
        YtelseDtoBuilder ytelselseBuilder = YtelseDtoBuilder.ny().medPeriode(Intervall.fraOgMedTilOgMed(fom, tom)).medYtelseType(relatertYtelseType);
        return ytelselseBuilder.medYtelseType(YtelseType.DAGPENGER)
                .medVedtaksDagsats(beløp)
                .leggTilYtelseAnvist(ytelselseBuilder.getAnvistBuilder()
                        .medAnvistPeriode(Intervall.fraOgMedTilOgMed(meldekortFom, meldekortTom))
                        .medDagsats(beløp)
                        .medUtbetalingsgradProsent(Stillingsprosent.fra(utbetalingsgrad))
                        .build());
    }

    private AktørArbeidDto lagAktørArbeid(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                          Arbeidsgiver arbeidsgiver, LocalDate fom, LocalDate tom, ArbeidType arbeidType, Optional<InternArbeidsforholdRefDto> arbeidsforholdRef) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder
                .getAktørArbeidBuilder();

        OpptjeningsnøkkelDto opptjeningsnøkkel = arbeidsforholdRef.map(behandlingReferanse -> new OpptjeningsnøkkelDto(behandlingReferanse, arbeidsgiver)).
                orElseGet(() -> OpptjeningsnøkkelDto.forOrgnummer(arbeidsgiver.getIdentifikator()));
        yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(opptjeningsnøkkel, arbeidType);
        AktivitetsAvtaleDtoBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        AktivitetsAvtaleDtoBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder.medPeriode(Intervall.fraOgMedTilOgMed(fom, tom)).medErAnsettelsesPeriode(false);
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
        OpptjeningsnøkkelDto opptjeningsnøkkel = OpptjeningsnøkkelDto.forOrgnummer(virksomhetOrgnr);

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder();

        Stream.of(InntektskildeType.INNTEKT_BEREGNING, InntektskildeType.INNTEKT_SAMMENLIGNING).forEach(kilde -> {
            InntektDtoBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
            InntektspostDtoBuilder inntektspost = InntektspostDtoBuilder.ny()
                    .medBeløp(Beløp.fra(INNTEKT_BELOP))
                    .medPeriode(fom, tom)
                    .medInntektspostType(InntektspostType.LØNN);
            inntektBuilder.leggTilInntektspost(inntektspost).medArbeidsgiver(yrkesaktivitetBuilder.build().getArbeidsgiver());
            aktørInntektBuilder.leggTilInntekt(inntektBuilder);
            inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
        });
    }

    @Test
    public void skalMapBGForSN() {
        //Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        InntektArbeidYtelseAggregatBuilder register = opprettForBehandling(iayGrunnlagBuilder);
        iayGrunnlagBuilder.medData(register);
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder(buildVLBeregningsgrunnlag()).leggTilSammenligningsgrunnlag(buildSammenligningsgrunnlagPrStatus(SammenligningsgrunnlagType.SAMMENLIGNING_SN)).build();
        buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER);
        var aktørInntektBuilder = leggTilInntekterFraSigrun();
        Optional<InntektArbeidYtelseAggregatDto> registerVersjon = iayGrunnlagBuilder.getKladd().getRegisterVersjon();
        InntektArbeidYtelseAggregatBuilder.oppdatere(registerVersjon, VersjonTypeDto.REGISTER)
                .leggTilAktørInntekt(aktørInntektBuilder);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatusForSN(bgPeriode);

        //Act
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlag(beregningsgrunnlag);
        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = map(koblingReferanse, grunnlag, iayGrunnlagBuilder);

        //Assert
        assertThat(resultatBG).isNotNull();
        verifiserInntekterFraSigrun(resultatBG, TOTALINNTEKT_SIGRUN);
        assertThat(resultatBG.getSkjæringstidspunkt()).isEqualTo(MINUS_DAYS_5);
        assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().get(0);
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

    private no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag map(KoblingReferanse koblingReferanse, BeregningsgrunnlagGrunnlagDto grunnlag, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {
        var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(inntektsmeldinger).build();
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagDtoBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag);
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(koblingReferanse, grunnlagDtoBuilder, grunnlag.getBeregningsgrunnlagTilstand(), iayGrunnlag);
        return mapBeregningsgrunnlagFraVLTilRegel.map(input, grunnlag);
    }

    private InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder leggTilInntekterFraSigrun() {
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder builder = inntektArbeidYtelseBuilder.getAktørInntektBuilder();
        InntektDtoBuilder inntektBuilder = builder.getInntektBuilder(InntektskildeType.SIGRUN, OpptjeningsnøkkelDto.forType(AktørId.dummy().getId(), OpptjeningsnøkkelDto.Type.AKTØR_ID));
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
        Inntektsgrunnlag ig = resultatBG.getInntektsgrunnlag();
        List<Periodeinntekt> fraSigrun = ig.getPeriodeinntekter().stream().filter(mi -> mi.getInntektskilde().equals(Inntektskilde.SIGRUN)).collect(Collectors.toList());
        assertThat(fraSigrun).isNotEmpty();
        int total = fraSigrun.stream().map(Periodeinntekt::getInntekt).mapToInt(BigDecimal::intValue).sum();
        assertThat(total).isEqualTo(totalinntektSigrun);
    }

    @Test
    public void skalMapBGForArebidstakerMedFlereBGPStatuser() {
        //Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        InntektArbeidYtelseAggregatBuilder register = opprettForBehandling(iayGrunnlagBuilder);
        iayGrunnlagBuilder.medData(register);
        BeregningsgrunnlagDto beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_2, MINUS_YEARS_1, Arbeidsgiver.virksomhet(virksomhetA), OpptjeningAktivitetType.ARBEID);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1, NOW, Arbeidsgiver.virksomhet(virksomhetB), OpptjeningAktivitetType.ARBEID);
        new TestHjelper().lagAktørArbeid(register, Arbeidsgiver.virksomhet(virksomhetA), MINUS_YEARS_3, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        new TestHjelper().lagAktørArbeid(register, Arbeidsgiver.virksomhet(virksomhetB), MINUS_YEARS_3, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        //Act
        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);

        //Assert
        assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().get(0);
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
                    List<LocalDate> startdatoer = resultatBGP.getBeregningsgrunnlagPrStatus().stream().flatMap(s -> s.getArbeidsforhold().stream()).map(a -> a.getArbeidsforhold().getStartdato()).distinct().toList();
                    assertThat(startdatoer).containsOnly(MINUS_YEARS_3);
                }
        );
    }

    @Test
    public void skal_mappe_bg_for_arbeidstaker_hos_privatperson_og_virksomhet() {
        //Arrange
        AktørId aktørId = AktørId.dummy();
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        InntektArbeidYtelseAggregatBuilder register = opprettForBehandling(iayGrunnlagBuilder);
        new TestHjelper().lagAktørArbeid(register, Arbeidsgiver.person(aktørId), MINUS_YEARS_3, ArbeidType.FORENKLET_OPPGJØRSORDNING);
        new TestHjelper().lagAktørArbeid(register, Arbeidsgiver.virksomhet(virksomhetB), MINUS_YEARS_3, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        iayGrunnlagBuilder.medData(register);
        BeregningsgrunnlagDto beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1, NOW, Arbeidsgiver.person(aktørId), OpptjeningAktivitetType.ARBEID);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_2, MINUS_YEARS_1, Arbeidsgiver.virksomhet(virksomhetB), OpptjeningAktivitetType.ARBEID);
        lagAktørArbeid(register, Arbeidsgiver.virksomhet(virksomhetB), MINUS_DAYS_10, LocalDate.MAX, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, Optional.empty());
        lagAktørArbeid(register, Arbeidsgiver.person(aktørId), MINUS_DAYS_10, LocalDate.MAX, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, Optional.empty());
        //Act

        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);

        //Assert
        assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().get(0);
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
    public void skalMapBGForATogSNBeregeningGPStatuser() {
        //Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        InntektArbeidYtelseAggregatBuilder register = opprettForBehandling(iayGrunnlagBuilder);
        new TestHjelper().lagAktørArbeid(register, Arbeidsgiver.virksomhet(virksomhetA), MINUS_YEARS_3, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        iayGrunnlagBuilder.medData(register);
        BeregningsgrunnlagDto beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatusForSN(bgPeriode);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1, NOW, Arbeidsgiver.virksomhet(virksomhetA), OpptjeningAktivitetType.ARBEID);

        //Act

        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);
        //Assert
        assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().get(0);
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
    public void skalMapBGForArbeidstakerMedInntektsgrunnlag() {
        // Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        InntektArbeidYtelseAggregatBuilder register = opprettForBehandling(iayGrunnlagBuilder);
        new TestHjelper().lagAktørArbeid(register, Arbeidsgiver.virksomhet(virksomhetA), MINUS_YEARS_3, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        iayGrunnlagBuilder.medData(register);
        BeregningsgrunnlagDto beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1, NOW, Arbeidsgiver.virksomhet(virksomhetA), OpptjeningAktivitetType.ARBEID);

        // Act

        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);

        // Assert
        assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().get(0);

        List<Periodeinntekt> månedsinntekter = resultatBG.getInntektsgrunnlag().getPeriodeinntekter();
        assertThat(antallMånedsinntekter(månedsinntekter, INNTEKTSKOMPONENTEN_BEREGNING)).isEqualTo(12);
        assertThat(antallMånedsinntekter(månedsinntekter, INNTEKTSKOMPONENTEN_SAMMENLIGNING)).isEqualTo(12);
        assertThat(månedsinntekter).hasSize(24);
        Optional<Periodeinntekt> inntektBeregning = resultatBGP.getInntektsgrunnlag().getPeriodeinntekt(INNTEKTSKOMPONENTEN_BEREGNING, FIRST_DAY_PREVIOUS_MONTH);
        Optional<Periodeinntekt> inntektSammenligning = resultatBGP.getInntektsgrunnlag().getPeriodeinntekt(INNTEKTSKOMPONENTEN_SAMMENLIGNING, FIRST_DAY_PREVIOUS_MONTH);
        assertInntektsgrunnlag(inntektBeregning);
        assertInntektsgrunnlag(inntektSammenligning);
    }

    @Test
    public void skalMappeTilstøtendeYtelserDPogAAP() {
        //Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = buildVLBeregningsgrunnlag();
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        lagIAYforTilstøtendeYtelser(iayGrunnlagBuilder, beregningsgrunnlag);
        buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.DAGPENGER);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.DAGPENGER, Inntektskategori.DAGPENGER, MINUS_DAYS_10, NOW);


        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);

        List<Periodeinntekt> dpMånedsInntekter = resultatBG.getInntektsgrunnlag().getPeriodeinntekter().stream()
                .filter(mi -> mi.getInntektskilde().equals(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP))
                .collect(Collectors.toList());
        assertThat(dpMånedsInntekter).hasSize(1);
        BigDecimal dagsats = BigDecimal.valueOf(MELDEKORTSATS1);
        assertThat(dpMånedsInntekter.get(0).getInntekt()).isEqualByComparingTo(dagsats);
        assertThat(dpMånedsInntekter.get(0).getUtbetalingsfaktor()).hasValueSatisfying(utbg ->
                assertThat(utbg).isEqualByComparingTo(BigDecimal.ONE));
    }

    @Test
    public void skalMappeTilstøtendeYtelserDPogAAPMarginalTilfelle() {
        //Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = buildVLBeregningsgrunnlag();
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        koblingReferanse = lagIAYforTilstøtendeYtelserForMarginalTilfelle(iayGrunnlagBuilder, beregningsgrunnlag);
        buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.DAGPENGER);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.DAGPENGER, Inntektskategori.DAGPENGER, MINUS_DAYS_10, NOW);


        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);

        List<Periodeinntekt> dpMånedsInntekter = resultatBG.getInntektsgrunnlag().getPeriodeinntekter().stream()
                .filter(mi -> mi.getInntektskilde().equals(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP))
                .collect(Collectors.toList());
        assertThat(dpMånedsInntekter).hasSize(1);
        BigDecimal dagsats = BigDecimal.valueOf(MELDEKORTSATS1);
        assertThat(dpMånedsInntekter.get(0).getInntekt()).isEqualByComparingTo(dagsats);
        assertThat(dpMånedsInntekter.get(0).getUtbetalingsfaktor()).hasValueSatisfying(utbg ->
                assertThat(utbg).isEqualByComparingTo(BigDecimal.ONE));
    }

    @Test
    public void skalMappeTilRegelNårBrukerErFrilanser() {
        //Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        InntektArbeidYtelseAggregatBuilder register = opprettForBehandling(iayGrunnlagBuilder);
        new TestHjelper().lagAktørArbeid(register, Arbeidsgiver.virksomhet(virksomhetA), MINUS_YEARS_3, ArbeidType.FRILANSER);
        iayGrunnlagBuilder.medData(register);
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder(buildVLBeregningsgrunnlag()).leggTilSammenligningsgrunnlag(buildSammenligningsgrunnlagPrStatus(SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL)).build();
        buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.FRILANSER);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.FRILANSER, Inntektskategori.FRILANSER, MINUS_YEARS_2, MINUS_YEARS_1, null, OpptjeningAktivitetType.FRILANS);
        //Act

        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);
        //Assert
        assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().get(0);
        assertThat(resultatBGP.getBeregningsgrunnlagPrStatus()).hasSize(1);
        resultatBGP.getBeregningsgrunnlagPrStatus().forEach(resultatBGPS -> {
                    assertThat(resultatBGPS.getBeregningsperiode()).isNull();
                    assertThat(resultatBGPS.getBeregnetPrÅr().doubleValue()).isEqualTo(1000.01, within(0.01));
                }
        );
    }

    @Test
    public void skalMappeTilRegelNårBrukerErArbeidstaker() {
        //Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        InntektArbeidYtelseAggregatBuilder register = opprettForBehandling(iayGrunnlagBuilder);
        iayGrunnlagBuilder.medData(register);
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder(buildVLBeregningsgrunnlag()).leggTilSammenligningsgrunnlag(buildSammenligningsgrunnlagPrStatus(SammenligningsgrunnlagType.SAMMENLIGNING_AT)).build();
        buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_2, MINUS_YEARS_1, Arbeidsgiver.virksomhet(virksomhetA), OpptjeningAktivitetType.ARBEID);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1, NOW, Arbeidsgiver.virksomhet(virksomhetB), OpptjeningAktivitetType.ARBEID);
        new TestHjelper().lagAktørArbeid(register, Arbeidsgiver.virksomhet(virksomhetA), MINUS_YEARS_3, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        new TestHjelper().lagAktørArbeid(register, Arbeidsgiver.virksomhet(virksomhetB), MINUS_YEARS_3, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        //Act
        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = map(koblingReferanse, lagGrunnlag(beregningsgrunnlag), iayGrunnlagBuilder);
        //Assert
        assertThat(resultatBG.getBeregningsgrunnlagPerioder()).hasSize(1);
        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode resultatBGP = resultatBG.getBeregningsgrunnlagPerioder().get(0);
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
