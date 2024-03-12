package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;


public class AvklaringsbehovUtlederFastsettBeregningsaktiviteterPleiepengerTest {

    private static final String INNTEKT_RAPPORTERING_FRIST_DATO = "inntekt.rapportering.frist.dato";
    private BeregningAktivitetAggregatDto beregningAktivitetAggregat;
    private KoblingReferanse ref;

    private AvklaringsbehovUtlederFastsettBeregningsaktiviteterPleiepenger apUtleder = new AvklaringsbehovUtlederFastsettBeregningsaktiviteterPleiepenger();
    private boolean erOverstyrt;

    private OpptjeningAktiviteterDto opptjeningAktiviteter = new OpptjeningAktiviteterDto();

    private InntektArbeidYtelseGrunnlagDto iayMock = mock(InntektArbeidYtelseGrunnlagDto.class);
    private AktørYtelseDto ay = mock(AktørYtelseDto.class);
    private BeregningsgrunnlagInput input;
    private LocalDate tom;
    private BeregningsgrunnlagDto beregningsgrunnlag;

    @BeforeEach
    public void setUp() {

        erOverstyrt = false;

        LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
        tom = SKJÆRINGSTIDSPUNKT.withDayOfMonth(1).minusDays(1);


        beregningsgrunnlag = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        ref = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
        beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT)
                .leggTilAktivitet(BeregningAktivitetDto.builder().medOpptjeningAktivitetType(OpptjeningAktivitetType.FRILANS)
                        .medPeriode(Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10)))
                        .build())
            .build();
        input = new BeregningsgrunnlagInput(ref, null, null, null, null);
        input.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 100000);
    }

    @Test
    public void skalSettePåVentNårFørRapporteringsfrist() {
        // Arrange
        int rapporteringsfrist = 1000;
        LocalDateTime frist = tom.plusDays(rapporteringsfrist).plusDays(1).atStartOfDay();

        //Act
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = lagRegelresultat();
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
        List<BeregningAvklaringsbehovResultat> resultater = new AvklaringsbehovUtlederFastsettBeregningsaktiviteterPleiepenger().utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagBeregningsgrunnlagInput(rapporteringsfrist), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(1);
        BeregningAvklaringsbehovResultat beregningAvklaringsbehovResultat = resultater.get(0);

        assertThat(beregningAvklaringsbehovResultat.getBeregningAvklaringsbehovDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.AUTO_VENT_PÅ_INNTKT_RAP_FRST);

        assertThat(beregningAvklaringsbehovResultat.getVenteårsak())
                .isNotNull()
                .isEqualTo(BeregningVenteårsak.VENT_INNTEKT_RAPPORTERINGSFRIST);

        assertThat(beregningAvklaringsbehovResultat.getVentefrist())
                .isNotNull()
                .isAfterOrEqualTo(frist)
                .isBeforeOrEqualTo(frist.plusDays(4)); //utledning tar hensyn til bevegelige helligdager. Gir testen slack for å tillate det.
    }

    private BeregningsgrunnlagRegelResultat lagRegelresultat() {
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(beregningsgrunnlag, Collections.emptyList());
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
        return beregningsgrunnlagRegelResultat;
    }


    @Test
    public void skalIkkeUtledeAutopunktNårLøpendeYtelseOgMeldekortForAAPIkkeErMottatt() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        LocalDate ytelsePeriodeFom = skjæringstidspunkt.minusMonths(2);
        YtelseDtoBuilder yb = YtelseDtoBuilder.ny();
        YtelseDto ytelse = yb.medYtelseType(YtelseType.ARBEIDSAVKLARINGSPENGER)
                .medPeriode(Intervall.fraOgMed(ytelsePeriodeFom))
                .leggTilYtelseAnvist(lagYtelseAnvist(yb.getAnvistBuilder(), skjæringstidspunkt.minusWeeks(3), skjæringstidspunkt.minusWeeks(1)))
                .build();

        BeregningsgrunnlagDto bgMedKunYtelse = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.KUN_YTELSE))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
                .build(bgMedKunYtelse);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
                .build(periode);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.FORELDREPENGER, Intervall.fraOgMed(ytelsePeriodeFom));

        List<YtelseDto> liste = Collections.singletonList(ytelse);
        when(iayMock.getAktørYtelseFraRegister()).thenReturn(Optional.of(ay));
        when(ay.getAlleYtelser()).thenReturn(liste);

        // Act
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(bgMedKunYtelse, Collections.emptyList());
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
        List<BeregningAvklaringsbehovResultat> resultater = apUtleder.utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagMockBeregningsgrunnlagInput(), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(0);
    }


    @Test
    public void skalUtledeAutopunktNårLøpendeYtelseOgMeldekortSomInkludererStpIkkeErMottatt() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        LocalDate ytelsePeriodeFom = skjæringstidspunkt.minusMonths(2);
        YtelseDtoBuilder yb = YtelseDtoBuilder.ny();
        YtelseDto ytelse = yb.medYtelseType(YtelseType.DAGPENGER)
            .medPeriode(Intervall.fraOgMed(ytelsePeriodeFom))
            .leggTilYtelseAnvist(lagYtelseAnvist(yb.getAnvistBuilder(), skjæringstidspunkt.minusWeeks(3), skjæringstidspunkt.minusWeeks(1)))
            .build();

        BeregningsgrunnlagDto bgMedDagpenger = BeregningsgrunnlagDto.builder()
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER))
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
            .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.DAGPENGER).build(periode);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.DAGPENGER, Intervall.fraOgMed(ytelsePeriodeFom));

        List<YtelseDto> liste = Collections.singletonList(ytelse);
        when(iayMock.getAktørYtelseFraRegister()).thenReturn(Optional.of(ay));
        when(ay.getAlleYtelser()).thenReturn(liste);

        // Act
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(bgMedDagpenger, Collections.emptyList());
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
        List<BeregningAvklaringsbehovResultat> resultater = apUtleder.utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagMockBeregningsgrunnlagInput(), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(1);
        assertThat(resultater).anySatisfy(resultat ->
            assertThat(resultat.getBeregningAvklaringsbehovDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.AUTO_VENT_PÅ_SISTE_AAP_DP_MELDKRT)
        );
    }

    private YtelseAnvistDto lagYtelseAnvist(YtelseAnvistDtoBuilder anvistBuilder, LocalDate fom, LocalDate tom) {
        return anvistBuilder.medAnvistPeriode(Intervall.fraOgMedTilOgMed(fom, tom)).build();
    }

    @Test
    public void skalUtledeAutopunktVentPåInntektFrilansNårManSkalVentePåBådeInntekterFrilansOgAAPMeldekort() {
        // Arrange
        int rapporteringsfrist = 1000;

        // Act
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = lagRegelresultat();
        List<BeregningAvklaringsbehovResultat> resultater = apUtleder.utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagBeregningsgrunnlagInput(rapporteringsfrist), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(1);
        assertThat(resultater).anySatisfy(resultat ->
            assertThat(resultat.getBeregningAvklaringsbehovDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.AUTO_VENT_PÅ_INNTKT_RAP_FRST)
        );
    }

    @Test
    public void skalIkkeUtledeAutopunktNårLøpendeYtelseOgMeldekortSomInkludererStpErMottatt() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        LocalDate ytelsePeriodeFom = skjæringstidspunkt.minusMonths(2);
        YtelseDtoBuilder yb = YtelseDtoBuilder.ny();
        YtelseAnvistDto meldekort1 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(7), skjæringstidspunkt.minusWeeks(5))).build();
        YtelseAnvistDto meldekort2 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(4), skjæringstidspunkt.minusWeeks(2))).build();
        YtelseAnvistDto meldekort3 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(1), skjæringstidspunkt)).build();
        YtelseAnvistDto meldekort4 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.plusDays(1), skjæringstidspunkt.plusWeeks(2))).build();
        YtelseDto ytelse = yb.medYtelseType(YtelseType.ARBEIDSAVKLARINGSPENGER)
                .medPeriode(Intervall.fraOgMed(ytelsePeriodeFom))
                .leggTilYtelseAnvist(meldekort1)
                .leggTilYtelseAnvist(meldekort2)
                .leggTilYtelseAnvist(meldekort3)
                .leggTilYtelseAnvist(meldekort4)
                .build();

        BeregningsgrunnlagDto bgMedDagpenger = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
                .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.DAGPENGER).build(periode);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.DAGPENGER, Intervall.fraOgMed(ytelsePeriodeFom));

        List<YtelseDto> liste = Collections.singletonList(ytelse);
        when(iayMock.getAktørYtelseFraRegister()).thenReturn(Optional.of(ay));
        when(ay.getAlleYtelser()).thenReturn(liste);

        // Act
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(bgMedDagpenger, Collections.emptyList());
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
        List<BeregningAvklaringsbehovResultat> resultater = apUtleder.utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagMockBeregningsgrunnlagInput(), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(0);
    }

    @Test
    public void skalIkkeUtledeAutopunktNårYtelseOpphørerToDagerFørStp() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        Intervall periodeIntervallForAktivitet = Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(10), skjæringstidspunkt.minusDays(2));
        YtelseDtoBuilder yb = YtelseDtoBuilder.ny();
        YtelseAnvistDto meldekort1 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(6), skjæringstidspunkt.minusWeeks(4))).build();
        YtelseDto ytelse = yb.medYtelseType(YtelseType.ARBEIDSAVKLARINGSPENGER)
                .medPeriode(periodeIntervallForAktivitet)
                .leggTilYtelseAnvist(meldekort1)
                .build();

        BeregningsgrunnlagDto bgMedDagpenger = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
                .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER).build(periode);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.AAP, periodeIntervallForAktivitet);


        List<YtelseDto> liste = Collections.singletonList(ytelse);
        when(iayMock.getAktørYtelseFraRegister()).thenReturn(Optional.of(ay));
        when(ay.getAlleYtelser()).thenReturn(liste);
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(bgMedDagpenger, Collections.emptyList());
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);

        // Act
        List<BeregningAvklaringsbehovResultat> resultater = apUtleder.utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagMockBeregningsgrunnlagInput(), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(0);
    }

    @Test
    public void skalUtledeAutopunktNårYtelseOpphørerEnDagFørStpOgMeldekortIkkeMottatt() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        Intervall periodeIntervallForAktivitet = Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(10), skjæringstidspunkt.minusDays(1));
        YtelseDtoBuilder yb = YtelseDtoBuilder.ny();
        YtelseAnvistDto meldekort1 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(4), skjæringstidspunkt.minusWeeks(2))).build();
        YtelseDto ytelse = yb.medYtelseType(YtelseType.DAGPENGER)
                .medPeriode(periodeIntervallForAktivitet)
                .leggTilYtelseAnvist(meldekort1)
                .build();

        BeregningsgrunnlagDto bgMedDagpenger = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
                .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.DAGPENGER).build(periode);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.DAGPENGER, periodeIntervallForAktivitet);

        List<YtelseDto> liste = Collections.singletonList(ytelse);
        when(iayMock.getAktørYtelseFraRegister()).thenReturn(Optional.of(ay));
        when(ay.getAlleYtelser()).thenReturn(liste);

        // Act
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(bgMedDagpenger, Collections.emptyList());
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
        List<BeregningAvklaringsbehovResultat> resultater = apUtleder.utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagMockBeregningsgrunnlagInput(), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(1);
        assertThat(resultater).anySatisfy(resultat ->
                assertThat(resultat.getBeregningAvklaringsbehovDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.AUTO_VENT_PÅ_SISTE_AAP_DP_MELDKRT)
        );
    }

    @Test
    public void skalIkkeUtledeAutopunktNårYtelseOpphørerEnDagFørStpOgMeldekortErMottatt() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        Intervall periodeIntervallForAktivitet = Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(10), skjæringstidspunkt.minusDays(1));
        YtelseDtoBuilder yb = YtelseDtoBuilder.ny();
        YtelseAnvistDto meldekort1 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(2), skjæringstidspunkt.minusDays(1))).build();
        YtelseDto ytelse = yb.medYtelseType(YtelseType.ARBEIDSAVKLARINGSPENGER)
                .medPeriode(periodeIntervallForAktivitet)
                .leggTilYtelseAnvist(meldekort1)
                .build();

        BeregningsgrunnlagDto bgMedDagpenger = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
                .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER).build(periode);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.AAP, periodeIntervallForAktivitet);

        List<YtelseDto> liste = Collections.singletonList(ytelse);
        when(iayMock.getAktørYtelseFraRegister()).thenReturn(Optional.of(ay));
        when(ay.getAlleYtelser()).thenReturn(liste);

        // Act
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(bgMedDagpenger, Collections.emptyList());
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
        List<BeregningAvklaringsbehovResultat> resultater = apUtleder.utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagMockBeregningsgrunnlagInput(), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(0);
    }

    private BeregningsgrunnlagInput lagBeregningsgrunnlagInput(int rapporteringsfrist) {
        BeregningsgrunnlagInput beregningsgrunnlagInput = new BeregningsgrunnlagInput(ref, getIAYGrunnlag(), opptjeningAktiviteter, List.of(), new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false));
        beregningsgrunnlagInput.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, rapporteringsfrist);
        return beregningsgrunnlagInput;
    }

    private BeregningsgrunnlagInput lagMockBeregningsgrunnlagInput() {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(ref, getMockedIAYGrunnlag(), opptjeningAktiviteter, List.of(), new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false));
        input.leggTilKonfigverdi("inntekt.rapportering.frist.dato", 5);
        return input;
    }

    private InntektArbeidYtelseGrunnlagDto getIAYGrunnlag() {
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
    }

    private InntektArbeidYtelseGrunnlagDto getMockedIAYGrunnlag() {
        return iayMock;
    }

    private BeregningAktivitetAggregatDto lagBeregningAktivitetAggregatDto(LocalDate skjæringstidspunkt, OpptjeningAktivitetType opptjeningType, Intervall periodeForAktivitet){
        BeregningAktivitetDto beregningAktivitetDto = BeregningAktivitetDto.builder()
                .medOpptjeningAktivitetType(opptjeningType)
                .medPeriode(periodeForAktivitet)
                .build();
        return BeregningAktivitetAggregatDto.builder()
                .leggTilAktivitet(beregningAktivitetDto)
                .medSkjæringstidspunktOpptjening(skjæringstidspunkt)
                .build();
    }

}
