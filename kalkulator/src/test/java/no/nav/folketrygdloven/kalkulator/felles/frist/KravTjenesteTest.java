package no.nav.folketrygdloven.kalkulator.felles.frist;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonsperiodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

class KravTjenesteTest {

    public static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = LocalDate.now();
    public static final Arbeidsgiver ARBEIDSGIVER1 = Arbeidsgiver.virksomhet("37432232");

    private static final Beløp BELØP_TEN = Beløp.fra(10);

    @Test
    void skal_lage_tom_tidslinje_uten_kravperioder() {
        List<PerioderForKravDto> kravperioder = List.of();
        Intervall ansattPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(12));
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagGjeldendeAktiviteter(ansattPeriode);
        YrkesaktivitetDto yrkesaktivitet = lagYrkesaktivitet(ansattPeriode);

        LocalDateTimeline<KravOgUtfall> kravTidslinje = KravTjeneste.lagTidslinjeForYrkesaktivitet(
                kravperioder,
                yrkesaktivitet,
                gjeldendeAktiviteter,
                SKJÆRINGSTIDSPUNKT_BEREGNING,
                Optional.empty(), FagsakYtelseType.FORELDREPENGER);

        assertThat(kravTidslinje.isEmpty()).isTrue();
    }

    @Test
    void skal_lage_tidslinje_med_krav_fra_skjæringstidspunkt_innsendt_i_tide() {
        LocalDate startRefusjon = SKJÆRINGSTIDSPUNKT_BEREGNING;
        List<PerioderForKravDto> kravperioder = List.of(new PerioderForKravDto(startRefusjon.plusMonths(2),
                List.of(new RefusjonsperiodeDto(Intervall.fraOgMed(startRefusjon), BELØP_TEN))));
        Intervall ansattPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(12));
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagGjeldendeAktiviteter(ansattPeriode);
        YrkesaktivitetDto yrkesaktivitet = lagYrkesaktivitet(ansattPeriode);

        LocalDateTimeline<KravOgUtfall> kravTidslinje = KravTjeneste.lagTidslinjeForYrkesaktivitet(
                kravperioder,
                yrkesaktivitet,
                gjeldendeAktiviteter,
                SKJÆRINGSTIDSPUNKT_BEREGNING,
                Optional.empty(), FagsakYtelseType.FORELDREPENGER);

        assertThat(kravTidslinje.size()).isEqualTo(1);
        LocalDateSegment<KravOgUtfall> kravSegment = kravTidslinje.iterator().next();
        assertThat(kravSegment.getValue().utfall()).isEqualTo(Utfall.GODKJENT);
        assertThat(kravSegment.getValue().refusjonskrav()).isEqualTo(BELØP_TEN);
        assertThat(kravSegment.getFom()).isEqualTo(startRefusjon);
    }

    @Test
    void skal_lage_tidslinje_med_krav_fra_skjæringstidspunkt_innsendt_i_for_sent() {
        LocalDate startRefusjon = SKJÆRINGSTIDSPUNKT_BEREGNING;
        LocalDate innsendingsdato = startRefusjon.plusMonths(4);
        List<PerioderForKravDto> kravperioder = List.of(
                new PerioderForKravDto(innsendingsdato, List.of(new RefusjonsperiodeDto(Intervall.fraOgMed(startRefusjon), BELØP_TEN))));
        Intervall ansattPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(12));
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagGjeldendeAktiviteter(ansattPeriode);
        YrkesaktivitetDto yrkesaktivitet = lagYrkesaktivitet(ansattPeriode);

        LocalDateTimeline<KravOgUtfall> kravTidslinje = KravTjeneste.lagTidslinjeForYrkesaktivitet(
                kravperioder,
                yrkesaktivitet,
                gjeldendeAktiviteter,
                SKJÆRINGSTIDSPUNKT_BEREGNING,
                Optional.empty(), FagsakYtelseType.FORELDREPENGER);

        assertThat(kravTidslinje.size()).isEqualTo(2);
        Iterator<LocalDateSegment<KravOgUtfall>> iterator = kravTidslinje.iterator();
        LocalDateSegment<KravOgUtfall> kravSegment1 = iterator.next();
        assertThat(kravSegment1.getValue().utfall()).isEqualTo(Utfall.UNDERKJENT);
        assertThat(kravSegment1.getValue().refusjonskrav()).isEqualTo(BELØP_TEN);
        assertThat(kravSegment1.getFom()).isEqualTo(startRefusjon);

        LocalDateSegment<KravOgUtfall> kravSegment2 = iterator.next();
        assertThat(kravSegment2.getValue().utfall()).isEqualTo(Utfall.GODKJENT);
        assertThat(kravSegment2.getValue().refusjonskrav()).isEqualTo(BELØP_TEN);
        assertThat(kravSegment2.getFom()).isEqualTo(innsendingsdato.minusMonths(3).withDayOfMonth(1));
    }

    @Test
    void skal_lage_tidslinje_med_krav_flere_overlappende_krav_alle_innsendt_i_tide() {
        LocalDate startRefusjon = SKJÆRINGSTIDSPUNKT_BEREGNING;
        var refusjonskrav1 = BELØP_TEN;
        var refusjonskrav2 = Beløp.fra(10_000);
        var refusjonskrav3 = Beløp.fra(20_000);
        List<PerioderForKravDto> kravperioder = List.of(
                new PerioderForKravDto(startRefusjon.plusMonths(1), List.of(new RefusjonsperiodeDto(Intervall.fraOgMed(startRefusjon), refusjonskrav1))),
                new PerioderForKravDto(startRefusjon.plusMonths(2), List.of(new RefusjonsperiodeDto(Intervall.fraOgMed(startRefusjon), refusjonskrav2))),
                new PerioderForKravDto(startRefusjon.plusMonths(3), List.of(new RefusjonsperiodeDto(Intervall.fraOgMed(startRefusjon), refusjonskrav3))));
        Intervall ansattPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(12));
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagGjeldendeAktiviteter(ansattPeriode);
        YrkesaktivitetDto yrkesaktivitet = lagYrkesaktivitet(ansattPeriode);

        LocalDateTimeline<KravOgUtfall> kravTidslinje = KravTjeneste.lagTidslinjeForYrkesaktivitet(
                kravperioder,
                yrkesaktivitet,
                gjeldendeAktiviteter,
                SKJÆRINGSTIDSPUNKT_BEREGNING,
                Optional.empty(), FagsakYtelseType.FORELDREPENGER);

        assertThat(kravTidslinje.size()).isEqualTo(1);
        Iterator<LocalDateSegment<KravOgUtfall>> iterator = kravTidslinje.iterator();
        LocalDateSegment<KravOgUtfall> kravSegment1 = iterator.next();
        assertThat(kravSegment1.getValue().utfall()).isEqualTo(Utfall.GODKJENT);
        assertThat(kravSegment1.getValue().refusjonskrav()).isEqualTo(refusjonskrav3);
        assertThat(kravSegment1.getFom()).isEqualTo(startRefusjon);
    }

    @Test
    void skal_lage_tidslinje_med_krav_flere_overlappende_krav_første_tidsnok_andre_for_sent_gir_godkjent() {
        LocalDate startRefusjon = SKJÆRINGSTIDSPUNKT_BEREGNING;
        var refusjonskrav1 = BELØP_TEN;
        var refusjonskrav2 = Beløp.fra(10_000);
        List<PerioderForKravDto> kravperioder = List.of(
                new PerioderForKravDto(startRefusjon.plusMonths(1), List.of(new RefusjonsperiodeDto(Intervall.fraOgMed(startRefusjon), refusjonskrav1))),
                new PerioderForKravDto(startRefusjon.plusMonths(5), List.of(new RefusjonsperiodeDto(Intervall.fraOgMed(startRefusjon), refusjonskrav2))));
        Intervall ansattPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(12));
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagGjeldendeAktiviteter(ansattPeriode);
        YrkesaktivitetDto yrkesaktivitet = lagYrkesaktivitet(ansattPeriode);

        LocalDateTimeline<KravOgUtfall> kravTidslinje = KravTjeneste.lagTidslinjeForYrkesaktivitet(
                kravperioder,
                yrkesaktivitet,
                gjeldendeAktiviteter,
                SKJÆRINGSTIDSPUNKT_BEREGNING,
                Optional.empty(), FagsakYtelseType.FORELDREPENGER);

        assertThat(kravTidslinje.size()).isEqualTo(1);
        Iterator<LocalDateSegment<KravOgUtfall>> iterator = kravTidslinje.iterator();
        LocalDateSegment<KravOgUtfall> kravSegment1 = iterator.next();
        assertThat(kravSegment1.getValue().utfall()).isEqualTo(Utfall.GODKJENT);
        assertThat(kravSegment1.getValue().refusjonskrav()).isEqualTo(refusjonskrav2);
        assertThat(kravSegment1.getFom()).isEqualTo(startRefusjon);
    }

    @Test
    void skal_lage_tidslinje_med_for_sent_krav_sendt_inn_deler_av_kravet_i_tide() {
        LocalDate startRefusjon = SKJÆRINGSTIDSPUNKT_BEREGNING;
        var refusjonskrav1 = BELØP_TEN;
        var refusjonskrav2 = Beløp.fra(10_000);
        LocalDate innsendingsdato2 = startRefusjon.plusMonths(7);
        LocalDate innsendingsdato1 = startRefusjon.minusMonths(2);
        LocalDate startRefusjon1 = startRefusjon.plusMonths(2);
        List<PerioderForKravDto> kravperioder = List.of(
                new PerioderForKravDto(innsendingsdato1, List.of(new RefusjonsperiodeDto(Intervall.fraOgMed(startRefusjon1), refusjonskrav1))),
                new PerioderForKravDto(innsendingsdato2, List.of(new RefusjonsperiodeDto(Intervall.fraOgMed(startRefusjon), refusjonskrav2))));
        Intervall ansattPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(12));
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagGjeldendeAktiviteter(ansattPeriode);
        YrkesaktivitetDto yrkesaktivitet = lagYrkesaktivitet(ansattPeriode);

        LocalDateTimeline<KravOgUtfall> kravTidslinje = KravTjeneste.lagTidslinjeForYrkesaktivitet(
                kravperioder,
                yrkesaktivitet,
                gjeldendeAktiviteter,
                SKJÆRINGSTIDSPUNKT_BEREGNING,
                Optional.empty(), FagsakYtelseType.FORELDREPENGER);

        assertThat(kravTidslinje.size()).isEqualTo(2);
        Iterator<LocalDateSegment<KravOgUtfall>> iterator = kravTidslinje.iterator();
        LocalDateSegment<KravOgUtfall> kravSegment1 = iterator.next();
        assertThat(kravSegment1.getValue().utfall()).isEqualTo(Utfall.UNDERKJENT);
        assertThat(kravSegment1.getValue().refusjonskrav()).isEqualTo(refusjonskrav2);
        assertThat(kravSegment1.getFom()).isEqualTo(startRefusjon);

        LocalDateSegment<KravOgUtfall> kravSegment2 = iterator.next();
        assertThat(kravSegment2.getValue().utfall()).isEqualTo(Utfall.GODKJENT);
        assertThat(kravSegment2.getValue().refusjonskrav()).isEqualTo(refusjonskrav2);
        assertThat(kravSegment2.getFom()).isEqualTo(startRefusjon1);
    }


    @Test
    void skal_lage_tidslinje_med_for_sent_krav_sendt_inn_deler_av_kravet_i_tide_med_nullbeløp() {
        LocalDate startRefusjon = SKJÆRINGSTIDSPUNKT_BEREGNING;
        var refusjonskrav1 = Beløp.ZERO;
        var refusjonskrav2 = Beløp.fra(10_000);
        LocalDate innsendingsdato2 = startRefusjon.plusMonths(7);
        LocalDate innsendingsdato1 = startRefusjon.minusMonths(2);
        LocalDate startRefusjon1 = startRefusjon.plusMonths(2);
        List<PerioderForKravDto> kravperioder = List.of(
                new PerioderForKravDto(innsendingsdato1, List.of(new RefusjonsperiodeDto(Intervall.fraOgMed(startRefusjon1), refusjonskrav1))),
                new PerioderForKravDto(innsendingsdato2, List.of(new RefusjonsperiodeDto(Intervall.fraOgMed(startRefusjon), refusjonskrav2))));
        Intervall ansattPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(12));
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagGjeldendeAktiviteter(ansattPeriode);
        YrkesaktivitetDto yrkesaktivitet = lagYrkesaktivitet(ansattPeriode);

        LocalDateTimeline<KravOgUtfall> kravTidslinje = KravTjeneste.lagTidslinjeForYrkesaktivitet(
                kravperioder,
                yrkesaktivitet,
                gjeldendeAktiviteter,
                SKJÆRINGSTIDSPUNKT_BEREGNING,
                Optional.empty(), FagsakYtelseType.FORELDREPENGER);

        assertThat(kravTidslinje.size()).isEqualTo(2);
        Iterator<LocalDateSegment<KravOgUtfall>> iterator = kravTidslinje.iterator();
        LocalDateSegment<KravOgUtfall> kravSegment1 = iterator.next();
        assertThat(kravSegment1.getValue().utfall()).isEqualTo(Utfall.UNDERKJENT);
        assertThat(kravSegment1.getValue().refusjonskrav()).isEqualTo(refusjonskrav2);
        assertThat(kravSegment1.getFom()).isEqualTo(startRefusjon);

        LocalDateSegment<KravOgUtfall> kravSegment2 = iterator.next();
        assertThat(kravSegment2.getValue().utfall()).isEqualTo(Utfall.GODKJENT);
        assertThat(kravSegment2.getValue().refusjonskrav()).isEqualTo(refusjonskrav2);
        assertThat(kravSegment2.getFom()).isEqualTo(innsendingsdato2.minusMonths(3).withDayOfMonth(1));
    }

    @Test
    void skal_lage_tidslinje_med_krav_fra_skjæringstidspunkt_innsendt_i_for_sent_med_overstyrt_gyldighet() {
        LocalDate startRefusjon = SKJÆRINGSTIDSPUNKT_BEREGNING;
        var refusjonskrav = BELØP_TEN;
        LocalDate innsendingsdato = startRefusjon.plusMonths(4);
        Optional<LocalDate> overstyrtFom = Optional.of(startRefusjon);
        List<PerioderForKravDto> kravperioder = List.of(
                new PerioderForKravDto(innsendingsdato, List.of(new RefusjonsperiodeDto(Intervall.fraOgMed(startRefusjon), refusjonskrav))));
        Intervall ansattPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(12));
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagGjeldendeAktiviteter(ansattPeriode);
        YrkesaktivitetDto yrkesaktivitet = lagYrkesaktivitet(ansattPeriode);

        LocalDateTimeline<KravOgUtfall> kravTidslinje = KravTjeneste.lagTidslinjeForYrkesaktivitet(
                kravperioder,
                yrkesaktivitet,
                gjeldendeAktiviteter,
                SKJÆRINGSTIDSPUNKT_BEREGNING,
                overstyrtFom, FagsakYtelseType.FORELDREPENGER);

        assertThat(kravTidslinje.size()).isEqualTo(1);
        Iterator<LocalDateSegment<KravOgUtfall>> iterator = kravTidslinje.iterator();
        LocalDateSegment<KravOgUtfall> kravSegment1 = iterator.next();
        assertThat(kravSegment1.getValue().utfall()).isEqualTo(Utfall.GODKJENT);
        assertThat(kravSegment1.getValue().refusjonskrav()).isEqualTo(refusjonskrav);
        assertThat(kravSegment1.getFom()).isEqualTo(startRefusjon);
    }

    private YrkesaktivitetDto lagYrkesaktivitet(Intervall ansattPeriode) {
        return YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(ARBEIDSGIVER1)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(lagAnsettelsesPeriode(ansattPeriode))
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nullRef())
                .build();
    }

    private BeregningAktivitetAggregatDto lagGjeldendeAktiviteter(Intervall ansattPeriode) {
        return BeregningAktivitetAggregatDto.builder()
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
                .leggTilAktivitet(lagBeregningsaktivitet(ansattPeriode, ARBEIDSGIVER1))
                .build();
    }

    private AktivitetsAvtaleDtoBuilder lagAnsettelsesPeriode(Intervall ansattPeriode) {
        return AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(ansattPeriode)
                .medErAnsettelsesPeriode(true);
    }

    private BeregningAktivitetDto lagBeregningsaktivitet(Intervall periode, Arbeidsgiver arbeidsgiver) {
        return BeregningAktivitetDto.builder()
                .medPeriode(periode)
                .medArbeidsgiver(arbeidsgiver)
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                .medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef())
                .build();
    }


}
