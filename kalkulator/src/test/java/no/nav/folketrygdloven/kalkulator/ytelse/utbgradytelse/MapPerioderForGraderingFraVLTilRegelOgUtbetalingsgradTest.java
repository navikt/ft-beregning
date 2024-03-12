package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodeModellRefusjon;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon.MapRefusjonPerioderFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.fp.MapRefusjonPerioderFraVLTilRegelFP;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

public class MapPerioderForGraderingFraVLTilRegelOgUtbetalingsgradTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, 1, 1);
    public static final KoblingReferanseMock REF = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    public static final BigDecimal REFUSJON = BigDecimal.valueOf(44733);
    private final BeregningsgrunnlagDto bg = lagBgMedEnPeriode();

    /**
     * 2 Arbeidsforhold i samme virksomhet. Arbeidsforhold1 er aktivt på skjæringstidspunkt, Arbeidsforhold2 tilkommer etter.
     * Inntektsmelding kommer med Id for arbeidsforholdet som er aktivt på skjæringstidspunktet.
     */
    @Test
    public void skal_mappe_til_regel_for_arbeid_over_skjæringstidspunktet_med_inntektsmelding_med_id_og_arbeid_i_samme_virksomhet_som_tilkommer_etter_skjæringstidspunkt() {
        Arbeidsgiver ag1 = Arbeidsgiver.virksomhet("994507508");
        MapRefusjonPerioderFraVLTilRegel mapper = new MapRefusjonPerioderFraVLTilRegelFP();
        KoblingReferanseMock behandlingRef = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
        InternArbeidsforholdRefDto refArbeidsforhold1 = InternArbeidsforholdRefDto.nyRef();
        BeregningsgrunnlagDto bg = lagBgMedEnAndel(ag1, refArbeidsforhold1);

        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagDtoBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(lagAktivitetAggregat(ag1, SKJÆRINGSTIDSPUNKT.plusDays(1), SKJÆRINGSTIDSPUNKT))
                .medBeregningsgrunnlag(bg);
        InternArbeidsforholdRefDto refArbeidsforhold2 = InternArbeidsforholdRefDto.nyRef();
        InntektArbeidYtelseAggregatBuilder register = byggRegister(ag1, SKJÆRINGSTIDSPUNKT.plusDays(2), refArbeidsforhold2, refArbeidsforhold1,
                SKJÆRINGSTIDSPUNKT.plusDays(1), SKJÆRINGSTIDSPUNKT.plusMonths(5));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = byggIAY(ag1, register, refArbeidsforhold1, SKJÆRINGSTIDSPUNKT.plusDays(2));
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(behandlingRef, grunnlagDtoBuilder, BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag);

        PeriodeModellRefusjon map = mapper.map(input, bg);

        assertThat(map.getArbeidsforholdOgInntektsmeldinger()).hasSize(1);
        assertThat(map.getArbeidsforholdOgInntektsmeldinger().get(0).getRefusjoner()).hasSize(1);
        assertThat(map.getArbeidsforholdOgInntektsmeldinger().get(0).getRefusjoner().get(0).getFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(map.getArbeidsforholdOgInntektsmeldinger().get(0).getAndelsnr()).isEqualTo(1);
    }


    /**
     * 2 Arbeidsforhold i samme virksomhet. Arbeidsforhold1 avslutter 2 uker før skjæringstidspunkt for opptjening, Arbeidsforhold2 starter på skjærinstidspunkt for opptjening.
     * Inntektsmelding kommer inn uten id med start i permisjon på skjæringstidspunkt for opptjening.
     */
    @Test
    public void arbeid_slutter_før_skjæringstidspunkt_for_opptjening_inntektsmelding_kommer_uten_id_for_arbeidsforhold_i_samme_virksomhet_etter_skjæringstidspunktet() {
        Arbeidsgiver ag1 = Arbeidsgiver.virksomhet("994507508");
        MapRefusjonPerioderFraVLTilRegel mapper = new MapRefusjonPerioderFraVLTilRegelFP();
        LocalDate skjæringstidspunktOpptjening = SKJÆRINGSTIDSPUNKT.plusDays(15);
        KoblingReferanseMock behandlingRef = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT, skjæringstidspunktOpptjening);
        BeregningsgrunnlagDto bg = lagBgMedEnAndel(ag1, InternArbeidsforholdRefDto.nullRef());

        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagDtoBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(lagAktivitetAggregat(ag1, SKJÆRINGSTIDSPUNKT.minusDays(1), skjæringstidspunktOpptjening))
                .medBeregningsgrunnlag(bg);
        InternArbeidsforholdRefDto refArbeidsforhold2 = InternArbeidsforholdRefDto.nyRef();
        InternArbeidsforholdRefDto arbeidsforholdRef = InternArbeidsforholdRefDto.nyRef();
        InntektArbeidYtelseAggregatBuilder register = byggRegister(ag1, skjæringstidspunktOpptjening,
                refArbeidsforhold2, arbeidsforholdRef, SKJÆRINGSTIDSPUNKT.minusDays(1), skjæringstidspunktOpptjening.plusMonths(5));
        InternArbeidsforholdRefDto idForInntektsmelding = InternArbeidsforholdRefDto.nullRef(); // Inntektsmelding kommer uten ID
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = byggIAY(ag1, register, idForInntektsmelding, skjæringstidspunktOpptjening);
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(behandlingRef, grunnlagDtoBuilder, BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag);

        PeriodeModellRefusjon map = mapper.map(input, bg);

        assertThat(map.getArbeidsforholdOgInntektsmeldinger()).hasSize(1);
        assertThat(map.getArbeidsforholdOgInntektsmeldinger().get(0).getRefusjoner()).hasSize(1);
        assertThat(map.getArbeidsforholdOgInntektsmeldinger().get(0).getRefusjoner().get(0).getFom()).isEqualTo(input.getBeregningsgrunnlag().getSkjæringstidspunkt());
        assertThat(map.getArbeidsforholdOgInntektsmeldinger().get(0).getRefusjoner().get(0).getMånedsbeløp()).isEqualTo(REFUSJON);
        assertThat(map.getArbeidsforholdOgInntektsmeldinger().get(0).getAndelsnr()).isEqualTo(1L);
    }

    /**
     * 2 Arbeidsforhold i samme virksomhet. Arbeidsforhold1 avslutter 2 uker før skjæringstidspunkt for opptjening, Arbeidsforhold2 starter på skjærinstidspunkt for opptjening.
     * Inntektsmelding kommer inn med id for Arbeidsforhold2 med start i permisjon på skjæringstidspunkt for opptjening.
     */
    @Test
    public void arbeid_slutter_før_skjæringstidspunkt_for_opptjening_inntektsmelding_kommer_med_id_for_arbeidsforhold_i_samme_virksomhet_etter_skjæringstidspunktet() {
        Arbeidsgiver ag1 = Arbeidsgiver.virksomhet("994507508");
        MapRefusjonPerioderFraVLTilRegel mapper = new MapRefusjonPerioderFraVLTilRegelFP();
        LocalDate skjæringstidspunktOpptjening = SKJÆRINGSTIDSPUNKT.plusDays(15);
        KoblingReferanseMock behandlingRef = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT, skjæringstidspunktOpptjening);
        BeregningsgrunnlagDto bg = lagBgMedEnAndel(ag1, InternArbeidsforholdRefDto.nullRef());

        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagDtoBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(lagAktivitetAggregat(ag1, SKJÆRINGSTIDSPUNKT.minusDays(1), skjæringstidspunktOpptjening))
                .medBeregningsgrunnlag(bg);
        InternArbeidsforholdRefDto refArbeidsforhold2 = InternArbeidsforholdRefDto.nyRef();
        InternArbeidsforholdRefDto arbeidsforholdRef = InternArbeidsforholdRefDto.nyRef();
        InntektArbeidYtelseAggregatBuilder register = byggRegister(ag1, skjæringstidspunktOpptjening, refArbeidsforhold2,
                arbeidsforholdRef, SKJÆRINGSTIDSPUNKT.minusDays(1), skjæringstidspunktOpptjening.plusMonths(5));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = byggIAY(ag1, register, refArbeidsforhold2, skjæringstidspunktOpptjening);
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(behandlingRef, grunnlagDtoBuilder, BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag);

        PeriodeModellRefusjon map = mapper.map(input, bg);

        assertThat(map.getArbeidsforholdOgInntektsmeldinger()).hasSize(1);
        assertThat(map.getArbeidsforholdOgInntektsmeldinger().get(0).getRefusjoner()).hasSize(1);
        assertThat(map.getArbeidsforholdOgInntektsmeldinger().get(0).getRefusjoner().get(0).getFom()).isEqualTo(skjæringstidspunktOpptjening);
        assertThat(map.getArbeidsforholdOgInntektsmeldinger().get(0).getAndelsnr()).isNull();
    }


    @Test
    public void skal_teste_endring_i_ytelse_svp() {
        // Skal hente gradering fra uttak fram til der oppgittfordeling starter
        String orgnr1 = "123";
        String orgnr2 = "321";
        LocalDate date = LocalDate.now();
        PeriodeMedUtbetalingsgradDto periode1 = lagPeriodeMedUtbetaling(date, BigDecimal.valueOf(100));
        PeriodeMedUtbetalingsgradDto periode2 = lagPeriodeMedUtbetaling(date.plusMonths(1), BigDecimal.valueOf(100));
        UtbetalingsgradPrAktivitetDto tilrette1 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(orgnr1),
                periode1, periode2);

        PeriodeMedUtbetalingsgradDto periode3 = lagPeriodeMedUtbetaling(date, BigDecimal.valueOf(100));
        UtbetalingsgradPrAktivitetDto tilrette2 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(orgnr2),
                periode3);

        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(List.of(byggYA(orgnr1, date), byggYA(orgnr2, date)));


        var tilrettelegginger = List.of(tilrette1, tilrette2);

        // Act
        var andelGraderingList = MapPerioderForUtbetalingsgradFraVLTilRegel.mapTilrettelegginger(REF, new SvangerskapspengerGrunnlag(tilrettelegginger), bg, filter);

        // Assert
        assertThat(andelGraderingList).hasSize(2);
        var andelArb1 = forArbeidsgiver(andelGraderingList, Arbeidsgiver.virksomhet(orgnr1), AktivitetStatusV2.AT);
        var andelArb2 = forArbeidsgiver(andelGraderingList, Arbeidsgiver.virksomhet(orgnr2), AktivitetStatusV2.AT);
        assertThat(andelArb1).hasSize(1);
        assertThat(andelArb2).hasSize(1);
        assertThat(andelArb1.get(0).getUbetalingsgrader()).hasSize(2);
        assertThat(andelArb2.get(0).getUbetalingsgrader()).hasSize(1);
        assertThat(andelArb1.get(0).getUbetalingsgrader()).anySatisfy(gradering -> {
            assertThat(gradering.getPeriode().getFom()).isEqualTo(periode1.getPeriode().getFomDato());
            assertThat(gradering.getPeriode().getTom()).isEqualTo(periode1.getPeriode().getTomDato());
            assertThat(gradering.getUtbetalingsprosent()).isEqualByComparingTo(periode1.getUtbetalingsgrad().verdi());
        });
        assertThat(andelArb1.get(0).getUbetalingsgrader()).anySatisfy(gradering -> {
            assertThat(gradering.getPeriode().getFom()).isEqualTo(periode2.getPeriode().getFomDato());
            assertThat(gradering.getPeriode().getTom()).isEqualTo(periode2.getPeriode().getTomDato());
            assertThat(gradering.getUtbetalingsprosent()).isEqualByComparingTo(periode2.getUtbetalingsgrad().verdi());
        });
        assertThat(andelArb2.get(0).getUbetalingsgrader()).anySatisfy(gradering -> {
            assertThat(gradering.getPeriode().getFom()).isEqualTo(periode3.getPeriode().getFomDato());
            assertThat(gradering.getPeriode().getTom()).isEqualTo(periode3.getPeriode().getTomDato());
            assertThat(gradering.getUtbetalingsprosent()).isEqualByComparingTo(periode3.getUtbetalingsgrad().verdi());
        });
    }

    private YrkesaktivitetDto byggYA(String orgnr1, LocalDate date) {
        return YrkesaktivitetDtoBuilder.oppdatere(Optional.empty()).medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD).medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr1))
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(Intervall.fraOgMedTilOgMed(date.minusMonths(10), date.plusMonths(10))).medErAnsettelsesPeriode(true)).build();
    }

    @Test
    public void skal_ikke_lage_perioder_med_fom_før_skjæringstidspunkt() {
        // Skal hente gradering fra uttak fram til der oppgittfordeling starter
        String orgnr1 = "123";
        LocalDate date = LocalDate.now();
        PeriodeMedUtbetalingsgradDto periode1 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.minusDays(5), BigDecimal.valueOf(100));
        PeriodeMedUtbetalingsgradDto periode2 = lagPeriodeMedUtbetaling(date.plusDays(2), BigDecimal.valueOf(100));
        UtbetalingsgradPrAktivitetDto tilrette1 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(orgnr1),
                periode1, periode2);

        var tilrettelegginger = List.of(tilrette1);

        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(List.of(byggYA(orgnr1, date)));

        // Act
        var andelGraderingList = MapPerioderForUtbetalingsgradFraVLTilRegel.mapTilrettelegginger(REF, new SvangerskapspengerGrunnlag(tilrettelegginger), bg, filter);

        // Assert
        assertThat(andelGraderingList).hasSize(1);
        var andelArb1 = forArbeidsgiver(andelGraderingList, Arbeidsgiver.virksomhet(orgnr1), AktivitetStatusV2.AT);
        assertThat(andelArb1).hasSize(1);
        assertThat(andelArb1.get(0).getUbetalingsgrader()).hasSize(2);
        assertThat(andelArb1.get(0).getUbetalingsgrader()).anySatisfy(gradering -> {
            assertThat(gradering.getPeriode().getFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
            assertThat(gradering.getPeriode().getTom()).isEqualTo(periode1.getPeriode().getTomDato());
            assertThat(gradering.getUtbetalingsprosent()).isEqualByComparingTo(periode1.getUtbetalingsgrad().verdi());
        });
        assertThat(andelArb1.get(0).getUbetalingsgrader()).anySatisfy(gradering -> {
            assertThat(gradering.getPeriode().getFom()).isEqualTo(periode2.getPeriode().getFomDato());
            assertThat(gradering.getPeriode().getTom()).isEqualTo(periode2.getPeriode().getTomDato());
            assertThat(gradering.getUtbetalingsprosent()).isEqualByComparingTo(periode2.getUtbetalingsgrad().verdi());
        });
    }

    private BeregningsgrunnlagDto lagBgMedEnPeriode() {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();

        BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(bg);
        return bg;
    }

    private List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.AndelUtbetalingsgrad> forArbeidsgiver(List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.AndelUtbetalingsgrad> andelGraderingList, Arbeidsgiver arbeidsgiver, AktivitetStatusV2 status) {
        return andelGraderingList.stream()
                .filter(ag -> Objects.equals(arbeidsgiver.getIdentifikator(), ag.getArbeidsforhold().getOrgnr()) && ag.getAktivitetStatus().equals(status))
                .collect(Collectors.toList());
    }

    private UtbetalingsgradPrAktivitetDto lagTilretteleggingMedUtbelingsgrad(UttakArbeidType uttakArbeidType,
                                                                             Arbeidsgiver arbeidsgiver,
                                                                             PeriodeMedUtbetalingsgradDto... perioder) {
        var tilretteleggingArbeidsforhold = new AktivitetDto(arbeidsgiver, InternArbeidsforholdRefDto.nyRef(), uttakArbeidType);
        return new UtbetalingsgradPrAktivitetDto(tilretteleggingArbeidsforhold, List.of(perioder));
    }

    private PeriodeMedUtbetalingsgradDto lagPeriodeMedUtbetaling(LocalDate skjæringstidspunkt, BigDecimal utbetalingsgrad) {
        return new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(skjæringstidspunkt, skjæringstidspunkt.plusWeeks(1)), Utbetalingsgrad.fra(utbetalingsgrad));
    }

    private InntektsmeldingDto lagInntektsmelding(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef, LocalDate startDatoPermisjon) {
        return InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medStartDatoPermisjon(startDatoPermisjon)
                .medRefusjon(Beløp.fra(REFUSJON))
                .medBeløp(Beløp.fra(44733))
                .medArbeidsforholdId(arbeidsforholdRef)
                .build();
    }

    private YrkesaktivitetDtoBuilder lagYrkesaktivitet(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef, LocalDate fraDato, LocalDate tilDato) {
        return YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsforholdId(arbeidsforholdRef)
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny()
                        .medErAnsettelsesPeriode(true)
                        .medPeriode(Intervall.fraOgMedTilOgMed(fraDato, tilDato)));
    }

    private BeregningAktivitetAggregatDto lagAktivitetAggregat(Arbeidsgiver ag1, LocalDate sluttdatoArbeidFørStp, LocalDate skjæringstidspunktOpptjening) {
        return BeregningAktivitetAggregatDto.builder()
                .leggTilAktivitet(BeregningAktivitetDto.builder()
                        .medArbeidsgiver(ag1)
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                        .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10), sluttdatoArbeidFørStp))
                        .medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef())
                        .build())
                .medSkjæringstidspunktOpptjening(skjæringstidspunktOpptjening)
                .build();
    }

    private BeregningsgrunnlagDto lagBgMedEnAndel(Arbeidsgiver ag1, InternArbeidsforholdRefDto arbeidsforholdRef) {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medGrunnbeløp(Beløp.fra(99000))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .build();
        BeregningsgrunnlagPeriodeDto periodeDto = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
                .medBruttoPrÅr(Beløp.fra(531064))
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(Beløp.fra(531064))
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ag1)
                        .medArbeidsforholdRef(arbeidsforholdRef))
                .build(periodeDto);
        return bg;
    }

    private InntektArbeidYtelseGrunnlagDto byggIAY(Arbeidsgiver ag1, InntektArbeidYtelseAggregatBuilder register, InternArbeidsforholdRefDto regArbeidsforhold1, LocalDate localDate) {
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medInntektsmeldinger(lagInntektsmelding(ag1, regArbeidsforhold1, localDate))
                .medData(register)
                .build();
    }

    private InntektArbeidYtelseAggregatBuilder byggRegister(Arbeidsgiver ag1, LocalDate skjæringstidspunktOpptjening,
                                                            InternArbeidsforholdRefDto arbeidsforholdRef2,
                                                            InternArbeidsforholdRefDto arbeidsforholdRef1,
                                                            LocalDate tilDato1, LocalDate tilDato2) {
        InntektArbeidYtelseAggregatBuilder register = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        register.leggTilAktørArbeid(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                .leggTilYrkesaktivitet(lagYrkesaktivitet(ag1, arbeidsforholdRef1, SKJÆRINGSTIDSPUNKT.minusMonths(10), tilDato1))
                .leggTilYrkesaktivitet(lagYrkesaktivitet(ag1, arbeidsforholdRef2, skjæringstidspunktOpptjening, tilDato2)));
        return register;
    }
}
