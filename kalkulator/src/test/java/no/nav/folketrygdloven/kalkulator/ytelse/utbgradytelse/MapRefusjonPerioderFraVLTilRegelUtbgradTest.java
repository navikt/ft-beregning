package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodeModellRefusjon;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;
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
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonFilter;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

class MapRefusjonPerioderFraVLTilRegelUtbgradTest {


    private final MapRefusjonPerioderFraVLTilRegelUtbgrad mapper = new MapRefusjonPerioderFraVLTilRegelUtbgrad();

    @Test
    void skal_finne_første_dag_etter_permisjon() {
        var stp = LocalDate.now();
        var arbeidsgiver = Arbeidsgiver.virksomhet("123456789");
        var utbetalingsperiode = Intervall.fraOgMedTilOgMed(stp, stp.plusDays(15));
        var ytelsespesifiktGrunnlag = lagUtbetalingsgrunnlag(arbeidsgiver, utbetalingsperiode);
        var inntektsmelding = lagInntektsmelding(stp, arbeidsgiver);
        var aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny().medPeriode(Intervall.fraOgMedTilOgMed(stp.minusDays(1), stp.plusDays(15))).medErAnsettelsesPeriode(true);
        var permisjonsperiode = Intervall.fraOgMedTilOgMed(stp.minusDays(1), stp.plusDays(15));
        var relaterteYrkesaktiviteter = Set.of(lagYrkesaktivitet(arbeidsgiver, aktivitetsAvtaleDtoBuilder, permisjonsperiode));
        var permisjonFilter = new PermisjonFilter(Collections.emptyList(), relaterteYrkesaktiviteter, stp);

        var startdato = mapper.utledStartdatoEtterPermisjon(stp, inntektsmelding, relaterteYrkesaktiviteter, permisjonFilter, ytelsespesifiktGrunnlag);

        assertThat(startdato.isPresent()).isTrue();
        assertThat(startdato.get()).isEqualTo(permisjonsperiode.getTomDato().plusDays(1));
    }

    @Test
    void skal_ikke_bruke_permisjon_som_starter_etter_stp() {
        var stp = LocalDate.now();
        var arbeidsgiver = Arbeidsgiver.virksomhet("123456789");
        var utbetalingsperiode = Intervall.fraOgMedTilOgMed(stp, stp.plusDays(15));
        var ytelsespesifiktGrunnlag = lagUtbetalingsgrunnlag(arbeidsgiver, utbetalingsperiode);
        var inntektsmelding = lagInntektsmelding(stp, arbeidsgiver);
        var aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny().medPeriode(utbetalingsperiode).medErAnsettelsesPeriode(true);
        var permisjonsperiode = Intervall.fraOgMedTilOgMed(stp.plusDays(1), stp.plusDays(15));
        var relaterteYrkesaktiviteter = Set.of(lagYrkesaktivitet(arbeidsgiver, aktivitetsAvtaleDtoBuilder, permisjonsperiode));
        var permisjonFilter = new PermisjonFilter(Collections.emptyList(), relaterteYrkesaktiviteter, stp);

        var startdato = mapper.utledStartdatoEtterPermisjon(stp, inntektsmelding, relaterteYrkesaktiviteter, permisjonFilter, ytelsespesifiktGrunnlag);

        assertThat(startdato.isPresent()).isTrue();
        assertThat(startdato.get()).isEqualTo(stp);
    }

    @Test
    void skal_returnerer_første_dag_med_søkt_utbetaling() {
        var stp = LocalDate.now();
        var arbeidsgiver = Arbeidsgiver.virksomhet("123456789");
        var utbetalingsperiode = Intervall.fraOgMedTilOgMed(stp.plusDays(20), stp.plusDays(25));
        var ytelsespesifiktGrunnlag = lagUtbetalingsgrunnlag(arbeidsgiver, utbetalingsperiode);
        var inntektsmelding = lagInntektsmelding(stp, arbeidsgiver);
        var ansettelsesperiode = Intervall.fraOgMedTilOgMed(stp.minusYears(10), stp.plusYears(15));
        var aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny().medPeriode(ansettelsesperiode).medErAnsettelsesPeriode(true);
        var permisjonsperiode = Intervall.fraOgMedTilOgMed(stp.minusDays(1), stp.plusDays(15));
        var relaterteYrkesaktiviteter = Set.of(lagYrkesaktivitet(arbeidsgiver, aktivitetsAvtaleDtoBuilder, permisjonsperiode));
        var permisjonFilter = new PermisjonFilter(Collections.emptyList(), relaterteYrkesaktiviteter, stp);

        var startdato = mapper.utledStartdatoEtterPermisjon(stp, inntektsmelding, relaterteYrkesaktiviteter, permisjonFilter, ytelsespesifiktGrunnlag);

        assertThat(startdato.isPresent()).isTrue();
        assertThat(startdato.get()).isEqualTo(utbetalingsperiode.getFomDato());
    }

