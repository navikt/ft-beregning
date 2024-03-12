package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonPeriode;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonPeriodeEndring;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

class RefusjonTidslinjeTjenesteTest {
    private static final Arbeidsgiver AG1 = Arbeidsgiver.virksomhet("999999999");
    private static final Arbeidsgiver AG2 = Arbeidsgiver.virksomhet("999999998");
    private static final InternArbeidsforholdRefDto REF1 = InternArbeidsforholdRefDto.nyRef();
    private static final InternArbeidsforholdRefDto REF2 = InternArbeidsforholdRefDto.nyRef();
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    public static final YtelsespesifiktGrunnlag YTELSESPESIFIKT_GRUNNLAG = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
    private static BeregningsgrunnlagDto originaltBG;
    private static BeregningsgrunnlagDto revurderingBG;


    @BeforeEach
    public void setup() {
        originaltBG = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(Beløp.fra(GrunnbeløpTestKonstanter.GRUNNBELØP_2018))
                .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medHjemmel(Hjemmel.F_14_7)
                .build(originaltBG);
        BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medHjemmel(Hjemmel.F_14_7)
                .build(originaltBG);

        revurderingBG = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(Beløp.fra(GrunnbeløpTestKonstanter.GRUNNBELØP_2018))
                .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medHjemmel(Hjemmel.F_14_7)
                .build(revurderingBG);
        BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medHjemmel(Hjemmel.F_14_7)
                .build(revurderingBG);
    }

