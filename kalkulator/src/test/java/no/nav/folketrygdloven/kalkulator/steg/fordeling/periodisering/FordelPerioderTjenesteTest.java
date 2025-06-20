package no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.folketrygdloven.kalkulator.tid.Intervall.fraOgMed;
import static no.nav.folketrygdloven.kalkulator.tid.Intervall.fraOgMedTilOgMed;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.OpprettKravPerioderFraInntektsmeldinger;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAktivitetHandlingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.utils.BeregningsgrunnlagTestUtil;
import no.nav.fpsak.tidsserie.LocalDateInterval;

class FordelPerioderTjenesteTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, Month.JANUARY, 4);
    private static final Beløp GRUNNBELØP = Beløp.fra(90000L);
    private static final String ORG_NUMMER = "974652269";
    private static final String ORG_NUMMER_2 = "999999999";
    private static final String ORG_NUMMER_3 = "9999999998";

    private static final AktørId ARBEIDSGIVER_AKTØR_ID = AktørId.dummy();
    private static final BigDecimal ANTALL_MÅNEDER_I_ÅR = KonfigTjeneste.getMånederIÅr();
    private static final Intervall ARBEIDSPERIODE = fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);
    private final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);
    private final Arbeidsgiver arbeidsgiver2 = Arbeidsgiver.virksomhet(ORG_NUMMER_2);
    private final List<BeregningAktivitetDto> aktiviteter = new ArrayList<>();
    private final KoblingReferanse behandlingRef = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningAktivitetAggregatDto beregningAktivitetAggregat;
    private FordelPerioderTjeneste tjeneste;
    private InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder;


    @BeforeEach
    void setUp() {
        iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        tjeneste = lagTjeneste();
        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER), iayGrunnlagBuilder);

    }

    private FordelPerioderTjeneste lagTjeneste() {
        return new FordelPerioderTjeneste(
        );
    }

    private void leggTilYrkesaktiviteterOgBeregningAktiviteter(List<String> orgnrs, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(iayGrunnlagBuilder.getKladd().getAktørArbeidFraRegister());
        for (var orgnr : orgnrs) {
            var arbeidsgiver = leggTilYrkesaktivitet(ARBEIDSPERIODE, aktørArbeidBuilder, orgnr, null);
            fjernOgLeggTilNyBeregningAktivitet(ARBEIDSPERIODE.getFomDato(), ARBEIDSPERIODE.getTomDato(), arbeidsgiver, InternArbeidsforholdRefDto.nullRef());
        }

        var inntektArbeidYtelseAggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(iayGrunnlagBuilder.getKladd().getRegisterVersjon(), VersjonTypeDto.REGISTER)
                .leggTilAktørArbeid(aktørArbeidBuilder);
        iayGrunnlagBuilder.medData(inntektArbeidYtelseAggregatBuilder);
    }

    private Arbeidsgiver leggTilYrkesaktivitet(Intervall arbeidsperiode, InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder,
                                               String orgnr, Intervall permisjonperiode) {
        var arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        var aaBuilder1 = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(arbeidsperiode);
        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(aaBuilder1);
        if (permisjonperiode != null) {
            yaBuilder.leggTilPermisjon(PermisjonDtoBuilder.ny().medProsentsats(Stillingsprosent.HUNDRED).medPeriode(permisjonperiode).medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.VELFERDSPERMISJON));
        }
        aktørArbeidBuilder.leggTilYrkesaktivitet(yaBuilder);
        return arbeidsgiver;
    }

    private void fjernOgLeggTilNyBeregningAktivitet(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        if (fom.isAfter(SKJÆRINGSTIDSPUNKT)) {
            throw new IllegalArgumentException("Kan ikke lage BeregningAktivitet som starter etter skjæringstidspunkt");
        }
        fjernAktivitet(arbeidsgiver, arbeidsforholdRef);
        aktiviteter.add(lagAktivitet(fom, tom, arbeidsgiver, arbeidsforholdRef));
        lagAggregatEntitetFraListe(aktiviteter);
    }

    private void fjernAktivitet(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        aktiviteter.stream()
                .filter(a -> a.gjelderFor(arbeidsgiver, arbeidsforholdRef)).findFirst()
                .ifPresent(aktiviteter::remove);
        lagAggregatEntitetFraListe(aktiviteter);
    }

    private void lagAggregatEntitetFraListe(List<BeregningAktivitetDto> aktiviteter) {
        var builder = BeregningAktivitetAggregatDto.builder();
        aktiviteter.forEach(builder::leggTilAktivitet);
        beregningAktivitetAggregat = builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
    }

    private BeregningAktivitetDto lagAktivitet(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        return BeregningAktivitetDto.builder()
                .medPeriode(fraOgMedTilOgMed(fom, tom))
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(arbeidsforholdRef)
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                .build();
    }

    /**
     * Simulerer at vi har 2 arbeidsforhold i samme organisasjon
     * Arbeidsforhold1 avslutter dagen før skjæringstidspunktet og har andel med arbeidsforholdId lik null (skal ikkje ha refusjon)
     * Arbeidsforhold2 er aktivt på skjæringstidspunktet og skal ha refusjon (har arbeidsforholdId ulik null)
     */
    @Test
    void skal_ikkje_sette_refusjon_for_andeler_som_slutter_dagen_før_skjæringstidspunktet() {
        // Arrange
        var beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medGrunnbeløp(GRUNNBELØP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .build();

        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
                .build(beregningsgrunnlag);

        var arbeidsforholdRef = InternArbeidsforholdRefDto.nyRef();
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver).medArbeidsforholdRef(arbeidsforholdRef))
                .build(periode);

        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver))
                .build(periode);

        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .build(BeregningsgrunnlagTilstand.FORESLÅTT);

        var oppdatere = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørArbeidBuilder = oppdatere.getAktørArbeidBuilder();
        aktørArbeidBuilder
                .leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                        .medArbeidsgiver(arbeidsgiver)
                        .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                        .medArbeidsforholdId(InternArbeidsforholdRefDto.nyRef())
                        .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10), SKJÆRINGSTIDSPUNKT.minusDays(1)))))
                .leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                        .medArbeidsgiver(arbeidsgiver)
                        .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                        .medArbeidsforholdId(arbeidsforholdRef)
                        .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(fraOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10)))));
        oppdatere.leggTilAktørArbeid(aktørArbeidBuilder);
        iayGrunnlagBuilder.medData(oppdatere);

        var refusjon = BigDecimal.valueOf(23987);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, arbeidsforholdRef, SKJÆRINGSTIDSPUNKT, refusjon.intValue());
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);
        var bgArbeidsforholdMedRef = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(a -> a.getArbeidsforholdRef().isPresent() && a.getArbeidsforholdRef().get().equals(arbeidsforholdRef))
                .flatMap(a -> a.getBgAndelArbeidsforhold().stream())
                .findFirst();
        assertThat(bgArbeidsforholdMedRef).hasValueSatisfying(bga -> assertThat(bga.getRefusjonskravPrÅr().verdi()).isEqualByComparingTo(refusjon.multiply(ANTALL_MÅNEDER_I_ÅR)));

        var bgArbeidsforholdUtenRef = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(a -> a.getArbeidsforholdRef().isPresent() && !a.getArbeidsforholdRef().get().gjelderForSpesifiktArbeidsforhold())
                .flatMap(a -> a.getBgAndelArbeidsforhold().stream())
                .findFirst();
        assertThat(bgArbeidsforholdUtenRef).hasValueSatisfying(bga -> assertThat(bga.getRefusjonskravPrÅr()).isNull());
    }

    @Test
    void ikkeLagPeriodeForRefusjonHvisKunEnInntektsmeldingIngenEndringIRefusjon() {
        // Arrange
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        var inntekt = Beløp.fra(23987);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);
        var bgaOpt = finnBGAndelArbeidsforhold(perioder.get(0), ORG_NUMMER);
        assertThat(bgaOpt).hasValueSatisfying(bga -> assertThat(bga.getRefusjonskravPrÅr()).isEqualByComparingTo(inntekt.multipliser(ANTALL_MÅNEDER_I_ÅR)));
    }

    private BeregningsgrunnlagDto fastsettPerioderForRefusjonOgGradering(KoblingReferanse ref,
                                                                         BeregningsgrunnlagGrunnlagDto grunnlag,
                                                                         AktivitetGradering aktivitetGradering,
                                                                         InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {
        var iayGrunnlag = iayGrunnlagBuilder.build();
        var refusjonskravDatoDtos = OpprettKravPerioderFraInntektsmeldinger.opprett(iayGrunnlag, ref.getSkjæringstidspunktBeregning());
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false, aktivitetGradering);
        var input = new BeregningsgrunnlagInput(ref, iayGrunnlag, null, refusjonskravDatoDtos, foreldrepengerGrunnlag)
                .medBeregningsgrunnlagGrunnlag(grunnlag);
        var refusjonBg = tjeneste.fastsettPerioderForRefusjon(input).getBeregningsgrunnlag();
        return tjeneste.fastsettPerioderForUtbetalingsgradEllerGradering(input, refusjonBg).getBeregningsgrunnlag();
    }

    private BeregningsgrunnlagDto fastsettPerioderForRefusjonUtenGradering(KoblingReferanse ref,
                                                                           BeregningsgrunnlagGrunnlagDto grunnlag,
                                                                           InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder,
                                                                           List<KravperioderPrArbeidsforholdDto> refusjonskravDatoer) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false, AktivitetGradering.INGEN_GRADERING);
        var input = new BeregningsgrunnlagInput(ref, iayGrunnlagBuilder.build(), null, refusjonskravDatoer, foreldrepengerGrunnlag)
                .medBeregningsgrunnlagGrunnlag(grunnlag);
        var refusjonBg = tjeneste.fastsettPerioderForRefusjon(input).getBeregningsgrunnlag();
        return tjeneste.fastsettPerioderForUtbetalingsgradEllerGradering(input, refusjonBg).getBeregningsgrunnlag();
    }


    @Test
    void lagPeriodeForRefusjonHvisKunEnInntektsmeldingIngenEndringIRefusjonArbeidsgiverSøkerForSent() {
        // Arrange
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        var inntekt = Beløp.fra(23987);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        var kravPerioder = OpprettKravPerioderFraInntektsmeldinger.opprett(
                iayGrunnlagBuilder.getKladd(),
                SKJÆRINGSTIDSPUNKT,
                Map.of(im1.getArbeidsgiver(), LocalDate.of(2019, Month.MAY, 2))
        );

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonUtenGradering(behandlingRef,
                grunnlag,
                iayGrunnlagBuilder, kravPerioder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, LocalDate.of(2019, Month.JANUARY, 31));
        assertThat(finnBGAndelArbeidsforhold(perioder.get(0), ORG_NUMMER))
                .hasValueSatisfying(bga -> assertThat(bga.getGjeldendeRefusjonPrÅr()).isEqualByComparingTo(Beløp.ZERO));
        assertBeregningsgrunnlagPeriode(perioder.get(1), LocalDate.of(2019, Month.FEBRUARY, 1), TIDENES_ENDE, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertThat(finnBGAndelArbeidsforhold(perioder.get(1), ORG_NUMMER))
                .hasValueSatisfying(bga -> assertThat(bga.getGjeldendeRefusjonPrÅr()).isEqualByComparingTo(inntekt.multipliser(ANTALL_MÅNEDER_I_ÅR)));
    }

    @Test
    void ikkeLagPeriodeForZeroRefusjonHvisKunEnInntektsmeldingIngenEndringIRefusjon() {
        // Arrange
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        var inntekt = Beløp.fra(23987);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, Beløp.ZERO);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);
    }

    @Test
    void lagPeriodeForRefusjonOpphører() {
        // Arrange
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        var inntekt = Beløp.fra(40000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, SKJÆRINGSTIDSPUNKT.plusDays(100)
        );
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);
        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(100));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(101), TIDENES_ENDE, PeriodeÅrsak.REFUSJON_OPPHØRER);
    }

    @Test
    void lagPeriodeForGraderingLik6G() {
        // Arrange
        var refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1);
        var graderingFom = refusjonOpphørerDato.plusDays(1);
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);
        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);
        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER, ORG_NUMMER_2), iayGrunnlagBuilder);
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER, ORG_NUMMER_2), beregningAktivitetAggregat);

        var inntekt1 = Beløp.fra(90000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, GRUNNBELØP.multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi()), inntekt1
        );
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medArbeidsgiver(arbeidsgiverGradering)
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medGradering(graderingFom, graderingTom, 50)
                .build());

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, aktivitetGradering, iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusWeeks(9), graderingTom, PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
    }

    @Test
    void lagPeriodeForGraderingSN_refusjon_over_6G() {
        // Arrange
        var graderingFom = SKJÆRINGSTIDSPUNKT.plusWeeks(9);
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);

        iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medGradering(graderingFom, graderingTom, 50)
                .build());

        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER, ORG_NUMMER_2), iayGrunnlagBuilder);
        var inntekt1 = Beløp.fra(90000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, inntekt1, inntekt1);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER, ORG_NUMMER_2), beregningAktivitetAggregat);
        var beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .build(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0));

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, aktivitetGradering, iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, graderingFom.minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), graderingFom, graderingTom, PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
    }

    @Test
    void lagPeriodeForGraderingSN_bg_over_6g() {
        // Arrange
        var graderingFom = SKJÆRINGSTIDSPUNKT.plusWeeks(9);
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);
        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER_2), iayGrunnlagBuilder);
        var inntekt1 = BigDecimal.valueOf(90000);
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        var beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();
        var bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .build(bgPeriode);
        BeregningsgrunnlagPrStatusOgAndelDto.kopier(bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0))
                .medBeregnetPrÅr(Beløp.fra(inntekt1.multiply(ANTALL_MÅNEDER_I_ÅR)));

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medGradering(graderingFom, graderingTom, 50)
                .build());

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, aktivitetGradering, iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusWeeks(9), SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1), PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), SKJÆRINGSTIDSPUNKT.plusWeeks(18), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
    }

    @Test
    void lagPeriodeForGraderingOver6GOgOpphørRefusjonSammeDag() {
        // Arrange
        var refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1);
        var graderingFom = refusjonOpphørerDato.plusDays(1);
        var graderingTom = TIDENES_ENDE;

        var newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();

        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);

        var orgnrs = List.of(ORG_NUMMER, ORG_NUMMER_3, ORG_NUMMER_2);
        leggTilYrkesaktiviteterOgBeregningAktiviteter(orgnrs, newGrunnlagBuilder);
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(orgnrs, beregningAktivitetAggregat);

        var inntekt1 = Beløp.fra(90000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_3, SKJÆRINGSTIDSPUNKT, inntekt1, inntekt1);
        var im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, inntekt1, inntekt1, refusjonOpphørerDato);
        newGrunnlagBuilder.medInntektsmeldinger(im1, im2);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medArbeidsgiver(arbeidsgiverGradering)
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medGradering(graderingFom, graderingTom, 50)
                .build());
        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef,
                grunnlag,
                aktivitetGradering,
                newGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, refusjonOpphørerDato);
        assertBeregningsgrunnlagPeriode(perioder.get(1), graderingFom, TIDENES_ENDE, PeriodeÅrsak.GRADERING, PeriodeÅrsak.REFUSJON_OPPHØRER);

    }

    @Test
    void lagPeriodeForGraderingOver6GFL() {
        // Arrange
        var refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1);
        var graderingFom = refusjonOpphørerDato.plusDays(1);
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);
        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);
        var newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER, ORG_NUMMER_2), newGrunnlagBuilder);
        var inntekt1 = Beløp.fra(90000);
        var refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1);
        newGrunnlagBuilder.medInntektsmeldinger(im1);
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER, ORG_NUMMER_2), beregningAktivitetAggregat);
        var beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE)
                .build(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0));

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medArbeidsgiver(arbeidsgiverGradering)
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medGradering(graderingFom, graderingTom, 50)
                .build());

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, aktivitetGradering, newGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusWeeks(9), graderingTom, PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);

    }

    @Test
    void lagPeriodeForGraderingOgRefusjonArbeidsforholdTilkomEtterStp() {
        // Arrange
        var graderingFom = SKJÆRINGSTIDSPUNKT.plusWeeks(9);
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);


        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);

        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        var arbId2 = InternArbeidsforholdRefDto.namedRef("B");

        var ansettelsesDato = graderingFom;
        var startDatoRefusjon = graderingFom;
        var iayBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT, ansettelsesDato, TIDENES_ENDE, arbId, arbeidsgiverGradering,
                Beløp.fra(10), iayBuilder);
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT, ansettelsesDato, TIDENES_ENDE, arbId2, arbeidsgiverGradering,
                Beløp.fra(10), iayBuilder);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiverGradering.getIdentifikator(), startDatoRefusjon, Beløp.fra(20000), Beløp.fra(20000)
        );
        iayBuilder.medInntektsmeldinger(im1);
        fjernAktivitet(arbeidsgiverGradering, arbId);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medArbeidsgiver(arbeidsgiverGradering)
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medGradering(graderingFom, graderingTom, 50)
                .build());

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, aktivitetGradering, iayBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, graderingFom.minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), graderingFom, TIDENES_ENDE, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(finnBGAndelArbeidsforhold(perioder.get(0), ORG_NUMMER_2)).isPresent();
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
    }

    @Test
    void lagPeriodeForGraderingToArbeidsforholdTilkomEtterStpInntektsmeldingMedId() {
        // Arrange
        var graderingFom = SKJÆRINGSTIDSPUNKT.plusWeeks(9);
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);


        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);

        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        var arbId2 = InternArbeidsforholdRefDto.namedRef("B");

        var ansettelsesDato = graderingFom;
        var iayBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT, ansettelsesDato, TIDENES_ENDE, arbId, arbeidsgiverGradering,
                Beløp.fra(10), iayBuilder);
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT, ansettelsesDato, TIDENES_ENDE, arbId2, arbeidsgiverGradering,
                Beløp.fra(10), iayBuilder);
        fjernAktivitet(arbeidsgiverGradering, arbId);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medArbeidsgiver(arbeidsgiverGradering)
                .medArbeidsforholdRef(arbId2)
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medGradering(graderingFom, graderingTom, 50)
                .build());

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, aktivitetGradering, iayBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, graderingFom.minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), graderingFom, graderingTom, PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1L), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(finnBGAndelArbeidsforhold(perioder.get(0), ORG_NUMMER_2)).isPresent();
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        assertThat(perioder.get(2).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        assertThat(finnBGAndelArbeidsforhold(perioder.get(2), ORG_NUMMER_2)).isPresent();
    }


    @Test
    void lagPeriodeForGraderingOgRefusjonArbeidsforholdTilkomEtterStpInntektsmeldingMedId() {
        // Arrange
        var graderingFom = SKJÆRINGSTIDSPUNKT.plusWeeks(9);
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);


        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);

        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        var arbId = InternArbeidsforholdRefDto.namedRef("A");

        var ansettelsesDato = graderingFom;
        var startDatoRefusjon = graderingFom;
        var iayBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT, ansettelsesDato, TIDENES_ENDE, arbId, arbeidsgiverGradering,
                Beløp.fra(10), iayBuilder);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiverGradering, arbId, startDatoRefusjon, Beløp.fra(20000), Beløp.fra(20000),
                null,
                List.of(),
                List.of()
        );
        iayBuilder.medInntektsmeldinger(im1);
        fjernAktivitet(arbeidsgiverGradering, arbId);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medArbeidsgiver(arbeidsgiverGradering)
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medGradering(graderingFom, graderingTom, 50)
                .build());

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, aktivitetGradering, iayBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, graderingFom.minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), graderingFom, TIDENES_ENDE, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(finnBGAndelArbeidsforhold(perioder.get(0), ORG_NUMMER_2)).isPresent();
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
    }


    @Test
    void lagPeriodeForGraderingArbeidsforholdTilkomEtterStp() {
        // Arrange
        var graderingFom = SKJÆRINGSTIDSPUNKT.plusWeeks(9);
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);


        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);

        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        var arbeidsgiver3 = Arbeidsgiver.virksomhet(ORG_NUMMER);
        var arbeidsgiver4 = Arbeidsgiver.virksomhet(ORG_NUMMER_2);
        var inntekt1 = Beløp.fra(90000);
        var refusjonskrav1 = inntekt1;
        var newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(9), SKJÆRINGSTIDSPUNKT.plusMonths(5).minusDays(2),
                arbId, arbeidsgiver3, Beløp.fra(10), newGrunnlagBuilder);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1
        );
        newGrunnlagBuilder.medInntektsmeldinger(im1);
        fjernAktivitet(arbeidsgiver3, arbId);
        fjernOgLeggTilNyBeregningAktivitet(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE, arbeidsgiver4, arbId);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medArbeidsgiver(arbeidsgiverGradering)
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medGradering(graderingFom, graderingTom, 50)
                .build());

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, aktivitetGradering, newGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusWeeks(9), SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1), PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), SKJÆRINGSTIDSPUNKT.plusWeeks(18), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(finnBGAndelArbeidsforhold(perioder.get(0), ORG_NUMMER_2)).isPresent();
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        assertThat(perioder.get(2).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        assertThat(finnBGAndelArbeidsforhold(perioder.get(2), ORG_NUMMER_2)).isPresent();
    }

    @Test
    void lagPeriodeForRefusjonArbeidsforholdTilkomEtterStp() {
        var arbId = InternArbeidsforholdRefDto.namedRef("A");

        // Arrange
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(), beregningAktivitetAggregat);
        var ansettelsesDato = SKJÆRINGSTIDSPUNKT.plusWeeks(2);
        var startDatoRefusjon = SKJÆRINGSTIDSPUNKT.plusWeeks(1);
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);
        var newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT, ansettelsesDato, TIDENES_ENDE, arbId, arbeidsgiver,
                Beløp.fra(10), newGrunnlagBuilder);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, startDatoRefusjon, Beløp.fra(20000), Beløp.fra(20000)
        );
        newGrunnlagBuilder.medInntektsmeldinger(im1);
        fjernAktivitet(arbeidsgiver, arbId);

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, newGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, ansettelsesDato.minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), ansettelsesDato, TIDENES_ENDE, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).isEmpty();
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
    }

    @Test
    void ikkeLagAndelForRefusjonForArbeidsforholdSomBortfallerFørSkjæringstidspunkt() {
        var arbId = InternArbeidsforholdRefDto.namedRef("A");

        // Arrange
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(), beregningAktivitetAggregat);
        var startDatoRefusjon = SKJÆRINGSTIDSPUNKT.plusWeeks(1);
        var ansattFom = SKJÆRINGSTIDSPUNKT.minusYears(2);
        var ansattTom = SKJÆRINGSTIDSPUNKT.minusMonths(2);
        var newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT, ansattFom, ansattTom, arbId, Arbeidsgiver.virksomhet(ORG_NUMMER), Beløp.fra(10), newGrunnlagBuilder);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, startDatoRefusjon, Beløp.fra(20000), Beløp.fra(20000)
        );
        newGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, newGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).isEmpty();
    }

    @Test
    void lagPeriodeForRefusjonArbeidsforholdTilkomEtterStpFlerePerioder() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        var arbeidsgiver3 = Arbeidsgiver.virksomhet(ORG_NUMMER);

        var startDatoPermisjon = SKJÆRINGSTIDSPUNKT.plusWeeks(1);
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE,
                arbId, Arbeidsgiver.virksomhet(ORG_NUMMER), iayGrunnlagBuilder);
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT, startDatoPermisjon, TIDENES_ENDE,
                arbId, Arbeidsgiver.virksomhet(ORG_NUMMER_2), iayGrunnlagBuilder);

        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, Beløp.fra(30000), Beløp.fra(30000),
                SKJÆRINGSTIDSPUNKT.plusWeeks(12));
        var im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, startDatoPermisjon, Beløp.fra(20000), Beløp.fra(20000)
        );
        iayGrunnlagBuilder.medInntektsmeldinger(im1, im2);
        fjernOgLeggTilNyBeregningAktivitet(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE, arbeidsgiver3, arbId);

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, startDatoPermisjon.minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), startDatoPermisjon, SKJÆRINGSTIDSPUNKT.plusWeeks(12), PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertBeregningsgrunnlagPeriode(perioder.get(2), SKJÆRINGSTIDSPUNKT.plusWeeks(12).plusDays(1), TIDENES_ENDE, PeriodeÅrsak.REFUSJON_OPPHØRER);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        assertThat(perioder.get(2).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
    }

    @Test
    void skalSetteRefusjonskravForSøktRefusjonFraSkjæringstidspunktUtenOpphørsdato() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE, arbId,
                Arbeidsgiver.virksomhet(ORG_NUMMER), iayGrunnlagBuilder);
        var berPerioder = singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT, null));
        var beregningsgrunnlag = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT, berPerioder, iayGrunnlagBuilder.getKladd());
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        var inntekt1 = Beløp.fra(90000);
        var refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        var andeler = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(null))
                .isEqualByComparingTo(refusjonskrav1.multipliser(ANTALL_MÅNEDER_I_ÅR));
    }

    @Test
    void skalSetteRefusjonskravForSøktRefusjonFraSkjæringstidspunktUtenOpphørsdatoPrivatpersonSomArbeidsgiver() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        var arbeidsgiver = Arbeidsgiver.person(ARBEIDSGIVER_AKTØR_ID);
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT,
                SKJÆRINGSTIDSPUNKT.minusYears(2),
                TIDENES_ENDE, arbId, arbeidsgiver, iayGrunnlagBuilder);
        var berPerioder = singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT, null));
        var beregningsgrunnlag = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT, berPerioder, iayGrunnlagBuilder.getKladd());
        var aktivitetEntitet = BeregningAktivitetDto.builder().medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(arbId)
                .medPeriode(fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE)).build();
        beregningAktivitetAggregat = leggTilAktivitet(aktivitetEntitet);
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        var inntekt1 = Beløp.fra(90000);
        var refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver, arbId, SKJÆRINGSTIDSPUNKT,
                refusjonskrav1, inntekt1, null, emptyList(), emptyList());
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        var andeler = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(null))
                .isEqualByComparingTo(refusjonskrav1.multipliser(ANTALL_MÅNEDER_I_ÅR));
    }

    private BeregningAktivitetAggregatDto leggTilAktivitet(BeregningAktivitetDto aktivitetEntitet) {
        aktiviteter.add(aktivitetEntitet);
        var builder = BeregningAktivitetAggregatDto.builder();
        aktiviteter.forEach(builder::leggTilAktivitet);
        return builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
    }

    @Test
    void skalSetteRefusjonskravForSøktRefusjonFraSkjæringstidspunktMedOpphørsdato() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);

        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT,
                SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE, arbId, arbeidsgiver, iayGrunnlagBuilder);
        var berPerioder = singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT, null));
        var beregningsgrunnlag = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT,
                berPerioder, iayGrunnlagBuilder.getKladd());
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        var inntekt1 = Beløp.fra(90000);
        var refusjonskrav1 = inntekt1;
        var refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusWeeks(6).minusDays(1);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1, refusjonOpphørerDato
        );
        iayGrunnlagBuilder.medInntektsmeldinger(im1);
        fjernOgLeggTilNyBeregningAktivitet(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE, arbeidsgiver, arbId);

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        var andelerIFørstePeriode = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIFørstePeriode).hasSize(1);
        assertThat(andelerIFørstePeriode.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(null))
                .isEqualByComparingTo(refusjonskrav1.multipliser(ANTALL_MÅNEDER_I_ÅR));

        assertThat(perioder.get(1).getBeregningsgrunnlagPeriodeFom()).isEqualTo(refusjonOpphørerDato.plusDays(1));
        var andelerIAndrePeriode = perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIAndrePeriode).hasSize(1);
        assertThat(andelerIAndrePeriode.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).map(Beløp::verdi).orElse(null))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void skalIkkeSetteRefusjonForAktivitetSomErFjernetIOverstyring() {
        // Arrange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);

        var handlingIkkeBenytt = BeregningAktivitetHandlingType.IKKE_BENYTT;
        var overstyring = BeregningAktivitetOverstyringerDto.builder().leggTilOverstyring(lagOverstyringForAktivitet(InternArbeidsforholdRefDto.nullRef(), arbeidsgiver, handlingIkkeBenytt)).build();
        var grunnlag = lagBeregningsgrunnlag(List.of(), beregningAktivitetAggregat, overstyring);
        var inntekt1 = Beløp.fra(90000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt1, inntekt1,
                null);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        var andelerIFørstePeriode = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIFørstePeriode).hasSize(0);
    }

    @Test
    void skalSetteRefusjonForAktivitetSomErFjernetISaksbehandlet() {
        // Arrange
        var grunnlag = lagBeregningsgrunnlagMedSaksbehandlet(List.of(),
                beregningAktivitetAggregat, BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build());
        var inntekt1 = Beløp.fra(90000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt1, inntekt1,
                null);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef,
                grunnlag,
                AktivitetGradering.INGEN_GRADERING,
                iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        var andelerIFørstePeriode = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIFørstePeriode).hasSize(1);
    }

    @Test
    void skalSetteRefusjonskravForSøktRefusjonFraEtterSkjæringstidspunktMedOpphørsdato() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);

        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE, arbId,
                Arbeidsgiver.virksomhet(ORG_NUMMER), iayGrunnlagBuilder);
        var berPerioder = singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT, null));
        var beregningsgrunnlag = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT,
                berPerioder, iayGrunnlagBuilder.getKladd());
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        var inntekt1 = Beløp.fra(90000);
        var refusjonskrav1 = inntekt1;
        var refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusWeeks(6).minusDays(1);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1, refusjonOpphørerDato
        );
        iayGrunnlagBuilder.medInntektsmeldinger(im1);
        fjernOgLeggTilNyBeregningAktivitet(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE, arbeidsgiver, arbId);

        // Act

        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        var andelerIFørstePeriode = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIFørstePeriode).hasSize(1);
        assertThat(andelerIFørstePeriode.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(null))
                .isEqualByComparingTo(refusjonskrav1.multipliser(ANTALL_MÅNEDER_I_ÅR));
        assertThat(perioder.get(1).getBeregningsgrunnlagPeriodeFom()).isEqualTo(refusjonOpphørerDato.plusDays(1));
        assertThat(perioder.get(1).getPeriodeÅrsaker()).isEqualTo(singletonList(PeriodeÅrsak.REFUSJON_OPPHØRER));
        var andelerIAndrePeriode = perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIAndrePeriode).hasSize(1);
        assertThat(andelerIAndrePeriode.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(null))
                .isEqualByComparingTo(Beløp.ZERO);
    }

    @Test
    void skalSetteRefusjonskravForSøktRefusjonFraEtterSkjæringstidspunktUtenOpphørsdato() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);

        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE, arbId,
                Arbeidsgiver.virksomhet(ORG_NUMMER), iayGrunnlagBuilder);
        var berPerioder = singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT, null));
        var beregningsgrunnlag = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT,
                berPerioder, iayGrunnlagBuilder.getKladd());
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        var inntekt1 = Beløp.fra(90000);
        var refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1, null
        );
        iayGrunnlagBuilder.medInntektsmeldinger(im1);
        fjernOgLeggTilNyBeregningAktivitet(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE, arbeidsgiver, arbId);

        // Act

        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        var andelerIFørstePeriode = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIFørstePeriode).hasSize(1);
        assertThat(andelerIFørstePeriode.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(null))
                .isEqualByComparingTo(refusjonskrav1.multipliser(ANTALL_MÅNEDER_I_ÅR));
    }

    @Test
    void skalTesteEndringIRefusjon() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);

        var arbeidsperiode = fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);
        List<String> orgnrs = List.of();
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(orgnrs, beregningAktivitetAggregat);
        var beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();
        var inntekt = Beløp.fra(40000);
        var refusjonsListe = List.of(
                new RefusjonDto(Beløp.fra(20000), SKJÆRINGSTIDSPUNKT.plusMonths(3)),
                new RefusjonDto(Beløp.fra(10000), SKJÆRINGSTIDSPUNKT.plusMonths(6)));
        var refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusMonths(9).minusDays(1);
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT.minusDays(1),
                arbeidsperiode, arbId, Arbeidsgiver.virksomhet(ORG_NUMMER), iayGrunnlagBuilder);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmeldingMedEndringerIRefusjon(ORG_NUMMER, arbId, SKJÆRINGSTIDSPUNKT, inntekt,
                inntekt, refusjonOpphørerDato, refusjonsListe);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);
        fjernOgLeggTilNyBeregningAktivitet(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE,
                arbeidsgiver, arbId);
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            var bga = BGAndelArbeidsforholdDto.builder()
                    .medArbeidsgiver(arbeidsgiver)
                    .medArbeidsperiodeFom(arbeidsperiode.getFomDato())
                    .medArbeidsperiodeTom(arbeidsperiode.getTomDato())
                    .medArbeidsforholdRef(arbId.getReferanse());

            BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medBGAndelArbeidsforhold(bga)
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                    .build(periode);
        });

        // Act

        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(4);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(3).minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusMonths(3), SKJÆRINGSTIDSPUNKT.plusMonths(6).minusDays(1),
                PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertBeregningsgrunnlagPeriode(perioder.get(2), SKJÆRINGSTIDSPUNKT.plusMonths(6), refusjonOpphørerDato, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertBeregningsgrunnlagPeriode(perioder.get(3), refusjonOpphørerDato.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.REFUSJON_OPPHØRER);
        var andeler = perioder.stream()
                .collect(Collectors.toMap(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPeriodeFom, p -> p.getBeregningsgrunnlagPrStatusOgAndelList().get(0)));
        assertThat(andeler.get(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr)
                .orElse(null))
                .isEqualByComparingTo(inntekt.multipliser(ANTALL_MÅNEDER_I_ÅR));
        assertThat(andeler.get(perioder.get(1).getBeregningsgrunnlagPeriodeFom()).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).map(Beløp::verdi)
                .orElse(null)).isEqualByComparingTo(BigDecimal.valueOf(20000 * 12));
        assertThat(andeler.get(perioder.get(2).getBeregningsgrunnlagPeriodeFom()).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).map(Beløp::verdi)
                .orElse(null)).isEqualByComparingTo(BigDecimal.valueOf(10000 * 12));
        assertThat(andeler.get(perioder.get(3).getBeregningsgrunnlagPeriodeFom()).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr)
                .orElse(null)).isEqualByComparingTo(Beløp.ZERO);
    }

    @Test
    void skalSplitteBeregningsgrunnlagOgLeggeTilNyAndelVedEndringssøknadNårSelvstendigNæringsdrivendeTilkommerOgGraderes() {
        // Arrange
        var graderingFom = SKJÆRINGSTIDSPUNKT.plusDays(10);
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusDays(20);
        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medGradering(graderingFom, graderingTom, 50)
                .build());
        var newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER, ORG_NUMMER_2), newGrunnlagBuilder);
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER, ORG_NUMMER_2), beregningAktivitetAggregat);

        var inntekt1 = Beløp.fra(90000);
        var refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1
        );
        newGrunnlagBuilder.medInntektsmeldinger(im1);

        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, aktivitetGradering, newGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(9));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(10), graderingTom, PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(3);
        assertAndelStatuser(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList(),
                Arrays.asList(AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));

    }

    @Test
    void skalLeggeTilAndelSomTilkommerEtterSkjæringstidspunktForSøktGraderingUtenRefusjon() {
        // Arrange
        var graderingFom = SKJÆRINGSTIDSPUNKT.plusMonths(2);
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusMonths(5);
        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medGradering(graderingFom, graderingTom, 50)
                .build());

        var inntekt = Beløp.fra(40000);
        var arbeidsperiode = fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);

        var aaBuilder2 = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(arbeidsperiode);

        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(aaBuilder2);

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                .leggTilYrkesaktivitet(yaBuilder);
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        var newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        newGrunnlagBuilder.medData(registerBuilder);

        leggTilYrkesaktiviteterOgBeregningAktiviteter(singletonList(ORG_NUMMER_2), newGrunnlagBuilder);
        fjernAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef());

        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);
        newGrunnlagBuilder.medInntektsmeldinger(im1);

        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        var beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            var bga = BGAndelArbeidsforholdDto.builder()
                    .medArbeidsperiodeFom(arbeidsperiode.getFomDato())
                    .medArbeidsperiodeTom(arbeidsperiode.getTomDato())
                    .medArbeidsgiver(arbeidsgiver2);

            BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medBGAndelArbeidsforhold(bga)
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                    .build(periode);
        });

        // Act

        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, aktivitetGradering, newGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        var beregningsgrunnlagPeriode = perioder.get(0);
        assertThat(beregningsgrunnlagPeriode.getPeriodeÅrsaker()).isEmpty();
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);

        var beregningsgrunnlagPeriode2 = perioder.get(1);
        assertThat(beregningsgrunnlagPeriode2.getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.GRADERING);
        assertThat(beregningsgrunnlagPeriode2.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        var baaOpt = finnBGAndelArbeidsforhold(beregningsgrunnlagPeriode2, arbeidsgiver.getIdentifikator());
        assertThat(baaOpt).as("BGAndelArbeidsforhold")
                .hasValueSatisfying(baa -> assertThat(baa.getRefusjonskravPrÅr()).as("RefusjonskravPrÅr").isNull());

        var beregningsgrunnlagPeriode3 = perioder.get(2);
        assertThat(beregningsgrunnlagPeriode3.getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.GRADERING_OPPHØRER);
        assertThat(beregningsgrunnlagPeriode3.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
    }

    @Test
    void skalLeggeTilAndelHvorBrukerErIPermisjonPåSkjæringstidspunktetOgSøkerRefusjon() {
        var arbeidsforholdRef = InternArbeidsforholdRefDto.nyRef();
        var inntekt = Beløp.fra(40000);
        var permisjonFom = SKJÆRINGSTIDSPUNKT.minusMonths(1);
        var permisjonTom = SKJÆRINGSTIDSPUNKT.plusMonths(1);

        var arbeidsperiode1 = fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        var arbeidsgiver = leggTilYrkesaktivitet(arbeidsperiode1, aktørArbeidBuilder, ORG_NUMMER, Intervall.fraOgMedTilOgMed(permisjonFom, permisjonTom));
        fjernOgLeggTilNyBeregningAktivitet(arbeidsperiode1.getFomDato(), permisjonFom.minusDays(1), arbeidsgiver, InternArbeidsforholdRefDto.nullRef());
        aktiviteter.add(lagAktivitet(arbeidsperiode1.getFomDato(), arbeidsperiode1.getTomDato(), arbeidsgiver, arbeidsforholdRef));

        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        var newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        newGrunnlagBuilder.medData(registerBuilder);

        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);
        newGrunnlagBuilder.medInntektsmeldinger(im1);

        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(), beregningAktivitetAggregat);

        // Act

        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, newGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        var beregningsgrunnlagPeriode = perioder.get(0);
        assertThat(beregningsgrunnlagPeriode.getPeriodeÅrsaker()).isEmpty();
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).isEmpty();

        var beregningsgrunnlagPeriode2 = perioder.get(1);
        assertThat(beregningsgrunnlagPeriode2.getPeriode().getFomDato()).isEqualTo(permisjonTom.plusDays(1));
        assertThat(beregningsgrunnlagPeriode2.getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertThat(beregningsgrunnlagPeriode2.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        var baaOpt = finnBGAndelArbeidsforhold(beregningsgrunnlagPeriode2, arbeidsgiver.getIdentifikator());
        assertThat(baaOpt).as("BGAndelArbeidsforhold")
                .hasValueSatisfying(baa -> assertThat(baa.getRefusjonskravPrÅr()).as("RefusjonskravPrÅr").isEqualByComparingTo(Beløp.fra(480_000)));
    }

    @Test
    void skalLeggeTilAndelSomTilkommerPåSkjæringstidspunkt() {
        var inntekt = Beløp.fra(40000);
        var arbeidsperiode = fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);

        var aaBuilder2 = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(arbeidsperiode);

        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(aaBuilder2);

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                .leggTilYrkesaktivitet(yaBuilder);
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        var newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        newGrunnlagBuilder.medData(registerBuilder);
        leggTilYrkesaktiviteterOgBeregningAktiviteter(singletonList(ORG_NUMMER_2), newGrunnlagBuilder);
        fjernAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef());

        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);
        newGrunnlagBuilder.medInntektsmeldinger(im1);

        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        var beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            var bga = BGAndelArbeidsforholdDto.builder()
                    .medArbeidsperiodeFom(arbeidsperiode.getFomDato())
                    .medArbeidsperiodeTom(arbeidsperiode.getTomDato())
                    .medArbeidsgiver(arbeidsgiver2);

            BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medBGAndelArbeidsforhold(bga)
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                    .build(periode);
        });

        // Act

        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, newGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        var beregningsgrunnlagPeriode = perioder.get(0);
        assertThat(beregningsgrunnlagPeriode.getPeriodeÅrsaker()).isEmpty();
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        var baaOpt = finnBGAndelArbeidsforhold(beregningsgrunnlagPeriode, arbeidsgiver.getIdentifikator());
        assertThat(baaOpt).as("BGAndelArbeidsforhold").hasValueSatisfying(
                baa -> assertThat(baa.getRefusjonskravPrÅr()).as("RefusjonskravPrÅr").isEqualByComparingTo(inntekt.multipliser(ANTALL_MÅNEDER_I_ÅR)));
    }

    @Test
    void skalLeggeTilAndelSomTilkommerPåSkjæringstidspunktOgSletteVedOpphør() {
        var inntekt = Beløp.fra(40000);
        var arbeidsperiode = fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);
        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var aaBuilder2 = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(arbeidsperiode);

        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(aaBuilder2);

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                .leggTilYrkesaktivitet(yaBuilder);
        iayGrunnlagBuilder.medData(InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER).leggTilAktørArbeid(aktørArbeidBuilder));

        var refusjonOpphørerFom = SKJÆRINGSTIDSPUNKT.plusMonths(1);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, refusjonOpphørerFom
        );
        iayGrunnlagBuilder.medInntektsmeldinger(im1);
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        var beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            var bga = BGAndelArbeidsforholdDto.builder()
                    .medArbeidsperiodeFom(arbeidsperiode.getFomDato())
                    .medArbeidsperiodeTom(arbeidsperiode.getTomDato())
                    .medArbeidsgiver(arbeidsgiver2);

            BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medBGAndelArbeidsforhold(bga)
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                    .build(periode);
        });

        // Act

        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        var beregningsgrunnlagPeriode = perioder.get(0);
        assertThat(beregningsgrunnlagPeriode.getPeriodeÅrsaker()).isEmpty();
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        var baaOpt = finnBGAndelArbeidsforhold(beregningsgrunnlagPeriode, arbeidsgiver.getIdentifikator());
        assertThat(baaOpt).hasValueSatisfying(baa -> assertThat(baa.getRefusjonskravPrÅr()).isEqualByComparingTo(inntekt.multipliser(ANTALL_MÅNEDER_I_ÅR)));

        var beregningsgrunnlagPeriode2 = perioder.get(1);
        assertThat(beregningsgrunnlagPeriode2.getPeriodeÅrsaker()).containsExactlyInAnyOrder(PeriodeÅrsak.REFUSJON_OPPHØRER);
        assertThat(beregningsgrunnlagPeriode2.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        baaOpt = finnBGAndelArbeidsforhold(beregningsgrunnlagPeriode2, arbeidsgiver.getIdentifikator());
        assertThat(baaOpt).isNotPresent();
    }

    @Test
    void skalLeggeTilAndelSomTilkommerPåSkjæringstidspunktForSøktGraderingUtenRefusjon() {
        // Arrange
        var graderingFom = SKJÆRINGSTIDSPUNKT;
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusMonths(2);
        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medGradering(graderingFom, graderingTom, 50)
                .build());

        var inntekt = Beløp.fra(40000);
        var arbeidsperiode = fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);

        var aaBuilder2 = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(arbeidsperiode);

        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(aaBuilder2);

        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørArbeidBuilder = registerBuilder.getAktørArbeidBuilder()
                .leggTilYrkesaktivitet(yaBuilder);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        var grunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        grunnlagBuilder.medData(registerBuilder);
        leggTilYrkesaktiviteterOgBeregningAktiviteter(singletonList(ORG_NUMMER_2), iayGrunnlagBuilder);
        fjernAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef());
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);
        grunnlagBuilder.medInntektsmeldinger(im1);

        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        var beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            var bga = BGAndelArbeidsforholdDto.builder()
                    .medArbeidsperiodeFom(arbeidsperiode.getFomDato())
                    .medArbeidsperiodeTom(arbeidsperiode.getTomDato())
                    .medArbeidsgiver(arbeidsgiver2);

            BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medBGAndelArbeidsforhold(bga)
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                    .build(periode);
        });

        // Act

        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, aktivitetGradering, grunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        var beregningsgrunnlagPeriode1 = perioder.get(0);
        assertThat(beregningsgrunnlagPeriode1.getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.GRADERING);
        assertThat(beregningsgrunnlagPeriode1.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        var baaOpt = finnBGAndelArbeidsforhold(beregningsgrunnlagPeriode1, arbeidsgiver.getIdentifikator());
        assertThat(baaOpt).as("BGAndelArbeidsforhold")
                .hasValueSatisfying(baa -> assertThat(baa.getRefusjonskravPrÅr()).as("RefusjonskravPrÅr").isNull());

        var beregningsgrunnlagPeriode2 = perioder.get(1);
        assertThat(beregningsgrunnlagPeriode2.getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.GRADERING_OPPHØRER);
        assertThat(beregningsgrunnlagPeriode2.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
    }

    @Test
    void skalLeggeTilAndelSomTilkommerPåSkjæringstidspunktMedOpphørUtenSlettingPgaGradering() {
        // Arrange
        var graderingFom = SKJÆRINGSTIDSPUNKT;
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusMonths(2);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medGradering(graderingFom, graderingTom, 50)
                .build());

        var inntekt = Beløp.fra(40000);
        var arbeidsperiode = fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);

        var aaBuilder2 = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(arbeidsperiode);

        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(aaBuilder2);

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                .leggTilYrkesaktivitet(yaBuilder);
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        var newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        newGrunnlagBuilder.medData(registerBuilder);

        leggTilYrkesaktiviteterOgBeregningAktiviteter(singletonList(ORG_NUMMER_2), iayGrunnlagBuilder);
        fjernAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef());

        var refusjonOpphørerFom = SKJÆRINGSTIDSPUNKT.plusMonths(1);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, refusjonOpphørerFom
        );
        newGrunnlagBuilder.medInntektsmeldinger(im1);

        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        var beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            var bga = BGAndelArbeidsforholdDto.builder()
                    .medArbeidsperiodeFom(arbeidsperiode.getFomDato())
                    .medArbeidsperiodeTom(arbeidsperiode.getTomDato())
                    .medArbeidsgiver(arbeidsgiver2);

            BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medBGAndelArbeidsforhold(bga)
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                    .build(periode);
        });
        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, aktivitetGradering, newGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        var beregningsgrunnlagPeriode = perioder.get(0);
        assertThat(beregningsgrunnlagPeriode.getPeriodeÅrsaker()).isEmpty();
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        var baaOpt = finnBGAndelArbeidsforhold(beregningsgrunnlagPeriode, arbeidsgiver.getIdentifikator());
        assertThat(baaOpt).hasValueSatisfying(baa -> assertThat(baa.getRefusjonskravPrÅr()).isEqualByComparingTo(inntekt.multipliser(ANTALL_MÅNEDER_I_ÅR)));

        var beregningsgrunnlagPeriode2 = perioder.get(1);
        assertThat(beregningsgrunnlagPeriode2.getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.REFUSJON_OPPHØRER);
        assertThat(beregningsgrunnlagPeriode2.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);

        var beregningsgrunnlagPeriode3 = perioder.get(2);
        assertThat(beregningsgrunnlagPeriode3.getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.REFUSJON_OPPHØRER, PeriodeÅrsak.GRADERING_OPPHØRER);
        assertThat(beregningsgrunnlagPeriode3.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
    }

    @Test
    void lagPeriodeForGraderingOver6G() {
        // Arrange
        var graderingFom = SKJÆRINGSTIDSPUNKT.plusWeeks(9);
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);

        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);
        var newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER, ORG_NUMMER_2), newGrunnlagBuilder);
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER, ORG_NUMMER_2), beregningAktivitetAggregat);

        var inntekt1 = Beløp.fra(90000);
        var refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1
        );
        newGrunnlagBuilder.medInntektsmeldinger(im1);
        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medArbeidsgiver(arbeidsgiverGradering)
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medGradering(graderingFom, graderingTom, 50)
                .build());

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, aktivitetGradering, newGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusWeeks(9), graderingTom, PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
    }

    @Test
    void skalSplitteBeregningsgrunnlagOgLeggeTilNyAndelVedEndringssøknadNårFrilansTilkommerOgGraderes() {
        // Arrange
        var graderingFom = SKJÆRINGSTIDSPUNKT.plusDays(10);
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusDays(20);

        var newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();

        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER, ORG_NUMMER_2), newGrunnlagBuilder);
        var grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER, ORG_NUMMER_2), beregningAktivitetAggregat);

        var inntekt1 = Beløp.fra(90000);
        var refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1
        );
        newGrunnlagBuilder.medInntektsmeldinger(im1);
        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medStatus(AktivitetStatus.FRILANSER)
                .medGradering(graderingFom, graderingTom, 50)
                .build());

        // Act
        var nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, aktivitetGradering, newGrunnlagBuilder);

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(9));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(10), graderingTom, PeriodeÅrsak.GRADERING);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(3);
        assertAndelStatuser(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList(),
                Arrays.asList(AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.FRILANSER));
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);

    }

    @Test
    void skalLageEnPeriodeNårGraderingPåVirksomhetOgAndelMedRefusjonIkkeHarNullIBruttoOgFlereArbeidsforholdISammeOrganisasjon() {
        // Arrange
        var beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medGrunnbeløp(GRUNNBELØP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .build();

        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
                .build(beregningsgrunnlag);

        var arbeidsforholdRef = InternArbeidsforholdRefDto.nyRef();
        var arbeidsforholdRef2 = InternArbeidsforholdRefDto.nyRef();
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver).medArbeidsforholdRef(arbeidsforholdRef))
                .medBeregnetPrÅr(Beløp.fra(40_000))
                .build(periode);

        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver).medArbeidsforholdRef(arbeidsforholdRef2))
                .medBeregnetPrÅr(Beløp.ZERO)
                .build(periode);

        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .build(BeregningsgrunnlagTilstand.FORESLÅTT);

        var oppdatere = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørArbeidBuilder = oppdatere.getAktørArbeidBuilder();
        aktørArbeidBuilder
                .leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                        .medArbeidsgiver(arbeidsgiver)
                        .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                        .medArbeidsforholdId(arbeidsforholdRef)
                        .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10), SKJÆRINGSTIDSPUNKT.plusYears(1)))))
                .leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                        .medArbeidsgiver(arbeidsgiver)
                        .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                        .medArbeidsforholdId(arbeidsforholdRef2)
                        .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(15), SKJÆRINGSTIDSPUNKT.plusYears(2)))));
        oppdatere.leggTilAktørArbeid(aktørArbeidBuilder);
        iayGrunnlagBuilder.medData(oppdatere);

        var refusjon = BigDecimal.valueOf(23987);

        var im1 = InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medRefusjon(Beløp.fra(refusjon))
                .medArbeidsforholdId(arbeidsforholdRef)
                .medBeløp(Beløp.fra(40_000))
                .medStartDatoPermisjon(SKJÆRINGSTIDSPUNKT)
                .build();

        var im2 = InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdId(arbeidsforholdRef2)
                .medStartDatoPermisjon(SKJÆRINGSTIDSPUNKT)
                .medBeløp(Beløp.ZERO)
                .build();

        iayGrunnlagBuilder.medInntektsmeldinger(im1, im2);

        var iayGrunnlag = iayGrunnlagBuilder.build();
        var input = new BeregningsgrunnlagInput(behandlingRef, iayGrunnlag, null, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlag);

        // Act
        var nyttBeregningsgrunnlag = tjeneste.fastsettPerioderForUtbetalingsgradEllerGradering(input, beregningsgrunnlag).getBeregningsgrunnlag();

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);
    }

    @Test
    void skalLageTrePerioderForRiktigAndelNårGraderingPåEtArbeidsforholdMedNullIBrutto() {
        // Arrange
        var graderingFom = SKJÆRINGSTIDSPUNKT.plusMonths(1);
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusMonths(2);
        var beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medGrunnbeløp(GRUNNBELØP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .build();

        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
                .build(beregningsgrunnlag);

        var arbeidsgiver1 = Arbeidsgiver.virksomhet(ORG_NUMMER);
        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ORG_NUMMER_2);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver1))
                .medBeregnetPrÅr(Beløp.fra(500_000))
                .build(periode);

        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver2))
                .medBeregnetPrÅr(Beløp.ZERO)
                .build(periode);
        var aktivitetEntitet = BeregningAktivitetDto.builder().medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                .medArbeidsgiver(arbeidsgiver2)
                .medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef())
                .medPeriode(fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(23), SKJÆRINGSTIDSPUNKT.plusYears(2))).build();
        beregningAktivitetAggregat = leggTilAktivitet(aktivitetEntitet);
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .build(BeregningsgrunnlagTilstand.FORESLÅTT);

        var oppdatere = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørArbeidBuilder = oppdatere.getAktørArbeidBuilder();
        aktørArbeidBuilder
                .leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                        .medArbeidsgiver(arbeidsgiver1)
                        .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                        .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10), SKJÆRINGSTIDSPUNKT.plusYears(1)))))
                .leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                        .medArbeidsgiver(arbeidsgiver2)
                        .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                        .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(23), SKJÆRINGSTIDSPUNKT.plusYears(2)))));
        oppdatere.leggTilAktørArbeid(aktørArbeidBuilder);
        iayGrunnlagBuilder.medData(oppdatere);

        var im1 = InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medStartDatoPermisjon(SKJÆRINGSTIDSPUNKT)
                .medBeløp(Beløp.fra(500_000))
                .build();

        var im2 = InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(arbeidsgiver2)
                .medStartDatoPermisjon(SKJÆRINGSTIDSPUNKT)
                .medBeløp(Beløp.ZERO)
                .build();

        iayGrunnlagBuilder.medInntektsmeldinger(im1, im2);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medArbeidsgiver(arbeidsgiver2)
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medGradering(graderingFom, graderingTom, 50)
                .build());

        var iayGrunnlag = iayGrunnlagBuilder.build();
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false, aktivitetGradering);
        var input = new BeregningsgrunnlagInput(behandlingRef, iayGrunnlag, null, List.of(), foreldrepengerGrunnlag)
                .medBeregningsgrunnlagGrunnlag(grunnlag);

        // Act
        var nyttBeregningsgrunnlag = tjeneste.fastsettPerioderForUtbetalingsgradEllerGradering(input, beregningsgrunnlag).getBeregningsgrunnlag();

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, graderingFom.minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), graderingFom, graderingTom, PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
    }

    @Test
    void skalLageTrePerioderNårDetIkkeErRefusjonOgAndelerHarNullIBruttoOgFlereArbeidsforholdISammeOrganisasjon() {
        // Arrange
        var graderingFom = SKJÆRINGSTIDSPUNKT.plusMonths(1);
        var graderingTom = SKJÆRINGSTIDSPUNKT.plusMonths(2);
        var beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medGrunnbeløp(GRUNNBELØP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .build();

        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
                .build(beregningsgrunnlag);

        var arbeidsforholdRef = InternArbeidsforholdRefDto.nyRef();
        var arbeidsforholdRef2 = InternArbeidsforholdRefDto.nyRef();
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver).medArbeidsforholdRef(arbeidsforholdRef))
                .medBeregnetPrÅr(Beløp.ZERO)
                .build(periode);

        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver).medArbeidsforholdRef(arbeidsforholdRef2))
                .medBeregnetPrÅr(Beløp.ZERO)
                .build(periode);

        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .build(BeregningsgrunnlagTilstand.FORESLÅTT);

        var oppdatere = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørArbeidBuilder = oppdatere.getAktørArbeidBuilder();
        aktørArbeidBuilder
                .leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                        .medArbeidsgiver(arbeidsgiver)
                        .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                        .medArbeidsforholdId(arbeidsforholdRef)
                        .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10), SKJÆRINGSTIDSPUNKT.plusYears(1)))))
                .leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                        .medArbeidsgiver(arbeidsgiver)
                        .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                        .medArbeidsforholdId(arbeidsforholdRef2)
                        .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(15), SKJÆRINGSTIDSPUNKT.plusYears(2)))));
        oppdatere.leggTilAktørArbeid(aktørArbeidBuilder);
        iayGrunnlagBuilder.medData(oppdatere);

        var im1 = InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdId(arbeidsforholdRef)
                .medStartDatoPermisjon(SKJÆRINGSTIDSPUNKT)
                .build();

        var im2 = InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdId(arbeidsforholdRef2)
                .medStartDatoPermisjon(SKJÆRINGSTIDSPUNKT)
                .medBeløp(Beløp.ZERO)
                .build();

        iayGrunnlagBuilder.medInntektsmeldinger(im1, im2);

        var aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medGradering(graderingFom, graderingTom, 50)
                .build());

        var iayGrunnlag = iayGrunnlagBuilder.build();
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false, aktivitetGradering);
        var input = new BeregningsgrunnlagInput(behandlingRef, iayGrunnlag, null, List.of(), foreldrepengerGrunnlag)
                .medBeregningsgrunnlagGrunnlag(grunnlag);

        // Act
        var nyttBeregningsgrunnlag = tjeneste.fastsettPerioderForUtbetalingsgradEllerGradering(input, beregningsgrunnlag).getBeregningsgrunnlag();

        // Assert
        var perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, graderingFom.minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), graderingFom, graderingTom, PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
    }

    private BeregningAktivitetOverstyringDto lagOverstyringForAktivitet(InternArbeidsforholdRefDto arbId, Arbeidsgiver arbeidsgiver, BeregningAktivitetHandlingType handlingIkkeBenytt) {
        return BeregningAktivitetOverstyringDto.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(arbId)
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                .medPeriode(fraOgMedTilOgMed(ARBEIDSPERIODE.getFomDato(), ARBEIDSPERIODE.getTomDato()))
                .medHandling(handlingIkkeBenytt).build();
    }

    private void assertAndelStatuser(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler, List<AktivitetStatus> statuser) {
        var aktivitetStatuser = andeler.stream().map(BeregningsgrunnlagPrStatusOgAndelDto::getAktivitetStatus).collect(Collectors.toList());
        assertThat(aktivitetStatuser).containsAll(statuser);

    }

    private Optional<BGAndelArbeidsforholdDto> finnBGAndelArbeidsforhold(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, String orgnr) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .flatMap(andel -> andel.getBgAndelArbeidsforhold().stream())
                .filter(bga -> bga.getArbeidsforholdOrgnr().equals(orgnr))
                .findFirst();
    }

    private void assertBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, LocalDate expectedFom, LocalDate expectedTom,
                                                 PeriodeÅrsak... perioderÅrsaker) {
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom()).as("fom").isEqualTo(expectedFom);
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()).as("tom").isEqualTo(expectedTom);
        assertThat(beregningsgrunnlagPeriode.getPeriodeÅrsaker()).as("periodeÅrsaker").containsExactlyInAnyOrder(perioderÅrsaker);
    }


    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlagMedOverstyring(List<String> orgnrs,
                                                                              BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        return lagBeregningsgrunnlag(orgnrs, beregningAktivitetAggregat, null);
    }

    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlagMedSaksbehandlet(List<String> orgnrs,
                                                                                BeregningAktivitetAggregatDto beregningAktivitetAggregat, BeregningAktivitetAggregatDto saksbehandlet) {
        var beregningsgrunnlagPeriodeBuilder = lagBeregningsgrunnlagPerioderBuilder(SKJÆRINGSTIDSPUNKT, null, orgnrs);
        var beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .medGrunnbeløp(GRUNNBELØP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medHjemmel(Hjemmel.F_14_7));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagPeriodeBuilder);
        var bg = beregningsgrunnlagBuilder.build();
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(bg)
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .medSaksbehandletAktiviteter(saksbehandlet)
                .build(BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlag(List<String> orgnrs,
                                                                BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                BeregningAktivitetOverstyringerDto BeregningAktivitetOverstyringer) {
        var beregningsgrunnlagPeriodeBuilder = lagBeregningsgrunnlagPerioderBuilder(SKJÆRINGSTIDSPUNKT, null, orgnrs);
        var beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .medGrunnbeløp(GRUNNBELØP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medHjemmel(Hjemmel.F_14_7));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagPeriodeBuilder);
        var bg = beregningsgrunnlagBuilder.build();
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(bg)
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .medOverstyring(BeregningAktivitetOverstyringer)
                .build(BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    private BeregningsgrunnlagPeriodeDto.Builder lagBeregningsgrunnlagPerioderBuilder(LocalDate fom, LocalDate tom, List<String> orgnrs) {
        var builder = BeregningsgrunnlagPeriodeDto.ny();
        for (var orgnr : orgnrs) {
            var arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
            var andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                    .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                            .medArbeidsgiver(arbeidsgiver)
                            .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT.minusYears(1)));
            builder.leggTilBeregningsgrunnlagPrStatusOgAndel(andelBuilder);
        }
        return builder
                .medBeregningsgrunnlagPeriode(fom, tom);
    }

}