    @Test
    void skal_ikke_returnerer_første_dag_med_søkt_utbetaling_ved_inaktiv_og_ingen_arbeid() {
        var stp = LocalDate.now();
        var arbeidsgiver = Arbeidsgiver.virksomhet("123456789");
        var utbetalingsperiode = Intervall.fraOgMedTilOgMed(stp.plusDays(20), stp.plusDays(25));
        var ytelsespesifiktGrunnlag = lagUtbetalingsgrunnlagInaktiv(utbetalingsperiode);
        var inntektsmelding = lagInntektsmelding(stp, arbeidsgiver);
        var ansettelsesperiode = Intervall.fraOgMedTilOgMed(stp.minusYears(10), stp.plusYears(15));
        var aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny().medPeriode(ansettelsesperiode).medErAnsettelsesPeriode(true);
        var permisjonsperiode = Intervall.fraOgMedTilOgMed(stp.minusDays(1), stp.plusDays(15));
        var relaterteYrkesaktiviteter = Set.of(lagYrkesaktivitet(arbeidsgiver, aktivitetsAvtaleDtoBuilder, permisjonsperiode));
        var permisjonFilter = new PermisjonFilter(Collections.emptyList(), relaterteYrkesaktiviteter, stp);

        var startdato = mapper.utledStartdatoEtterPermisjon(stp, inntektsmelding, relaterteYrkesaktiviteter, permisjonFilter, ytelsespesifiktGrunnlag);

        assertThat(startdato.isPresent()).isFalse();
    }


