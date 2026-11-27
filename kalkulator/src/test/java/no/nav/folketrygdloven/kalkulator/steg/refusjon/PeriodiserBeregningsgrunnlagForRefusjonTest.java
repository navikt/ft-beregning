package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsandel;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsperiode;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.JournalpostId;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

class PeriodiserBeregningsgrunnlagForRefusjonTest {
    private static final LocalDate FOM = LocalDate.of(2026, 1, 1);
    private static final Arbeidsgiver AG = Arbeidsgiver.virksomhet("999999999");
    private static final InternArbeidsforholdRefDto INTERN_ARBEIDSFORHOLD_REF = InternArbeidsforholdRefDto.namedRef("første");

    @Test
    void skal_avvise_manglende_beregningsgrunnlag() {
        var originalInput = input(bg());
        var inputUtenBeregningsgrunnlag = new BeregningsgrunnlagInput(originalInput.getKoblingReferanse(),
            originalInput.getIayGrunnlag(), null, List.of(), originalInput.getYtelsespesifiktGrunnlag());
        var tomtFordelingsgrunnlag = new FordelingGrunnlag(List.of());

        assertThatThrownBy(() -> PeriodiserBeregningsgrunnlagForRefusjon.periodiser(inputUtenBeregningsgrunnlag, tomtFordelingsgrunnlag))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Beregningsgrunnlag må være satt.");
    }

