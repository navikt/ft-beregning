package no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

@ExtendWith(MockitoExtension.class)
public class FordelPerioderTjenesteSVPTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, Month.JANUARY, 4);
    private static final Skjæringstidspunkt skjæringstidspunkt = Skjæringstidspunkt.builder()
        .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT)
        .medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT)
        .build();
    private static final Beløp GRUNNBELØP = Beløp.fra(90000L);
    private static final String ORG_NUMMER = "45345";
    private static final String ORG_NUMMER_2 = "15345";

    private final BeregningAktivitetAggregatDto beregningAktivitetAggregat = Mockito.mock(BeregningAktivitetAggregatDto.class);
    private List<BeregningAktivitetDto> aktiviteter = new ArrayList<>();

    private FordelPerioderTjeneste tjeneste;

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT, FagsakYtelseType.SVANGERSKAPSPENGER);


    @BeforeEach
    public void setUp() {
        this.tjeneste = new FordelPerioderTjeneste();

    }

    private InntektArbeidYtelseAggregatBuilder leggTilYrkesaktiviteterOgBeregningAktiviteter(List<String> orgnrs) {
        Intervall arbeidsperiode1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        for (String orgnr : orgnrs) {
            Arbeidsgiver arbeidsgiver = leggTilYrkesaktivitet(arbeidsperiode1, aktørArbeidBuilder, orgnr);
            fjernOgLeggTilNyBeregningAktivitet(arbeidsperiode1.getFomDato(), arbeidsperiode1.getTomDato(), arbeidsgiver, InternArbeidsforholdRefDto.nullRef());
        }

        return InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
            .leggTilAktørArbeid(aktørArbeidBuilder);
    }

    private Arbeidsgiver leggTilYrkesaktivitet(Intervall arbeidsperiode, InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder,
                                               String orgnr) {
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        AktivitetsAvtaleDtoBuilder aaBuilder1 = AktivitetsAvtaleDtoBuilder.ny()
            .medPeriode(arbeidsperiode);
        YrkesaktivitetDtoBuilder yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aaBuilder1);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yaBuilder);
        return arbeidsgiver;
    }

    private void fjernOgLeggTilNyBeregningAktivitet(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        if (fom.isAfter(SKJÆRINGSTIDSPUNKT)) {
            throw new IllegalArgumentException("Kan ikke lage BeregningAktivitet som starter etter skjæringstidspunkt");
        }
        fjernAktivitet(arbeidsgiver, arbeidsforholdRef);
        aktiviteter.add(lagAktivitet(fom, tom, arbeidsgiver, arbeidsforholdRef));
    }

    private void fjernAktivitet(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        aktiviteter.stream()
            .filter(a -> a.gjelderFor(arbeidsgiver, arbeidsforholdRef)).findFirst()
            .ifPresent(a -> aktiviteter.remove(a));
    }

    private BeregningAktivitetDto lagAktivitet(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        return BeregningAktivitetDto.builder()
            .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidsforholdRef(arbeidsforholdRef)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .build();
    }

    @Test
    public void lagPeriodeForEndringISøktYtelseForSVP() {
        // Arrange
        PeriodeMedUtbetalingsgradDto periode1 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(1), BigDecimal.ZERO);
        PeriodeMedUtbetalingsgradDto periode2 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(1), BigDecimal.valueOf(100));
        PeriodeMedUtbetalingsgradDto periode3 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusMonths(1).plusDays(1), SKJÆRINGSTIDSPUNKT.plusMonths(3),
            BigDecimal.ZERO);

        UtbetalingsgradPrAktivitetDto tilrette1 = lagUtbetalingsgradPrAktivitet(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(ORG_NUMMER),
            periode1, periode2, periode3);

        PeriodeMedUtbetalingsgradDto periode4 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusDays(14), SKJÆRINGSTIDSPUNKT.plusMonths(1),
            BigDecimal.valueOf(100));
        PeriodeMedUtbetalingsgradDto periode5 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusMonths(1).plusDays(1), SKJÆRINGSTIDSPUNKT.plusMonths(2),
            BigDecimal.valueOf(40));

        UtbetalingsgradPrAktivitetDto tilrette2 = lagUtbetalingsgradPrAktivitet(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(ORG_NUMMER_2),
            periode4, periode5);

        InntektArbeidYtelseAggregatBuilder iayAggregatBuilder = leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER, ORG_NUMMER_2));

        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(List.of(ORG_NUMMER, ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();

        var inntekt = Beløp.fra(40000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, Beløp.ZERO, inntekt);
        var im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, Beløp.ZERO, inntekt);

        SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = new SvangerskapspengerGrunnlag(List.of(tilrette1, tilrette2));

        // Act
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(iayAggregatBuilder)
            .medInntektsmeldinger(im1, im2).build();
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(koblingReferanse, grunnlag, beregningsgrunnlag, iayGrunnlag, skjæringstidspunkt, svangerskapspengerGrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(4);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(13));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(14), SKJÆRINGSTIDSPUNKT.plusMonths(1),
            PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
        assertBeregningsgrunnlagPeriode(perioder.get(2), SKJÆRINGSTIDSPUNKT.plusMonths(1).plusDays(1), SKJÆRINGSTIDSPUNKT.plusMonths(2), PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
        assertBeregningsgrunnlagPeriode(perioder.get(3), SKJÆRINGSTIDSPUNKT.plusMonths(2).plusDays(1), TIDENES_ENDE, PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);

    }


    @Test
    public void lagPeriodeForEndringISøktYtelseFraSkjæringstidspunktet() {
        // Arrange
        PeriodeMedUtbetalingsgradDto periode = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(1), BigDecimal.valueOf(50));

        UtbetalingsgradPrAktivitetDto tilrette1 = lagUtbetalingsgradPrAktivitet(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(ORG_NUMMER), periode);

        InntektArbeidYtelseAggregatBuilder iayAggregatBuilder = leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER, ORG_NUMMER_2)
        );

        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().get();

        var inntekt = Beløp.fra(40000);
        var im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, Beløp.ZERO, inntekt);

        SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = new SvangerskapspengerGrunnlag(List.of(tilrette1));


        // Act
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(iayAggregatBuilder)
            .medInntektsmeldinger(im2).build();
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(koblingReferanse,
            grunnlag, beregningsgrunnlag, iayGrunnlag, skjæringstidspunkt, svangerskapspengerGrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(1));
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertBeregningsgrunnlagPeriode(perioder.get(1),  SKJÆRINGSTIDSPUNKT.plusMonths(1).plusDays(1), TIDENES_ENDE,
                PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
    }


    private BeregningsgrunnlagDto fastsettPerioderForRefusjonOgGradering(KoblingReferanse koblingReferanse,
                                                                         BeregningsgrunnlagGrunnlagDto grunnlag,
                                                                         BeregningsgrunnlagDto beregningsgrunnlag,
                                                                         InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                         Skjæringstidspunkt skjæringstidspunkt,
                                                                         SvangerskapspengerGrunnlag svangerskapspengerGrunnlag) {
        KoblingReferanse refMeStp = koblingReferanse.medSkjæringstidspunkt(skjæringstidspunkt);
        var input = new BeregningsgrunnlagInput(refMeStp, iayGrunnlag, null, List.of(), svangerskapspengerGrunnlag)
                .medBeregningsgrunnlagGrunnlag(grunnlag);
        return tjeneste.fastsettPerioderForUtbetalingsgradEllerGradering(input, beregningsgrunnlag).getBeregningsgrunnlag();
    }

    private void assertBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, LocalDate expectedFom, LocalDate expectedTom,
                                                 PeriodeÅrsak... perioderÅrsaker) {
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom()).as("fom").isEqualTo(expectedFom);
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()).as("tom").isEqualTo(expectedTom);
        assertThat(beregningsgrunnlagPeriode.getPeriodeÅrsaker()).as("periodeÅrsaker").containsExactlyInAnyOrder(perioderÅrsaker);
    }

    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlag(List<String> orgnrs,
                                                                BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        BeregningsgrunnlagPeriodeDto.Builder beregningsgrunnlagPeriodeBuilder = lagBeregningsgrunnlagPerioderBuilder(SKJÆRINGSTIDSPUNKT, null, orgnrs);
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medHjemmel(Hjemmel.F_14_7));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagPeriodeBuilder);
        BeregningsgrunnlagDto bg = beregningsgrunnlagBuilder.build();
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(bg)
            .medRegisterAktiviteter(beregningAktivitetAggregat)
            .build(BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    private BeregningsgrunnlagPeriodeDto.Builder lagBeregningsgrunnlagPerioderBuilder(LocalDate fom, LocalDate tom, List<String> orgnrs) {
        BeregningsgrunnlagPeriodeDto.Builder builder = BeregningsgrunnlagPeriodeDto.ny();
        for (String orgnr : orgnrs) {
            Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
            BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                    .medArbeidsgiver(arbeidsgiver)
                    .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT.minusYears(1)));
            builder.leggTilBeregningsgrunnlagPrStatusOgAndel(andelBuilder);
        }
        return builder
            .medBeregningsgrunnlagPeriode(fom, tom);
    }

    private UtbetalingsgradPrAktivitetDto lagUtbetalingsgradPrAktivitet(UttakArbeidType uttakArbeidType, Arbeidsgiver arbeidsgiver,
                                                                        PeriodeMedUtbetalingsgradDto... perioder) {
        var tilretteleggingArbeidsforhold = new AktivitetDto(arbeidsgiver, InternArbeidsforholdRefDto.nyRef(), uttakArbeidType);
        return new UtbetalingsgradPrAktivitetDto(tilretteleggingArbeidsforhold, List.of(perioder));
    }

    private PeriodeMedUtbetalingsgradDto lagPeriodeMedUtbetaling(LocalDate skjæringstidspunkt, LocalDate tomDato, BigDecimal utbetalingsgrad) {
        return new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(skjæringstidspunkt, tomDato), Utbetalingsgrad.fra(utbetalingsgrad));
    }

}
