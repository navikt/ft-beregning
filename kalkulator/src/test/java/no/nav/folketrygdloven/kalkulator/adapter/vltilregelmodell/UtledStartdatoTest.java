package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

class UtledStartdatoTest {


    public static final LocalDate STP = LocalDate.now();
    public static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet("123456789");

    @Test
    void skal_finne_startdato_ved_en_periode_over_stp() {
        var ref = InternArbeidsforholdRefDto.nyRef();
        var yrkesaktivitet = byggYrkesaktivitet(List.of(Intervall.fraOgMedTilOgMed(STP.minusDays(10), STP.plusDays(10))), ref, ARBEIDSGIVER);

        var startdato = UtledStartdato.utledStartdato(ARBEIDSGIVER, ref.getReferanse(), lagIay(List.of(yrkesaktivitet)), STP);

        assertThat(startdato).isEqualTo(STP.minusDays(10));
    }

    @Test
    void skal_ikke_finne_startdato_ved_start_etter_stp() {
        var ref = InternArbeidsforholdRefDto.nyRef();
        var yrkesaktivitet = byggYrkesaktivitet(List.of(Intervall.fraOgMedTilOgMed(STP.plusDays(1), STP.plusDays(10))), ref, ARBEIDSGIVER);

        var startdato = UtledStartdato.utledStartdato(ARBEIDSGIVER, ref.getReferanse(), lagIay(List.of(yrkesaktivitet)), STP);

        assertThat(startdato).isNull();
    }

    @Test
    void skal_finne_startdato_ved_slutt_før_stp() {
        var ref = InternArbeidsforholdRefDto.nyRef();
        var yrkesaktivitet = byggYrkesaktivitet(List.of(Intervall.fraOgMedTilOgMed(STP.minusDays(10), STP.minusDays(1))), ref, ARBEIDSGIVER);

        var startdato = UtledStartdato.utledStartdato(ARBEIDSGIVER, ref.getReferanse(), lagIay(List.of(yrkesaktivitet)), STP);

        assertThat(startdato).isEqualTo(STP.minusDays(10));
    }

    @Test
    void skal_finne_startdato_ved_sammenhengende_periode_for_samme_arbeidsgiver_uten_referanse() {
        var ref = InternArbeidsforholdRefDto.nyRef();
        var ref2 = InternArbeidsforholdRefDto.nyRef();
        var yrkesaktivitet1 = byggYrkesaktivitet(List.of(Intervall.fraOgMedTilOgMed(STP.minusDays(10), STP.minusDays(9))), ref, ARBEIDSGIVER);
        var yrkesaktivitet2 = byggYrkesaktivitet(List.of(Intervall.fraOgMedTilOgMed(STP.minusDays(8), STP.plusDays(1))), ref2, ARBEIDSGIVER);

        var startdato = UtledStartdato.utledStartdato(ARBEIDSGIVER, null, lagIay(List.of(yrkesaktivitet1, yrkesaktivitet2)), STP);

        assertThat(startdato).isEqualTo(STP.minusDays(10));
    }

    @Test
    void skal_finne_siste_startdato_ved_flere_perioder() {
        var ref = InternArbeidsforholdRefDto.nyRef();
        var yrkesaktivitet1 = byggYrkesaktivitet(List.of(
                Intervall.fraOgMedTilOgMed(STP.minusDays(10), STP.minusDays(9)),
                Intervall.fraOgMedTilOgMed(STP.minusDays(5), STP.plusDays(9))), ref, ARBEIDSGIVER);

        var startdato = UtledStartdato.utledStartdato(ARBEIDSGIVER, null, lagIay(List.of(yrkesaktivitet1)), STP);

        assertThat(startdato).isEqualTo(STP.minusDays(5));
    }

    @Test
    void skal_finne_startdato_ved_overlappende_perioder_for_samme_arbeidsgiver_uten_referanse() {
        var ref = InternArbeidsforholdRefDto.nyRef();
        var ref2 = InternArbeidsforholdRefDto.nyRef();
        var yrkesaktivitet1 = byggYrkesaktivitet(List.of(Intervall.fraOgMedTilOgMed(STP.minusDays(10), STP.minusDays(9))), ref, ARBEIDSGIVER);
        var yrkesaktivitet2 = byggYrkesaktivitet(List.of(Intervall.fraOgMedTilOgMed(STP.minusDays(9), STP.plusDays(1))), ref2, ARBEIDSGIVER);

        var startdato = UtledStartdato.utledStartdato(ARBEIDSGIVER, null, lagIay(List.of(yrkesaktivitet1, yrkesaktivitet2)), STP);

        assertThat(startdato).isEqualTo(STP.minusDays(10));
    }
    private static InntektArbeidYtelseGrunnlagDto lagIay(List<YrkesaktivitetDto> yrkesaktiviteter) {
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        yrkesaktiviteter.forEach(aktørArbeidBuilder::leggTilYrkesaktivitet);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER).leggTilAktørArbeid(aktørArbeidBuilder)).build();
        return iayGrunnlag;
    }

    private static YrkesaktivitetDto byggYrkesaktivitet(List<Intervall> ansettelsesperioder, InternArbeidsforholdRefDto ref, Arbeidsgiver arbeidsgiver) {
        var yrkesaktivitetBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsforholdId(ref)
                .medArbeidsgiver(arbeidsgiver);
        ansettelsesperioder.forEach(p -> leggTilAnsettelsesperiode(yrkesaktivitetBuilder, p));
        var yrkesaktivitet = yrkesaktivitetBuilder.build();
        return yrkesaktivitet;
    }

    private static void leggTilAnsettelsesperiode(YrkesaktivitetDtoBuilder yrkesaktivitetBuilder, Intervall p) {
        var aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        aktivitetsAvtaleBuilder.medPeriode(p)
                .medErAnsettelsesPeriode(true);
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(aktivitetsAvtaleBuilder);
    }
}
