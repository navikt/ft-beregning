package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

class BeregningRefusjonTjenesteTest {
    private static final Arbeidsgiver AG1 = Arbeidsgiver.virksomhet("999999999");
    private static final Arbeidsgiver AG2 = Arbeidsgiver.virksomhet("111111111");
    private static final Arbeidsgiver AG3 = Arbeidsgiver.virksomhet("222222222");
    private static final InternArbeidsforholdRefDto REF1 = InternArbeidsforholdRefDto.nyRef();
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    // Sørger for at vi tar med alle perioder
    private static final LocalDate ALLEREDE_UTBETALT_TOM = TIDENES_ENDE;

    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
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
    public void skal_ikke_finne_andeler_når_det_ikke_har_vært_endring_i_refusjon() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 0);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 0);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_ikke_ta_med_periode_som_kun_finnes_i_orginalt_grunnlag() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(10), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1))
                .build(originaltBG);
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 500000, 0);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 500000, 0);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode3 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode3, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 500000, 200000);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(10));

        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AG1, REF1, 500000, 200000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(10));
        assertMap(resultat, forventetInterval, Collections.singletonList(forventetAndel));
    }

    @Test
    public void skal_ikke_ta_med_periode_som_kun_finnes_i_nytt_grunnlag() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 500000, 0);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(10), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1))
                .build(revurderingBG);
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode3 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 500000, 200000);
        leggTilAndel(beregningsgrunnlagPeriode3, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 500000, 200000);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(10));

        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AG1, REF1, 500000, 200000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(10));
        assertMap(resultat, forventetInterval, Collections.singletonList(forventetAndel));
    }


    @Test
    public void skal_matche_andel_når_arbeidsforhold_ref_er_tilkommet_med_økt_refkrav() {
        // Arrange
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, InternArbeidsforholdRefDto.nullRef(), 100000, 0);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 50000);

        // Act
        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        // Assert
        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AG1, REF1, 100000, 50000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Collections.singletonList(forventetAndel));
    }


    @Test
    public void skal_finne_andel_hvis_refusjonskrav_har_økt() {
        // Arrange
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 0);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 50000);

        // Act
        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        // Assert
        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AG1, REF1, 100000, 50000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Collections.singletonList(forventetAndel));
    }

    @Test
    public void skal_ikke_finne_andel_hvis_refusjonskrav_har_sunket() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 100000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 50000);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_finne_korrekt_andel_når_flere_finnes() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 100000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 200000, 100000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG3, REF1, 300000, 300000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 100000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 200000, 200000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG3, REF1, 300000, 300000);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AG2, REF1,200000, 200000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Collections.singletonList(forventetAndel));
    }

    @Test
    public void skal_ikke_finne_andel_hvis_inntekt_og_ref_økes_like_mye() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 0);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 300000, 300000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 100000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 300000, 300000);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_finne_begge_andeler_hvis_ref_økes() {
        // Original
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 50000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 200000, 100000);

        // Revurdering
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 100000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 200000, 200000);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel1 = lagForventetAndel(AG1, REF1, 100000, 100000);
        RefusjonAndel forventetAndel2 = lagForventetAndel(AG2, REF1, 200000, 200000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Arrays.asList(forventetAndel1, forventetAndel2));
    }

    @Test
    public void skal_finne_nytt_ref_krav() {
        // Original
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 200000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 300000, 0);

        // Revurdering
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 200000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 300000, 300000);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AG2, REF1, 300000, 300000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Collections.singletonList(forventetAndel));
    }

    @Test
    public void skal_ikke_finne_tilkommet_arbfor_når_det_ikke_endrer_brukers_andel() {
        // Original
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 200000);

        // Revurdering
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(20))
                .build(revurderingBG);
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode3 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(21), TIDENES_ENDE)
                .build(revurderingBG);

        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 200000);

        leggTilAndel(beregningsgrunnlagPeriode3, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 200000);
        leggTilAndel(beregningsgrunnlagPeriode3, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 300000, 300000);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        assertThat(resultat).isEmpty();
    }

    /**
     * I tilfeller der det blir mindre til bruker men refusjonskravet er likt skal det ikke opprettes avklaringsbehov.
     * Dette betyr da at brutto er senket, enten av saksbehandler eller av ny inntektsmelding og skal ikke vurderes.
     */
    @Test
    public void skal_ikke_finne_noen_andel_dersom_det_blir_mindre_til_bruker_men_refkrav_er_likt() {
        // Original
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(20))
                .build(originaltBG);
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(21), TIDENES_ENDE)
                .build(originaltBG);

        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 50000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 50000);

        // Revurdering
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode3 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(20))
                .build(revurderingBG);
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode4 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(21), TIDENES_ENDE)
                .build(revurderingBG);

        leggTilAndel(beregningsgrunnlagPeriode3, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 50000);
        leggTilAndel(beregningsgrunnlagPeriode4, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 50000, 50000);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        assertThat(resultat).isEmpty();
    }

    @Test
    public void arbeidsforhold_splittes_i_tre_andeler_og_stjeler_fra_sn() {
        InternArbeidsforholdRefDto ref1 = InternArbeidsforholdRefDto.nyRef();
        InternArbeidsforholdRefDto ref2 = InternArbeidsforholdRefDto.nyRef();
        InternArbeidsforholdRefDto ref3 = InternArbeidsforholdRefDto.nyRef();
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, null, 191368.44, 191368.44);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG2, null, 279759.96, 279759.96);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 180238.31, 0);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, ref1, 105999.96, 105999.96);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, ref2, 78583.56, 78583.56);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, ref3, 191368.44, 191368.44);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG2, null, 279759.96, 279759.96);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 0, 0);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AG1, ref1, 105999.96, 105999.96);
        RefusjonAndel forventetAndel2 = lagForventetAndel(AG1, ref2, 78583.56, 78583.56);
        RefusjonAndel forventetAndel3 = lagForventetAndel(AG1, ref3, 191368.44, 191368.44);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Arrays.asList(forventetAndel, forventetAndel2, forventetAndel3));
    }

    @Test
    public void arbeidsforhold_slås_sammen_og_øker_refusjon_men_det_har_aldri_vært_utbetaling() {
        InternArbeidsforholdRefDto ref1 = InternArbeidsforholdRefDto.nyRef();
        InternArbeidsforholdRefDto ref2 = InternArbeidsforholdRefDto.nyRef();
        InternArbeidsforholdRefDto ref3 = InternArbeidsforholdRefDto.nyRef();
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, ref1, 150000, 150000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, ref2, 150000, 150000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, ref3, 100000, 100000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG2, null, 100000, 100000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, null, 500000, 500000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG2, null, 100000, 100000);


        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        assertThat(resultat).isEmpty();
    }

    @Test
    public void arbeidsforhold_slås_sammen_og_øker_refusjon_og_det_har_tidligere_vært_utbetaling_til_søker() {
        InternArbeidsforholdRefDto ref1 = InternArbeidsforholdRefDto.nyRef();
        InternArbeidsforholdRefDto ref2 = InternArbeidsforholdRefDto.nyRef();
        InternArbeidsforholdRefDto ref3 = InternArbeidsforholdRefDto.nyRef();
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, ref1, 150000, 100000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, ref2, 150000, 150000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, ref3, 100000, 100000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG2, null, 100000, 100000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, null, 500000, 500000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG2, null, 100000, 100000);


        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AG1, null, 500000, 500000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Collections.singletonList(forventetAndel));
    }

    // Skjer f.eks hvis en andel utenom AT er manuelt fastsatt til et lavere beløp
    @Test
    public void mindre_utbetaling_til_søker_men_ingen_arbeidsforhold_har_økt_refusjon() {
        InternArbeidsforholdRefDto ref1 = InternArbeidsforholdRefDto.nyRef();
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, ref1, 300000, 300000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG2, null, 100000, 100000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 200000, 0);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, null, 300000, 300000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG2, null, 100000, 100000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 500, 0);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        assertThat(resultat).isEmpty();
    }

    @Test
    public void revurdering_andel_med_og_uten_referanse_originalt_ingen_referanse_ikke_økt_refusjon() {
        InternArbeidsforholdRefDto ref1 = InternArbeidsforholdRefDto.nyRef();
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, null, 300000, 300000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, null, 200000, 200000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, ref1, 100000, 100000);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        assertThat(resultat).isEmpty();
    }

    @Test
    public void revurdering_andel_med_og_uten_referanse_originalt_ingen_referanse_økt_refusjon() {
        InternArbeidsforholdRefDto ref1 = InternArbeidsforholdRefDto.nyRef();
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, null, 400000, 350000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, null, 300000, 300000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, ref1, 100000, 100000);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AG1, null, 300000, 300000);
        RefusjonAndel forventetAndel2 = lagForventetAndel(AG1, ref1, 100000, 100000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Arrays.asList(forventetAndel, forventetAndel2));
    }

    @Test
    public void revurdering_andel_med_og_uten_referanse_originalt_med_og_uten_referanse_økt_refusjon() {
        InternArbeidsforholdRefDto ref1 = InternArbeidsforholdRefDto.nyRef();
        InternArbeidsforholdRefDto ref2 = InternArbeidsforholdRefDto.nyRef();
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, null, 200000, 250000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, ref2, 200000, 100000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, null, 300000, 300000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, ref1, 100000, 100000);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AG1, null, 300000, 300000);
        RefusjonAndel forventetAndel2 = lagForventetAndel(AG1, ref1, 100000, 100000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Arrays.asList(forventetAndel, forventetAndel2));
    }

    @Test
    public void inntekt_øker_like_mye_som_refusjon_men_kuttes_av_grenseverdi() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, null, 600000, 550000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, null, 650000, 600000);

        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AG1, null, 650000, 600000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Collections.singletonList(forventetAndel));
    }

    @Test
    public void skal_finne_andel_hvis_refusjonskrav_har_økt_før_og_etter_helg() {
        // Arrange
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriodeOriginal = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriodeOriginal, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 0);


        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, LocalDate.of(2022, Month.JANUARY, 7))
                .build(revurderingBG);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(LocalDate.of(2022, Month.JANUARY, 8), LocalDate.of(2022, Month.JANUARY, 9))
                .build(revurderingBG);


        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode3 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(LocalDate.of(2022, Month.JANUARY, 10), TIDENES_ENDE)
                .build(revurderingBG);

        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 50000);


        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 0);

        leggTilAndel(beregningsgrunnlagPeriode3, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 50000);


        // Act
        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        // Assert
        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AG1, REF1, 100000, 50000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Collections.singletonList(forventetAndel));
    }

    @Test
    public void skal_finne_to_andeler_hvis_refusjonskrav_har_økt_før_og_etter_helg_men_ulikt() {
        // Arrange
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriodeOriginal = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriodeOriginal, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 0);


        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, LocalDate.of(2022, Month.JANUARY, 7))
                .build(revurderingBG);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(LocalDate.of(2022, Month.JANUARY, 8), LocalDate.of(2022, Month.JANUARY, 9))
                .build(revurderingBG);


        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode3 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(LocalDate.of(2022, Month.JANUARY, 10), TIDENES_ENDE)
                .build(revurderingBG);

        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 50000);


        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 0);

        leggTilAndel(beregningsgrunnlagPeriode3, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 40000);


        // Act
        Map<Intervall, List<RefusjonAndel>> resultat = kjørUtleder(ALLEREDE_UTBETALT_TOM);

        // Assert
        assertThat(resultat).hasSize(2);
        RefusjonAndel forventetAndel = lagForventetAndel(AG1, REF1, 100000, 50000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING,  LocalDate.of(2022, Month.JANUARY, 7));
        assertMap(resultat, forventetInterval, Collections.singletonList(forventetAndel));

        RefusjonAndel forventetAndel2 = lagForventetAndel(AG1, REF1, 100000, 40000);
        Intervall forventetInterval2 = Intervall.fraOgMedTilOgMed(LocalDate.of(2022, Month.JANUARY, 10),  TIDENES_ENDE);
        assertMap(resultat, forventetInterval2, Collections.singletonList(forventetAndel2));
    }




    private Map<Intervall, List<RefusjonAndel>> kjørUtleder(LocalDate alleredeUtbetaltTOM) {
        return BeregningRefusjonTjeneste.finnUtbetaltePerioderMedAndelerMedØktRefusjon(revurderingBG, originaltBG, alleredeUtbetaltTOM, Beløp.fra(600000), new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false));
    }

    private void leggTilAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                              AktivitetStatus aktivitetStatus, Arbeidsgiver ag,
                              InternArbeidsforholdRefDto ref,
                              double bruttoPrÅr, double refusjonskravPrÅr) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(aktivitetStatus)
                .medRedusertBrukersAndelPrÅr(Beløp.fra(BigDecimal.valueOf(bruttoPrÅr - refusjonskravPrÅr)))
                .medRedusertRefusjonPrÅr(Beløp.fra(BigDecimal.valueOf(refusjonskravPrÅr)))
                .medBeregnetPrÅr(Beløp.fra(BigDecimal.valueOf(bruttoPrÅr)));
        if (ag != null) {
            BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforholdBuilder = BGAndelArbeidsforholdDto.builder()
                    .medArbeidsgiver(ag)
                    .medArbeidsforholdRef(ref)
                    .medRefusjonskravPrÅr(Beløp.fra(BigDecimal.valueOf(refusjonskravPrÅr)), Utfall.GODKJENT);
            andelBuilder.medBGAndelArbeidsforhold(bgAndelArbeidsforholdBuilder);
        }
        andelBuilder.build(beregningsgrunnlagPeriode);
    }

    private void assertMap(Map<Intervall, List<RefusjonAndel>> resultat, Intervall forventetInterval, List<RefusjonAndel> forventedeAndeler) {
        List<RefusjonAndel> faktiskeAndeler = resultat.get(forventetInterval);
        assertThat(faktiskeAndeler).hasSameSizeAs(forventedeAndeler);
        forventedeAndeler.forEach(forventet -> {
            Optional<RefusjonAndel> faktiskOpt = faktiskeAndeler.stream().filter(a -> matcherEksakt(a, forventet)).findFirst();
            assertThat(faktiskOpt).isPresent();
            RefusjonAndel faktisk = faktiskOpt.get();
            assertThat(faktisk.getRefusjon()).isEqualByComparingTo(forventet.getRefusjon());
            assertThat(faktisk.getBrutto()).isEqualByComparingTo(forventet.getBrutto());
        });
    }

    public boolean matcherEksakt(RefusjonAndel one, RefusjonAndel other) {
        return Objects.equals(other.getAktivitetStatus(), one.getAktivitetStatus())
                && Objects.equals(other.getArbeidsgiver(), one.getArbeidsgiver())
                && Objects.equals(other.getArbeidsforholdRef().getReferanse(), one.getArbeidsforholdRef().getReferanse());
    }

    private RefusjonAndel lagForventetAndel(Arbeidsgiver ag, InternArbeidsforholdRefDto ref, double brutto, double refusjon) {
        return new RefusjonAndel(ag == null ? AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE : AktivitetStatus.ARBEIDSTAKER, ag, ref, Beløp.fra(BigDecimal.valueOf(brutto)), Beløp.fra(BigDecimal.valueOf(refusjon)));
    }

}
