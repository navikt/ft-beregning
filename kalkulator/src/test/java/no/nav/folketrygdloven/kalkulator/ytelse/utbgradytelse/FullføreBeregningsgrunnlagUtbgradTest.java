package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.RegelFullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.FullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.FullføreBeregningsgrunnlagUtbgrad;
import no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.folketrygdloven.utils.Tuple;

public class FullføreBeregningsgrunnlagUtbgradTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.APRIL, 10);
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final Beløp GRUNNBELØP = Beløp.fra(100_000);
    private static final Beløp SEKS_G = GRUNNBELØP.multipliser(6);
    private static final String ORGNR1 = "654";
    private static final UUID ORGNR1_ARB_ID1 = UUID.randomUUID();
    private static final UUID ORGNR1_ARB_ID2 = UUID.randomUUID();
    private static final String ORGNR2 = "765";
    private static final UUID ORGNR2_ARB_ID1 = UUID.randomUUID();
    private static final String ORGNR3 = "888";
    private static final UUID ORGNR3_ARB_ID1 = UUID.randomUUID();
    private static final LocalDate ARBEIDSPERIODE_FOM = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1);
    private static final LocalDate ARBEIDSPERIODE_TOM = SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(2);

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);
    private FullføreBeregningsgrunnlag tjeneste;
    private BeregningsgrunnlagDto beregningsgrunnlag;

    @BeforeEach
    public void setup() {
        tjeneste = new FullføreBeregningsgrunnlagUtbgrad();
        beregningsgrunnlag = lagBeregningsgrunnlagAT();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagAT() {
        var sg = SammenligningsgrunnlagPrStatusDto.builder()
                .medSammenligningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING)
                .medRapportertPrÅr(Beløp.ZERO)
                .medAvvikPromilleNy(BigDecimal.ZERO)
                .medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL)
                .build();
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
                .medGrunnbeløp(GRUNNBELØP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medHjemmel(Hjemmel.F_14_7_8_30))
                .leggTilSammenligningsgrunnlag(sg)
                .build();
        BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
                .build(bg);
        return bg;
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagATMedToPerioder() {
        var sg = SammenligningsgrunnlagPrStatusDto.builder()
                .medSammenligningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING)
                .medRapportertPrÅr(Beløp.ZERO)
                .medAvvikPromilleNy(BigDecimal.ZERO)
                .medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL)
                .build();
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
                .medGrunnbeløp(GRUNNBELØP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medHjemmel(Hjemmel.F_14_7_8_30))
                .leggTilSammenligningsgrunnlag(sg)
                .build();
        BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2))
                .build(bg);
        BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2).plusDays(1), null)
                .leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR)
                .build(bg);
        return bg;
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagAndel(BeregningsgrunnlagPeriodeDto periode, String orgnr, UUID arbRefId, int inntekt, int refusjon) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(lagBgAndelArbeidsforhold(Arbeidsgiver.virksomhet(orgnr), arbRefId, refusjon))
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1))
                .medBeregnetPrÅr(Beløp.fra(inntekt))
                .build(periode);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagFrilansAndel(BeregningsgrunnlagPeriodeDto periode, int inntekt) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medInntektskategori(Inntektskategori.FRILANSER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1))
                .medBeregnetPrÅr(Beløp.fra(inntekt))
                .build(periode);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagNæringAndel(BeregningsgrunnlagPeriodeDto periode, int inntekt) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1))
                .medBeregnetPrÅr(Beløp.fra(inntekt))
                .build(periode);
    }

    private BGAndelArbeidsforholdDto.Builder lagBgAndelArbeidsforhold(Arbeidsgiver arbeidsgiver, UUID arbRefId, int refusjon) {
        return BGAndelArbeidsforholdDto.builder()
                .medArbeidsperiodeFom(ARBEIDSPERIODE_FOM)
                .medArbeidsperiodeTom(ARBEIDSPERIODE_TOM)
                .medArbeidsforholdRef(arbRefId != null ? arbRefId.toString() : null)
                .medArbeidsgiver(arbeidsgiver)
                .medRefusjonskravPrÅr(Beløp.fra(refusjon), Utfall.GODKJENT);
    }

    @Test
    public void skal_teste_et_arbeidsforhold_med_refusjon_over_6G() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 612_000, 612_000);

        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgrad = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgrad = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(ORGNR1), ORGNR1_ARB_ID1, periodeMedUtbetalingsgrad);
        List<InntektsmeldingDto> inntektsmeldinger = List.of();

        SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of(tilretteleggingMedUtbelingsgrad));

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 612_000, 600_000, 2308, 600_000);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto andel = bgAndeler.get(0);
        assertAndel(andel, Beløp.ZERO, SEKS_G, SEKS_G);
        assertRegelsporing(resultat.getRegelsporinger());
    }


    @Test
    public void skal_teste_to_arbeidsforhold_beregningsgrunnlag_under_6G_full_refusjon_gradert() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 200_000, 200_000);
        lagAndel(periode, ORGNR2, ORGNR2_ARB_ID1, 300_000, 300_000);

        Map<String, UUID> arbeidsforhold = Map.of(ORGNR1, ORGNR1_ARB_ID1, ORGNR2, ORGNR2_ARB_ID1);

        Map<String, Tuple<LocalDate, Integer>> mapGradering = new HashMap<>();
        mapGradering.put(ORGNR1, new Tuple<>(SKJÆRINGSTIDSPUNKT_BEREGNING, 100));
        mapGradering.put(ORGNR2, new Tuple<>(SKJÆRINGSTIDSPUNKT_BEREGNING, 50));
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(lagUttakResultatMedGraderingFraDato(mapGradering, arbeidsforhold));

        List<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        BeregningsgrunnlagPeriodeDto resPeriode = bgPerioder.get(0);
        assertPeriode(resPeriode, 500_000, 350_000, 1346, 350_000);
        assertAndel(getAndel(resPeriode, ORGNR1), Beløp.ZERO, Beløp.fra(200_000), Beløp.fra(200_000));
        assertAndel(getAndel(resPeriode, ORGNR2), Beløp.ZERO, Beløp.fra(150_000), Beløp.fra(150_000));

        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void næring_med_beregningsgrunnlag_under_6G() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagNæringAndel(periode, 300_000);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgrad = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgrad = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, periodeMedUtbetalingsgrad);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of(tilretteleggingMedUtbelingsgrad));

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 300_000, 300_000, 1154, 300_000);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto andel = bgAndeler.get(0);
        assertAndel(andel, Beløp.fra(300_000), Beløp.ZERO, Beløp.fra(300_000));
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void næring_med_beregningsgrunnlag_under_6G_ikke_søkt_ytelse() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagNæringAndel(periode, 300_000);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of());

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 300_000, 0, 0, 0);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto andel = bgAndeler.get(0);
        assertAndel(andel, Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void næring_med_beregningsgrunnlag_under_6G_delvis_søkt_ytelse() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagNæringAndel(periode, 300_000);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgrad = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 50);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgrad = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, periodeMedUtbetalingsgrad);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of(tilretteleggingMedUtbelingsgrad));

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 300_000, 150_000, 577, 150_000);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto andel = bgAndeler.get(0);
        assertAndel(andel, Beløp.fra(150_000), Beløp.ZERO, Beløp.fra(150_000));
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void næring_med_beregningsgrunnlag_over_6G_delvis_søkt_ytelse() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagNæringAndel(periode, 800_000);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgrad = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 50);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgrad = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, periodeMedUtbetalingsgrad);
        SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of(tilretteleggingMedUtbelingsgrad));

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 800_000, 300_000, 1154, 300_000);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto andel = bgAndeler.get(0);
        assertAndel(andel, Beløp.fra(300_000), Beløp.ZERO, Beløp.fra(300_000));
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_to_arbeidsforhold_beregningsgrunnlag_over_6G_refusjon_under_6G_gradert() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 800_000, 200_000);
        lagAndel(periode, ORGNR2, ORGNR2_ARB_ID1, 200_000, 200_000);

        Map<String, UUID> arbeidsforhold = Map.of(ORGNR1, ORGNR1_ARB_ID1, ORGNR2, ORGNR2_ARB_ID1);

        Map<String, Tuple<LocalDate, Integer>> mapGradering = new HashMap<>();
        mapGradering.put(ORGNR1, new Tuple<>(SKJÆRINGSTIDSPUNKT_BEREGNING, 100));
        mapGradering.put(ORGNR2, new Tuple<>(SKJÆRINGSTIDSPUNKT_BEREGNING, 50));
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(lagUttakResultatMedGraderingFraDato(mapGradering, arbeidsforhold));

        List<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        BeregningsgrunnlagPeriodeDto resPeriode = bgPerioder.get(0);
        assertPeriode(resPeriode, 1_000_000, 540_000, 2077, 540_000);
        assertAndel(getAndel(resPeriode, ORGNR1), Beløp.fra(240_000), Beløp.fra(200_000), Beløp.fra(440_000));
        assertAndel(getAndel(resPeriode, ORGNR2), Beløp.ZERO, Beløp.fra(100_000), Beløp.fra(100_000));

        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_to_arbeidsforhold_to_perioder_med_refusjon_over_6G_gradert() {
        // Arrange
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlagATMedToPerioder();
        var periode1 = bg.getBeregningsgrunnlagPerioder().get(0);
        var periode2 = bg.getBeregningsgrunnlagPerioder().get(1);

        lagAndel(periode1, ORGNR1, ORGNR1_ARB_ID1, 600_000, 600_000);
        lagAndel(periode1, ORGNR2, ORGNR2_ARB_ID1, 600_000, 0);
        lagAndel(periode2, ORGNR1, ORGNR1_ARB_ID1, 600_000, 600_000);
        lagAndel(periode2, ORGNR2, ORGNR2_ARB_ID1, 600_000, 600_000);

        Map<String, UUID> arbeidsforhold = Map.of(ORGNR1, ORGNR1_ARB_ID1, ORGNR2, ORGNR2_ARB_ID1);

        Map<String, Tuple<LocalDate, Integer>> mapGradering = new HashMap<>();
        mapGradering.put(ORGNR1, new Tuple<>(SKJÆRINGSTIDSPUNKT_BEREGNING, 100));
        mapGradering.put(ORGNR2, new Tuple<>(SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2).plusDays(1), 50));
        SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(lagUttakResultatMedGraderingFraDato(mapGradering, arbeidsforhold));

        List<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        var resultat = act(bg, inntektsmeldinger, svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(2);
        BeregningsgrunnlagPeriodeDto resPeriode = bgPerioder.get(0);
        assertPeriode(resPeriode, 1_200_000, 300_000, 1154, 300_000);
        assertAndel(getAndel(resPeriode, ORGNR1), Beløp.ZERO, Beløp.fra(300_000), Beløp.fra(300_000));
        assertAndel(getAndel(resPeriode, ORGNR2), Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);

        BeregningsgrunnlagPeriodeDto resPeriode2 = bgPerioder.get(1);
        assertPeriode(resPeriode2, 1_200_000, 450_000, 1731, 450_000);
        assertAndel(getAndel(resPeriode2, ORGNR1), Beløp.ZERO, Beløp.fra(300_000), Beløp.fra(300_000));
        assertAndel(getAndel(resPeriode2, ORGNR2), Beløp.ZERO, Beløp.fra(150_000), Beløp.fra(150_000));

        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_to_arbeidsforhold_to_perioder_med_refusjon_over_6G_gradert_ulik_refusjon() {
        // Arrange
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlagATMedToPerioder();
        var periode1 = bg.getBeregningsgrunnlagPerioder().get(0);
        var periode2 = bg.getBeregningsgrunnlagPerioder().get(1);

        lagAndel(periode1, ORGNR1, ORGNR1_ARB_ID1, 600_000, 600_000);
        lagAndel(periode1, ORGNR2, ORGNR2_ARB_ID1, 600_000, 0);
        lagAndel(periode2, ORGNR1, ORGNR1_ARB_ID1, 600_000, 600_000);
        lagAndel(periode2, ORGNR2, ORGNR2_ARB_ID1, 600_000, 100_000);

        Map<String, UUID> arbeidsforhold = Map.of(ORGNR1, ORGNR1_ARB_ID1, ORGNR2, ORGNR2_ARB_ID1);

        Map<String, Tuple<LocalDate, Integer>> mapGradering = new HashMap<>();
        mapGradering.put(ORGNR1, new Tuple<>(SKJÆRINGSTIDSPUNKT_BEREGNING, 100));
        mapGradering.put(ORGNR2, new Tuple<>(SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2).plusDays(1), 50));
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(lagUttakResultatMedGraderingFraDato(mapGradering, arbeidsforhold));

        List<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        var resultat = act(bg, inntektsmeldinger, svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(2);
        BeregningsgrunnlagPeriodeDto resPeriode = bgPerioder.get(0);
        assertPeriode(resPeriode, 1_200_000, 300_000, 1154, 300_000);
        assertAndel(getAndel(resPeriode, ORGNR1), Beløp.ZERO, Beløp.fra(300_000), Beløp.fra(300_000));
        assertAndel(getAndel(resPeriode, ORGNR2), Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);

        BeregningsgrunnlagPeriodeDto resPeriode2 = bgPerioder.get(1);
        assertPeriode(resPeriode2, 1_200_000, 450_000, 1730, 450_000);
        assertAndel(getAndel(resPeriode2, ORGNR1), Beløp.ZERO, Beløp.fra(400_000), Beløp.fra(400_000));
        assertAndel(getAndel(resPeriode2, ORGNR2), Beløp.ZERO, Beløp.fra(50_000), Beløp.fra(50_000));

        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_to_arbeidsforhold_to_perioder_med_refusjon_over_6G() {
        // Arrange
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlagATMedToPerioder();
        var periode1 = bg.getBeregningsgrunnlagPerioder().get(0);
        var periode2 = bg.getBeregningsgrunnlagPerioder().get(1);

        lagAndel(periode1, ORGNR1, ORGNR1_ARB_ID1, 9 * GRUNNBELØP.intValue(), 9 * GRUNNBELØP.intValue());
        lagAndel(periode1, ORGNR2, ORGNR2_ARB_ID1, 3 * GRUNNBELØP.intValue(), 0);
        lagAndel(periode2, ORGNR1, ORGNR1_ARB_ID1, 9 * GRUNNBELØP.intValue(), 9 * GRUNNBELØP.intValue());
        lagAndel(periode2, ORGNR2, ORGNR2_ARB_ID1, 3 * GRUNNBELØP.intValue(), 0);

        Map<String, UUID> arbeidsforhold = Map.of(ORGNR1, ORGNR1_ARB_ID1, ORGNR2, ORGNR2_ARB_ID1);

        Map<String, LocalDate> map = new HashMap<>();
        map.put(ORGNR1, SKJÆRINGSTIDSPUNKT_BEREGNING);
        map.put(ORGNR2, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2).plusDays(1));

        SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(lagUttakResultat(map, arbeidsforhold));
        List<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        var resultat = act(bg, inntektsmeldinger, svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(2);
        BeregningsgrunnlagPeriodeDto resPeriode = bgPerioder.get(0);
        assertPeriode(resPeriode, 12 * GRUNNBELØP.intValue(), (int) (4.5 * GRUNNBELØP.intValue()), 1731, (int) (4.5 * GRUNNBELØP.intValue()));
        assertAndel(getAndel(resPeriode, ORGNR1), Beløp.ZERO, GRUNNBELØP.multipliser(BigDecimal.valueOf(4.5)), GRUNNBELØP.multipliser(BigDecimal.valueOf(4.5)));
        assertAndel(getAndel(resPeriode, ORGNR2), Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);

        BeregningsgrunnlagPeriodeDto resPeriode2 = bgPerioder.get(1);
        assertPeriode(resPeriode2, 12 * GRUNNBELØP.intValue(), 6 * GRUNNBELØP.intValue(), 2308, 6 * GRUNNBELØP.intValue());
        assertAndel(getAndel(resPeriode2, ORGNR1), Beløp.ZERO, SEKS_G, SEKS_G);
        assertAndel(getAndel(resPeriode2, ORGNR2), Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);

        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_to_arbeidsforhold_med_refusjon_over_6G() {
        // Arrange
        var periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 600_000, 560_000);
        lagAndel(periode, ORGNR2, ORGNR2_ARB_ID1, 750_000, 200_000);
        lagAndel(periode, ORGNR3, ORGNR3_ARB_ID1, 250_000, 0);

        Map<String, UUID> arbeidsforhold = Map.of(ORGNR1, ORGNR1_ARB_ID1, ORGNR2, ORGNR2_ARB_ID1);

        Map<String, LocalDate> map = new HashMap<>();
        map.put(ORGNR1, SKJÆRINGSTIDSPUNKT_BEREGNING);
        map.put(ORGNR2, SKJÆRINGSTIDSPUNKT_BEREGNING);
        SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(lagUttakResultat(map, arbeidsforhold));
        List<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        BeregningsgrunnlagPeriodeDto resPeriode = bgPerioder.get(0);
        assertPeriode(resPeriode, 1_600_000, 506_250, 1947, 506_250);
        assertAndel(getAndel(resPeriode, ORGNR1), Beløp.ZERO, Beløp.fra(306_250), null);
        assertAndel(getAndel(resPeriode, ORGNR2), Beløp.ZERO, Beløp.fra(200_000), null);
        assertAndel(getAndel(resPeriode, ORGNR3), Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_to_arbeidsforhold_med_refusjon_under_6G() {
        // Arrange
        var periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 600_000, 150_000);
        lagAndel(periode, ORGNR2, ORGNR2_ARB_ID1, 750_000, 200_000);
        lagAndel(periode, ORGNR3, ORGNR3_ARB_ID1, 250_000, 0);

        Map<String, UUID> arbeidsforhold = Map.of(ORGNR1, ORGNR1_ARB_ID1, ORGNR2, ORGNR2_ARB_ID1, ORGNR3, ORGNR3_ARB_ID1);

        Map<String, LocalDate> map = new HashMap<>();
        map.put(ORGNR1, SKJÆRINGSTIDSPUNKT_BEREGNING);
        map.put(ORGNR2, SKJÆRINGSTIDSPUNKT_BEREGNING);
        List<InntektsmeldingDto> inntektsmeldinger = List.of();

        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(lagUttakResultat(map, arbeidsforhold));

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        BeregningsgrunnlagPeriodeDto resPeriode = bgPerioder.get(0);
        assertPeriode(resPeriode, 1_600_000, 506_250, 1947, 506_250);
        assertAndel(getAndel(resPeriode, ORGNR1), Beløp.fra(75_000), Beløp.fra(150_000), Beløp.fra(225_000));
        assertAndel(getAndel(resPeriode, ORGNR2), Beløp.fra(81_250), Beløp.fra(200_000), Beløp.fra(281_250));
        assertAndel(getAndel(resPeriode, ORGNR3), Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_to_arbeidsforhold_med_refusjon_og_inntekt_under_6G() {
        // Arrange
        var periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 150_000, 100_000);
        lagAndel(periode, ORGNR2, ORGNR2_ARB_ID1, 200_000, 200_000);
        lagAndel(periode, ORGNR3, ORGNR3_ARB_ID1, 100_000, 0);

        Map<String, UUID> arbeidsforhold = Map.of(ORGNR1, ORGNR1_ARB_ID1, ORGNR2, ORGNR2_ARB_ID1, ORGNR3, ORGNR3_ARB_ID1);

        Map<String, LocalDate> map = new HashMap<>();
        map.put(ORGNR1, SKJÆRINGSTIDSPUNKT_BEREGNING);
        map.put(ORGNR2, SKJÆRINGSTIDSPUNKT_BEREGNING);
        List<InntektsmeldingDto> inntektsmeldinger = List.of();

        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(lagUttakResultat(map, arbeidsforhold));

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        BeregningsgrunnlagPeriodeDto resPeriode = bgPerioder.get(0);
        assertPeriode(resPeriode, 450_000, 350_000, 1346, 350_000);
        assertAndel(getAndel(resPeriode, ORGNR1), Beløp.fra(50_000), Beløp.fra(100_000), Beløp.fra(150_000));
        assertAndel(getAndel(resPeriode, ORGNR2), Beløp.ZERO, Beløp.fra(200_000), Beløp.fra(200_000));
        assertAndel(getAndel(resPeriode, ORGNR3), Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_to_arbeidsforhold_med_refusjon_lik_6G_og_inntekt_over_6G_gradert() {
        // Arrange
        var periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 600_000, 600_000);
        lagAndel(periode, ORGNR2, ORGNR2_ARB_ID1, 600_000, 0);

        Map<String, UUID> arbeidsforhold = Map.of(ORGNR1, ORGNR1_ARB_ID1, ORGNR2, ORGNR2_ARB_ID1);

        Map<String, Integer> map = new HashMap<>();
        map.put(ORGNR1, 100);
        map.put(ORGNR2, 50);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(lagUttakResultatMedGraderingFraSkjæringstidspunkt(map, arbeidsforhold));
        List<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        BeregningsgrunnlagPeriodeDto resPeriode = bgPerioder.get(0);
        assertPeriode(resPeriode, 1_200_000, 450_000, 1731, 450_000);
        assertAndel(getAndel(resPeriode, ORGNR1), Beløp.ZERO, Beløp.fra(450_000), Beløp.fra(450_000));
        assertAndel(getAndel(resPeriode, ORGNR2), Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_to_arbeidsforhold_med_refusjon_lik_6G_for_begge_og_inntekt_over_6G_gradert() {
        // Arrange
        var periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 600_000, 600_000);
        lagAndel(periode, ORGNR2, ORGNR2_ARB_ID1, 600_000, 600_000);

        Map<String, UUID> arbeidsforhold = Map.of(ORGNR1, ORGNR1_ARB_ID1, ORGNR2, ORGNR2_ARB_ID1);

        Map<String, Integer> map = new HashMap<>();
        map.put(ORGNR1, 100);
        map.put(ORGNR2, 50);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(lagUttakResultatMedGraderingFraSkjæringstidspunkt(map, arbeidsforhold));
        List<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        BeregningsgrunnlagPeriodeDto resPeriode = bgPerioder.get(0);
        assertPeriode(resPeriode, 1_200_000, 450_000, 1731, 450_000);
        assertAndel(getAndel(resPeriode, ORGNR1), Beløp.ZERO, Beløp.fra(300_000), Beløp.fra(300_000));
        assertAndel(getAndel(resPeriode, ORGNR2), Beløp.ZERO, Beløp.fra(150_000), Beløp.fra(150_000));
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_to_arbeidsforhold_med_refusjon_lik_6G_og_1G_og_inntekt_over_6G_gradert() {
        // Arrange
        var periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 600_000, 600_000);
        lagAndel(periode, ORGNR2, ORGNR2_ARB_ID1, 600_000, 100_000);

        Map<String, UUID> arbeidsforhold = Map.of(ORGNR1, ORGNR1_ARB_ID1, ORGNR2, ORGNR2_ARB_ID1);

        Map<String, Integer> map = new HashMap<>();
        map.put(ORGNR1, 100);
        map.put(ORGNR2, 50);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(lagUttakResultatMedGraderingFraSkjæringstidspunkt(map, arbeidsforhold));
        List<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        BeregningsgrunnlagPeriodeDto resPeriode = bgPerioder.get(0);
        assertPeriode(resPeriode, 1_200_000, 450_000, 1730, 450_000);
        assertAndel(getAndel(resPeriode, ORGNR1), Beløp.ZERO, Beløp.fra(400_000), Beløp.fra(400_000));
        assertAndel(getAndel(resPeriode, ORGNR2), Beløp.ZERO, Beløp.fra(50_000), Beløp.fra(50_000));
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test //PFP-8177
    public void skal_teste_arbeidsforhold_med_refusjon_uten_tilrettelegging_og_tilrettelegging_uten_refusjon() {
        // Arrange
        var periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 300_000, 150_000);
        lagAndel(periode, ORGNR2, ORGNR2_ARB_ID1, 210_000, 210_000);
        lagAndel(periode, ORGNR3, ORGNR3_ARB_ID1, 240_000, 0);

        Map<String, UUID> arbeidsforhold = Map.of(ORGNR1, ORGNR1_ARB_ID1, ORGNR2, ORGNR2_ARB_ID1, ORGNR3, ORGNR3_ARB_ID1);

        Map<String, LocalDate> map = new HashMap<>();
        map.put(ORGNR1, SKJÆRINGSTIDSPUNKT_BEREGNING);
        map.put(ORGNR3, SKJÆRINGSTIDSPUNKT_BEREGNING);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(lagUttakResultat(map, arbeidsforhold));
        List<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        BeregningsgrunnlagPeriodeDto resPeriode = bgPerioder.get(0);
        assertPeriode(resPeriode, 750000, 432_000, 1661, 432_000);
        assertAndel(getAndel(resPeriode, ORGNR1), Beløp.fra(90_000), Beløp.fra(150_000), Beløp.fra(240_000));
        assertAndel(getAndel(resPeriode, ORGNR2), Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);
        assertAndel(getAndel(resPeriode, ORGNR3), Beløp.fra(192_000), Beløp.ZERO, Beløp.fra(192_000));
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_at_arbeidsforhold_uten_match_i_tilrettelegging_ikke_feiler() {
        // Arrange
        var periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 150_000, 100_000);

        Map<String, UUID> arbeidsforhold = Map.of(ORGNR1, ORGNR1_ARB_ID1);

        Map<String, LocalDate> map = new HashMap<>();
        map.put(ORGNR2, SKJÆRINGSTIDSPUNKT_BEREGNING);
        List<InntektsmeldingDto> inntektsmeldinger = List.of();

        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(lagUttakResultat(map, arbeidsforhold));

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        BeregningsgrunnlagPeriodeDto resPeriode = bgPerioder.get(0);
        assertThat(resPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertAndel(getAndel(resPeriode, ORGNR1), Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_arbeidstaker_med_delvis_søkt_ytelse() {
        // Arrange
        var periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 300_000, 0);

        var uttakResultat = lagUttakResultat(45);
        List<InntektsmeldingDto> inntektsmeldinger = List.of();

        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(uttakResultat);

        // Act
        var resultat = act(beregningsgrunnlag, inntektsmeldinger, svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 300_000, 135_000, 519, 135_000);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto andel = bgAndeler.get(0);
        assertAndel(andel, Beløp.fra(135_000), Beløp.ZERO, Beløp.fra(135_000));
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_frilans_med_beregningsgrunnlag_under_6G() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagFrilansAndel(periode, 300_000);

        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgrad = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgrad = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.FRILANS, null, null, periodeMedUtbetalingsgrad);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of(tilretteleggingMedUtbelingsgrad));


        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 300_000, 300_000, 1154, 300_000);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto andel = bgAndeler.get(0);
        assertAndel(andel, Beløp.fra(300_000), Beløp.ZERO, Beløp.fra(300_000));
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_frilans_med_beregningsgrunnlag_under_6G_delvis_søkt_ytelse() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagFrilansAndel(periode, 300_000);

        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgrad = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 50);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgrad = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.FRILANS, null, null, periodeMedUtbetalingsgrad);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of(tilretteleggingMedUtbelingsgrad));

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 300_000, 150_000, 577, 150_000);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto andel = bgAndeler.get(0);
        assertAndel(andel, Beløp.fra(150_000), Beløp.ZERO, Beløp.fra(150_000));
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_frilans_med_beregningsgrunnlag_over_6G_delvis_søkt_ytelse() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagFrilansAndel(periode, 800_000);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgrad = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 50);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgrad = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.FRILANS, null, null, periodeMedUtbetalingsgrad);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of(tilretteleggingMedUtbelingsgrad));

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 800_000, 300_000, 1154, 300_000);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto andel = bgAndeler.get(0);
        assertAndel(andel, Beløp.fra(300_000), Beløp.ZERO, Beløp.fra(300_000));
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_frilans_med_beregningsgrunnlag_under_6G_ikkje_søkt_ytelse() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagFrilansAndel(periode, 300_000);

        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of());

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 300_000, 0, 0, 0);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto andel = bgAndeler.get(0);
        assertAndel(andel, Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_frilans_og_arbeid_med_beregningsgrunnlag_under_6G_søkt_ytelse_for_alle() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagFrilansAndel(periode, 200_000);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 200_000, 0);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradFrilans = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradFrilans = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.FRILANS, null, null, periodeMedUtbetalingsgradFrilans);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradArbeid = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradArbeid = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(ORGNR1), ORGNR1_ARB_ID1, periodeMedUtbetalingsgradArbeid);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of(tilretteleggingMedUtbelingsgradFrilans, tilretteleggingMedUtbelingsgradArbeid));

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 400_000, 400_000, 1538, 400_000);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto frilans = bgAndeler.get(0);
        assertAndel(frilans, Beløp.fra(200_000), Beløp.ZERO, Beløp.fra(200_000));
        BeregningsgrunnlagPrStatusOgAndelDto arbeid = bgAndeler.get(1);
        assertAndel(arbeid, Beløp.fra(200_000), Beløp.ZERO, Beløp.fra(200_000));
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_frilans_og_arbeid_med_beregningsgrunnlag_over_6G_for_arbeid_søkt_ytelse_for_alle() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagFrilansAndel(periode, 200_000);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 800_000, 0);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradFrilans = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradFrilans = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.FRILANS, null, null, periodeMedUtbetalingsgradFrilans);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradArbeid = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradArbeid = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(ORGNR1), ORGNR1_ARB_ID1, periodeMedUtbetalingsgradArbeid);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of(tilretteleggingMedUtbelingsgradFrilans, tilretteleggingMedUtbelingsgradArbeid));

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 1_000_000, 600_000, 2308, 600_000);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto frilans = bgAndeler.get(0);
        assertAndel(frilans, Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);
        BeregningsgrunnlagPrStatusOgAndelDto arbeid = bgAndeler.get(1);
        assertAndel(arbeid, Beløp.fra(600_000), Beløp.ZERO, Beløp.fra(600_000));
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_frilans_og_arbeid_med_beregningsgrunnlag_over_6G_for_arbeid_søkt_ytelse_for_frilans() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagFrilansAndel(periode, 200_000);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 800_000, 0);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradFrilans = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradFrilans = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.FRILANS, null, null, periodeMedUtbetalingsgradFrilans);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of(tilretteleggingMedUtbelingsgradFrilans));

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 1_000_000, 0, 0, 0);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto frilans = bgAndeler.get(0);
        assertAndel(frilans, Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);
        BeregningsgrunnlagPrStatusOgAndelDto arbeid = bgAndeler.get(1);
        assertAndel(arbeid, Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_frilans_og_arbeid_med_beregningsgrunnlag_over_6G_til_sammen_søkt_delvis_ytelse_for_frilans() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagFrilansAndel(periode, 500_000);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 500_000, 0);

        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradFrilans = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 50);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradFrilans = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.FRILANS, null, null, periodeMedUtbetalingsgradFrilans);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of(tilretteleggingMedUtbelingsgradFrilans));

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 1_000_000, 50_000, 192, 50_000);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto frilans = bgAndeler.get(0);
        assertAndel(frilans, Beløp.fra(50_000), Beløp.ZERO, Beløp.fra(50_000));
        BeregningsgrunnlagPrStatusOgAndelDto arbeid = bgAndeler.get(1);
        assertAndel(arbeid, Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_frilans_og_to_arbeid_med_beregningsgrunnlag_over_6G_til_sammen_søkt_ytelse_for_alle_med_refusjonkrav_som_overstiger_total_avkortet_for_arbeid() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagFrilansAndel(periode, 500_000);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 500_000, 500_000);
        lagAndel(periode, ORGNR2, ORGNR2_ARB_ID1, 500_000, 200_000);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradFrilans = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradFrilans = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.FRILANS, null, null, periodeMedUtbetalingsgradFrilans);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradArbeid = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradArbeid = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(ORGNR1), ORGNR1_ARB_ID1, periodeMedUtbetalingsgradArbeid);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradArbeid2 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradArbeid2 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(ORGNR2), ORGNR2_ARB_ID1, periodeMedUtbetalingsgradArbeid2);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of(tilretteleggingMedUtbelingsgradFrilans, tilretteleggingMedUtbelingsgradArbeid, tilretteleggingMedUtbelingsgradArbeid2));

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 1_500_000, 600_000, 2307, 600_000);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto frilans = bgAndeler.get(0);
        assertAndel(frilans, Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);
        BeregningsgrunnlagPrStatusOgAndelDto arbeid = bgAndeler.get(1);
        assertAndel(arbeid, Beløp.ZERO, Beløp.fra(400_000), Beløp.fra(400_000));
        BeregningsgrunnlagPrStatusOgAndelDto arbeid2 = bgAndeler.get(2);
        assertAndel(arbeid2, Beløp.ZERO, Beløp.fra(200_000), Beløp.fra(200_000));
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_frilans_og_to_arbeid_med_beregningsgrunnlag_over_6G_til_sammen_søkt_ytelse_for_alle_med_refusjonkrav_som_overstiger_total_avkortet_for_arbeid_med_fordeling_av_refusjonskrav() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagFrilansAndel(periode, 500_000);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 500_000, 500_000);
        lagAndel(periode, ORGNR2, ORGNR2_ARB_ID1, 500_000, 300_000);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradFrilans = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradFrilans = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.FRILANS, null, null, periodeMedUtbetalingsgradFrilans);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradArbeid = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradArbeid = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(ORGNR1), ORGNR1_ARB_ID1, periodeMedUtbetalingsgradArbeid);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradArbeid2 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradArbeid2 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(ORGNR2), ORGNR2_ARB_ID1, periodeMedUtbetalingsgradArbeid2);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of(tilretteleggingMedUtbelingsgradFrilans, tilretteleggingMedUtbelingsgradArbeid, tilretteleggingMedUtbelingsgradArbeid2));

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 1_500_000, 600_000, 2308, 600_000);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto frilans = bgAndeler.get(0);
        assertAndel(frilans, Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);
        BeregningsgrunnlagPrStatusOgAndelDto arbeid = bgAndeler.get(1);
        assertAndel(arbeid, Beløp.ZERO, Beløp.fra(300_000), Beløp.fra(300_000));
        BeregningsgrunnlagPrStatusOgAndelDto arbeid2 = bgAndeler.get(2);
        assertAndel(arbeid2, Beløp.ZERO, Beløp.fra(300_000), Beløp.fra(300_000));
        assertRegelsporing(resultat.getRegelsporinger());
    }

    @Test
    public void skal_teste_to_arbeidsforhold_hos_en_arbeidsgiver_med_beregningsgrunnlag_over_6G() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID1, 400_000, 300_000);
        lagAndel(periode, ORGNR1, ORGNR1_ARB_ID2, 300_000, 300_000);

        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradArbeid = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradArbeid = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(ORGNR1), ORGNR1_ARB_ID1, periodeMedUtbetalingsgradArbeid);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradArbeid2 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradArbeid2 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(ORGNR1), ORGNR1_ARB_ID2, periodeMedUtbetalingsgradArbeid2);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of(tilretteleggingMedUtbelingsgradArbeid, tilretteleggingMedUtbelingsgradArbeid2));

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 700_000, 600_000, 2308, 600_000);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto arbeid = bgAndeler.get(0);
        assertAndel(arbeid, Beløp.ZERO, Beløp.fra(300_000), Beløp.fra(300_000));
        assertThat(arbeid.getArbeidsgiver()).isPresent();
        assertThat(arbeid.getArbeidsgiver().get().getOrgnr()).isEqualTo(ORGNR1);
        assertThat(arbeid.getArbeidsforholdRef().get().getUUIDReferanse()).isEqualTo(ORGNR1_ARB_ID1);
        BeregningsgrunnlagPrStatusOgAndelDto arbeid2 = bgAndeler.get(1);
        assertAndel(arbeid2, Beløp.ZERO, Beløp.fra(300_000), Beløp.fra(300_000));
        assertThat(arbeid2.getArbeidsgiver()).isPresent();
        assertThat(arbeid2.getArbeidsgiver().get().getOrgnr()).isEqualTo(ORGNR1);
        assertThat(arbeid2.getArbeidsforholdRef().get().getUUIDReferanse()).isEqualTo(ORGNR1_ARB_ID2);

        assertRegelsporing(resultat.getRegelsporinger());
    }

    private BeregningsgrunnlagPrStatusOgAndelDto getAndel(BeregningsgrunnlagPeriodeDto periode, String orgnr) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(andel -> andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver).filter(arb -> arb.getIdentifikator().equals(orgnr)).isPresent())
                .findFirst()
                .orElse(null);
    }

    private void assertAndel(BeregningsgrunnlagPrStatusOgAndelDto andel, Beløp bruker, Beløp refusjon, Beløp avkortet) {
        Function<Beløp, Long> calcDagsats = a -> a.verdi().divide(KonfigTjeneste.getYtelsesdagerIÅr(), 0, RoundingMode.HALF_UP).longValue();
        var total = bruker.adder(refusjon);
        assertThat(andel.getRedusertBrukersAndelPrÅr()).isEqualByComparingTo(bruker);
        assertThat(andel.getRedusertRefusjonPrÅr()).isEqualByComparingTo(refusjon);
        assertThat(andel.getDagsatsBruker()).isEqualTo(calcDagsats.apply(bruker));
        assertThat(andel.getDagsatsArbeidsgiver()).isEqualTo(calcDagsats.apply(refusjon));
        assertThat(BigDecimal.valueOf(andel.getDagsats())).isCloseTo(BigDecimal.valueOf(calcDagsats.apply(total)), Offset.offset(BigDecimal.ONE));
        if (avkortet == null) {
            assertThat(andel.getAvkortetPrÅr()).isEqualByComparingTo(total);
        } else {
            assertThat(andel.getAvkortetPrÅr()).isEqualByComparingTo(avkortet);
        }
        assertThat(andel.getRedusertPrÅr()).isEqualByComparingTo(total);
    }

    @Test
    public void skal_teste_frilans_og_to_arbeid_med_beregningsgrunnlag_over_6G_til_sammen_søkt_ytelse_for_alle_med_refusjonkrav_som_overstiger_total_avkortet_for_arbeid_uten_arbeidsforhold_id() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        lagFrilansAndel(periode, 500_000);
        lagAndel(periode, ORGNR1, null, 500_000, 500_000);
        lagAndel(periode, ORGNR2, null, 500_000, 200_000);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradFrilans = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradFrilans = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.FRILANS, null, null, periodeMedUtbetalingsgradFrilans);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradArbeid = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradArbeid = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(ORGNR1), null, periodeMedUtbetalingsgradArbeid);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradArbeid2 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, 100);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgradArbeid2 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(ORGNR2), null, periodeMedUtbetalingsgradArbeid2);
        var svangerskapspengerGrunnlag = lagSvangerskapspengerGrunnlag(List.of(tilretteleggingMedUtbelingsgradFrilans, tilretteleggingMedUtbelingsgradArbeid, tilretteleggingMedUtbelingsgradArbeid2));

        // Act
        var resultat = act(beregningsgrunnlag, List.of(), svangerskapspengerGrunnlag);

        // Assert
        var bgPerioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(bgPerioder).hasSize(1);
        assertPeriode(bgPerioder.get(0), 1_500_000, 600_000, 2307, 600_000);
        var bgAndeler = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        BeregningsgrunnlagPrStatusOgAndelDto frilans = bgAndeler.get(0);
        assertAndel(frilans, Beløp.ZERO, Beløp.ZERO, Beløp.ZERO);
        BeregningsgrunnlagPrStatusOgAndelDto arbeid = bgAndeler.get(1);
        assertAndel(arbeid, Beløp.ZERO, Beløp.fra(400_000), Beløp.fra(400_000));
        BeregningsgrunnlagPrStatusOgAndelDto arbeid2 = bgAndeler.get(2);
        assertAndel(arbeid2, Beløp.ZERO, Beløp.fra(200_000), Beløp.fra(200_000));
        assertRegelsporing(resultat.getRegelsporinger());
    }


    private void assertPeriode(BeregningsgrunnlagPeriodeDto resPeriode, int brutto, int avkortet, int dagsats, int redusert) {
        assertThat(resPeriode.getBruttoPrÅr()).isEqualByComparingTo(Beløp.fra(brutto));
        assertThat(resPeriode.getAvkortetPrÅr()).isEqualByComparingTo(Beløp.fra(avkortet));
        assertThat(resPeriode.getDagsats()).isEqualTo(dagsats);
        assertThat(resPeriode.getRedusertPrÅr()).isEqualByComparingTo(Beløp.fra(redusert));
    }


    private void assertRegelsporing(Optional<RegelSporingAggregat> regelSporingAggregat) {

        RegelSporingPeriode finnGrenseverdi = regelSporingAggregat.get().regelsporingPerioder().stream()
                .filter(rs -> rs.regelType().equals(BeregningsgrunnlagPeriodeRegelType.FINN_GRENSEVERDI))
                .findFirst().get();

        RegelSporingPeriode fastsett = regelSporingAggregat.get().regelsporingPerioder().stream()
                .filter(rs -> rs.regelType().equals(BeregningsgrunnlagPeriodeRegelType.FASTSETT))
                .findFirst().get();

        assertThat(finnGrenseverdi.regelInput()).isNotNull();
        assertThat(finnGrenseverdi.regelEvaluering()).isNotNull();
        assertThat(fastsett.regelInput()).isNotNull();
        assertThat(fastsett.regelEvaluering()).isNotNull();
        assertThat(fastsett.regelEvaluering()).contains(RegelFullføreBeregningsgrunnlag.ID);
    }

    private BeregningsgrunnlagRegelResultat act(BeregningsgrunnlagDto beregningsgrunnlag,
                                                Collection<InntektsmeldingDto> inntektsmeldinger, SvangerskapspengerGrunnlag svangerskapspengerGrunnlag) {
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagDtoBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .medRegisterAktiviteter(BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING, OpptjeningAktivitetType.ARBEID));
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(inntektsmeldinger).build();
        var input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(koblingReferanse, grunnlagDtoBuilder, BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag, svangerskapspengerGrunnlag);
        return tjeneste.fullføreBeregningsgrunnlag(input);
    }

    private List<UtbetalingsgradPrAktivitetDto> lagUttakResultat(Integer utbetalingsgrad) {
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgrad = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, utbetalingsgrad);
        UtbetalingsgradPrAktivitetDto tilretteleggingMedUtbelingsgrad = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(ORGNR1), ORGNR1_ARB_ID1, periodeMedUtbetalingsgrad);

        return List.of(tilretteleggingMedUtbelingsgrad);
    }

    private List<UtbetalingsgradPrAktivitetDto> lagUttakResultatMedGraderingFraDato(Map<String, Tuple<LocalDate, Integer>> orgnrUtbetalingsgradMap, Map<String, UUID> arbeidsforhold) {
        return orgnrUtbetalingsgradMap.entrySet().stream().map(entry -> {
            PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgrad = lagPeriodeMedUtbetaling(entry.getValue().getElement1(), entry.getValue().getElement2());
            return lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(entry.getKey()), arbeidsforhold.get(entry.getKey()), periodeMedUtbetalingsgrad);
        }).collect(Collectors.toList());
    }

    private List<UtbetalingsgradPrAktivitetDto> lagUttakResultat(Map<String, LocalDate> orgnrUtbetalingsgradMap, Map<String, UUID> arbeidsforhold) {
        return orgnrUtbetalingsgradMap.entrySet().stream().map(entry -> {
            PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgrad = lagPeriodeMedUtbetaling(entry.getValue(), 100);
            return lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(entry.getKey()), arbeidsforhold.get(entry.getKey()), periodeMedUtbetalingsgrad);
        }).collect(Collectors.toList());
    }

    private List<UtbetalingsgradPrAktivitetDto> lagUttakResultatMedGraderingFraSkjæringstidspunkt(Map<String, Integer> orgnrUtbetalingsgradMap, Map<String, UUID> arbeidsforhold) {
        return orgnrUtbetalingsgradMap.entrySet().stream().map(entry -> {
            PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgrad = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT_BEREGNING, entry.getValue());
            return lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(entry.getKey()), arbeidsforhold.get(entry.getKey()), periodeMedUtbetalingsgrad);
        }).collect(Collectors.toList());
    }

    private UtbetalingsgradPrAktivitetDto lagTilretteleggingMedUtbelingsgrad(UttakArbeidType uttakArbeidType, Arbeidsgiver arbeidsgiver, UUID arbRefUuid, PeriodeMedUtbetalingsgradDto... perioder) {
        var tilretteleggingArbeidsforhold = new AktivitetDto(arbeidsgiver, InternArbeidsforholdRefDto.ref(arbRefUuid), uttakArbeidType);
        return new UtbetalingsgradPrAktivitetDto(tilretteleggingArbeidsforhold, List.of(perioder));
    }

    private PeriodeMedUtbetalingsgradDto lagPeriodeMedUtbetaling(LocalDate skjæringstidspunkt, Integer utbetalingsgrad) {
        return new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(skjæringstidspunkt, skjæringstidspunkt.plusMonths(3)), Utbetalingsgrad.valueOf(utbetalingsgrad));
    }

    private SvangerskapspengerGrunnlag lagSvangerskapspengerGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad) {
        SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = new SvangerskapspengerGrunnlag(
                tilretteleggingMedUtbelingsgrad
        );
        return svangerskapspengerGrunnlag;
    }

}
