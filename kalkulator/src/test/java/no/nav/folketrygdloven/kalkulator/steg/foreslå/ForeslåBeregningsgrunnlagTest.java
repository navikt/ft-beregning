package no.nav.folketrygdloven.kalkulator.steg.foreslå;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_BEGYNNELSE;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.NaturalYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OpptjeningsnøkkelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.testutilities.TestHjelper;
import no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.NaturalYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

class ForeslåBeregningsgrunnlagTest {

    private static final Beløp MÅNEDSINNTEKT1 = Beløp.fra(12345);
    private static final Beløp MÅNEDSINNTEKT2 = Beløp.fra(6000);
    private static final Beløp ÅRSINNTEKT1 = MÅNEDSINNTEKT1.multipliser(12);
    private static final Beløp ÅRSINNTEKT2 = MÅNEDSINNTEKT2.multipliser(12);
    private static final Beløp NATURALYTELSE_I_PERIODE_2 = Beløp.fra(200);
    private static final Beløp NATURALYTELSE_I_PERIODE_3 = Beløp.fra(400);
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.APRIL, 10);
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final Beløp GRUNNBELØP = Beløp.fra(90000);
    private static final String ARBEIDSFORHOLD_ORGNR1 = "654";
    private static final String ARBEIDSFORHOLD_ORGNR2 = "765";
    private static final LocalDate MINUS_YEARS_2 = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(2);
    private static final LocalDate MINUS_YEARS_1 = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1);
    private static final LocalDate ARBEIDSPERIODE_FOM = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1);
    private static final LocalDate ARBEIDSPERIODE_TOM = SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(2);
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);
    private AktørId beregningsAkrød1 = AktørId.dummy();
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private TestHjelper testHjelper = new TestHjelper();
    private InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder;
    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlag = new ForeslåBeregningsgrunnlag();

    @BeforeEach
    void setup() {
        iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        beregningsgrunnlag = lagBeregningsgrunnlagAT(true);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagAT(boolean erArbeidsgiverVirksomhet) {
        var beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder();
        beregningsgrunnlagBuilder.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
                .medGrunnbeløp(GRUNNBELØP);
        beregningsgrunnlagBuilder.leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                        .medArbforholdType(OpptjeningAktivitetType.ARBEID)
                        .medBGAndelArbeidsforhold(lagBgAndelArbeidsforhold(ARBEIDSPERIODE_FOM, ARBEIDSPERIODE_TOM,
                                erArbeidsgiverVirksomhet ? Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1) : Arbeidsgiver.person(beregningsAkrød1)))
                        .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                        .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                        .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1),
                                SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1))));
        return beregningsgrunnlagBuilder.build();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagFL() {
        var beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder();
        beregningsgrunnlagBuilder.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
                .medGrunnbeløp(GRUNNBELØP);
        beregningsgrunnlagBuilder.leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.FRILANSER));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                        .medBGAndelArbeidsforhold(lagBgAndelArbeidsforhold(ARBEIDSPERIODE_FOM, ARBEIDSPERIODE_TOM, Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1)))
                        .medAktivitetStatus(AktivitetStatus.FRILANSER)
                        .medInntektskategori(Inntektskategori.FRILANSER)
                        .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1),
                                SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1))));
        return beregningsgrunnlagBuilder.build();
    }

    private BGAndelArbeidsforholdDto.Builder lagBgAndelArbeidsforhold(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver) {
        return BGAndelArbeidsforholdDto.builder().medArbeidsperiodeFom(fom).medArbeidsperiodeTom(tom).medArbeidsgiver(arbeidsgiver);
    }

    private void lagBehandling(Beløp inntektSammenligningsgrunnlag,
                               Beløp inntektBeregningsgrunnlag, Arbeidsgiver arbeidsgiver, LocalDate fraOgMed, LocalDate tilOgMed, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {

        var inntektArbeidYtelseBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);

        testHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, arbeidsgiver, fraOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        testHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), fraOgMed,
                ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        for (var dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            testHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                    arbeidsgiver);
            testHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektBeregningsgrunnlag,
                    arbeidsgiver);
        }
        iayGrunnlagBuilder.medData(inntektArbeidYtelseBuilder);
    }

    private void lagBehandlingFL(Beløp inntektSammenligningsgrunnlag,
                                 Beløp inntektFrilans, String virksomhetOrgnr) {
        var fraOgMed = MINUS_YEARS_1.withDayOfMonth(1);
        var tilOgMed = fraOgMed.plusYears(1);
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(iayGrunnlagBuilder.getKladd().getRegisterVersjon(), VersjonTypeDto.REGISTER);
        testHjelper.initBehandlingFL(inntektSammenligningsgrunnlag, inntektFrilans, virksomhetOrgnr, fraOgMed, tilOgMed, registerBuilder);
        iayGrunnlagBuilder.medData(registerBuilder);
    }

    private void lagKortvarigArbeidsforhold(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate fomDato, LocalDate tomDato) {
        var andel = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        var aktivitetsAvtaleBuilder = YrkesaktivitetDtoBuilder.nyAktivitetsAvtaleBuilder()
                .medPeriode(Intervall.fraOgMedTilOgMed(fomDato, tomDato));
        var yrkesaktivitet = YrkesaktivitetDtoBuilder
                .oppdatere(Optional.empty()).leggTilAktivitetsAvtale(aktivitetsAvtaleBuilder)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsforholdId(andel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef())
                .medArbeidsgiver(andel.getArbeidsgiver().get()).build();
        var registerVersjon = iayGrunnlagBuilder.getKladd().getRegisterVersjon();
        var builder = InntektArbeidYtelseAggregatBuilder.oppdatere(registerVersjon, VersjonTypeDto.REGISTER);

        var aktørArbeid = registerVersjon.map(InntektArbeidYtelseAggregatDto::getAktørArbeid);
        var ya = aktørArbeid.get().hentAlleYrkesaktiviteter()
                .stream()
                .filter(y -> y.equals(yrkesaktivitet))
                .findFirst().get();

        var periode = ya.getAlleAktivitetsAvtaler().iterator().next().getPeriode();
        builder.getAktørArbeidBuilder()
                .getYrkesaktivitetBuilderForNøkkelAvType(new OpptjeningsnøkkelDto(yrkesaktivitet), ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(periode.getFomDato(), periode.getTomDato()), true)
                .medPeriode(Intervall.fraOgMedTilOgMed(fomDato, tomDato));

        iayGrunnlagBuilder.medData(builder);
    }

    @Test
    void skalLageEnPeriode() {
        // Arrange
        lagBehandling(MÅNEDSINNTEKT1, MÅNEDSINNTEKT1,
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var im1 = testHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MÅNEDSINNTEKT1.adder(Beløp.fra(1000)), null, null);
        var inntektsmeldinger = List.of(im1);
        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, (FaktaAggregatDto) null);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        var sammenligningATFL = resultat.getBeregningsgrunnlag().getSammenligningsgrunnlagForStatus(SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL);
        assertThat(sammenligningATFL).isPresent();
        verifiserSammenligningsgrunnlag(sammenligningATFL.get(), ÅRSINNTEKT1,
                SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1),
                BigDecimal.valueOf(81.004455200d), SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL);
        var periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE, 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MÅNEDSINNTEKT1.adder(Beløp.fra(1000)).multipliser(12),
                null, null);
    }

    @Test
    void skalLageEnPeriodeNårNaturalytelseBortfallerPåSkjæringstidspunktet() {
        // Arrange
        lagBehandling(MÅNEDSINNTEKT1, MÅNEDSINNTEKT1,
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_BEREGNING, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        var im1 = opprettInntektsmeldingNaturalytelseBortfaller(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MÅNEDSINNTEKT1,
                NATURALYTELSE_I_PERIODE_2, SKJÆRINGSTIDSPUNKT_BEREGNING);
        var inntektsmeldinger = List.of(im1);
        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, (FaktaAggregatDto) null);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        var periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE, 1, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1,
                NATURALYTELSE_I_PERIODE_2.multipliser(12), null);
    }

    private BeregningsgrunnlagRegelResultat act(BeregningsgrunnlagDto beregningsgrunnlag, Collection<InntektsmeldingDto> inntektsmeldinger, FaktaAggregatDto faktaAggregat) {
        var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(inntektsmeldinger).build();
        var bgGrunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medFaktaAggregat(faktaAggregat)
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .medRegisterAktiviteter(BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING, OpptjeningAktivitetType.ARBEID));
        var input = BeregningsgrunnlagInputTestUtil.lagForeslåttBeregningsgrunnlagInput(koblingReferanse, bgGrunnlagBuilder, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag);
        return foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);
    }

    private BeregningsgrunnlagRegelResultat act(BeregningsgrunnlagDto beregningsgrunnlag, Collection<InntektsmeldingDto> inntektsmeldinger, OmsorgspengerGrunnlag omsorgspengerGrunnlag) {
        var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(inntektsmeldinger).build();
        var bgGrunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .medRegisterAktiviteter(BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING, OpptjeningAktivitetType.ARBEID));
        var ompKobling = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING, FagsakYtelseType.OMSORGSPENGER);
        var input = BeregningsgrunnlagInputTestUtil.lagForeslåttBeregningsgrunnlagInput(ompKobling, bgGrunnlagBuilder,
                BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag, omsorgspengerGrunnlag);
        var foreslåBeregningsgrunnlag = new ForeslåBeregningsgrunnlag();
        return foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);
    }

    @Test
    void skalLageToPerioderNaturalYtelseBortfaller() {
        // Arrange
        lagBehandling(MÅNEDSINNTEKT1, MÅNEDSINNTEKT1,
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        var im1 = opprettInntektsmeldingNaturalytelseBortfaller(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MÅNEDSINNTEKT1,
                NATURALYTELSE_I_PERIODE_2, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4));
        var inntektsmeldinger = List.of(im1);
        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, (FaktaAggregatDto) null);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        var periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);
        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), TIDENES_ENDE, 1, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1,
                NATURALYTELSE_I_PERIODE_2.multipliser(12), null);
    }

    @Test
    void skalLageToPerioderNaturalYtelseTilkommer() {
        // Arrange
        lagBehandling(MÅNEDSINNTEKT1, MÅNEDSINNTEKT1,
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
        var im1 = opprettInntektsmeldingNaturalytelseTilkommer(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MÅNEDSINNTEKT1,
                NATURALYTELSE_I_PERIODE_2, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4));
        var inntektsmeldinger = List.of(im1);

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, (FaktaAggregatDto) null);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        var periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);
        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), TIDENES_ENDE, 1, PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null,
                NATURALYTELSE_I_PERIODE_2.multipliser(12));
    }

    @Test
    void skalLageToPerioderKortvarigArbeidsforhold() {
        // Arrange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1);
        lagBehandling(MÅNEDSINNTEKT1, MÅNEDSINNTEKT1,
                arbeidsgiver, MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var nyttGrunnlag = beregningsgrunnlag;
        var faktaAggregat = lagFakta(arbeidsgiver, true, null);
        lagKortvarigArbeidsforhold(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Før steget er det ingen inntekter på andelen
        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, faktaAggregat);
        // Her skulle det vært inntekter på andelen
        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        var periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1, null, null);
        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), TIDENES_ENDE, 1, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1, null, null);
    }

    @Test
    void skalLageTrePerioderKortvarigArbeidsforholdOgNaturalYtelse() {
        // Arrange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1);
        lagBehandling(MÅNEDSINNTEKT1, MÅNEDSINNTEKT1,
                arbeidsgiver, MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var nyttGrunnlag = beregningsgrunnlag;
        var faktaAggregat = lagFakta(arbeidsgiver, true, null);
        var im1 = opprettInntektsmeldingNaturalytelseBortfaller(arbeidsgiver, MÅNEDSINNTEKT1,
                NATURALYTELSE_I_PERIODE_2, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(3));
        splitBeregningsgrunnlagPeriode(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(3), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        lagKortvarigArbeidsforhold(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));
        var inntektsmeldinger = List.of(im1);

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, faktaAggregat);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(3);
        var periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1,
                PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1,
                NATURALYTELSE_I_PERIODE_2.multipliser(12), null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(2);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), TIDENES_ENDE, 1, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1,
                NATURALYTELSE_I_PERIODE_2.multipliser(12), null);
    }

    @Test
    void skalLageEnPeriodeFrilanser() {
        // Arrange
        var grunnlagFL = lagBeregningsgrunnlagFL();
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(grunnlagFL.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0))
                .build(grunnlagFL.getBeregningsgrunnlagPerioder().get(0));
        lagBehandlingFL(MÅNEDSINNTEKT1, MÅNEDSINNTEKT1, ARBEIDSFORHOLD_ORGNR1);
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        var resultat = act(grunnlagFL, inntektsmeldinger, (FaktaAggregatDto) null);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        var periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE, 1);
        verifiserBGFL(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1);
    }

    @Test
    void skal_lage_en_periode_for_private_arbeidsgiver() {
        // Arrange
        var privateArbeidsgiver = Arbeidsgiver.person(beregningsAkrød1);
        var grunnlagAT = lagBeregningsgrunnlagAT(false);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(grunnlagAT.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0))
                .build(grunnlagAT.getBeregningsgrunnlagPerioder().get(0));
        lagBehandling(MÅNEDSINNTEKT1, MÅNEDSINNTEKT1, privateArbeidsgiver,
                MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        var resultat = act(grunnlagAT, inntektsmeldinger, (FaktaAggregatDto) null);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        var periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE, 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), privateArbeidsgiver, ÅRSINNTEKT1, null, null);
    }

    private void splitBeregningsgrunnlagPeriode(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate nyPeriodeFom, PeriodeÅrsak nyPeriodeÅrsak) {
        var perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        var beregningsgrunnlagPeriode = perioder.get(perioder.size() - 1);
        if (beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom().equals(nyPeriodeFom)) {
            BeregningsgrunnlagPeriodeDto.oppdater(beregningsgrunnlagPeriode)
                    .leggTilPeriodeÅrsak(nyPeriodeÅrsak);
            return;
        }
        BeregningsgrunnlagPeriodeDto.oppdater(beregningsgrunnlagPeriode)
                .medBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), nyPeriodeFom.minusDays(1));

        BeregningsgrunnlagPeriodeDto.Builder.kopier(beregningsgrunnlagPeriode)
                .medBeregningsgrunnlagPeriode(nyPeriodeFom, null)
                .leggTilPeriodeÅrsak(nyPeriodeÅrsak).build(beregningsgrunnlag);
    }

    @Test
    void skalLageToPerioderKortvarigArbeidsforholdHvorTomSammenfallerMedBortfallAvNaturalytelse() {
        // Arrange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1);
        lagBehandling(MÅNEDSINNTEKT1, MÅNEDSINNTEKT1,
                arbeidsgiver, MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var nyttGrunnlag = beregningsgrunnlag;
        var faktaAggregat = lagFakta(arbeidsgiver, true, null);
        var im1 = opprettInntektsmeldingNaturalytelseBortfaller(arbeidsgiver, MÅNEDSINNTEKT1,
                NATURALYTELSE_I_PERIODE_2, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4));
        splitBeregningsgrunnlagPeriode(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        lagKortvarigArbeidsforhold(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));
        var inntektsmeldinger = List.of(im1);

        // Act
        var resultat = act(nyttGrunnlag, inntektsmeldinger, faktaAggregat);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        var periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), TIDENES_ENDE, 1, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET,
                PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1,
                NATURALYTELSE_I_PERIODE_2.multipliser(12), null);
    }

    @Test
    void skalLageBeregningsgrunnlagMedTrePerioder() {
        // Arrange
        lagBehandling(MÅNEDSINNTEKT1.adder(MÅNEDSINNTEKT2), MÅNEDSINNTEKT1.adder(MÅNEDSINNTEKT2),
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(Optional.empty())
                .medArbforholdType(OpptjeningAktivitetType.ARBEID)
                .medBGAndelArbeidsforhold(lagBgAndelArbeidsforhold(ARBEIDSPERIODE_FOM, ARBEIDSPERIODE_TOM, Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2)))
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1))
                .build(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0));
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        var im1 = opprettInntektsmeldingNaturalytelseBortfaller(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), MÅNEDSINNTEKT2,
                NATURALYTELSE_I_PERIODE_2, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2));
        var im2 = opprettInntektsmeldingNaturalytelseBortfaller(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MÅNEDSINNTEKT1,
                NATURALYTELSE_I_PERIODE_3, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4));
        var inntektsmeldinger = List.of(im1, im2);

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, (FaktaAggregatDto) null);

        // Assert
        var beregningsgrunnlag = resultat.getBeregningsgrunnlag();
        assertThat(beregningsgrunnlag).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(3);

        var periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2).minusDays(1), 2);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), ÅRSINNTEKT2, null, null);

        periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 2,
                PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), ÅRSINNTEKT2,
                NATURALYTELSE_I_PERIODE_2.multipliser(12), null);

        periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(2);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), TIDENES_ENDE, 2, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1,
                NATURALYTELSE_I_PERIODE_3.multipliser(12), null);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), ÅRSINNTEKT2,
                NATURALYTELSE_I_PERIODE_2.multipliser(12), null);
    }

    @Test
    void skalLageBeregningsgrunnlagMedTrePerioderKortvarigFørNaturalytelse() {
        // Arrange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1);
        lagBehandling(MÅNEDSINNTEKT1, MÅNEDSINNTEKT1,
                arbeidsgiver, MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var faktaAggregat = lagFakta(arbeidsgiver, true, null);
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(3), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        lagKortvarigArbeidsforhold(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2).minusDays(1));
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, faktaAggregat);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(3);

        var periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3).minusDays(1), 1,
                PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(2);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3), TIDENES_ENDE, 1, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1, null, null);
    }

    @Test
    void skalIkkeReturnereAvklaringsbehovNårOmsorgspengerOgATMedOver25ProsentAvvikOOgRefusjonTilsvarerBeregnet() {
        // Arrange
        var periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING,
                SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(1)), Utbetalingsgrad.valueOf(100));
        var aktivitetDto = new AktivitetDto(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        var utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(aktivitetDto, List.of(periodeMedUtbetalingsgradDto));
        var omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto), List.of());
        var inntektBeregnet = MÅNEDSINNTEKT1;
        var inntektSammenligningsgrunnlag = MÅNEDSINNTEKT1.multipliser(2);
        lagBehandling(inntektSammenligningsgrunnlag, inntektBeregnet,
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var im1 = testHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                inntektBeregnet, null, inntektBeregnet);
        var inntektsmeldinger = List.of(im1);
        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, omsorgspengerGrunnlag);
        // Assert
        var aps = resultat.getAvklaringsbehov();
        var apDefs = aps.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    @Test
    void skalIkkeReturnereAvklaringsbehovNårOmsorgspengerOgATMedAvvikOver25ProsentAvvikOOgRefusjon6G() {
        // Arrange
        var periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING,
                SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(1)), Utbetalingsgrad.valueOf(100));
        var aktivitetDto = new AktivitetDto(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        var utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(aktivitetDto, List.of(periodeMedUtbetalingsgradDto));
        var omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto), List.of());
        var inntektBeregnet = Beløp.fra(70_000);
        var refusjon = Beløp.fra(50_000);
        var inntektSammenligningsgrunnlag = Beløp.fra(40_000);
        lagBehandling(inntektSammenligningsgrunnlag, inntektBeregnet,
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var im1 = testHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                inntektBeregnet, null, refusjon);
        var inntektsmeldinger = List.of(im1);
        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, omsorgspengerGrunnlag);
        // Assert
        var aps = resultat.getAvklaringsbehov();
        var apDefs = aps.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    @Test
    void skalIkkeReturnereAvklaringsbehovNårOmsorgspengerOgATMedAvvikOver25ProsentAvvikOgAvkorting() {
        // Arrange
        var periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING,
                SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(1)), Utbetalingsgrad.valueOf(100));
        var aktivitetDto = new AktivitetDto(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        var utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(aktivitetDto, List.of(periodeMedUtbetalingsgradDto));
        var omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto), List.of());
        var inntektBeregnet = Beløp.fra(90_000);
        var refusjon = Beløp.fra(90_000);
        var inntektSammenligningsgrunnlag = Beløp.fra(40_000);
        lagBehandling(inntektSammenligningsgrunnlag, inntektBeregnet,
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var im1 = testHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                inntektBeregnet, null, refusjon);
        var inntektsmeldinger = List.of(im1);
        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, omsorgspengerGrunnlag);
        // Assert
        var aps = resultat.getAvklaringsbehov();
        var apDefs = aps.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    @Test
    void skalReturnereAvklaringsbehovNårOmsorgspengerOgATMedOver25ProsentAvvikOgUtbetalingDirekteTilBruker() {
        // Arrange
        var periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING,
                SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(1)), Utbetalingsgrad.valueOf(100));
        var aktivitetDto = new AktivitetDto(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        var utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(aktivitetDto, List.of(periodeMedUtbetalingsgradDto));
        var omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto), List.of(periodeMedUtbetalingsgradDto.getPeriode()));
        var inntektBeregnet = MÅNEDSINNTEKT1;
        var inntektSammenligningsgrunnlag = MÅNEDSINNTEKT1.multipliser(2);
        var refusjonskrav = MÅNEDSINNTEKT1.map(v -> v.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
        lagBehandling(inntektSammenligningsgrunnlag, inntektBeregnet,
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var im1 = InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1))
                .medBeløp(inntektBeregnet)
                .medRefusjon(refusjonskrav)
                .leggTil(new RefusjonDto(inntektBeregnet, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(4)))
                .leggTil(new RefusjonDto(refusjonskrav, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(8)))
                .build();

        var inntektsmeldinger = List.of(im1);
        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, omsorgspengerGrunnlag);
        // Assert
        var aps = resultat.getAvklaringsbehov();
        var apDefs = aps.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);
    }

    @Test
    void skalReturnereAvklaringsbehovNårOmsorgspengerOgFLMedOver25ProsentAvvik() {
        // Arrange
        var beregningsgrunnlag = lagBeregningsgrunnlagFL();
        var periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING,
                SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(1)), Utbetalingsgrad.valueOf(100));
        var aktivitetDto = new AktivitetDto(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.FRILANS);
        var utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(aktivitetDto, List.of(periodeMedUtbetalingsgradDto));
        var omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto), List.of(periodeMedUtbetalingsgradDto.getPeriode()));
        var inntektBeregnet = MÅNEDSINNTEKT1;
        var inntektSammenligningsgrunnlag = MÅNEDSINNTEKT1.multipliser(2);

        var inntektArbeidYtelseBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        testHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), ArbeidType.FRILANSER_OPPDRAGSTAKER);
        for (var dt = MINUS_YEARS_2.withDayOfMonth(1); dt.isBefore(MINUS_YEARS_1.withDayOfMonth(1).plusYears(2)); dt = dt.plusMonths(1)) {
            testHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                    Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1));
            testHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektBeregnet,
                    Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1));
        }
        iayGrunnlagBuilder.medData(inntektArbeidYtelseBuilder);

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), omsorgspengerGrunnlag);
        // Assert
        var aps = resultat.getAvklaringsbehov();
        var apDefs = aps.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);
    }

    @Test
    void skalIkkeReturnereAvklaringsbehovNårOmsorgspengerOgFlereArbeidsforholdMedAvvikOgEttKravMedFullRefusjon() {
        // Arrange
        var inntektBeregnetArbeidsgiver1 = Beløp.fra(40_000);
        var refusjon =  Beløp.fra(40_000);
        var inntektSammenligningsgrunnlagArbeidsgiver1 =  Beløp.fra(40_000);
        var inntektBeregnetArbeidsgiver2 =  Beløp.fra(10_000);
        var inntektSammenligningsgrunnlagArbeidsgiver2 =  Beløp.fra(80_000);

        var fraOgMed = MINUS_YEARS_2.withDayOfMonth(1);
        var tilOgMed = MINUS_YEARS_1.withDayOfMonth(1).plusYears(2);
        var Arbeidsgiver1 = Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1);
        var Arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2);

        var periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING,
                SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(1)), Utbetalingsgrad.valueOf(100));
        var aktivitetDto = new AktivitetDto(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        var utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(aktivitetDto, List.of(periodeMedUtbetalingsgradDto));
        var omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto), List.of());

        var inntektArbeidYtelseBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);

        testHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, Arbeidsgiver1, fraOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        testHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), fraOgMed,
                ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        for (var dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            testHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektSammenligningsgrunnlagArbeidsgiver1,
                    Arbeidsgiver1);
            testHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektBeregnetArbeidsgiver1,
                    Arbeidsgiver1);
        }
        for (var dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            testHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektSammenligningsgrunnlagArbeidsgiver2,
                    Arbeidsgiver2);
            testHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektBeregnetArbeidsgiver2,
                    Arbeidsgiver2);
        }
        iayGrunnlagBuilder.medData(inntektArbeidYtelseBuilder);

        var im1 = testHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                inntektBeregnetArbeidsgiver1, null, refusjon);
        var inntektsmeldinger = List.of(im1);
        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, omsorgspengerGrunnlag);
        // Assert
        var aps = resultat.getAvklaringsbehov();
        var apDefs = aps.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    private void verifiserPeriode(BeregningsgrunnlagPeriodeDto periode, LocalDate fom, LocalDate tom, int antallAndeler,
                                  PeriodeÅrsak... forventedePeriodeÅrsaker) {
        assertThat(periode.getBeregningsgrunnlagPeriodeFom()).isEqualTo(fom);
        assertThat(periode.getBeregningsgrunnlagPeriodeTom()).isEqualTo(tom);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(antallAndeler);
        assertThat(periode.getPeriodeÅrsaker()).containsExactlyInAnyOrder(forventedePeriodeÅrsaker);
    }

    private void verifiserSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlag, Beløp rapportertPrÅr, LocalDate fom,
                                                 LocalDate tom, BigDecimal avvikPromille, SammenligningsgrunnlagType forventetType) {
        assertThat(sammenligningsgrunnlag.getRapportertPrÅr()).isEqualTo(rapportertPrÅr);
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeFom()).isEqualTo(fom);
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeTom()).isEqualTo(tom);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagType()).isEqualTo(forventetType);
        assertThat(sammenligningsgrunnlag.getAvvikPromilleNy().compareTo(avvikPromille)).isEqualTo(0);
    }

    private void verifiserBGAT(BeregningsgrunnlagPrStatusOgAndelDto bgpsa, Arbeidsgiver arbeidsgiver, Beløp årsinntekt,
                               Beløp naturalytelseBortfaltPrÅr, Beløp naturalytelseTilkommerPrÅr) {
        assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(bgpsa.getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(bgpsa.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver))
                .hasValueSatisfying(virk -> assertThat(virk).isEqualTo(arbeidsgiver));
        assertThat(bgpsa.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)
                .map(InternArbeidsforholdRefDto::gjelderForSpesifiktArbeidsforhold).orElse(false))
                .as("gjelderSpesifiktArbeidsforhold").isFalse();
        assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ARBEID);
        assertThat(bgpsa.getAvkortetPrÅr()).isNull();
        assertThat(bgpsa.getBeregnetPrÅr()).isEqualTo(årsinntekt);
        assertThat(bgpsa.getBruttoPrÅr()).isEqualTo(årsinntekt);
        assertThat(bgpsa.getOverstyrtPrÅr()).isNull();
        if (naturalytelseBortfaltPrÅr == null) {
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr)).as("naturalytelseBortfalt").isEmpty();
        } else {
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr)).as("naturalytelseBortfalt")
                    .hasValueSatisfying(naturalytelse -> assertThat(naturalytelse).isEqualTo(naturalytelseBortfaltPrÅr));
        }
        if (naturalytelseTilkommerPrÅr == null) {
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseTilkommetPrÅr)).as("naturalytelseTilkommer").isEmpty();
        } else {
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseTilkommetPrÅr)).as("naturalytelseTilkommer")
                    .hasValueSatisfying(naturalytelse -> assertThat(naturalytelse).isEqualTo(naturalytelseTilkommerPrÅr));
        }
        assertThat(bgpsa.getRedusertPrÅr()).isNull();
    }

    private void verifiserBGFL(BeregningsgrunnlagPrStatusOgAndelDto bgpsa, Arbeidsgiver arbeidsgiver, Beløp årsinntekt) {
        assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
        assertThat(bgpsa.getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
        assertThat(bgpsa.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver))
                .hasValueSatisfying(virk -> assertThat(virk).isEqualTo(arbeidsgiver));
        assertThat(bgpsa.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)
                .map(InternArbeidsforholdRefDto::gjelderForSpesifiktArbeidsforhold).orElse(false))
                .as("gjelderSpesifiktArbeidsforhold").isFalse();
        assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.FRILANS);
        assertThat(bgpsa.getAvkortetPrÅr()).isNull();
        assertThat(bgpsa.getBeregnetPrÅr()).isEqualTo(årsinntekt);
        assertThat(bgpsa.getBruttoPrÅr()).as("BruttoPrÅr").isEqualTo(årsinntekt);
        assertThat(bgpsa.getOverstyrtPrÅr()).as("OverstyrtPrÅr").isNull();
        assertThat(bgpsa.getRedusertPrÅr()).isNull();
    }

    private InntektsmeldingDto opprettInntektsmeldingNaturalytelseBortfaller(Arbeidsgiver arbeidsgiver, Beløp inntektInntektsmelding, Beløp naturalytelseBortfaller,
                                                                             LocalDate naturalytelseBortfallerDato) {
        return testHjelper.opprettInntektsmeldingMedRefusjonskrav(arbeidsgiver, inntektInntektsmelding,
                new NaturalYtelseDto(TIDENES_BEGYNNELSE, naturalytelseBortfallerDato.minusDays(1), naturalytelseBortfaller, NaturalYtelseType.ANNET),
                null);
    }

    private InntektsmeldingDto opprettInntektsmeldingNaturalytelseTilkommer(Arbeidsgiver arbeidsgiver, Beløp inntektInntektsmelding, Beløp naturalytelseTilkommer,
                                                                            LocalDate naturalytelseTilkommerDato) {
        return testHjelper.opprettInntektsmeldingMedRefusjonskrav(arbeidsgiver, inntektInntektsmelding,
                new NaturalYtelseDto(naturalytelseTilkommerDato, TIDENES_ENDE, naturalytelseTilkommer, NaturalYtelseType.ANNET), null);
    }

    private FaktaAggregatDto lagFakta(Arbeidsgiver virksomhet, boolean erTidsbegrenset, Boolean erNyIArbeidslivet) {
        return FaktaAggregatDto.builder()
                .medFaktaAktør(erNyIArbeidslivet == null ? null : FaktaAktørDto.builder().medErNyIArbeidslivetSNFastsattAvSaksbehandler(erNyIArbeidslivet).build())
                .erstattEksisterendeEllerLeggTil(new FaktaArbeidsforholdDto.Builder(virksomhet, InternArbeidsforholdRefDto.nullRef())
                        .medErTidsbegrensetFastsattAvSaksbehandler(erTidsbegrenset)
                        .build()).build();
    }

}
