package no.nav.folketrygdloven.kalkulator.guitjenester;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;

class FinnArbeidsprosenterFPTest {
    public static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet("123456789");
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(LocalDate.now().minusYears(1).getYear(), Month.MAY, 10);
    private BeregningsgrunnlagPrStatusOgAndelDto arbeidstakerAndel;

    @BeforeEach
    void setUp() {
        arbeidstakerAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(1L)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER))
            .build();
    }

    @Test
    void arbeidsprosenter_for_uavsluttet_periode() {
        var arbeidsprosent = Aktivitetsgrad.fra(20);
        var graderinger = List.of(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(1), arbeidsprosent));

        var arbeidsandeler = new FinnArbeidsprosenterFP().finnArbeidsprosenterIPeriode(arbeidstakerAndel, lagForeldrepengerGrunnlag(graderinger),
            Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT));

        assertThat(arbeidsandeler).containsExactlyInAnyOrder(BigDecimal.ZERO, arbeidsprosent.verdi());
    }

    @Test
    void arbeidsprosenter_for_uavsluttet_periode_og_uavsluttet_gradering() {
        var arbeidsprosent = Aktivitetsgrad.fra(20);
        var graderinger = List.of(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE, arbeidsprosent));

        var arbeidsandeler = new FinnArbeidsprosenterFP().finnArbeidsprosenterIPeriode(arbeidstakerAndel, lagForeldrepengerGrunnlag(graderinger),
            Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT));

        assertThat(arbeidsandeler).containsExactly(arbeidsprosent.verdi());
    }

    @Test
    void arbeidsprosenter_for_sammenhengende_gradering_med_hull_på_slutten() {
        var arbeidsprosent1 = Aktivitetsgrad.fra(20);
        var arbeidsprosent2 = Aktivitetsgrad.fra(30);
        var arbeidsprosent3 = Aktivitetsgrad.fra(40);
        var arbeidsprosent4 = Aktivitetsgrad.fra(50);
        var graderinger = List.of(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(1), arbeidsprosent1),
            new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT.plusWeeks(1).plusDays(1), SKJÆRINGSTIDSPUNKT.plusWeeks(2), arbeidsprosent2),
            new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT.plusWeeks(2).plusDays(1), SKJÆRINGSTIDSPUNKT.plusWeeks(3), arbeidsprosent3),
            new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT.plusWeeks(3).plusDays(1), SKJÆRINGSTIDSPUNKT.plusWeeks(4), arbeidsprosent4));

        var arbeidsandeler = new FinnArbeidsprosenterFP().finnArbeidsprosenterIPeriode(arbeidstakerAndel, lagForeldrepengerGrunnlag(graderinger),
            Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(4).plusDays(1)));

        assertThat(arbeidsandeler).containsExactlyInAnyOrder(BigDecimal.ZERO, arbeidsprosent1.verdi(), arbeidsprosent2.verdi(),
            arbeidsprosent3.verdi(), arbeidsprosent4.verdi());
    }

    @Test
    void arbeidsprosenter_for_ikke_sammenhengende_gradering() {
        var arbeidsprosent1 = Aktivitetsgrad.fra(20);
        var arbeidsprosent2 = Aktivitetsgrad.fra(30);
        var arbeidsprosent3 = Aktivitetsgrad.fra(40);
        var arbeidsprosent4 = Aktivitetsgrad.fra(50);
        var arbeidsprosent5 = Aktivitetsgrad.fra(60);
        var graderinger = List.of(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(1), arbeidsprosent1),
            new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT.plusWeeks(1).plusDays(1), SKJÆRINGSTIDSPUNKT.plusWeeks(2), arbeidsprosent2),
            new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT.plusWeeks(2).plusDays(1), SKJÆRINGSTIDSPUNKT.plusWeeks(3), arbeidsprosent3),
            new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT.plusWeeks(3).plusDays(1), SKJÆRINGSTIDSPUNKT.plusWeeks(4), arbeidsprosent4),
            new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT.plusWeeks(4).plusDays(2), SKJÆRINGSTIDSPUNKT.plusWeeks(5), arbeidsprosent5));

        var arbeidsandeler = new FinnArbeidsprosenterFP().finnArbeidsprosenterIPeriode(arbeidstakerAndel, lagForeldrepengerGrunnlag(graderinger),
            Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(5)));

        assertThat(arbeidsandeler).containsExactlyInAnyOrder(BigDecimal.ZERO, arbeidsprosent1.verdi(), arbeidsprosent2.verdi(),
            arbeidsprosent3.verdi(), arbeidsprosent4.verdi(), arbeidsprosent5.verdi());
    }


    private ForeldrepengerGrunnlag lagForeldrepengerGrunnlag(List<AndelGradering.Gradering> graderinger) {
        var graderingBuilder = AndelGradering.builder().medArbeidsgiver(ARBEIDSGIVER).medStatus(AktivitetStatus.ARBEIDSTAKER);
        graderinger.forEach(graderingBuilder::leggTilGradering);
        return new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false, new AktivitetGradering(List.of(graderingBuilder.build())));
    }
}