    @Test
    public void tester_at_timeline_lages_korrekt_med_en_andel_pr_periode() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 500000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 400000);
        LocalDateTimeline<RefusjonPeriode> refusjonsdataLocalDateTimeline = RefusjonTidslinjeTjeneste.lagTidslinje(originaltBG, true, YTELSESPESIFIKT_GRUNNLAG);
        LocalDateTimeline<RefusjonPeriode> refusjonsdataLocalDateTimeline1 = RefusjonTidslinjeTjeneste.lagTidslinje(revurderingBG, false, YTELSESPESIFIKT_GRUNNLAG);
        LocalDateTimeline<RefusjonPeriodeEndring> tidslinje = RefusjonTidslinjeTjeneste.kombinerTidslinjer(refusjonsdataLocalDateTimeline, refusjonsdataLocalDateTimeline1);
        assertThat(tidslinje.toSegments()).hasSize(1);
    }

    @Test
    public void tester_at_timeline_lages_og_kombineres_korrekt_med_ulikt_antall_andeler() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 500000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF2, 100000, 500000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, InternArbeidsforholdRefDto.nullRef(), 100000, 400000);

        LocalDateTimeline<RefusjonPeriode> refusjonsdataLocalDateTimeline = RefusjonTidslinjeTjeneste.lagTidslinje(originaltBG, true, YTELSESPESIFIKT_GRUNNLAG);
        LocalDateTimeline<RefusjonPeriode> refusjonsdataLocalDateTimeline1 = RefusjonTidslinjeTjeneste.lagTidslinje(revurderingBG, false, YTELSESPESIFIKT_GRUNNLAG);
        LocalDateTimeline<RefusjonPeriodeEndring> tidslinje = RefusjonTidslinjeTjeneste.kombinerTidslinjer(refusjonsdataLocalDateTimeline, refusjonsdataLocalDateTimeline1);
        assertThat(tidslinje.toSegments()).hasSize(1);
        LocalDateSegment<RefusjonPeriodeEndring> segment = tidslinje.getSegment(new LocalDateInterval(beregningsgrunnlagPeriode1.getBeregningsgrunnlagPeriodeFom(), beregningsgrunnlagPeriode1.getBeregningsgrunnlagPeriodeTom()));
        assertThat(segment).isNotNull();
        RefusjonPeriodeEndring periodeEndring = segment.getValue();
        assertThat(periodeEndring).isNotNull();
        assertThat(periodeEndring.getOriginalBrutto()).isEqualByComparingTo(Beløp.fra(200000));
        assertThat(periodeEndring.getOriginalRefusjon()).isEqualByComparingTo(Beløp.fra(1000000));
        assertThat(periodeEndring.getRevurderingBrutto()).isEqualByComparingTo(Beløp.fra(100000));
        assertThat(periodeEndring.getRevurderingRefusjon()).isEqualByComparingTo(Beløp.fra(400000));
    }

    @Test
    public void tester_at_timeline_lages_og_kombineres_korrekt_med_ulike_arbeidsgivere() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 500000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, InternArbeidsforholdRefDto.nullRef(), 100000, 400000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG2, InternArbeidsforholdRefDto.nullRef(), 200000, 200000);

        LocalDateTimeline<RefusjonPeriode> refusjonsdataLocalDateTimeline = RefusjonTidslinjeTjeneste.lagTidslinje(originaltBG, true, YTELSESPESIFIKT_GRUNNLAG);
        LocalDateTimeline<RefusjonPeriode> refusjonsdataLocalDateTimeline1 = RefusjonTidslinjeTjeneste.lagTidslinje(revurderingBG, false, YTELSESPESIFIKT_GRUNNLAG);
        LocalDateTimeline<RefusjonPeriodeEndring> tidslinje = RefusjonTidslinjeTjeneste.kombinerTidslinjer(refusjonsdataLocalDateTimeline, refusjonsdataLocalDateTimeline1);
        assertThat(tidslinje.toSegments()).hasSize(1);
        LocalDateSegment<RefusjonPeriodeEndring> segment = tidslinje.getSegment(new LocalDateInterval(beregningsgrunnlagPeriode1.getBeregningsgrunnlagPeriodeFom(), beregningsgrunnlagPeriode1.getBeregningsgrunnlagPeriodeTom()));
        assertThat(segment).isNotNull();
        RefusjonPeriodeEndring periodeEndring = segment.getValue();
        assertThat(periodeEndring).isNotNull();
        assertThat(periodeEndring.getOriginaleAndeler()).hasSize(1);
        assertThat(periodeEndring.getRevurderingAndeler()).hasSize(2);

        RefusjonAndel orginalAndel = periodeEndring.getOriginaleAndeler().stream().filter(a -> a.getArbeidsgiver().equals(AG1)).findFirst().orElse(null);
        assertThat(orginalAndel).isNotNull();
        assertThat(orginalAndel.getBrutto()).isEqualByComparingTo(Beløp.fra(100000));
        assertThat(orginalAndel.getRefusjon()).isEqualByComparingTo(Beløp.fra(500000));

        RefusjonAndel revurderingAndelAG1 = periodeEndring.getRevurderingAndeler().stream().filter(a -> a.getArbeidsgiver().equals(AG1)).findFirst().orElse(null);
        assertThat(revurderingAndelAG1).isNotNull();
        assertThat(revurderingAndelAG1.getBrutto()).isEqualByComparingTo(Beløp.fra(100000));
        assertThat(revurderingAndelAG1.getRefusjon()).isEqualByComparingTo(Beløp.fra(400000));

        RefusjonAndel revurderingAndelAG2 = periodeEndring.getRevurderingAndeler().stream().filter(a -> a.getArbeidsgiver().equals(AG2)).findFirst().orElse(null);
        assertThat(revurderingAndelAG2).isNotNull();
        assertThat(revurderingAndelAG2.getBrutto()).isEqualByComparingTo(Beløp.fra(200000));
        assertThat(revurderingAndelAG2.getRefusjon()).isEqualByComparingTo(Beløp.fra(200000));
    }

    private void leggTilAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                              AktivitetStatus aktivitetStatus, Arbeidsgiver ag,
                              InternArbeidsforholdRefDto ref,
                              int bruttoPrÅr, int refusjonskravPrÅr) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(aktivitetStatus)
                .medRedusertBrukersAndelPrÅr(refusjonskravPrÅr > bruttoPrÅr ? Beløp.ZERO : Beløp.fra(bruttoPrÅr - refusjonskravPrÅr))
                .medRedusertRefusjonPrÅr(refusjonskravPrÅr > bruttoPrÅr ? Beløp.fra(bruttoPrÅr) : Beløp.fra(refusjonskravPrÅr))
                .medBeregnetPrÅr(Beløp.fra(bruttoPrÅr));
        if (ag != null) {
            BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforholdBuilder = BGAndelArbeidsforholdDto.builder()
                    .medArbeidsgiver(ag)
                    .medArbeidsforholdRef(ref)
                    .medRefusjonskravPrÅr(Beløp.fra(refusjonskravPrÅr), Utfall.GODKJENT);
            andelBuilder.medBGAndelArbeidsforhold(bgAndelArbeidsforholdBuilder);
        }
        andelBuilder.build(beregningsgrunnlagPeriode);
    }

}
