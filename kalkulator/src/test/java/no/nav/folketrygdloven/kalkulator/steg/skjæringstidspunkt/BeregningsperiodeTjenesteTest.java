package no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class BeregningsperiodeTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, Month.JANUARY, 1);
    private static final String INNTEKT_RAPPORTERING_FRIST_DATO = "inntekt.rapportering.frist.dato";

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private Arbeidsgiver arbeidsgiverA = Arbeidsgiver.virksomhet("123456789");
    private Arbeidsgiver arbeidsgiverB = Arbeidsgiver.virksomhet("987654321");
    private BeregningsgrunnlagInput input;
    private BeregningsperiodeTjeneste beregningsperiodeTjeneste = new BeregningsperiodeTjeneste();


    @BeforeEach
    public void setUp() {
        input = new BeregningsgrunnlagInput(koblingReferanse, null, null, null, null);
        input.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
    }

    @Test
    public void skalTesteAtBeregningsperiodeBlirSattRiktig() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 5, 15);

        // Act
        Intervall periode = beregningsperiodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(skjæringstidspunkt);

        // Assert
        assertThat(periode.getFomDato()).isEqualTo(LocalDate.of(2019, 2, 1));
        assertThat(periode.getTomDato()).isEqualTo(LocalDate.of(2019, 4, 30));
    }

    @Test
    public void skalIkkeSettesPåVentNårIkkeErATFL() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT;
        var beregningAktivitetAggregatDto = lagBergningaktivitetAggregat1SNAndel();

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste.skalVentePåInnrapporteringAvInntektATFL(input, List.of(), dagensdato, beregningAktivitetAggregatDto, input.getSkjæringstidspunktForBeregning());

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skalIkkeSettesPåVentNårNåtidErEtterFrist() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(7); // 8. januar
        var beregningAktivitetAggregatDto = lagBeregningaktiviteter1ArbeidstakerAndel();

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste.skalVentePåInnrapporteringAvInntektATFL(input, List.of(arbeidsgiverA), dagensdato, beregningAktivitetAggregatDto, input.getSkjæringstidspunktForBeregning());

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skalIkkeSettesPåVentNårNåtidErLengeEtterFrist() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(45);
        var beregningAktivitetAggregatDto = lagBeregningaktiviteter1ArbeidstakerAndel();

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste.skalVentePåInnrapporteringAvInntektATFL(input, List.of(arbeidsgiverA), dagensdato, beregningAktivitetAggregatDto, input.getSkjæringstidspunktForBeregning());

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skalAlltidSettesPåVentNårBrukerErFrilanserFørFrist() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(4);
        var beregningAktivitetAggregatDto = lagAktivitetAggregat1FrilansAndel();

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste.skalVentePåInnrapporteringAvInntektATFL(input, List.of(), dagensdato, beregningAktivitetAggregatDto, input.getSkjæringstidspunktForBeregning());

        // Assert
        assertThat(resultat).isPresent();
        assertThat(resultat).hasValue(LocalDate.of(2019,1,8));
    }

    @Test
    public void skalIkkeSettesPåVentNårHarInntektsmeldingFørFrist() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(3);
        var beregningAktivitetAggregatDto = lagBeregningaktiviteter1ArbeidstakerAndel();
        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste.skalVentePåInnrapporteringAvInntektATFL(input, List.of(arbeidsgiverA), dagensdato, beregningAktivitetAggregatDto, input.getSkjæringstidspunktForBeregning());
        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skalSettesPåVentNårFørFristUtenInntektsmelding() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(4);
        var beregningAktivitetAggregatDto = lagBeregningaktiviteter1ArbeidstakerAndel();
        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste.skalVentePåInnrapporteringAvInntektATFL(input, List.of(), dagensdato, beregningAktivitetAggregatDto, input.getSkjæringstidspunktForBeregning());

        // Assert
        assertThat(resultat).isPresent();
        assertThat(resultat).hasValue(LocalDate.of(2019,1,8));
    }

    @Test
    public void skalSettesPåVentNårUtenInntektsmeldingFørFristFlereArbeidsforhold() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(4);
        var beregningAktivitetAggregatDto = lagAktivitetAggregat2ArbeidstakerAndeler();

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste.skalVentePåInnrapporteringAvInntektATFL(input, List.of(), dagensdato, beregningAktivitetAggregatDto, input.getSkjæringstidspunktForBeregning());

        // Assert
        assertThat(resultat).isPresent();
        assertThat(resultat).hasValue(LocalDate.of(2019,1,8));
    }

    @Test
    public void skalSettesPåVentNårHarInntektsmeldingFørFristForBareEttAvFlereArbeidsforhold() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(2);
        var beregningAktivitetAggregatDto = lagAktivitetAggregat2ArbeidstakerAndeler();

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste.skalVentePåInnrapporteringAvInntektATFL(input, List.of(arbeidsgiverA), dagensdato, beregningAktivitetAggregatDto, input.getSkjæringstidspunktForBeregning());

        // Assert
        assertThat(resultat).isPresent();
        assertThat(resultat).hasValue(LocalDate.of(2019,1,8));
    }

    @Test
    public void skalIkkeSettesPåVentNårAlleHarInntektsmeldingFørFristFlereArbeidsforhold() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(2);
        var beregningAktivitetAggregatDto = lagAktivitetAggregat2ArbeidstakerAndeler();

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste.skalVentePåInnrapporteringAvInntektATFL(input, List.of(arbeidsgiverA, arbeidsgiverB), dagensdato, beregningAktivitetAggregatDto, input.getSkjæringstidspunktForBeregning());

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skalIkkeSettesPåVentNårArbeidsforholdUtenInntektsmeldingErLagtTilAvSaksbehandler() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(2);
        var beregningAktivitetAggregatDto = lagBeregningaktiviteter1ArbeidstakerAndel();
        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste.skalVentePåInnrapporteringAvInntektATFL(input, List.of(arbeidsgiverA), dagensdato, beregningAktivitetAggregatDto, input.getSkjæringstidspunktForBeregning());
        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skalUtledeRiktigFrist() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(2);
        var beregningAktivitetAggregatDto = lagAktivitetAggregat2ArbeidstakerAndeler();

        // Act
        Optional<LocalDate> frist = AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste.skalVentePåInnrapporteringAvInntektATFL(input, List.of(arbeidsgiverA), dagensdato, beregningAktivitetAggregatDto, input.getSkjæringstidspunktForBeregning());

        // Assert
        assertThat(frist).isPresent();
        assertThat(frist).hasValue(SKJÆRINGSTIDSPUNKT.plusDays(7));
    }

    @Test
    public void skalUtledeRiktigFristKunFL() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(2);
        var beregningAktivitetAggregatDto = lagAktivitetAggregat1FrilansAndel();

        // Act
        Optional<LocalDate> frist = AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste.skalVentePåInnrapporteringAvInntektFL(input, dagensdato, beregningAktivitetAggregatDto, input.getSkjæringstidspunktForBeregning());

        // Assert
        assertThat(frist).isPresent();
        assertThat(frist).hasValue(SKJÆRINGSTIDSPUNKT.plusDays(7));
    }

    private BeregningAktivitetAggregatDto lagAktivitetAggregat1FrilansAndel() {
        return BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT)
                .leggTilAktivitet(BeregningAktivitetDto.builder()
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.FRILANS)
                        .medPeriode(Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10))).build()).build();
    }

    private BeregningAktivitetAggregatDto lagBeregningaktiviteter1ArbeidstakerAndel() {
        return BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT)
                .leggTilAktivitet(BeregningAktivitetDto.builder()
                        .medArbeidsgiver(arbeidsgiverA)
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                        .medPeriode(Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10))).build()).build();
    }


    private BeregningAktivitetAggregatDto lagBergningaktivitetAggregat1SNAndel() {
        return BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT)
                .leggTilAktivitet(BeregningAktivitetDto.builder()
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.NÆRING)
                        .medPeriode(Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10))).build()).build();
    }

    private BeregningAktivitetAggregatDto lagAktivitetAggregat2ArbeidstakerAndeler() {
        return BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT)
                .leggTilAktivitet(BeregningAktivitetDto.builder()
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                        .medPeriode(Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10)))
                        .medArbeidsgiver(arbeidsgiverA)
                        .build())
                .leggTilAktivitet(BeregningAktivitetDto.builder()
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                        .medPeriode(Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10)))
                        .medArbeidsgiver(arbeidsgiverB)
                        .build()).build();
    }

}