    @Test
    void skal_gi_refusjon_fra_start_for_arbeid_som_har_oppgitt_feil_startdato_i_inntektsmelding() {

        // Arrange
        String orgnr = "974749866";
        LocalDate stp = LocalDate.of(2019, 10, 16);
        AktivitetsAvtaleDtoBuilder ansettelsesPeriode = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(Intervall.fraOgMed(LocalDate.of(2013, 5, 27)))
                .medErAnsettelsesPeriode(true);
        Intervall periode = Intervall.fraOgMed(LocalDate.of(2019, 1, 1));
        AktivitetsAvtaleDtoBuilder aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(periode)
                .medSisteLønnsendringsdato(LocalDate.of(2018, 7, 1))
                .medErAnsettelsesPeriode(false);

        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(orgnr);
        YrkesaktivitetDto yrkesaktivitet = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .leggTilAktivitetsAvtale(aktivitetsAvtaleDtoBuilder)
                .leggTilAktivitetsAvtale(ansettelsesPeriode)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsgiver(virksomhet)
                .build();
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
                .leggTilAktørArbeid(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                        .leggTilYrkesaktivitet(yrkesaktivitet));


        InntektsmeldingDto im = InntektsmeldingDtoBuilder.builder()
                .medBeløp(Beløp.fra(11)).medRefusjon(Beløp.fra(11))
                .medArbeidsgiver(virksomhet)
                .medStartDatoPermisjon(LocalDate.of(2019, 10, 22)).build();

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(registerBuilder)
                .medInntektsmeldinger(im)
                .build();

        AktivitetDto tilretteleggingArbeidsforhold = new AktivitetDto(virksomhet, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetaling = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp, LocalDate.of(2019, 10, 21)), Utbetalingsgrad.valueOf(100));
        PeriodeMedUtbetalingsgradDto periodeMedUtbetaling2 = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(LocalDate.of(2019, 10, 22), LocalDate.of(2020, 1, 19)), Utbetalingsgrad.valueOf(40));

        UtbetalingsgradPrAktivitetDto tilrettelegging = new UtbetalingsgradPrAktivitetDto(tilretteleggingArbeidsforhold,
                List.of(periodeMedUtbetaling, periodeMedUtbetaling2));
        SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = new SvangerskapspengerGrunnlag(List.of(tilrettelegging));


        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medAndelsnr(1L)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomhet))
                .build();
        BeregningsgrunnlagPeriodeDto.Builder bgperiode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(stp, TIDENES_ENDE)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(andel);
        BeregningsgrunnlagDto beregningsgrunnlagDto = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(stp)
                .medGrunnbeløp(Beløp.fra(99000))
                .leggTilBeregningsgrunnlagPeriode(bgperiode)
                .build();
        BeregningsgrunnlagGrunnlagDtoBuilder bg = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .medSkjæringstidspunktOpptjening(stp)
                        .leggTilAktivitet(BeregningAktivitetDto.builder()
                                .medPeriode(Intervall.fraOgMed(LocalDate.of(2013, 5, 27)))
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                                .medArbeidsgiver(virksomhet)
                                .build())
                        .build())
                .medBeregningsgrunnlag(beregningsgrunnlagDto);


        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(new KoblingReferanseMock(stp), bg,
                BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag, svangerskapspengerGrunnlag);

        // Act
        PeriodeModellRefusjon map = mapper.map(input, beregningsgrunnlagDto);

        // Assert
        Assertions.assertThat(map.getArbeidsforholdOgInntektsmeldinger()).hasSize(1);
        ArbeidsforholdOgInntektsmelding arbeidsforholdOgInntektsmelding = map.getArbeidsforholdOgInntektsmeldinger().get(0);
        Assertions.assertThat(arbeidsforholdOgInntektsmelding.getRefusjoner()).hasSize(2);
        Refusjonskrav refusjonskrav = arbeidsforholdOgInntektsmelding.getRefusjoner().get(0);
        Assertions.assertThat(refusjonskrav.getPeriode().getFom()).isEqualTo(stp);

        Refusjonskrav opphør = arbeidsforholdOgInntektsmelding.getRefusjoner().get(1);
        Assertions.assertThat(opphør.getMånedsbeløp()).isEqualTo(BigDecimal.ZERO);
        Assertions.assertThat(opphør.getPeriode().getFom()).isEqualTo(LocalDate.of(2020, 1, 19).plusDays(1));
    }


    private PleiepengerSyktBarnGrunnlag lagUtbetalingsgrunnlag(Arbeidsgiver arbeidsgiver, Intervall utbetalingsperiode) {
        return new PleiepengerSyktBarnGrunnlag(List.of(new UtbetalingsgradPrAktivitetDto(
                new AktivitetDto(arbeidsgiver, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                List.of(new PeriodeMedUtbetalingsgradDto(utbetalingsperiode, Utbetalingsgrad.valueOf(1))))));
    }

    private PleiepengerSyktBarnGrunnlag lagUtbetalingsgrunnlagInaktiv(Intervall utbetalingsperiode) {
        return new PleiepengerSyktBarnGrunnlag(List.of(new UtbetalingsgradPrAktivitetDto(
                new AktivitetDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.MIDL_INAKTIV),
                List.of(new PeriodeMedUtbetalingsgradDto(utbetalingsperiode, Utbetalingsgrad.valueOf(1))))));
    }


    private InntektsmeldingDto lagInntektsmelding(LocalDate stp, Arbeidsgiver arbeidsgiver) {
        return InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nullRef())
                .medRefusjon(Beløp.fra(10), stp.plusDays(16))
                .medBeløp(Beløp.fra(10))
                .build();
    }

    private YrkesaktivitetDto lagYrkesaktivitet(Arbeidsgiver arbeidsgiver, AktivitetsAvtaleDtoBuilder aktivitetsavtale, Intervall permisjonsperiode) {
        var builder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nullRef());
        if (permisjonsperiode != null) {
            builder.leggTilPermisjon(PermisjonDtoBuilder.ny().medPeriode(permisjonsperiode)
                    .medProsentsats(Stillingsprosent.HUNDRED)
                    .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.VELFERDSPERMISJON));
        }
        builder.leggTilAktivitetsAvtale(aktivitetsavtale);
        return builder
                .build();
    }
}
