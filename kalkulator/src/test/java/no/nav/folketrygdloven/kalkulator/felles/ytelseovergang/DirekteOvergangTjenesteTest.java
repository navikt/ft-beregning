package no.nav.folketrygdloven.kalkulator.felles.ytelseovergang;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.AnvistAndel;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

class DirekteOvergangTjenesteTest {


    @Test
    void skal_returnere_tom_tidslinje_uten_ytelser() {
        var tidslinje = DirekteOvergangTjeneste.direkteUtbetalingTidslinje(List.of(), y -> alwaysTrue());

        assertThat(tidslinje.isEmpty()).isTrue();
    }

    @Test
    void skal_returnere_tidslinje_med_en_sammenhengende_periode_og_frilans() {
        var fom = LocalDate.now();
        var periode = Intervall.fraOgMedTilOgMed(fom, fom.plusDays(5));
        var ytelse = lagYtelse(periode, List.of(frilansAndel()));
        var tidslinje = DirekteOvergangTjeneste.direkteUtbetalingTidslinje(List.of(ytelse), y -> alwaysTrue());

        var segmenter = tidslinje.toSegments();
        assertThat(segmenter.size()).isEqualTo(1);
        var segment = segmenter.iterator().next();
        assertThat(segment.getFom()).isEqualTo(periode.getFomDato());
        assertThat(segment.getTom()).isEqualTo(periode.getTomDato());
        assertThat(segment.getValue().size()).isEqualTo(1);
        var value = segment.getValue().iterator().next();
        assertThat(value.arbeidsgiver()).isNull();
        assertThat(value.inntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
    }

    @Test
    void skal_returnere_tidslinje_med_en_sammenhengende_periode_og_en_arbeidsgiver_uten_refusjon() {
        var fom = LocalDate.now();
        var periode = Intervall.fraOgMedTilOgMed(fom, fom.plusDays(5));
        var arbeidsgiver = Arbeidsgiver.virksomhet("12345587");
        var ytelse = lagYtelse(periode, List.of(arbeidstakerAndel(arbeidsgiver, Stillingsprosent.ZERO)));
        var tidslinje = DirekteOvergangTjeneste.direkteUtbetalingTidslinje(List.of(ytelse), y -> alwaysTrue());

        var segmenter = tidslinje.toSegments();
        assertThat(segmenter.size()).isEqualTo(1);
        var segment = segmenter.iterator().next();
        assertThat(segment.getFom()).isEqualTo(periode.getFomDato());
        assertThat(segment.getTom()).isEqualTo(periode.getTomDato());
        assertThat(segment.getValue().size()).isEqualTo(1);
        var value = segment.getValue().iterator().next();
        assertThat(value.arbeidsgiver()).isEqualTo(arbeidsgiver);
        assertThat(value.inntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
    }

    @Test
    void skal_returnere_tidslinje_med_en_sammenhengende_periode_og_en_arbeidsgiver_med_delvis_refusjon() {
        var fom = LocalDate.now();
        var periode = Intervall.fraOgMedTilOgMed(fom, fom.plusDays(5));
        var arbeidsgiver = Arbeidsgiver.virksomhet("12345587");
        var ytelse = lagYtelse(periode, List.of(arbeidstakerAndel(arbeidsgiver, Stillingsprosent.fra(50))));
        var tidslinje = DirekteOvergangTjeneste.direkteUtbetalingTidslinje(List.of(ytelse), y -> alwaysTrue());

        var segmenter = tidslinje.toSegments();
        assertThat(segmenter.size()).isEqualTo(1);
        var segment = segmenter.iterator().next();
        assertThat(segment.getFom()).isEqualTo(periode.getFomDato());
        assertThat(segment.getTom()).isEqualTo(periode.getTomDato());
        assertThat(segment.getValue().size()).isEqualTo(1);
        var value = segment.getValue().iterator().next();
        assertThat(value.arbeidsgiver()).isEqualTo(arbeidsgiver);
        assertThat(value.inntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
    }

    @Test
    void skal_returnere_tom_tidslinje_for_en_arbeidsgiver_med_full_refusjon() {
        var fom = LocalDate.now();
        var periode = Intervall.fraOgMedTilOgMed(fom, fom.plusDays(5));
        var arbeidsgiver = Arbeidsgiver.virksomhet("12345587");
        var ytelse = lagYtelse(periode, List.of(arbeidstakerAndel(arbeidsgiver, Stillingsprosent.HUNDRED)));
        var tidslinje = DirekteOvergangTjeneste.direkteUtbetalingTidslinje(List.of(ytelse), y -> alwaysTrue());

        assertThat(tidslinje.isEmpty()).isTrue();
    }

    @Test
    void skal_returnere_tidslinje_med_udefinert_aktivitetstatus_dersom_ingen_anviste_andeler() {
        var fom = LocalDate.now();
        var periode = Intervall.fraOgMedTilOgMed(fom, fom.plusDays(5));
        var ytelse = lagYtelse(periode, null);
        var tidslinje = DirekteOvergangTjeneste.direkteUtbetalingTidslinje(List.of(ytelse), y -> alwaysTrue());

        var segmenter = tidslinje.toSegments();
        assertThat(segmenter.size()).isEqualTo(1);
        var segment = segmenter.iterator().next();
        assertThat(segment.getFom()).isEqualTo(periode.getFomDato());
        assertThat(segment.getTom()).isEqualTo(periode.getTomDato());
        assertThat(segment.getValue().size()).isEqualTo(1);
        var value = segment.getValue().iterator().next();
        assertThat(value.arbeidsgiver()).isNull();
        assertThat(value.inntektskategori()).isEqualTo(Inntektskategori.UDEFINERT);
    }

    @Test
    void skal_returnere_tom_tidslinje_dersom_ytelse_filtreres_bort_i_predicate() {
        var fom = LocalDate.now();
        var periode = Intervall.fraOgMedTilOgMed(fom, fom.plusDays(5));
        var ytelse = lagYtelse(periode, null);

        var tidslinje = DirekteOvergangTjeneste.direkteUtbetalingTidslinje(List.of(ytelse), y -> !y.getYtelseType().equals(YtelseType.FORELDREPENGER));

        assertThat(tidslinje.isEmpty()).isTrue();
    }

    @Test
    void skal_returnere_tidslinje_med_en_sammenhengende_periode_og_to_arbeidsgivere() {
        var fom = LocalDate.now();
        var periode = Intervall.fraOgMedTilOgMed(fom, fom.plusDays(5));
        var arbeidsgiver1 = Arbeidsgiver.virksomhet("12345587");
        var arbeidsgiver2 = Arbeidsgiver.virksomhet("76345114");

        var ytelse = lagYtelse(periode, List.of(arbeidstakerAndel(arbeidsgiver1, Stillingsprosent.ZERO), arbeidstakerAndel(arbeidsgiver2, Stillingsprosent.ZERO)));
        var tidslinje = DirekteOvergangTjeneste.direkteUtbetalingTidslinje(List.of(ytelse), y -> alwaysTrue());

        var segmenter = tidslinje.toSegments();
        assertThat(segmenter.size()).isEqualTo(1);
        var segment = segmenter.iterator().next();
        assertThat(segment.getFom()).isEqualTo(periode.getFomDato());
        assertThat(segment.getTom()).isEqualTo(periode.getTomDato());
        assertThat(segment.getValue().size()).isEqualTo(2);
        var iterator = segment.getValue().iterator();
        var value1 = iterator.next();
        assertThat(value1.arbeidsgiver()).isEqualTo(arbeidsgiver1);
        assertThat(value1.inntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);

        var value2 = iterator.next();
        assertThat(value2.arbeidsgiver()).isEqualTo(arbeidsgiver2);
        assertThat(value2.inntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
    }

    @Test
    void skal_returnere_tidslinje_med_to_perioder_og_to_arbeidsgivere_ulike_perioder() {
        var fom = LocalDate.now();
        var periode1 = Intervall.fraOgMedTilOgMed(fom, fom.plusDays(5));
        var tom = fom.plusDays(10);
        var periode2 = Intervall.fraOgMedTilOgMed(fom.plusDays(6), tom);

        var arbeidsgiver1 = Arbeidsgiver.virksomhet("12345587");
        var arbeidsgiver2 = Arbeidsgiver.virksomhet("76345114");


        var ytelse = YtelseDtoBuilder.ny().medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medYtelseType(YtelseType.FORELDREPENGER)
                .medVedtaksDagsats(Beløp.fra(1000))
                .leggTilYtelseAnvist(YtelseAnvistDtoBuilder.ny().medAnvistPeriode(periode1)
                        .medBeløp(Beløp.fra(5000))
                        .medDagsats(Beløp.fra(1000))
                        .medUtbetalingsgradProsent(Stillingsprosent.HUNDRED)
                        .medAnvisteAndeler(List.of(arbeidstakerAndel(arbeidsgiver1, Stillingsprosent.ZERO), arbeidstakerAndel(arbeidsgiver2, Stillingsprosent.ZERO))).build())
                .leggTilYtelseAnvist(YtelseAnvistDtoBuilder.ny().medAnvistPeriode(periode2)
                        .medBeløp(Beløp.fra(5000))
                        .medDagsats(Beløp.fra(1000))
                        .medUtbetalingsgradProsent(Stillingsprosent.HUNDRED)
                        .medAnvisteAndeler(List.of(arbeidstakerAndel(arbeidsgiver1, Stillingsprosent.ZERO))).build()).build();

        var tidslinje = DirekteOvergangTjeneste.direkteUtbetalingTidslinje(List.of(ytelse), y -> alwaysTrue());

        var segmenter = tidslinje.toSegments();
        assertThat(segmenter.size()).isEqualTo(2);
        var segmentIterator = segmenter.iterator();
        var segment1 = segmentIterator.next();
        assertThat(segment1.getFom()).isEqualTo(periode1.getFomDato());
        assertThat(segment1.getTom()).isEqualTo(periode1.getTomDato());
        assertThat(segment1.getValue().size()).isEqualTo(2);
        var iterator = segment1.getValue().iterator();
        var value1 = iterator.next();
        assertThat(value1.arbeidsgiver()).isEqualTo(arbeidsgiver1);
        assertThat(value1.inntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);

        var value2 = iterator.next();
        assertThat(value2.arbeidsgiver()).isEqualTo(arbeidsgiver2);
        assertThat(value2.inntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);


        var segment2 = segmentIterator.next();
        assertThat(segment2.getFom()).isEqualTo(periode2.getFomDato());
        assertThat(segment2.getTom()).isEqualTo(periode2.getTomDato());
        assertThat(segment2.getValue().size()).isEqualTo(1);
        var valueIterator = segment2.getValue().iterator();
        var value1Segment2 = valueIterator.next();
        assertThat(value1Segment2.arbeidsgiver()).isEqualTo(arbeidsgiver1);
        assertThat(value1Segment2.inntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
    }

    private static AnvistAndel frilansAndel() {
        return new AnvistAndel(null, InternArbeidsforholdRefDto.nullRef(), Beløp.fra(5000), Beløp.fra(1000), Stillingsprosent.ZERO, Inntektskategori.FRILANSER);
    }

    private static AnvistAndel arbeidstakerAndel(Arbeidsgiver arbeidgiver, Stillingsprosent refusjonsgrad) {
        return new AnvistAndel(arbeidgiver, InternArbeidsforholdRefDto.nullRef(), Beløp.fra(5000), Beløp.fra(1000), refusjonsgrad, Inntektskategori.ARBEIDSTAKER);
    }

    private static YtelseDto lagYtelse(Intervall periode, List<AnvistAndel> andeler) {
        return YtelseDtoBuilder.ny().medPeriode(periode)
                .medYtelseType(YtelseType.FORELDREPENGER)
                .medVedtaksDagsats(Beløp.fra(1000))
                .leggTilYtelseAnvist(YtelseAnvistDtoBuilder.ny().medAnvistPeriode(periode)
                        .medBeløp(Beløp.fra(5000))
                        .medDagsats(Beløp.fra(1000))
                        .medUtbetalingsgradProsent(Stillingsprosent.HUNDRED)
                        .medAnvisteAndeler(andeler).build()).build();
    }

    private static boolean alwaysTrue() {
        return true;
    }
}