    @Test
    void skal_bevare_original_splitt_og_data_med_endring_i_april_og_nullstille_fra_mai_uten_mutasjon() {
        var bg = bg();
        var første = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, FOM.plusMonths(2).minusDays(1)).build(bg);
        leggTilAndel(første, AG, INTERN_ARBEIDSFORHOLD_REF, 1L, 300000);
        var andre = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM.plusMonths(2), null)
            .leggTilPeriodeÅrsak(PeriodeÅrsak.GRADERING).build(bg);
        leggTilAndel(andre, AG, INTERN_ARBEIDSFORHOLD_REF, 1L, 400000);
        BeregningsgrunnlagPeriodeDto.oppdater(andre).medBruttoPrÅr(Beløp.fra(410000))
            .medAvkortetPrÅr(Beløp.fra(390000)).medRedusertPrÅr(Beløp.fra(195000))
            .medTotalUtbetalingsgradFraUttak(BigDecimal.valueOf(50)).build();
        var før = BeregningsgrunnlagDto.builder(bg).build();
        var grunnlag = new FordelingGrunnlag(List.of(refusjonsandel(AG, INTERN_ARBEIDSFORHOLD_REF,
            periode(FOM, FOM.plusMonths(3).minusDays(1), "1234.56", RefusjonUtfall.INNVILGET),
            periode(FOM.plusMonths(3), FOM.plusMonths(4).minusDays(1), "2000", RefusjonUtfall.INNVILGET),
            periode(FOM.plusMonths(4), null, "0", RefusjonUtfall.INNVILGET))));

        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), grunnlag);

        var perioder = nytt.getBeregningsgrunnlagPerioder();
        assertThat(perioder).extracting(BeregningsgrunnlagPeriodeDto::getPeriode).containsExactly(
            Intervall.fraOgMedTilOgMed(FOM, LocalDate.of(2026, 2, 28)),
            Intervall.fraOgMedTilOgMed(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)),
            Intervall.fraOgMedTilOgMed(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30)),
            Intervall.fraOgMed(LocalDate.of(2026, 5, 1)));
        assertThat(perioder).extracting(p -> arbeidsforhold(p, 0).getSaksbehandletRefusjonPrÅr()).containsExactly(
            Beløp.fra(new BigDecimal("14814.72")), Beløp.fra(new BigDecimal("14814.72")), Beløp.fra(24000), Beløp.ZERO);
        assertThat(perioder).extracting(p -> arbeidsforhold(p, 0).getRefusjonskravPrÅr())
            .containsExactly(Beløp.fra(999), Beløp.fra(999), Beløp.fra(999), Beløp.fra(999));
        assertThat(perioder.get(2).getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.GRADERING, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertThat(perioder.get(3).getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.GRADERING, PeriodeÅrsak.REFUSJON_OPPHØRER);
        assertThat(arbeidsforhold(perioder.getLast(), 0).getGjeldendeRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(arbeidsforhold(perioder.getLast(), 0).getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(arbeidsforhold(perioder.getLast(), 0).getFordeltRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(arbeidsforhold(perioder.getLast(), 0).getRefusjon().orElseThrow().getManueltFordeltRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
        for (var p : perioder) {
            var original = p.getBeregningsgrunnlagPeriodeFom().isBefore(FOM.plusMonths(2)) ? første : andre;
            assertThat(p).usingRecursiveComparison().ignoringFields("periode", "beregningsgrunnlag",
                "beregningsgrunnlagPrStatusOgAndelList", "beregningsgrunnlagPeriodeÅrsaker").isEqualTo(original);
            assertThat(p.getBeregningsgrunnlagPrStatusOgAndelList().getFirst()).usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(".*beregningsgrunnlagPeriode", ".*beregningsgrunnlagPrStatusOgAndel", ".*refusjon")
                .isEqualTo(original.getBeregningsgrunnlagPrStatusOgAndelList().getFirst());
        }
        assertThat(nytt).isNotSameAs(bg);
        assertThat(bg).usingRecursiveComparison().isEqualTo(før);
    }

    @Test
    void skal_skille_arbeidsgivere_og_referanser_og_bevare_andeler_uten_krav() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var ref2 = InternArbeidsforholdRefDto.namedRef("andre");
        var ag2 = Arbeidsgiver.virksomhet("888888888");
        leggTilAndel(p, AG, INTERN_ARBEIDSFORHOLD_REF, 1L, 100000);
        leggTilAndel(p, AG, ref2, 2L, 200000);
        leggTilAndel(p, ag2, INTERN_ARBEIDSFORHOLD_REF, 3L, 300000);
        leggTilAndel(p, ag2, ref2, 4L, 400000);
        var grunnlag = new FordelingGrunnlag(List.of(
            refusjonsandel(AG, INTERN_ARBEIDSFORHOLD_REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)),
            refusjonsandel(AG, ref2, periode(FOM, null, "2000", RefusjonUtfall.INNVILGET)),
            refusjonsandel(ag2, INTERN_ARBEIDSFORHOLD_REF, periode(FOM, null, "3000", RefusjonUtfall.INNVILGET))));

        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), grunnlag).getBeregningsgrunnlagPerioder().getFirst();

        assertThat(nytt.getBeregningsgrunnlagPrStatusOgAndelList()).extracting(a -> a.getBgAndelArbeidsforhold().orElseThrow().getSaksbehandletRefusjonPrÅr())
            .containsExactly(Beløp.fra(12000), Beløp.fra(24000), Beløp.fra(36000), Beløp.fra(777));
    }

    @Test
    void skal_sette_beløp_etter_kilde_og_videreføre_nullstilling_av_fordelte_beløp() {
        for (var kilde : RefusjonKilde.values()) {
            var bg = bg();
            var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
            leggTilAndel(p, AG, INTERN_ARBEIDSFORHOLD_REF, 1L, 100000);
            var andel = new Refusjonsandel(AG, INTERN_ARBEIDSFORHOLD_REF, kilde,
                kilde == RefusjonKilde.INNTEKTSMELDING ? new JournalpostId("104") : null, List.of(
                    periode(FOM, FOM.plusMonths(1).minusDays(1), "1000", RefusjonUtfall.INNVILGET),
                    periode(FOM.plusMonths(1), FOM.plusMonths(2).minusDays(1), "0", RefusjonUtfall.INNVILGET),
                    periode(FOM.plusMonths(2), null, "2000", RefusjonUtfall.AVSLÅTT)));

            var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(andel)));

            var perioder = nytt.getBeregningsgrunnlagPerioder();
            if (kilde == RefusjonKilde.INNTEKTSMELDING) {
                assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getRefusjonskravPrÅr())
                    .containsExactly(Beløp.fra(12000), Beløp.ZERO, Beløp.fra(24000));
                assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getSaksbehandletRefusjonPrÅr())
                    .containsExactly(Beløp.fra(777), Beløp.ZERO, Beløp.ZERO);
            } else {
                assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getSaksbehandletRefusjonPrÅr())
                    .containsExactly(Beløp.fra(12000), Beløp.ZERO, Beløp.ZERO);
                assertThat(perioder).allSatisfy(periode ->
                    assertThat(arbeidsforhold(periode, 0).getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(999)));
            }
            assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getFordeltRefusjonPrÅr())
                .containsExactly(Beløp.fra(666), Beløp.ZERO, Beløp.ZERO);
            assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getRefusjon().orElseThrow().getManueltFordeltRefusjonPrÅr())
                .containsExactly(Beløp.fra(555), Beløp.ZERO, Beløp.ZERO);
            assertThat(perioder).allSatisfy(periode ->
                assertThat(arbeidsforhold(periode, 0).getHjemmelForRefusjonskravfrist()).isEqualTo(Hjemmel.F_22_13_6));
        }
    }

    @Test
    void skal_bevare_inntektsmeldingsbeløp_utenfor_overstyringsperioden_men_nullstille_opphørt_inntektsmelding() {
        for (var kilde : RefusjonKilde.values()) {
            var bg = bg();
            var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
            leggTilAndel(p, AG, InternArbeidsforholdRefDto.nullRef(), 1L, 100000);
            var refusjonsandel = new Refusjonsandel(AG, InternArbeidsforholdRefDto.nullRef(), kilde,
                kilde == RefusjonKilde.INNTEKTSMELDING ? new JournalpostId("105") : null,
                List.of(periode(FOM.plusMonths(1), FOM.plusMonths(2).minusDays(1), "1000", RefusjonUtfall.INNVILGET)));

            var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(refusjonsandel)));

            var perioder = nytt.getBeregningsgrunnlagPerioder();
            assertThat(perioder).extracting(BeregningsgrunnlagPeriodeDto::getPeriode).containsExactly(
                Intervall.fraOgMedTilOgMed(FOM, FOM.plusMonths(1).minusDays(1)),
                Intervall.fraOgMedTilOgMed(FOM.plusMonths(1), FOM.plusMonths(2).minusDays(1)),
                Intervall.fraOgMed(FOM.plusMonths(2)));
            if (kilde == RefusjonKilde.SAKSBEHANDLER) {
                assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getRefusjonskravPrÅr())
                    .containsExactly(Beløp.fra(999), Beløp.fra(999), Beløp.fra(999));
                assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getSaksbehandletRefusjonPrÅr())
                    .containsExactly(Beløp.ZERO, Beløp.fra(12000), Beløp.ZERO);
            } else {
                assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getRefusjonskravPrÅr())
                    .containsExactly(Beløp.ZERO, Beløp.fra(12000), Beløp.ZERO);
            }
            assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getFordeltRefusjonPrÅr())
                .containsExactly(Beløp.ZERO, Beløp.fra(666), Beløp.ZERO);
            assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getRefusjon().orElseThrow().getManueltFordeltRefusjonPrÅr())
                .containsExactly(Beløp.ZERO, Beløp.fra(555), Beløp.ZERO);
        }
    }

    @Test
    void skal_sette_årsbeløp_fra_inntektsmelding_uten_saksbehandlet_refusjon() {
        var bg = bg();
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var inntektsmelding = new Refusjonsandel(AG, INTERN_ARBEIDSFORHOLD_REF, RefusjonKilde.INNTEKTSMELDING, new JournalpostId("103"),
            List.of(periode(FOM, null, "1234.56", RefusjonUtfall.INNVILGET)));

        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(inntektsmelding)));

        var arbeidsforhold = arbeidsforhold(nytt.getBeregningsgrunnlagPerioder().getFirst(), 0);
        assertThat(arbeidsforhold.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(new BigDecimal("14814.72")));
        assertThat(arbeidsforhold.getSaksbehandletRefusjonPrÅr()).isNull();
    }

    @Test
    void skal_matche_uspesifisert_ref_mot_første_andel_og_ikke_matche_spesifisert_ref_mot_bg_uten_ref() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, INTERN_ARBEIDSFORHOLD_REF, 1L, 100000);
        leggTilAndel(p, AG, InternArbeidsforholdRefDto.namedRef("andre"), 2L, 200000);
        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(
            refusjonsandel(AG, null, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)))));
        assertThat(arbeidsforhold(nytt.getBeregningsgrunnlagPerioder().getFirst(), 0).getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.fra(12000));
        assertThat(arbeidsforhold(nytt.getBeregningsgrunnlagPerioder().getFirst(), 1).getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(999));

        var bgUtenRef = bg();
        var utenRef = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bgUtenRef);
        leggTilAndel(utenRef, AG, InternArbeidsforholdRefDto.nullRef(), 1L, 100000);
        var medNy = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bgUtenRef), new FordelingGrunnlag(List.of(
            refusjonsandel(AG, INTERN_ARBEIDSFORHOLD_REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)))));
        var andeler = medNy.getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(2);
        assertThat(andeler.getFirst().getBgAndelArbeidsforhold().orElseThrow().getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(999));
        assertThat(andeler.getLast().getArbeidsforholdRef()).contains(INTERN_ARBEIDSFORHOLD_REF);
    }

    @Test
    void skal_skille_krav_med_og_uten_referanse_uavhengig_av_rekkefølge() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var utenReferanse = InternArbeidsforholdRefDto.nullRef();
        leggTilAndel(p, AG, utenReferanse, 1L, 100000);
        var grunnlag = new FordelingGrunnlag(List.of(
            refusjonsandel(AG, INTERN_ARBEIDSFORHOLD_REF, periode(FOM, null, "2000", RefusjonUtfall.INNVILGET)),
            refusjonsandel(AG, utenReferanse, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET))));

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), grunnlag);

        var andeler = resultat.getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(2);
        assertThat(andeler).filteredOn(a -> a.getArbeidsforholdRef().orElseThrow().equals(utenReferanse))
            .singleElement().satisfies(a -> assertThat(a.getBgAndelArbeidsforhold().orElseThrow().getSaksbehandletRefusjonPrÅr())
                .isEqualTo(Beløp.fra(12000)));
        assertThat(andeler).filteredOn(a -> a.getArbeidsforholdRef().orElseThrow().equals(INTERN_ARBEIDSFORHOLD_REF))
            .singleElement().satisfies(a -> assertThat(a.getBgAndelArbeidsforhold().orElseThrow().getSaksbehandletRefusjonPrÅr())
                .isEqualTo(Beløp.fra(24000)));
    }

    @Test
    void skal_opprette_ny_andel_kun_med_positivt_krav_og_bruke_faktisk_arbeidsperiode() {
        var bg = bg();
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var arbeidsperiode = Intervall.fraOgMedTilOgMed(FOM.minusYears(2), FOM.plusYears(1));
        var refusjon = new Refusjonsandel(AG, INTERN_ARBEIDSFORHOLD_REF, RefusjonKilde.SAKSBEHANDLER, null, List.of(
            periode(FOM.plusMonths(1), FOM.plusMonths(2).minusDays(1), "1000", RefusjonUtfall.INNVILGET)));

        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg, arbeidsperiode), new FordelingGrunnlag(List.of(refusjon)));

        var perioder = nytt.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertThat(perioder.getFirst().getBeregningsgrunnlagPrStatusOgAndelList()).isEmpty();
        var ny = perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList().getFirst();
        assertThat(ny.getKilde()).isEqualTo(AndelKilde.PROSESS_PERIODISERING);
        assertThat(ny.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(ny.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ARBEID);
        assertThat(ny.getBeregnetPrÅr()).isNull();
        assertThat(arbeidsforhold(perioder.get(1), 0).getArbeidsperiodeFom()).isEqualTo(arbeidsperiode.getFomDato());
        assertThat(arbeidsforhold(perioder.get(1), 0).getArbeidsperiodeTom()).contains(arbeidsperiode.getTomDato());
        assertThat(arbeidsforhold(perioder.get(1), 0).getRefusjonskravPrÅr()).isNull();
        assertThat(arbeidsforhold(perioder.get(1), 0).getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.fra(12000));
        assertThat(perioder.getLast().getBeregningsgrunnlagPrStatusOgAndelList()).isEmpty();
        assertThat(perioder.getLast().getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.REFUSJON_OPPHØRER);
        assertThat(bg.getBeregningsgrunnlagPerioder()).singleElement().satisfies(p -> assertThat(p.getBeregningsgrunnlagPrStatusOgAndelList()).isEmpty());
    }

    @Test
    void skal_hente_arbeidsperiode_fra_riktig_arbeidsgiver_og_referanse() {
        var bg = bg();
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var iay = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(FOM, Intervall.fraOgMed(FOM.minusYears(5)),
            InternArbeidsforholdRefDto.namedRef("andre"), AG, iay);
        BeregningIAYTestUtil.byggArbeidForBehandling(FOM, Intervall.fraOgMed(FOM.minusYears(4)), INTERN_ARBEIDSFORHOLD_REF, Arbeidsgiver.virksomhet("888888888"), iay);
        var arbeidsperiode = Intervall.fraOgMedTilOgMed(FOM.minusYears(2), FOM.plusYears(1));
        BeregningIAYTestUtil.byggArbeidForBehandling(FOM, arbeidsperiode, INTERN_ARBEIDSFORHOLD_REF, AG, iay);
        var originalInput = input(bg);
        var input = new BeregningsgrunnlagInput(originalInput.getKoblingReferanse(), iay.build(), null, List.of(),
            originalInput.getYtelsespesifiktGrunnlag()).medBeregningsgrunnlagGrunnlag(originalInput.getBeregningsgrunnlagGrunnlag());
        var grunnlag = new FordelingGrunnlag(List.of(refusjonsandel(AG, INTERN_ARBEIDSFORHOLD_REF,
            periode(FOM, null, "1000", RefusjonUtfall.INNVILGET))));

        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input, grunnlag);

        var af = arbeidsforhold(nytt.getBeregningsgrunnlagPerioder().getFirst(), 0);
        assertThat(af.getArbeidsperiodeFom()).isEqualTo(arbeidsperiode.getFomDato());
        assertThat(af.getArbeidsperiodeTom()).contains(arbeidsperiode.getTomDato());
    }

    @Test
    void skal_mappe_avslag_uten_å_finne_på_hjemmel_og_avvise_overlappende_krav() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, INTERN_ARBEIDSFORHOLD_REF, 1L, 100000);
        var avslag = new FordelingGrunnlag(List.of(refusjonsandel(AG, INTERN_ARBEIDSFORHOLD_REF,
            periode(FOM.plusDays(10), null, "1000", RefusjonUtfall.AVSLÅTT))));

        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), avslag).getBeregningsgrunnlagPerioder().getLast();
        var af = arbeidsforhold(nytt, 0);
        assertThat(af.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(999));
        assertThat(af.getInnvilgetRefusjonskravPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(af.getGjeldendeRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(af.getRefusjon().orElseThrow().getRefusjonskravFristUtfall()).isEqualTo(Utfall.UNDERKJENT);
        assertThat(af.getHjemmelForRefusjonskravfrist()).isEqualTo(Hjemmel.F_22_13_6);
        assertThat(nytt.getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.REFUSJON_AVSLÅTT);
        var overlappendeInput = input(bg);
        var overlappendeFordelingsgrunnlag = new FordelingGrunnlag(List.of(
            refusjonsandel(AG, INTERN_ARBEIDSFORHOLD_REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)),
            refusjonsandel(AG, null, periode(FOM, null, "2000", RefusjonUtfall.INNVILGET))));
        assertThatThrownBy(() -> PeriodiserBeregningsgrunnlagForRefusjon.periodiser(overlappendeInput, overlappendeFordelingsgrunnlag))
            .isInstanceOf(IllegalArgumentException.class).hasMessage("Overlappende refusjonsperioder treffer samme BG-andel.");
    }

    @Test
    void skal_mappe_redusert_til_godkjent_uten_å_endre_fordelingsgrunnlaget() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, INTERN_ARBEIDSFORHOLD_REF, 1L, 100000);
        var redusert = periode(FOM, null, "1000", RefusjonUtfall.REDUSERT);
        var fordelingsgrunnlag = new FordelingGrunnlag(List.of(refusjonsandel(AG, INTERN_ARBEIDSFORHOLD_REF, redusert)));

        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), fordelingsgrunnlag);

        var af = arbeidsforhold(nytt.getBeregningsgrunnlagPerioder().getFirst(), 0);
        assertThat(af.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(999));
        assertThat(af.getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.fra(12000));
        assertThat(af.getInnvilgetRefusjonskravPrÅr()).isEqualTo(Beløp.fra(999));
        assertThat(af.getRefusjon().orElseThrow().getRefusjonskravFristUtfall()).isEqualTo(Utfall.GODKJENT);
        assertThat(fordelingsgrunnlag.refusjonsandeler().getFirst().refusjonsperioder()).containsExactly(redusert);
        assertThat(redusert.utfall()).isEqualTo(RefusjonUtfall.REDUSERT);
    }

    @Test
    void skal_ikke_bruke_avslått_saksbehandlerkrav_som_gjeldende_refusjon_uten_fordelte_beløp() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var utenReferanse = InternArbeidsforholdRefDto.nullRef();
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(1L).medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID).medBeregnetPrÅr(Beløp.fra(100000))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(AG).medArbeidsforholdRef(utenReferanse)
                .medArbeidsperiodeFom(FOM.minusYears(1))
                .medRefusjon(Refusjon.medRefusjonskravPrÅr(Beløp.fra(24000), null, Utfall.GODKJENT)))
            .build(p);
        var avslag = refusjonsandel(AG, utenReferanse, periode(FOM, null, "1000", RefusjonUtfall.AVSLÅTT));

        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(avslag)));

        var arbeidsforhold = arbeidsforhold(nytt.getBeregningsgrunnlagPerioder().getFirst(), 0);
        assertThat(arbeidsforhold.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(24000));
        assertThat(arbeidsforhold.getFordeltRefusjonPrÅr()).isNull();
        assertThat(arbeidsforhold.getRefusjon().orElseThrow().getManueltFordeltRefusjonPrÅr()).isNull();
        assertThat(arbeidsforhold.getRefusjon().orElseThrow().getRefusjonskravFristUtfall()).isEqualTo(Utfall.UNDERKJENT);
        assertThat(arbeidsforhold.getGjeldendeRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
    }

    @Test
    void skal_periodisere_hull_og_gjenopptak_av_refusjon() {
        for (var kilde : RefusjonKilde.values()) {
            var bg = bg();
            var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
            var utenReferanse = InternArbeidsforholdRefDto.nullRef();
            leggTilAndel(p, AG, utenReferanse, 1L, 100000);
            var refusjon = new Refusjonsandel(AG, utenReferanse, kilde,
                kilde == RefusjonKilde.INNTEKTSMELDING ? new JournalpostId("106") : null, List.of(
                    periode(FOM, FOM.plusMonths(1).minusDays(1), "1000", RefusjonUtfall.INNVILGET),
                    periode(FOM.plusMonths(2), null, "2000", RefusjonUtfall.INNVILGET)));

            var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(refusjon)));

            var perioder = nytt.getBeregningsgrunnlagPerioder();
            assertThat(perioder).extracting(BeregningsgrunnlagPeriodeDto::getPeriode).containsExactly(
                Intervall.fraOgMedTilOgMed(FOM, FOM.plusMonths(1).minusDays(1)),
                Intervall.fraOgMedTilOgMed(FOM.plusMonths(1), FOM.plusMonths(2).minusDays(1)),
                Intervall.fraOgMed(FOM.plusMonths(2)));
            if (kilde == RefusjonKilde.INNTEKTSMELDING) {
                assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getRefusjonskravPrÅr())
                    .containsExactly(Beløp.fra(12000), Beløp.ZERO, Beløp.fra(24000));
            } else {
                assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getSaksbehandletRefusjonPrÅr())
                    .containsExactly(Beløp.fra(12000), Beløp.ZERO, Beløp.fra(24000));
                assertThat(perioder).allSatisfy(periode ->
                    assertThat(arbeidsforhold(periode, 0).getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(999)));
            }
            assertThat(perioder.getFirst().getPeriodeÅrsaker()).isEmpty();
            assertThat(perioder.get(1).getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.REFUSJON_OPPHØRER);
            assertThat(perioder.getLast().getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
            assertThat(arbeidsforhold(perioder.get(1), 0).getRefusjon().orElseThrow().getRefusjonskravFristUtfall())
                .isEqualTo(Utfall.UDEFINERT);
        }
    }

    @Test
    void skal_avvise_overlappende_perioder_innenfor_samme_refusjonsandel() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var utenReferanse = InternArbeidsforholdRefDto.nullRef();
        leggTilAndel(p, AG, utenReferanse, 1L, 100000);
        var fellesDato = FOM.plusMonths(1).minusDays(1);
        var grunnlag = new FordelingGrunnlag(List.of(refusjonsandel(AG, utenReferanse,
            periode(FOM, fellesDato, "1000", RefusjonUtfall.INNVILGET),
            periode(fellesDato, null, "2000", RefusjonUtfall.INNVILGET))));
        var input = input(bg);

        assertThatThrownBy(() -> PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input, grunnlag))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void skal_periodisere_eksisterende_bg_andel_uten_refusjonsobjekt() {
        for (var kilde : RefusjonKilde.values()) {
            var bg = bg();
            var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
            var utenReferanse = InternArbeidsforholdRefDto.nullRef();
            BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(1L).medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medArbforholdType(OpptjeningAktivitetType.ARBEID).medBeregnetPrÅr(Beløp.fra(100000))
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(AG)
                    .medArbeidsforholdRef(utenReferanse).medArbeidsperiodeFom(FOM.minusYears(1))).build(p);
            assertThat(arbeidsforhold(p, 0).getRefusjon()).isEmpty();
            var refusjon = new Refusjonsandel(AG, utenReferanse, kilde,
                kilde == RefusjonKilde.INNTEKTSMELDING ? new JournalpostId("107") : null,
                List.of(periode(FOM.plusMonths(1), FOM.plusMonths(2).minusDays(1), "1000", RefusjonUtfall.INNVILGET)));

            var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(refusjon)));

            var perioder = nytt.getBeregningsgrunnlagPerioder();
            assertThat(perioder).hasSize(3).allSatisfy(periode ->
                assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList())
                    .extracting(BeregningsgrunnlagPrStatusOgAndelDto::getAndelsnr).containsExactly(1L));
            if (kilde == RefusjonKilde.INNTEKTSMELDING) {
                assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getRefusjonskravPrÅr())
                    .containsExactly(Beløp.ZERO, Beløp.fra(12000), Beløp.ZERO);
                assertThat(perioder).allSatisfy(periode ->
                    assertThat(arbeidsforhold(periode, 0).getSaksbehandletRefusjonPrÅr()).isNull());
            } else {
                assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getSaksbehandletRefusjonPrÅr())
                    .containsExactly(Beløp.ZERO, Beløp.fra(12000), Beløp.ZERO);
                assertThat(perioder).allSatisfy(periode ->
                    assertThat(arbeidsforhold(periode, 0).getRefusjonskravPrÅr()).isNull());
            }
            assertThat(perioder).allSatisfy(periode -> {
                var arbeidsforhold = arbeidsforhold(periode, 0);
                assertThat(arbeidsforhold.getFordeltRefusjonPrÅr()).isNull();
                assertThat(arbeidsforhold.getRefusjon().orElseThrow().getManueltFordeltRefusjonPrÅr()).isNull();
            });
        }
    }

    @Test
    void skal_prioritere_saksbehandlerandel_selv_om_den_bare_dekker_deler_av_inntektsmeldingsperioden() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var utenReferanse = InternArbeidsforholdRefDto.nullRef();
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(1L).medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID).medBeregnetPrÅr(Beløp.fra(100000))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(AG).medArbeidsforholdRef(utenReferanse)
                .medArbeidsperiodeFom(FOM.minusYears(1))
                .medRefusjon(Refusjon.medRefusjonskravPrÅr(Beløp.fra(12000), null, Utfall.GODKJENT))).build(p);
        var inntektsmelding = new Refusjonsandel(AG, utenReferanse, RefusjonKilde.INNTEKTSMELDING, new JournalpostId("108"),
            List.of(periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)));
        var overstyring = refusjonsandel(AG, utenReferanse,
            periode(FOM.plusMonths(1), FOM.plusMonths(2).minusDays(1), "2000", RefusjonUtfall.INNVILGET));

        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg),
            new FordelingGrunnlag(List.of(inntektsmelding, overstyring)));

        var perioder = nytt.getBeregningsgrunnlagPerioder();
        assertThat(perioder).extracting(BeregningsgrunnlagPeriodeDto::getPeriode).containsExactly(
            Intervall.fraOgMedTilOgMed(FOM, FOM.plusMonths(1).minusDays(1)),
            Intervall.fraOgMedTilOgMed(FOM.plusMonths(1), FOM.plusMonths(2).minusDays(1)),
            Intervall.fraOgMed(FOM.plusMonths(2)));
        assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getRefusjonskravPrÅr())
            .containsExactly(Beløp.fra(12000), Beløp.fra(12000), Beløp.fra(12000));
        assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getSaksbehandletRefusjonPrÅr())
            .containsExactly(Beløp.ZERO, Beløp.fra(24000), Beløp.ZERO);
        assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getGjeldendeRefusjonPrÅr())
            .containsExactly(Beløp.ZERO, Beløp.fra(24000), Beløp.ZERO);
    }

    private static BeregningsgrunnlagDto bg() {
        return BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(FOM).medGrunnbeløp(Beløp.fra(90000)).build();
    }

    private static BeregningsgrunnlagInput input(BeregningsgrunnlagDto bg, Intervall... arbeidsperioder) {
        var iay = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        for (var arbeidsperiode : arbeidsperioder) {
            BeregningIAYTestUtil.byggArbeidForBehandling(FOM, arbeidsperiode, INTERN_ARBEIDSFORHOLD_REF, AG, iay);
        }
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(bg).build(BeregningsgrunnlagTilstand.FORESLÅTT);
        return new BeregningsgrunnlagInput(new KoblingReferanseMock(FOM, FOM.minusDays(1)), iay.build(), null, List.of(),
            new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false)).medBeregningsgrunnlagGrunnlag(grunnlag);
    }

    private static void leggTilAndel(BeregningsgrunnlagPeriodeDto p, Arbeidsgiver ag, InternArbeidsforholdRefDto ref,
                                    Long andelsnr, int beregnet) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(andelsnr).medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID).medBeregnetPrÅr(Beløp.fra(beregnet))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ag).medArbeidsforholdRef(ref)
                .medArbeidsperiodeFom(FOM.minusYears(1)).medNaturalytelseBortfaltPrÅr(Beløp.fra(500))
                .medRefusjon(new Refusjon(Beløp.fra(999), Beløp.fra(777), Beløp.fra(666), Beløp.fra(555),
                    Hjemmel.F_22_13_6, Utfall.UNDERKJENT))).build(p);
    }

    private static BGAndelArbeidsforholdDto arbeidsforhold(BeregningsgrunnlagPeriodeDto p, int indeks) {
        return p.getBeregningsgrunnlagPrStatusOgAndelList().get(indeks).getBgAndelArbeidsforhold().orElseThrow();
    }

    private static Refusjonsandel refusjonsandel(Arbeidsgiver ag, InternArbeidsforholdRefDto ref, Refusjonsperiode... perioder) {
        return new Refusjonsandel(ag, ref, RefusjonKilde.SAKSBEHANDLER, null, List.of(perioder));
    }

    private static Refusjonsperiode periode(LocalDate fom, LocalDate tom, String beløp, RefusjonUtfall utfall) {
        return new Refusjonsperiode(Intervall.fraOgMedTilOgMed(fom, tom == null ? TIDENES_ENDE : tom),
            Beløp.fra(new BigDecimal(beløp)), utfall, null);
    }
}
