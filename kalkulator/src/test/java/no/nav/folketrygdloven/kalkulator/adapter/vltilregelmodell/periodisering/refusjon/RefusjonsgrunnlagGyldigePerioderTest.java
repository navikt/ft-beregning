package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

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
    void skal_bruke_utbetalingsdato_når_første_dag_etter_permisjon_mangler() {
        assertThat(utledStartdato(TIDENES_ENDE, STP)).isEqualTo(STP.plusDays(10));
    }

    @Test
    void skal_bruke_senere_startdato_fra_refusjonskrav_når_første_dag_etter_permisjon_mangler() {
        assertThat(utledStartdato(TIDENES_ENDE, STP.plusDays(30))).isEqualTo(STP.plusDays(30));
    }

    @Test
    void skal_fortsatt_bruke_senere_dato_etter_permisjon() {
        assertThat(utledStartdato(STP.plusDays(20), STP)).isEqualTo(STP.plusDays(21));
    }

    @Test
    void skal_bevare_startdato_når_permisjonsslutt_og_utbetalingsstart_gir_samme_dato() {
        assertThat(utledStartdato(STP.plusDays(9), STP)).isEqualTo(STP.plusDays(10));
    }

    @Test
    void skal_bruke_dato_etter_permisjon_når_utbetalingsdato_mangler() {
        assertThat(utledStartdato(STP.plusDays(20), STP, Utbetalingsgrad.ZERO)).isEqualTo(STP.plusDays(21));
    }

    private LocalDate utledStartdato(LocalDate permisjonTom, LocalDate startdatoFraIm) {
        return utledStartdato(permisjonTom, startdatoFraIm, Utbetalingsgrad.fra(BigDecimal.valueOf(100)));
    }

    private LocalDate utledStartdato(LocalDate permisjonTom, LocalDate startdatoFraIm, Utbetalingsgrad utbetalingsgrad) {
        var arbeidsgiver = Arbeidsgiver.virksomhet("999999999");
        var ref = InternArbeidsforholdRefDto.nullRef();
        var yrkesaktivitet = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(arbeidsgiver).medArbeidsforholdId(ref)
            .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny()
                .medErAnsettelsesPeriode(true).medPeriode(Intervall.fraOgMed(STP.minusYears(1))))
            .leggTilPermisjon(PermisjonDtoBuilder.ny()
                .medPeriode(Intervall.fraOgMedTilOgMed(STP.minusMonths(1), permisjonTom))
                .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.PERMISJON)
                .medProsentsats(Stillingsprosent.HUNDRED))
            .build();
        var yrkesaktiviteter = Set.of(yrkesaktivitet);
        var permisjonFilter = new PermisjonFilter(List.of(), yrkesaktiviteter, STP);
        var im = InntektsmeldingDtoBuilder.builder().medArbeidsgiver(arbeidsgiver).medArbeidsforholdId(ref)
            .medInntektsmeldingType(InntektsmeldingType.REFUSJONSKRAV).medStartDatoPermisjon(startdatoFraIm).build();
        var aktivitet = new AktivitetDto(arbeidsgiver, ref, UttakArbeidType.ORDINÆRT_ARBEID);
        var utbetalingsperiode = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMed(STP.plusDays(10)), utbetalingsgrad);
        var ytelsegrunnlag = new SvangerskapspengerGrunnlag(
            List.of(new UtbetalingsgradPrAktivitetDto(aktivitet, List.of(utbetalingsperiode))));
        var input = new BeregningsgrunnlagInput(new KoblingReferanseMock(STP, FagsakYtelseType.SVANGERSKAPSPENGER),
            InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), null, List.of(), ytelsegrunnlag);

        return RefusjonsgrunnlagGyldigePerioder.utledStartdato(input, im, yrkesaktiviteter,
            RefusjonsgrunnlagGyldigePerioder.finnAnsattperioder(yrkesaktiviteter, STP), permisjonFilter);
    }
}
