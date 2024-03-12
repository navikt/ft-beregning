package no.nav.folketrygdloven.kalkulator.steg.tilkommetInntekt;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.felles.inntektgradering.DagsatsPrKategoriOgArbeidsgiver;
import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

class UtvidetInntektsperiodeUtlederTest {

    @Test
    void skal_ikke_utvide_periode_dersom_tom_tidslinje() {
        var tidslinje = UtvidetInntektsperiodeUtleder.lagGodkjenteInntektsperiodeTidslinje(new LocalDateTimeline<>(List.of()), new PleiepengerSyktBarnGrunnlag(List.of()), 5);

        assertThat(tidslinje.isEmpty()).isTrue();
    }


    @Test
    void skal_godkjenne_hull_mellom_perioder_dersom_hull_lik_2_måneder_og_periode_fra_siste_ikke_rapporterte_måned() {
        var juli = new LocalDateSegment<>(LocalDate.of(2022, 7, 1), LocalDate.of(2022, 7, 31), Set.of(new DagsatsPrKategoriOgArbeidsgiver(Inntektskategori.ARBEIDSTAKER, Arbeidsgiver.virksomhet("123432"), Beløp.fra(1))));
        var oktober = new LocalDateSegment<>(LocalDate.of(2022, 10, 1), LocalDate.of(2022, 10, 15), Set.of(new DagsatsPrKategoriOgArbeidsgiver(Inntektskategori.ARBEIDSTAKER, Arbeidsgiver.virksomhet("123432"), Beløp.fra(1))));
        var inntektTidslinje = new LocalDateTimeline<>(List.of(juli, oktober));
        var iDag = LocalDate.now();
        var tidslinje = UtvidetInntektsperiodeUtleder.lagGodkjenteInntektsperiodeTidslinje(inntektTidslinje, new PleiepengerSyktBarnGrunnlag(List.of()), iDag.getDayOfMonth());

        var segmenter = tidslinje.compress().toSegments();
        assertThat(segmenter.size()).isEqualTo(2);
        var iterator = segmenter.iterator();
        var førsteSegment = iterator.next();
        assertThat(førsteSegment.getFom()).isEqualTo(juli.getFom());
        assertThat(førsteSegment.getTom()).isEqualTo(oktober.getTom());

        var andreSegment = iterator.next();
        assertThat(andreSegment.getFom()).isEqualTo(iDag.withDayOfMonth(1));
        assertThat(andreSegment.getTom()).isEqualTo(TIDENES_ENDE);
    }

    @Test
    void skal_godkjenne_hull_mellom_perioder_dersom_hull_mellom_2_og_3_måneder() {
        var juli = new LocalDateSegment<>(LocalDate.of(2022, 7, 1), LocalDate.of(2022, 7, 31), Set.of(new DagsatsPrKategoriOgArbeidsgiver(Inntektskategori.ARBEIDSTAKER, Arbeidsgiver.virksomhet("123432"),  Beløp.fra(1))));
        var oktober = new LocalDateSegment<>(LocalDate.of(2022, 10, 15), LocalDate.of(2022, 10, 30), Set.of(new DagsatsPrKategoriOgArbeidsgiver(Inntektskategori.ARBEIDSTAKER, Arbeidsgiver.virksomhet("123432"),  Beløp.fra(1))));
        var inntektTidslinje = new LocalDateTimeline<>(List.of(juli, oktober));
        var iDag = LocalDate.now();
        var tidslinje = UtvidetInntektsperiodeUtleder.lagGodkjenteInntektsperiodeTidslinje(inntektTidslinje, new PleiepengerSyktBarnGrunnlag(List.of()), iDag.getDayOfMonth());

        var segmenter = tidslinje.compress().toSegments();
        assertThat(segmenter.size()).isEqualTo(2);
        var iterator = segmenter.iterator();
        var førsteSegment = iterator.next();
        assertThat(førsteSegment.getFom()).isEqualTo(juli.getFom());
        assertThat(førsteSegment.getTom()).isEqualTo(oktober.getTom());

        var andreSegment = iterator.next();
        assertThat(andreSegment.getFom()).isEqualTo(iDag.withDayOfMonth(1));
        assertThat(andreSegment.getTom()).isEqualTo(TIDENES_ENDE);
    }

