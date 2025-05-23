package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
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
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;


class AvklaringsbehovUtlederFastsettBeregningsaktiviteterPleiepengerTest {

    private static final String INNTEKT_RAPPORTERING_FRIST_DATO = "inntekt.rapportering.frist.dato";
    private BeregningAktivitetAggregatDto beregningAktivitetAggregat;
    private KoblingReferanse ref;

    private AvklaringsbehovUtlederFastsettBeregningsaktiviteterPleiepenger apUtleder = new AvklaringsbehovUtlederFastsettBeregningsaktiviteterPleiepenger();
    private boolean erOverstyrt;

    private OpptjeningAktiviteterDto opptjeningAktiviteter = new OpptjeningAktiviteterDto();

    private BeregningsgrunnlagInput input;
    private LocalDate tom;
    private BeregningsgrunnlagDto beregningsgrunnlag;

    @BeforeEach
    void setUp() {

        erOverstyrt = false;

	    var SKJÆRINGSTIDSPUNKT = LocalDate.now();
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
    void skalSettePåVentNårFørRapporteringsfrist() {
        // Arrange
	    var rapporteringsfrist = 1000;
	    var frist = tom.plusDays(rapporteringsfrist).plusDays(1).atStartOfDay();

        //Act
	    var beregningsgrunnlagRegelResultat = lagRegelresultat();
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
	    var resultater = new AvklaringsbehovUtlederFastsettBeregningsaktiviteterPleiepenger().utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagBeregningsgrunnlagInput(rapporteringsfrist), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(1);
	    var beregningAvklaringsbehovResultat = resultater.get(0);

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
	    var beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(beregningsgrunnlag, Collections.emptyList());
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
        return beregningsgrunnlagRegelResultat;
    }


    @Test
    void skalIkkeUtledeAutopunktNårLøpendeYtelseOgMeldekortForAAPIkkeErMottatt() {
        // Arrange
	    var skjæringstidspunkt = LocalDate.now();
	    var ytelsePeriodeFom = skjæringstidspunkt.minusMonths(2);
	    var yb = YtelseDtoBuilder.ny();
	    var ytelse = yb.medYtelseType(YtelseType.ARBEIDSAVKLARINGSPENGER)
                .medPeriode(Intervall.fraOgMed(ytelsePeriodeFom))
                .leggTilYtelseAnvist(lagYtelseAnvist(yb.getAnvistBuilder(), skjæringstidspunkt.minusWeeks(3), skjæringstidspunkt.minusWeeks(1)));

	    var bgMedKunYtelse = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.KUN_YTELSE))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
	    var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
                .build(bgMedKunYtelse);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
                .build(periode);
	    var beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.FORELDREPENGER, Intervall.fraOgMed(ytelsePeriodeFom));


        // Act
	    var beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(bgMedKunYtelse, Collections.emptyList());
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
	    var resultater = apUtleder.utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagMockBeregningsgrunnlagInput(ytelse), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(0);
    }


    @Test
    void skalUtledeAutopunktNårLøpendeYtelseOgMeldekortSomInkludererStpIkkeErMottatt() {
        // Arrange
	    var skjæringstidspunkt = LocalDate.now();
	    var ytelsePeriodeFom = skjæringstidspunkt.minusMonths(2);
	    var yb = YtelseDtoBuilder.ny();
	    var ytelse = yb.medYtelseType(YtelseType.DAGPENGER)
            .medPeriode(Intervall.fraOgMed(ytelsePeriodeFom))
            .leggTilYtelseAnvist(lagYtelseAnvist(yb.getAnvistBuilder(), skjæringstidspunkt.minusWeeks(3), skjæringstidspunkt.minusWeeks(1)));

	    var bgMedDagpenger = BeregningsgrunnlagDto.builder()
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER))
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .build();
	    var periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
            .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.DAGPENGER).build(periode);
	    var beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.DAGPENGER, Intervall.fraOgMed(ytelsePeriodeFom));


        // Act
	    var beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(bgMedDagpenger, Collections.emptyList());
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
	    var resultater = apUtleder.utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagMockBeregningsgrunnlagInput(ytelse), erOverstyrt);

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
    void skalUtledeAutopunktVentPåInntektFrilansNårManSkalVentePåBådeInntekterFrilansOgAAPMeldekort() {
        // Arrange
	    var rapporteringsfrist = 1000;

        // Act
	    var beregningsgrunnlagRegelResultat = lagRegelresultat();
	    var resultater = apUtleder.utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagBeregningsgrunnlagInput(rapporteringsfrist), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(1);
        assertThat(resultater).anySatisfy(resultat ->
            assertThat(resultat.getBeregningAvklaringsbehovDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.AUTO_VENT_PÅ_INNTKT_RAP_FRST)
        );
    }

    @Test
    void skalIkkeUtledeAutopunktNårLøpendeYtelseOgMeldekortSomInkludererStpErMottatt() {
        // Arrange
	    var skjæringstidspunkt = LocalDate.now();
	    var ytelsePeriodeFom = skjæringstidspunkt.minusMonths(2);
	    var yb = YtelseDtoBuilder.ny();
	    var meldekort1 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(7), skjæringstidspunkt.minusWeeks(5))).build();
	    var meldekort2 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(4), skjæringstidspunkt.minusWeeks(2))).build();
	    var meldekort3 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(1), skjæringstidspunkt)).build();
	    var meldekort4 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.plusDays(1), skjæringstidspunkt.plusWeeks(2))).build();
        var ytelse = yb.medYtelseType(YtelseType.ARBEIDSAVKLARINGSPENGER)
                .medPeriode(Intervall.fraOgMed(ytelsePeriodeFom))
                .leggTilYtelseAnvist(meldekort1)
                .leggTilYtelseAnvist(meldekort2)
                .leggTilYtelseAnvist(meldekort3)
                .leggTilYtelseAnvist(meldekort4);

	    var bgMedDagpenger = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
	    var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
                .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.DAGPENGER).build(periode);
	    var beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.DAGPENGER, Intervall.fraOgMed(ytelsePeriodeFom));

        // Act
	    var beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(bgMedDagpenger, Collections.emptyList());
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
	    var resultater = apUtleder.utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagMockBeregningsgrunnlagInput(ytelse), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(0);
    }

    @Test
    void skalIkkeUtledeAutopunktNårYtelseOpphørerToDagerFørStp() {
        // Arrange
	    var skjæringstidspunkt = LocalDate.now();
	    var periodeIntervallForAktivitet = Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(10), skjæringstidspunkt.minusDays(2));
	    var yb = YtelseDtoBuilder.ny();
	    var meldekort1 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(6), skjæringstidspunkt.minusWeeks(4))).build();
        var ytelse = yb.medYtelseType(YtelseType.ARBEIDSAVKLARINGSPENGER)
                .medPeriode(periodeIntervallForAktivitet)
                .leggTilYtelseAnvist(meldekort1);

	    var bgMedDagpenger = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
	    var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
                .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER).build(periode);
	    var beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.AAP, periodeIntervallForAktivitet);


	    var beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(bgMedDagpenger, Collections.emptyList());
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);

        // Act
	    var resultater = apUtleder.utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagMockBeregningsgrunnlagInput(ytelse), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(0);
    }

    @Test
    void skalUtledeAutopunktNårYtelseOpphørerEnDagFørStpOgMeldekortIkkeMottatt() {
        // Arrange
	    var skjæringstidspunkt = LocalDate.now();
	    var periodeIntervallForAktivitet = Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(10), skjæringstidspunkt.minusDays(1));
	    var yb = YtelseDtoBuilder.ny();
	    var meldekort1 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(4), skjæringstidspunkt.minusWeeks(2))).build();
        var ytelse = yb.medYtelseType(YtelseType.DAGPENGER)
                .medPeriode(periodeIntervallForAktivitet)
                .leggTilYtelseAnvist(meldekort1);

	    var bgMedDagpenger = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
	    var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
                .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.DAGPENGER).build(periode);
	    var beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.DAGPENGER, periodeIntervallForAktivitet);

        // Act
	    var beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(bgMedDagpenger, Collections.emptyList());
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
	    var resultater = apUtleder.utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagMockBeregningsgrunnlagInput(ytelse), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(1);
        assertThat(resultater).anySatisfy(resultat ->
                assertThat(resultat.getBeregningAvklaringsbehovDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.AUTO_VENT_PÅ_SISTE_AAP_DP_MELDKRT)
        );
    }

    @Test
    void skalIkkeUtledeAutopunktNårYtelseOpphørerEnDagFørStpOgMeldekortErMottatt() {
        // Arrange
	    var skjæringstidspunkt = LocalDate.now();
	    var periodeIntervallForAktivitet = Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(10), skjæringstidspunkt.minusDays(1));
	    var yb = YtelseDtoBuilder.ny();
	    var meldekort1 = YtelseAnvistDtoBuilder.ny().medAnvistPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusWeeks(2), skjæringstidspunkt.minusDays(1))).build();
        var ytelse = yb.medYtelseType(YtelseType.ARBEIDSAVKLARINGSPENGER)
                .medPeriode(periodeIntervallForAktivitet)
                .leggTilYtelseAnvist(meldekort1);

	    var bgMedDagpenger = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();
	    var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(skjæringstidspunkt, null)
                .build(bgMedDagpenger);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER).build(periode);
	    var beregningAktivitetAggregat = lagBeregningAktivitetAggregatDto(skjæringstidspunkt,
                OpptjeningAktivitetType.AAP, periodeIntervallForAktivitet);

        // Act
	    var beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(bgMedDagpenger, Collections.emptyList());
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
	    var resultater = apUtleder.utledAvklaringsbehov(beregningsgrunnlagRegelResultat, lagMockBeregningsgrunnlagInput(ytelse), erOverstyrt);

        // Assert
        assertThat(resultater).hasSize(0);
    }

    private BeregningsgrunnlagInput lagBeregningsgrunnlagInput(int rapporteringsfrist) {
	    var beregningsgrunnlagInput = new BeregningsgrunnlagInput(ref, getIAYGrunnlag(), opptjeningAktiviteter, List.of(), new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false));
        beregningsgrunnlagInput.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, rapporteringsfrist);
        return beregningsgrunnlagInput;
    }

    private BeregningsgrunnlagInput lagMockBeregningsgrunnlagInput(YtelseDtoBuilder builder) {
	    var input = new BeregningsgrunnlagInput(ref, getMockedIAYGrunnlag(builder), opptjeningAktiviteter, List.of(), new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false));
        input.leggTilKonfigverdi("inntekt.rapportering.frist.dato", 5);
        return input;
    }

    private InntektArbeidYtelseGrunnlagDto getIAYGrunnlag() {
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
    }

    private InntektArbeidYtelseGrunnlagDto getMockedIAYGrunnlag(YtelseDtoBuilder builder) {
		var aggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
		var ayBuilder = aggregatBuilder.getAktørYtelseBuilder().leggTilYtelse(builder);
		aggregatBuilder.leggTilAktørYtelse(ayBuilder);

        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medData(aggregatBuilder).build();
    }

    private BeregningAktivitetAggregatDto lagBeregningAktivitetAggregatDto(LocalDate skjæringstidspunkt, OpptjeningAktivitetType opptjeningType, Intervall periodeForAktivitet){
	    var beregningAktivitetDto = BeregningAktivitetDto.builder()
                .medOpptjeningAktivitetType(opptjeningType)
                .medPeriode(periodeForAktivitet)
                .build();
        return BeregningAktivitetAggregatDto.builder()
                .leggTilAktivitet(beregningAktivitetDto)
                .medSkjæringstidspunktOpptjening(skjæringstidspunkt)
                .build();
    }

}
