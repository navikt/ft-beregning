package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonFilter;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektsmeldingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

class RefusjonsgrunnlagGyldigePerioderTest {

    private static final LocalDate STP = LocalDate.of(2026, 1, 1);

    @Test
    void skal_bruke_utbetalingsdato_uten_permisjon() {
        var ansettelseFom = STP.minusYears(1);
        var startdatoFraIm = STP;
        var utbetalingsdato = STP.plusDays(10);

        assertThat(utledStartdato(ansettelseFom, InntektsmeldingType.INNTEKTSMELDING, Optional.empty(), startdatoFraIm,
            utbetalingsdato, Utbetalingsgrad.fra(BigDecimal.valueOf(100)))).isEqualTo(utbetalingsdato);
    }

    @Test
    void skal_ikke_bruke_im_start_før_utbetalingsdato() {
        var startdatoFraIm = STP.plusDays(5);
        var utbetalingsdato = STP.plusDays(10);

        assertThat(utledStartdato(startdatoFraIm, utbetalingsdato)).isEqualTo(utbetalingsdato);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5})
    void skal_bruke_senere_startdato_fra_refusjonskrav(int dagerEtterStp) {
        var ansettelseFom = STP.plusDays(dagerEtterStp);
        var startdatoFraIm = STP.plusDays(30);
        var utbetalingsdato = STP.plusDays(10);

        assertThat(utledStartdato(ansettelseFom, InntektsmeldingType.REFUSJONSKRAV, Optional.empty(),
            startdatoFraIm, utbetalingsdato, Utbetalingsgrad.fra(BigDecimal.valueOf(100)))).isEqualTo(startdatoFraIm);
    }

    @ParameterizedTest
    @MethodSource("permisjonstilfeller")
    void skal_håndtere_permisjon_før_på_og_etter_skjæringstidspunktet(LocalDate permisjonTom,
                                                                    LocalDate forventetStartdato) {
        var ansettelseFom = STP.minusYears(1);
        var permisjonFom = STP.minusMonths(1);
        var startdatoFraIm = STP;
        var utbetalingsdato = STP.plusDays(10);
        var permisjon = Intervall.fraOgMedTilOgMed(permisjonFom, permisjonTom);

        assertThat(utledStartdato(ansettelseFom, InntektsmeldingType.INNTEKTSMELDING, Optional.of(permisjon), startdatoFraIm,
            utbetalingsdato, Utbetalingsgrad.fra(BigDecimal.valueOf(100))))
            .isEqualTo(forventetStartdato);
    }

    private static Stream<Arguments> permisjonstilfeller() {
        return Stream.of(
            Arguments.of(STP.plusDays(20), STP.plusDays(21)),
            Arguments.of(STP.plusDays(9), STP.plusDays(10)),
            Arguments.of(TIDENES_ENDE, STP));
    }

    @Test
    void skal_bruke_skjæringstidspunkt_når_utbetalingsdato_mangler() {
        var ansettelseFom = STP.minusYears(1);
        var startdatoFraIm = STP;
        var utbetalingsdato = STP.plusDays(10);

        assertThat(utledStartdato(ansettelseFom, InntektsmeldingType.INNTEKTSMELDING, Optional.empty(), startdatoFraIm,
            utbetalingsdato, Utbetalingsgrad.ZERO)).isEqualTo(STP);
    }

    @Test
    void skal_bruke_im_start_når_utbetalingsdato_mangler() {
        var ansettelseFom = STP;
        var startdatoFraIm = STP.plusDays(5);
        var utbetalingsdato = STP.plusDays(10);

        assertThat(utledStartdato(ansettelseFom, InntektsmeldingType.REFUSJONSKRAV, Optional.empty(), startdatoFraIm,
            utbetalingsdato, Utbetalingsgrad.ZERO)).isEqualTo(startdatoFraIm);
    }

    private LocalDate utledStartdato(LocalDate startdatoFraIm, LocalDate utbetalingsdato) {
        return utledStartdato(STP, InntektsmeldingType.REFUSJONSKRAV, Optional.empty(), startdatoFraIm,
            utbetalingsdato, Utbetalingsgrad.fra(BigDecimal.valueOf(100)));
    }

    private LocalDate utledStartdato(LocalDate ansettelseFom, InntektsmeldingType inntektsmeldingType,
                                    Optional<Intervall> permisjon, LocalDate startdatoFraIm,
                                    LocalDate utbetalingsdato, Utbetalingsgrad utbetalingsgrad) {
        var arbeidsgiver = Arbeidsgiver.virksomhet("999999999");
        var ref = InternArbeidsforholdRefDto.nullRef();
        var yrkesaktivitetBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(arbeidsgiver).medArbeidsforholdId(ref)
            .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny()
                .medErAnsettelsesPeriode(true).medPeriode(Intervall.fraOgMed(ansettelseFom)));
        permisjon.ifPresent(periode -> yrkesaktivitetBuilder.leggTilPermisjon(PermisjonDtoBuilder.ny()
                .medPeriode(periode)
                .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.PERMISJON)
                .medProsentsats(Stillingsprosent.HUNDRED)));
        var yrkesaktivitet = yrkesaktivitetBuilder.build();
        var yrkesaktiviteter = Set.of(yrkesaktivitet);
        var permisjonFilter = new PermisjonFilter(List.of(), yrkesaktiviteter, STP);
        var im = InntektsmeldingDtoBuilder.builder().medArbeidsgiver(arbeidsgiver).medArbeidsforholdId(ref)
            .medInntektsmeldingType(inntektsmeldingType).medStartDatoPermisjon(startdatoFraIm).build();
        var aktivitet = new AktivitetDto(arbeidsgiver, ref, UttakArbeidType.ORDINÆRT_ARBEID);
        var utbetalingsperiode = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMed(utbetalingsdato), utbetalingsgrad);
        var ytelsegrunnlag = new PleiepengerSyktBarnGrunnlag(
            List.of(new UtbetalingsgradPrAktivitetDto(aktivitet, List.of(utbetalingsperiode))));
        var input = new BeregningsgrunnlagInput(new KoblingReferanseMock(STP, FagsakYtelseType.PLEIEPENGER_SYKT_BARN),
            InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), null, List.of(), ytelsegrunnlag);

        return RefusjonsgrunnlagGyldigePerioder.utledStartdato(input, im, yrkesaktiviteter,
            RefusjonsgrunnlagGyldigePerioder.finnAnsattperioder(yrkesaktiviteter, STP), permisjonFilter);
    }
}