    @Test
    void skal_ikke_godkjenne_hull_mellom_perioder_dersom_hull_på_tre_måneder() {
        var juli = new LocalDateSegment<>(LocalDate.of(2022, 7, 1), LocalDate.of(2022, 7, 31), Set.of(new DagsatsPrKategoriOgArbeidsgiver(Inntektskategori.ARBEIDSTAKER, Arbeidsgiver.virksomhet("123432"),  Beløp.fra(1))));
        var november = new LocalDateSegment<>(LocalDate.of(2022, 11, 1), LocalDate.of(2022, 11, 15), Set.of(new DagsatsPrKategoriOgArbeidsgiver(Inntektskategori.ARBEIDSTAKER, Arbeidsgiver.virksomhet("123432"),  Beløp.fra(1))));
        var inntektTidslinje = new LocalDateTimeline<>(List.of(juli, november));
        var iDag = LocalDate.now();
        var tidslinje = UtvidetInntektsperiodeUtleder.lagGodkjenteInntektsperiodeTidslinje(inntektTidslinje, new PleiepengerSyktBarnGrunnlag(List.of()), iDag.getDayOfMonth());

        var segmenter = tidslinje.compress().toSegments();
        assertThat(segmenter.size()).isEqualTo(3);
        var iterator = segmenter.iterator();
        var førsteSegment = iterator.next();
        assertThat(førsteSegment.getFom()).isEqualTo(juli.getFom());
        assertThat(førsteSegment.getTom()).isEqualTo(juli.getTom());

        var andreSegment = iterator.next();
        assertThat(andreSegment.getFom()).isEqualTo(november.getFom());
        assertThat(andreSegment.getTom()).isEqualTo(november.getTom());

        var tredjeSegment = iterator.next();
        assertThat(tredjeSegment.getFom()).isEqualTo(iDag.withDayOfMonth(1));
        assertThat(tredjeSegment.getTom()).isEqualTo(TIDENES_ENDE);
    }

    @Test
    void skal_godkjenne_periode_fram_til_siste_ikke_rapporterte_måned_dersom_kortere_enn_3_måneder() {
        var iDag = LocalDate.now();
        var treMånederSiden = new LocalDateSegment<>(iDag.withDayOfMonth(1).minusMonths(3),
                iDag.minusMonths(3).with(TemporalAdjusters.lastDayOfMonth()),
                Set.of(new DagsatsPrKategoriOgArbeidsgiver(Inntektskategori.ARBEIDSTAKER, Arbeidsgiver.virksomhet("123432"),  Beløp.fra(1))));
        var inntektTidslinje = new LocalDateTimeline<>(List.of(treMånederSiden));
        var tidslinje = UtvidetInntektsperiodeUtleder.lagGodkjenteInntektsperiodeTidslinje(inntektTidslinje, new PleiepengerSyktBarnGrunnlag(List.of()), iDag.getDayOfMonth());

        var segmenter = tidslinje.compress().toSegments();
        assertThat(segmenter.size()).isEqualTo(1);
        var iterator = segmenter.iterator();
        var førsteSegment = iterator.next();
        assertThat(førsteSegment.getFom()).isEqualTo(treMånederSiden.getFom());
        assertThat(førsteSegment.getTom()).isEqualTo(TIDENES_ENDE);
    }

    @Test
    void skal_godkjenne_periode_med_fullt_fravær() {
        var arbeidsgiver = Arbeidsgiver.virksomhet("123432");
        var juli = new LocalDateSegment<>(LocalDate.of(2022, 7, 1), LocalDate.of(2022, 7, 31), Set.of(new DagsatsPrKategoriOgArbeidsgiver(Inntektskategori.ARBEIDSTAKER, arbeidsgiver,  Beløp.fra(1))));
        LocalDateTimeline<Set<DagsatsPrKategoriOgArbeidsgiver>> inntektTidslinje = new LocalDateTimeline<>(List.of());
        var iDag = LocalDate.now();
        var fulltFravær = lagUtbetalingsgradForFulltFravær(arbeidsgiver);
        var utbetalingsgradGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(
                fulltFravær
        ));
        var tidslinje = UtvidetInntektsperiodeUtleder.lagGodkjenteInntektsperiodeTidslinje(inntektTidslinje, utbetalingsgradGrunnlag, iDag.getDayOfMonth());

        var segmenter = tidslinje.compress().toSegments();
        assertThat(segmenter.size()).isEqualTo(1);
        var iterator = segmenter.iterator();
        var førsteSegment = iterator.next();
        assertThat(førsteSegment.getFom()).isEqualTo(juli.getFom());
        assertThat(førsteSegment.getTom()).isEqualTo(juli.getTom());
    }

    private static UtbetalingsgradPrAktivitetDto lagUtbetalingsgradForFulltFravær(Arbeidsgiver arbeidsgiver) {
        return new UtbetalingsgradPrAktivitetDto(
                new AktivitetDto(
                        arbeidsgiver, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID
                ),
                List.of(new PeriodeMedUtbetalingsgradDto(
                        Intervall.fraOgMedTilOgMed(LocalDate.of(2022, 7, 1), LocalDate.of(2022, 7, 31)),
                        Utbetalingsgrad.valueOf(100), Aktivitetsgrad.ZERO
                ))
        );
    }


}
