package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.RefusjonOverstyringHåndterer;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.OverstyrRefusjonDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonOverstyringAndel;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
    private static final InternArbeidsforholdRefDto REF = InternArbeidsforholdRefDto.namedRef("første");


    // Anja sin test
    @Test
    void skal_bevare_inntektsmeldingsbeløp_og_oppdatere_saksbehandlet_refusjon_med_endring_og_opphør() {
        var bg = bg();
        var opprinneligPeriode = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var overstyrtArbeidsgiver = Arbeidsgiver.virksomhet("888888888");
        var utenReferanse = InternArbeidsforholdRefDto.nullRef();
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(1L).medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID).medBeregnetPrÅr(Beløp.fra(100000))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(AG).medArbeidsforholdRef(utenReferanse)
                .medArbeidsperiodeFom(FOM.minusYears(1))
                .medRefusjon(Refusjon.medRefusjonskravPrÅr(Beløp.fra(12000), null, Utfall.GODKJENT)))
            .build(opprinneligPeriode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(2L).medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID).medBeregnetPrÅr(Beløp.fra(200000))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(overstyrtArbeidsgiver).medArbeidsforholdRef(utenReferanse)
                .medArbeidsperiodeFom(FOM.minusYears(1))
                .medRefusjon(new Refusjon(Beløp.fra(24000), Beløp.fra(36000), null, null, null, Utfall.GODKJENT)))
            .build(opprinneligPeriode);
        var inntektsmelding = new Refusjonsandel(AG, utenReferanse, RefusjonKilde.INNTEKTSMELDING, new JournalpostId("101"),
            List.of(periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)));
        var overstyrtInntektsmelding = new Refusjonsandel(overstyrtArbeidsgiver, utenReferanse, RefusjonKilde.INNTEKTSMELDING, new JournalpostId("102"),
            List.of(periode(FOM, null, "2000", RefusjonUtfall.INNVILGET)));
        var gammelOverstyring = refusjonsandelForSbh(overstyrtArbeidsgiver, utenReferanse,
            new Refusjonsperiode(Intervall.fraOgMed(FOM), Beløp.fra(3000), RefusjonUtfall.INNVILGET,
                RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT));
        var forrigeFordelingsgrunnlag = new FordelingGrunnlag(List.of(inntektsmelding, overstyrtInntektsmelding, gammelOverstyring));
        var håndterInput = new HåndterBeregningsgrunnlagInput(
            input(bg).medFordelingGrunnlag(forrigeFordelingsgrunnlag), BeregningsgrunnlagTilstand.FASTSATT_INN);
        var endringsdato = FOM.plusMonths(1);
        var opphørsdato = FOM.plusMonths(2);
        var dto = new OverstyrRefusjonDto(List.of(
            new RefusjonOverstyringAndel(AG, utenReferanse, RefusjonKilde.INNTEKTSMELDING, List.of(
                new RefusjonOverstyringAndel.RefusjonOverstyringPeriode(FOM, TIDENES_ENDE, BigDecimal.valueOf(1000), RefusjonUtfall.INNVILGET, null))),
            new RefusjonOverstyringAndel(overstyrtArbeidsgiver, utenReferanse, RefusjonKilde.INNTEKTSMELDING, List.of(
                new RefusjonOverstyringAndel.RefusjonOverstyringPeriode(FOM, TIDENES_ENDE, BigDecimal.valueOf(2000), RefusjonUtfall.INNVILGET, null))),
            new RefusjonOverstyringAndel(overstyrtArbeidsgiver, utenReferanse, RefusjonKilde.SAKSBEHANDLER, List.of(
                new RefusjonOverstyringAndel.RefusjonOverstyringPeriode(FOM, TIDENES_ENDE, BigDecimal.valueOf(3000), RefusjonUtfall.INNVILGET,
                    RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT))),
            new RefusjonOverstyringAndel(overstyrtArbeidsgiver, utenReferanse, RefusjonKilde.SAKSBEHANDLER, List.of(
                new RefusjonOverstyringAndel.RefusjonOverstyringPeriode(FOM, TIDENES_ENDE, BigDecimal.valueOf(5000), RefusjonUtfall.INNVILGET,
                    RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT))),
            new RefusjonOverstyringAndel(AG, utenReferanse, RefusjonKilde.SAKSBEHANDLER, List.of(
                new RefusjonOverstyringAndel.RefusjonOverstyringPeriode(FOM, endringsdato.minusDays(1), BigDecimal.valueOf(1000), RefusjonUtfall.INNVILGET,
                    RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT),
                new RefusjonOverstyringAndel.RefusjonOverstyringPeriode(endringsdato, opphørsdato.minusDays(1), BigDecimal.valueOf(4000), RefusjonUtfall.INNVILGET,
                    RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT),
                new RefusjonOverstyringAndel.RefusjonOverstyringPeriode(opphørsdato, TIDENES_ENDE, BigDecimal.ZERO, RefusjonUtfall.INNVILGET,
                    RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT)))));

        var resultat = RefusjonOverstyringHåndterer.håndter(håndterInput, dto);

        var perioder = resultat.beregningsgrunnlagGrunnlag().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(perioder).extracting(BeregningsgrunnlagPeriodeDto::getPeriode).containsExactly(
            Intervall.fraOgMedTilOgMed(FOM, endringsdato.minusDays(1)),
            Intervall.fraOgMedTilOgMed(endringsdato, opphørsdato.minusDays(1)), Intervall.fraOgMed(opphørsdato));
        assertThat(perioder).allSatisfy(p -> assertThat(p.getBeregningsgrunnlagPrStatusOgAndelList())
            .extracting(BeregningsgrunnlagPrStatusOgAndelDto::getAndelsnr).containsExactly(1L, 2L));
        assertThat(resultat.fordelingGrunnlag().refusjonsandeler()).hasSize(4)
            .contains(inntektsmelding, overstyrtInntektsmelding).doesNotContain(gammelOverstyring);
        assertThat(resultat.fordelingGrunnlag().refusjonsandeler())
            .filteredOn(a -> a.kilde() == RefusjonKilde.SAKSBEHANDLER).hasSize(2)
            .allSatisfy(a -> assertThat(a.refusjonsperioder()).allSatisfy(p ->
                assertThat(p.utfallÅrsak()).isEqualTo(RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT)));
        assertSoftly(softly -> {
            softly.assertThat(perioder).as("IM-beløpet på den tidligere ikke-overstyrte andelen")
                .extracting(p -> arbeidsforhold(p, 0).getRefusjonskravPrÅr())
                .containsExactly(Beløp.fra(12000), Beløp.fra(12000), Beløp.fra(12000));
            softly.assertThat(perioder).as("Ny overstyring med beløpsendring og opphør")
                .extracting(p -> arbeidsforhold(p, 0).getSaksbehandletRefusjonPrÅr())
                .containsExactly(Beløp.fra(12000), Beløp.fra(48000), Beløp.ZERO);
            softly.assertThat(perioder).as("IM-beløpet på den tidligere overstyrte andelen")
                .extracting(p -> arbeidsforhold(p, 1).getRefusjonskravPrÅr())
                .containsExactly(Beløp.fra(24000), Beløp.fra(24000), Beløp.fra(24000));
            softly.assertThat(perioder).as("Endret overstyring erstatter tidligere saksbehandlet beløp")
                .extracting(p -> arbeidsforhold(p, 1).getSaksbehandletRefusjonPrÅr())
                .containsExactly(Beløp.fra(60000), Beløp.fra(60000), Beløp.fra(60000));
        });
    }

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
        leggTilAndel(første, AG, REF, 1L, 300000);
        var andre = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM.plusMonths(2), null)
            .leggTilPeriodeÅrsak(PeriodeÅrsak.GRADERING).build(bg);
        leggTilAndel(andre, AG, REF, 1L, 400000);
        BeregningsgrunnlagPeriodeDto.oppdater(andre).medBruttoPrÅr(Beløp.fra(410000))
            .medAvkortetPrÅr(Beløp.fra(390000)).medRedusertPrÅr(Beløp.fra(195000))
            .medTotalUtbetalingsgradFraUttak(BigDecimal.valueOf(50)).build();
        var før = BeregningsgrunnlagDto.builder(bg).build();
        var grunnlag = new FordelingGrunnlag(List.of(refusjonsandel(AG, REF,
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
        assertThat(perioder).extracting(p -> arbeidsforhold(p, 0).getRefusjonskravPrÅr()).containsExactly(
            Beløp.fra(new BigDecimal("14814.72")), Beløp.fra(new BigDecimal("14814.72")), Beløp.fra(24000), Beløp.ZERO);
        assertThat(perioder.get(2).getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.GRADERING, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertThat(perioder.get(3).getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.GRADERING, PeriodeÅrsak.REFUSJON_OPPHØRER);
        assertThat(arbeidsforhold(perioder.getLast(), 0).getGjeldendeRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(arbeidsforhold(perioder.getLast(), 0).getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(arbeidsforhold(perioder.getLast(), 0).getFordeltRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
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
    void skal_matche_refusjonsandel_per_opprinnelig_bg_periode() {
        var bg = bg();
        var førstePeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(FOM, FOM.plusMonths(1).minusDays(1)).build(bg);
        leggTilAndel(førstePeriode, AG, REF, 1L, 100000);
        var andrePeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(FOM.plusMonths(1), null).build(bg);
        var annenArbeidsgiver = Arbeidsgiver.virksomhet("888888888");
        leggTilAndel(andrePeriode, annenArbeidsgiver, REF, 2L, 200000);
        var grunnlag = new FordelingGrunnlag(List.of(inntektsmelding(AG, REF,
            periode(FOM, null, "1000", RefusjonUtfall.INNVILGET))));

        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(
            input(bg, Intervall.fraOgMed(FOM.minusYears(1))), grunnlag);

        var perioder = nytt.getBeregningsgrunnlagPerioder();
        assertThat(perioder).extracting(BeregningsgrunnlagPeriodeDto::getPeriode)
            .containsExactly(førstePeriode.getPeriode(), andrePeriode.getPeriode());
        assertThat(perioder.getFirst().getBeregningsgrunnlagPrStatusOgAndelList()).singleElement()
            .satisfies(andel -> {
                assertThat(andel.getBeregnetPrÅr()).isEqualTo(Beløp.fra(100000));
                assertThat(andel.getBgAndelArbeidsforhold().orElseThrow().getRefusjonskravPrÅr())
                    .isEqualTo(Beløp.fra(12000));
            });
        var andelerIAndrePeriode = perioder.getLast().getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIAndrePeriode).extracting(andel -> andel.getArbeidsgiver().orElseThrow())
            .containsExactly(annenArbeidsgiver, AG);
        assertThat(arbeidsforhold(perioder.getLast(), 0).getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(999));
        assertThat(arbeidsforhold(perioder.getLast(), 1).getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(12000));
        assertThat(andelerIAndrePeriode.getLast().getKilde()).isEqualTo(AndelKilde.PROSESS_PERIODISERING);
    }

    @Test
    void skal_skille_arbeidsgivere_og_referanser_og_bevare_andeler_uten_krav() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var ref2 = InternArbeidsforholdRefDto.namedRef("andre");
        var ag2 = Arbeidsgiver.virksomhet("888888888");
        leggTilAndel(p, AG, REF, 1L, 100000);
        leggTilAndel(p, AG, ref2, 2L, 200000);
        leggTilAndel(p, ag2, REF, 3L, 300000);
        leggTilAndel(p, ag2, ref2, 4L, 400000);
        var grunnlag = new FordelingGrunnlag(List.of(
            refusjonsandel(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)),
            refusjonsandel(AG, ref2, periode(FOM, null, "2000", RefusjonUtfall.INNVILGET)),
            refusjonsandel(ag2, REF, periode(FOM, null, "3000", RefusjonUtfall.INNVILGET))));

        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), grunnlag).getBeregningsgrunnlagPerioder().getFirst();

        assertThat(nytt.getBeregningsgrunnlagPrStatusOgAndelList()).extracting(a -> a.getBgAndelArbeidsforhold().orElseThrow().getRefusjonskravPrÅr())
            .containsExactly(Beløp.fra(12000), Beløp.fra(24000), Beløp.fra(36000), Beløp.fra(999));
    }

    @Test
    void skal_matche_uspesifisert_ref_mot_første_andel_og_ikke_matche_spesifisert_ref_mot_bg_uten_ref() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000);
        leggTilAndel(p, AG, InternArbeidsforholdRefDto.namedRef("andre"), 2L, 200000);
        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(
            refusjonsandel(AG, null, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)))));
        assertThat(arbeidsforhold(nytt.getBeregningsgrunnlagPerioder().getFirst(), 0).getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(12000));
        assertThat(arbeidsforhold(nytt.getBeregningsgrunnlagPerioder().getFirst(), 1).getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(999));

        var bgUtenRef = bg();
        var utenRef = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bgUtenRef);
        leggTilAndel(utenRef, AG, InternArbeidsforholdRefDto.nullRef(), 1L, 100000);
        var medNy = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bgUtenRef), new FordelingGrunnlag(List.of(
            refusjonsandel(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)))));
        var andeler = medNy.getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(2);
        assertThat(andeler.getFirst().getBgAndelArbeidsforhold().orElseThrow().getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(999));
        assertThat(andeler.getLast().getArbeidsforholdRef()).contains(REF);
    }

    @Test
    void skal_skille_krav_med_og_uten_referanse_uavhengig_av_rekkefølge() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var utenReferanse = InternArbeidsforholdRefDto.nullRef();
        leggTilAndel(p, AG, utenReferanse, 1L, 100000);
        var grunnlag = new FordelingGrunnlag(List.of(
            refusjonsandel(AG, REF, periode(FOM, null, "2000", RefusjonUtfall.INNVILGET)),
            refusjonsandel(AG, utenReferanse, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET))));

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), grunnlag);

        var andeler = resultat.getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(2);
        assertThat(andeler).filteredOn(a -> a.getArbeidsforholdRef().orElseThrow().equals(utenReferanse))
            .singleElement().satisfies(a -> assertThat(a.getBgAndelArbeidsforhold().orElseThrow().getRefusjonskravPrÅr())
                .isEqualTo(Beløp.fra(12000)));
        assertThat(andeler).filteredOn(a -> a.getArbeidsforholdRef().orElseThrow().equals(REF))
            .singleElement().satisfies(a -> assertThat(a.getBgAndelArbeidsforhold().orElseThrow().getRefusjonskravPrÅr())
                .isEqualTo(Beløp.fra(24000)));
    }

    @Test
    void skal_opprette_ny_andel_kun_med_positivt_krav_og_bruke_faktisk_arbeidsperiode() {
        var bg = bg();
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var arbeidsperiode = Intervall.fraOgMedTilOgMed(FOM.minusYears(2), FOM.plusYears(1));
        var refusjon = inntektsmelding(AG, REF,
            periode(FOM.plusMonths(1), FOM.plusMonths(2).minusDays(1), "1000", RefusjonUtfall.INNVILGET));

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
        assertThat(arbeidsforhold(perioder.get(1), 0).getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(12000));
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
        BeregningIAYTestUtil.byggArbeidForBehandling(FOM, Intervall.fraOgMed(FOM.minusYears(4)),
            REF, Arbeidsgiver.virksomhet("888888888"), iay);
        var arbeidsperiode = Intervall.fraOgMedTilOgMed(FOM.minusYears(2), FOM.plusYears(1));
        BeregningIAYTestUtil.byggArbeidForBehandling(FOM, arbeidsperiode, REF, AG, iay);
        var originalInput = input(bg);
        var input = new BeregningsgrunnlagInput(originalInput.getKoblingReferanse(), iay.build(), null, List.of(),
            originalInput.getYtelsespesifiktGrunnlag()).medBeregningsgrunnlagGrunnlag(originalInput.getBeregningsgrunnlagGrunnlag());
        var grunnlag = new FordelingGrunnlag(List.of(refusjonsandel(AG, REF,
            periode(FOM, null, "1000", RefusjonUtfall.INNVILGET))));

        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input, grunnlag);

        var af = arbeidsforhold(nytt.getBeregningsgrunnlagPerioder().getFirst(), 0);
        assertThat(af.getArbeidsperiodeFom()).isEqualTo(arbeidsperiode.getFomDato());
        assertThat(af.getArbeidsperiodeTom()).contains(arbeidsperiode.getTomDato());
    }

    @Test
    void skal_mappe_avslag_og_bevare_eksisterende_hjemmel() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000);
        var avslag = new FordelingGrunnlag(List.of(refusjonsandel(AG, REF,
            periode(FOM.plusDays(10), null, "1000", RefusjonUtfall.AVSLÅTT))));

        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), avslag)
            .getBeregningsgrunnlagPerioder().getLast();

        var af = arbeidsforhold(nytt, 0);
        assertThat(af.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(12000));
        assertThat(af.getInnvilgetRefusjonskravPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(af.getGjeldendeRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(af.getRefusjon().orElseThrow().getRefusjonskravFristUtfall()).isEqualTo(Utfall.UNDERKJENT);
        assertThat(af.getHjemmelForRefusjonskravfrist()).isEqualTo(Hjemmel.F_22_13_6);
        assertThat(nytt.getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.REFUSJON_AVSLÅTT);
    }

    @Test
    void skal_avvise_overlappende_krav_som_matcher_samme_bg_andel() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000);
        var overlappendeInput = input(bg);
        var overlappendeFordelingsgrunnlag = new FordelingGrunnlag(List.of(
            refusjonsandel(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)),
            refusjonsandel(AG, null, periode(FOM, null, "2000", RefusjonUtfall.INNVILGET))));

        assertThatThrownBy(() -> PeriodiserBeregningsgrunnlagForRefusjon.periodiser(
            overlappendeInput, overlappendeFordelingsgrunnlag))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Overlappende refusjonsperioder treffer samme BG-andel.");
    }

    @Test
    void skal_prioritere_saksbehandler_fremfor_inntektsmelding_ved_opprettelse_av_ny_andel() {
        var bg = bg();
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var grunnlag = new FordelingGrunnlag(List.of(
            inntektsmelding(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)),
            saksbehandlerRefusjonsandel(AG, REF, periode(FOM, null, "2000", RefusjonUtfall.INNVILGET))));

        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), grunnlag);

        assertThat(nytt.getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPrStatusOgAndelList())
            .singleElement()
            .satisfies(a -> {
                var refusjon = a.getBgAndelArbeidsforhold().orElseThrow().getRefusjon().orElseThrow();
                assertThat(refusjon.getRefusjonskravPrÅr()).isEqualTo(Beløp.ZERO);
                assertThat(refusjon.getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.fra(24000));
            });
    }

    @Test
    void skal_mappe_redusert_til_godkjent_uten_å_endre_fordelingsgrunnlaget() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000);
        var redusert = periode(FOM, null, "1000", RefusjonUtfall.REDUSERT);
        var fordelingsgrunnlag = new FordelingGrunnlag(List.of(refusjonsandel(AG, REF, redusert)));

        var nytt = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), fordelingsgrunnlag);

        var af = arbeidsforhold(nytt.getBeregningsgrunnlagPerioder().getFirst(), 0);
        assertThat(af.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(12000));
        assertThat(af.getInnvilgetRefusjonskravPrÅr()).isEqualTo(Beløp.fra(12000));
        assertThat(af.getRefusjon().orElseThrow().getRefusjonskravFristUtfall()).isEqualTo(Utfall.GODKJENT);
        assertThat(fordelingsgrunnlag.refusjonsandeler().getFirst().refusjonsperioder()).containsExactly(redusert);
        assertThat(redusert.utfall()).isEqualTo(RefusjonUtfall.REDUSERT);
    }

    @Test
    void tomt_fordelingsgrunnlag_bevarer_perioder_andeler_og_alle_felt_uten_mutasjon() {
        var bg = bg();
        var første = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, FOM.plusDays(9)).build(bg);
        leggTilAndel(første, AG, REF, 1L, 100000);
        var ag2 = Arbeidsgiver.virksomhet("888888888");
        var andre = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM.plusDays(10), null).build(bg);
        leggTilAndel(andre, ag2, REF, 2L, 200000);
        var før = BeregningsgrunnlagDto.builder(bg).build();

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of()));

        assertThat(resultat).isNotSameAs(bg);
        assertThat(resultat.getBeregningsgrunnlagPerioder()).hasSize(2);
        assertThat(resultat.getBeregningsgrunnlagPerioder()).usingRecursiveComparison()
            .isEqualTo(før.getBeregningsgrunnlagPerioder());
        assertThat(bg).usingRecursiveComparison().isEqualTo(før);
    }

    @Test
    void ingen_beregningsgrunnlagsperioder_gir_tomt_resultat_uten_mutasjon() {
        var bg = bg();
        var før = BeregningsgrunnlagDto.builder(bg).build();

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg),
            new FordelingGrunnlag(List.of(
                inntektsmelding(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)))));

        assertThat(resultat.getBeregningsgrunnlagPerioder()).isEmpty();
        assertThat(bg).usingRecursiveComparison().isEqualTo(før);
    }

    @Test
    void tom_tidslinje_nullstiller_eksisterende_refusjonsverdier_og_bevarer_hjemmel() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000, refusjon(999, 777, 666, 555, Hjemmel.F_22_13_6, Utfall.GODKJENT));

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg),
            new FordelingGrunnlag(List.of(inntektsmelding(AG, REF))));

        var r = arbeidsforhold(resultat.getBeregningsgrunnlagPerioder().getFirst(), 0).getRefusjon().orElseThrow();
        assertThat(r.getRefusjonskravPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(r.getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(r.getFordeltRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(r.getManueltFordeltRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(r.getRefusjonskravFristUtfall()).isEqualTo(Utfall.UDEFINERT);
        assertThat(r.getHjemmelForRefusjonskravfrist()).isEqualTo(Hjemmel.F_22_13_6);
    }

    @Test
    void tom_ikke_matchet_tidslinje_oppretter_ikke_ny_andel() {
        var bg = bg();
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg),
            new FordelingGrunnlag(List.of(inntektsmelding(AG, REF))));

        assertThat(resultat.getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPrStatusOgAndelList())
            .isEmpty();
    }

    @Test
    void fullt_krav_bevarer_alle_eksisterende_refusjonsfelt_separat() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000, refusjon(999, 777, 666, 555, Hjemmel.F_22_13_6, Utfall.UNDERKJENT));

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg),
            new FordelingGrunnlag(List.of(
                inntektsmelding(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)))));

        var r = arbeidsforhold(resultat.getBeregningsgrunnlagPerioder().getFirst(), 0).getRefusjon().orElseThrow();
        assertThat(r.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(12000));
        assertThat(r.getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.fra(777));
        assertThat(r.getFordeltRefusjonPrÅr()).isEqualTo(Beløp.fra(666));
        assertThat(r.getManueltFordeltRefusjonPrÅr()).isEqualTo(Beløp.fra(555));
        assertThat(r.getHjemmelForRefusjonskravfrist()).isEqualTo(Hjemmel.F_22_13_6);
        assertThat(r.getRefusjonskravFristUtfall()).isEqualTo(Utfall.GODKJENT);
    }

    @Test
    void nytt_positivt_krav_beholder_nullverdier_for_eksisterende_felter_og_hjemmel() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000, new Refusjon(Beløp.fra(999), null, null, null, null, null));

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg),
            new FordelingGrunnlag(List.of(
                inntektsmelding(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.REDUSERT)))));

        var r = arbeidsforhold(resultat.getBeregningsgrunnlagPerioder().getFirst(), 0).getRefusjon().orElseThrow();
        assertThat(r.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(12000));
        assertThat(r.getSaksbehandletRefusjonPrÅr()).isNull();
        assertThat(r.getFordeltRefusjonPrÅr()).isNull();
        assertThat(r.getManueltFordeltRefusjonPrÅr()).isNull();
        assertThat(r.getHjemmelForRefusjonskravfrist()).isNull();
        assertThat(r.getRefusjonskravFristUtfall()).isEqualTo(Utfall.GODKJENT);
    }

    @Test
    void matchet_andel_uten_refusjon_får_krav_uten_fordeling_eller_hjemmel() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000, null);
        var grunnlag = new FordelingGrunnlag(List.of(
            inntektsmelding(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET))));

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), grunnlag);

        var r = arbeidsforhold(resultat.getBeregningsgrunnlagPerioder().getFirst(), 0).getRefusjon().orElseThrow();
        assertThat(r.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(12000));
        assertThat(r.getSaksbehandletRefusjonPrÅr()).isNull();
        assertThat(r.getFordeltRefusjonPrÅr()).isNull();
        assertThat(r.getManueltFordeltRefusjonPrÅr()).isNull();
        assertThat(r.getHjemmelForRefusjonskravfrist()).isNull();
        assertThat(r.getRefusjonskravFristUtfall()).isEqualTo(Utfall.GODKJENT);
        assertThat(arbeidsforhold(p, 0).getRefusjon()).isEmpty();
    }

    @Test
    void nullstilt_krav_beholder_null_for_opprinnelige_nullverdier() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000, new Refusjon(Beløp.fra(999), null, null, null, null, null));

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg),
            new FordelingGrunnlag(List.of(
                inntektsmelding(AG, REF, periode(FOM, null, "0", RefusjonUtfall.INNVILGET)))));

        var r = arbeidsforhold(resultat.getBeregningsgrunnlagPerioder().getFirst(), 0).getRefusjon().orElseThrow();
        assertThat(r.getRefusjonskravPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(r.getSaksbehandletRefusjonPrÅr()).isNull();
        assertThat(r.getFordeltRefusjonPrÅr()).isNull();
        assertThat(r.getManueltFordeltRefusjonPrÅr()).isNull();
        assertThat(r.getHjemmelForRefusjonskravfrist()).isNull();
    }

    @Test
    void avslag_nullstiller_eksisterende_fordeling_men_beholder_hjemmel() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000, refusjon(999, 777, 666, 555, Hjemmel.F_22_13_6, Utfall.GODKJENT));

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg),
            new FordelingGrunnlag(List.of(
                inntektsmelding(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.AVSLÅTT)))));

        var r = arbeidsforhold(resultat.getBeregningsgrunnlagPerioder().getFirst(), 0).getRefusjon().orElseThrow();
        assertThat(r.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(12000));
        assertThat(r.getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(r.getFordeltRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(r.getManueltFordeltRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(r.getHjemmelForRefusjonskravfrist()).isEqualTo(Hjemmel.F_22_13_6);
        assertThat(r.getRefusjonskravFristUtfall()).isEqualTo(Utfall.UNDERKJENT);
    }

    @Test
    void kravgrenser_og_hull_periodiseres_før_mellom_og_etter_aktive_segmenter() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, FOM.plusDays(19)).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000);
        var dag6 = FOM.plusDays(5);
        var dag10 = FOM.plusDays(9);
        var dag14 = FOM.plusDays(13);
        var dag17 = FOM.plusDays(16);

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(
            inntektsmelding(AG, REF, periode(dag6, dag10, "1000", RefusjonUtfall.INNVILGET),
                periode(dag14, dag17, "2000", RefusjonUtfall.INNVILGET)))));

        var perioder = resultat.getBeregningsgrunnlagPerioder();
        assertThat(perioder).extracting(BeregningsgrunnlagPeriodeDto::getPeriode).containsExactly(
            Intervall.fraOgMedTilOgMed(FOM, dag6.minusDays(1)),
            Intervall.fraOgMedTilOgMed(dag6, dag10),
            Intervall.fraOgMedTilOgMed(dag10.plusDays(1), dag14.minusDays(1)),
            Intervall.fraOgMedTilOgMed(dag14, dag17),
            Intervall.fraOgMedTilOgMed(dag17.plusDays(1), FOM.plusDays(19)));
        assertThat(perioder).extracting(x -> arbeidsforhold(x, 0).getRefusjonskravPrÅr())
            .containsExactly(Beløp.ZERO, Beløp.fra(12000), Beløp.ZERO, Beløp.fra(24000), Beløp.ZERO);
        assertThat(perioder.get(0).getPeriodeÅrsaker()).isEmpty();
        assertThat(perioder.get(2).getPeriodeÅrsaker()).contains(PeriodeÅrsak.REFUSJON_OPPHØRER);
        assertThat(perioder.get(3).getPeriodeÅrsaker()).contains(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertThat(perioder.get(4).getPeriodeÅrsaker()).contains(PeriodeÅrsak.REFUSJON_OPPHØRER);
    }

    @Test
    void endagskrav_treffes_på_eksakt_dato_og_segmenteres_rundt_dagen() {
        var bg = bg();
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, FOM.plusDays(2)).build(bg);
        var endagsdato = FOM.plusDays(1);
        var grunnlag = new FordelingGrunnlag(List.of(inntektsmelding(AG, REF,
            periode(endagsdato, endagsdato, "1000", RefusjonUtfall.INNVILGET))));

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), grunnlag);

        assertThat(resultat.getBeregningsgrunnlagPerioder()).extracting(BeregningsgrunnlagPeriodeDto::getPeriode)
            .containsExactly(Intervall.fraOgMedTilOgMed(FOM, FOM), Intervall.fraOgMedTilOgMed(endagsdato, endagsdato),
                Intervall.fraOgMedTilOgMed(FOM.plusDays(2), FOM.plusDays(2)));
        assertThat(arbeidsforhold(resultat.getBeregningsgrunnlagPerioder().get(1), 0).getRefusjonskravPrÅr())
            .isEqualTo(Beløp.fra(12000));
    }

    @Test
    void refusjonstidslinje_som_starter_og_slutter_utenfor_bg_perioden_lager_ikke_ekstra_splitt() {
        var bg = bg();
        var bgTom = FOM.plusDays(4);
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, bgTom).build(bg);
        var startFørBg = FOM.minusDays(2);
        var sluttEtterBg = bgTom.plusDays(2);

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(
            inntektsmelding(AG, REF, periode(startFørBg, sluttEtterBg, "1000", RefusjonUtfall.INNVILGET)))));

        assertThat(resultat.getBeregningsgrunnlagPerioder()).singleElement()
            .satisfies(x -> {
                assertThat(x.getPeriode()).isEqualTo(Intervall.fraOgMedTilOgMed(FOM, bgTom));
                assertThat(x.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
                assertThat(arbeidsforhold(x, 0).getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(12000));
            });
    }

    @Test
    void sammenhengende_segmenter_gir_ikke_opphørsårsak_mellom_segmentene() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000);
        var sisteDag = FOM.plusDays(9);

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(
            inntektsmelding(AG, REF, periode(FOM, sisteDag, "1000", RefusjonUtfall.INNVILGET),
                periode(sisteDag.plusDays(1), null, "2000", RefusjonUtfall.REDUSERT)))));

        assertThat(resultat.getBeregningsgrunnlagPerioder()).hasSize(2);
        assertThat(resultat.getBeregningsgrunnlagPerioder().get(1).getPeriodeÅrsaker())
            .containsExactly(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV)
            .doesNotContain(PeriodeÅrsak.REFUSJON_OPPHØRER);
    }

    @Test
    void skjæringstidspunkt_utelater_refusjonsårsak_fra_periodestart() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000);

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(
            inntektsmelding(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.REDUSERT)))));

        assertThat(resultat.getBeregningsgrunnlagPerioder()).singleElement()
            .satisfies(x -> assertThat(x.getPeriodeÅrsaker()).isEmpty());
    }

    @Test
    void første_zero_segment_gir_ikke_ekstra_splitt_mens_senere_zero_er_opphør() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000);
        var overgang = FOM.plusDays(4);
        var opphør = FOM.plusDays(8);

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(
            inntektsmelding(AG, REF, periode(FOM.plusDays(1), overgang.minusDays(1), "0", RefusjonUtfall.INNVILGET),
                periode(overgang, opphør.minusDays(1), "1000", RefusjonUtfall.INNVILGET),
                periode(opphør, null, "0", RefusjonUtfall.INNVILGET)))));

        assertThat(resultat.getBeregningsgrunnlagPerioder()).hasSize(3);
        assertThat(resultat.getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPeriodeTom())
            .isEqualTo(overgang.minusDays(1));
        assertThat(resultat.getBeregningsgrunnlagPerioder().get(1).getPeriodeÅrsaker())
            .containsExactly(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertThat(resultat.getBeregningsgrunnlagPerioder().getLast().getPeriodeÅrsaker())
            .containsExactly(PeriodeÅrsak.REFUSJON_OPPHØRER);
    }

    @Test
    void avslag_med_zero_beløp_får_avslagsårsak() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000);

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(
            inntektsmelding(AG, REF, periode(FOM, FOM, "1000", RefusjonUtfall.INNVILGET),
                periode(FOM.plusDays(1), null, "0", RefusjonUtfall.AVSLÅTT)))));

        assertThat(resultat.getBeregningsgrunnlagPerioder().getLast().getPeriodeÅrsaker())
            .containsExactly(PeriodeÅrsak.REFUSJON_AVSLÅTT);
    }

    @Test
    void første_zero_segment_gir_ingen_splitt_eller_årsak_selv_ved_avslag() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000);
        var grunnlag = new FordelingGrunnlag(List.of(
            inntektsmelding(AG, REF, periode(FOM.plusDays(1), null, "0", RefusjonUtfall.AVSLÅTT))));

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), grunnlag);

        assertThat(resultat.getBeregningsgrunnlagPerioder()).singleElement().satisfies(periode -> {
            assertThat(periode.getPeriode()).isEqualTo(Intervall.fraOgMed(FOM));
            assertThat(periode.getPeriodeÅrsaker()).isEmpty();
            var r = arbeidsforhold(periode, 0).getRefusjon().orElseThrow();
            assertThat(r.getRefusjonskravPrÅr()).isEqualTo(Beløp.ZERO);
            assertThat(r.getRefusjonskravFristUtfall()).isEqualTo(Utfall.UDEFINERT);
        });
    }

    @Test
    void ugyldig_refusjonsperiode_gir_presise_valideringsfeil_uten_mutasjon() {
        var negative = periode(FOM, null, "-1", RefusjonUtfall.INNVILGET);
        var manglendeBeløp = new Refusjonsperiode(Intervall.fraOgMed(FOM), null, RefusjonUtfall.INNVILGET, null);
        var manglendeUtfall = periode(FOM, null, "1000", null);

        assertThatThrownBy(() -> periodiserMedTidslinje(negative))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refusjonsperioden må ha et ikke-negativt månedsbeløp.");
        assertThatThrownBy(() -> periodiserMedTidslinje(manglendeBeløp))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refusjonsperioden må ha et ikke-negativt månedsbeløp.");
        assertThatThrownBy(() -> periodiserMedTidslinje(manglendeUtfall))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Refusjonsperioden mangler utfall.");
    }

    @Test
    void flere_tidslinjer_treffer_samme_bg_andel_med_krav_fra_riktig_dato_og_beholdt_andelsrekkefølge() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000);
        leggTilAndel(p, AG, InternArbeidsforholdRefDto.namedRef("andre"), 2L, 200000);
        var senere = FOM.plusDays(10);

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(
            inntektsmelding(AG, REF, periode(senere, null, "2000", RefusjonUtfall.INNVILGET)),
            inntektsmelding(AG, null, periode(FOM, senere.minusDays(1), "1000", RefusjonUtfall.INNVILGET)))));

        assertThat(resultat.getBeregningsgrunnlagPerioder()).hasSize(2);
        assertThat(resultat.getBeregningsgrunnlagPerioder()).allSatisfy(x ->
            assertThat(x.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2));
        assertThat(resultat.getBeregningsgrunnlagPerioder())
            .extracting(x -> arbeidsforhold(x, 0).getRefusjonskravPrÅr())
            .containsExactly(Beløp.fra(12000), Beløp.fra(24000));
        assertThat(resultat.getBeregningsgrunnlagPerioder())
            .extracting(x -> arbeidsforhold(x, 1).getRefusjonskravPrÅr())
            .containsExactly(Beløp.fra(999), Beløp.fra(999));
    }

    @Test
    void nye_arbeidsgivere_matcher_ikke_eksisterende_arbeidsforhold_og_bevarer_frilansandel() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var annetAg = Arbeidsgiver.virksomhet("888888888");
        leggTilAndel(p, AG, REF, 1L, 100000);
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(2L).medAktivitetStatus(AktivitetStatus.FRILANSER)
            .medArbforholdType(OpptjeningAktivitetType.FRILANS).medBeregnetPrÅr(Beløp.fra(200000)).build(p);

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg),
            new FordelingGrunnlag(List.of(inntektsmelding(annetAg, REF,
                periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)))));

        var andeler = resultat.getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(3);
        assertThat(andeler.get(0).getArbeidsgiver()).contains(AG);
        assertThat(andeler.get(1).getAktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
        assertThat(andeler.get(1).getAndelsnr()).isEqualTo(2L);
        assertThat(andeler.get(2).getArbeidsgiver()).contains(annetAg);
        assertThat(andeler.get(2).getBgAndelArbeidsforhold().orElseThrow().getRefusjonskravPrÅr())
            .isEqualTo(Beløp.fra(12000));
    }

    @Test
    void saksbehandlerkrav_beholder_opprinnelig_krav_og_oppdaterer_saksbehandlet_beløp() {
        var bg = bg();
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var saksbehandler = saksbehandlerRefusjonsandel(AG, REF, periode(FOM, null, "2000", RefusjonUtfall.INNVILGET));
        var inntektsmelding = inntektsmelding(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET));

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg),
            new FordelingGrunnlag(List.of(inntektsmelding, saksbehandler)));

        var andel = resultat.getBeregningsgrunnlagPerioder().getFirst()
            .getBeregningsgrunnlagPrStatusOgAndelList().getFirst();
        assertThat(andel.getKilde()).isEqualTo(AndelKilde.PROSESS_PERIODISERING);
        var af = andel.getBgAndelArbeidsforhold().orElseThrow();
        assertThat(af.getRefusjonskravPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(af.getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.fra(24000));
        assertThat(af.getFordeltRefusjonPrÅr()).isNull();
        assertThat(af.getRefusjon().orElseThrow().getManueltFordeltRefusjonPrÅr()).isNull();
        assertThat(af.getHjemmelForRefusjonskravfrist()).isNull();
    }

    @Test
    void dagens_kildehåndtering_mapper_inntektsmelding_til_årsbeløp_og_beholder_sbh_feltene() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000, refusjon(999, 777, 666, 555, Hjemmel.F_22_13_6, Utfall.UNDERKJENT));

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(
            inntektsmelding(AG, REF, periode(FOM, null, "1250", RefusjonUtfall.INNVILGET)))));

        var r = arbeidsforhold(resultat.getBeregningsgrunnlagPerioder().getFirst(), 0).getRefusjon().orElseThrow();
        assertThat(r.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(15000));
        assertThat(r.getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.fra(777));
        assertThat(r.getFordeltRefusjonPrÅr()).isEqualTo(Beløp.fra(666));
        assertThat(r.getManueltFordeltRefusjonPrÅr()).isEqualTo(Beløp.fra(555));
        assertThat(r.getHjemmelForRefusjonskravfrist()).isEqualTo(Hjemmel.F_22_13_6);
    }

    @Test
    void saksbehandlerkrav_bevarer_eksisterende_krav_og_oppdaterer_feltene() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000, refusjon(999, 777, 666, 555, Hjemmel.F_22_13_6, Utfall.GODKJENT));
        var opprinnelig = BeregningsgrunnlagDto.builder(bg).build();
        var grunnlag = new FordelingGrunnlag(List.of(
            inntektsmelding(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)),
            saksbehandlerRefusjonsandel(AG, REF, periode(FOM, null, "2000", RefusjonUtfall.INNVILGET))));

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), grunnlag);

        var r = arbeidsforhold(resultat.getBeregningsgrunnlagPerioder().getFirst(), 0).getRefusjon().orElseThrow();
        assertThat(r.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(999));
        assertThat(r.getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.fra(24000));
        assertThat(r.getFordeltRefusjonPrÅr()).isEqualTo(Beløp.fra(666));
        assertThat(r.getManueltFordeltRefusjonPrÅr()).isEqualTo(Beløp.fra(555));
        assertThat(r.getHjemmelForRefusjonskravfrist()).isEqualTo(Hjemmel.F_22_13_6);
        assertThat(r.getRefusjonskravFristUtfall()).isEqualTo(Utfall.GODKJENT);
        assertThat(bg).usingRecursiveComparison().isEqualTo(opprinnelig);
    }

    @ParameterizedTest
    @MethodSource("saksbehandlerperioder")
    void saksbehandlerkilde_beholder_kravfelt_og_mapper_innvilget_beløp(String beløp, RefusjonUtfall utfall,
                                                                   int innvilget, Utfall forventetUtfall) {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000, refusjon(108000, 777, 666, 555, Hjemmel.F_22_13_6, Utfall.GODKJENT));
        var krav = periode(FOM, null, beløp, utfall);
        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(
            inntektsmelding(AG, REF), saksbehandlerRefusjonsandel(AG, REF, krav))));
        var r = arbeidsforhold(resultat.getBeregningsgrunnlagPerioder().getFirst(), 0).getRefusjon().orElseThrow();
        var fordelingsbeløp = innvilget == 0 ? Beløp.ZERO : Beløp.fra(666);
        assertThat(r.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(108000));
        assertThat(r.getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.fra(innvilget));
        assertThat(r.getFordeltRefusjonPrÅr()).isEqualTo(fordelingsbeløp);
        assertThat(r.getManueltFordeltRefusjonPrÅr()).isEqualTo(innvilget == 0 ? Beløp.ZERO : Beløp.fra(555));
        assertThat(r.getHjemmelForRefusjonskravfrist()).isEqualTo(Hjemmel.F_22_13_6);
        assertThat(r.getRefusjonskravFristUtfall()).isEqualTo(forventetUtfall);
    }

    private static Stream<Arguments> saksbehandlerperioder() {
        return Stream.of(Arguments.of("1000", RefusjonUtfall.INNVILGET, 12000, Utfall.GODKJENT),
            Arguments.of("1000", RefusjonUtfall.REDUSERT, 12000, Utfall.GODKJENT),
            Arguments.of("0", RefusjonUtfall.INNVILGET, 0, Utfall.GODKJENT),
            Arguments.of("1000", RefusjonUtfall.AVSLÅTT, 0, Utfall.UNDERKJENT),
            Arguments.of("0", RefusjonUtfall.AVSLÅTT, 0, Utfall.UNDERKJENT));
    }

    @Test
    void saksbehandleropphør_og_hull_nullstiller_saksbehandlet_beløp() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, FOM.plusDays(15)).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000, refusjon(108000, 777, 666, 555, Hjemmel.F_22_13_6, Utfall.GODKJENT));
        var grunnlag = new FordelingGrunnlag(List.of(inntektsmelding(AG, REF),
            saksbehandlerRefusjonsandel(AG, REF,
                periode(FOM.plusDays(1), FOM.plusDays(2), "1000", RefusjonUtfall.INNVILGET),
                periode(FOM.plusDays(6), FOM.plusDays(8), "2000", RefusjonUtfall.REDUSERT))));
        var perioder = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), grunnlag)
            .getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(5);
        for (var indeks : List.of(1, 3)) {
            var r = arbeidsforhold(perioder.get(indeks), 0).getRefusjon().orElseThrow();
            assertThat(r.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(108000));
            assertThat(r.getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.fra(indeks == 1 ? 12000 : 24000));
            assertThat(r.getFordeltRefusjonPrÅr()).isEqualTo(Beløp.fra(666));
            assertThat(r.getManueltFordeltRefusjonPrÅr()).isEqualTo(Beløp.fra(555));
            assertThat(r.getHjemmelForRefusjonskravfrist()).isEqualTo(Hjemmel.F_22_13_6);
            assertThat(r.getRefusjonskravFristUtfall()).isEqualTo(Utfall.GODKJENT);
        }
        for (var indeks : List.of(0, 2, 4)) {
            var r = arbeidsforhold(perioder.get(indeks), 0).getRefusjon().orElseThrow();
            assertThat(r.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(108000));
            assertThat(r.getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
            assertThat(r.getFordeltRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
            assertThat(r.getManueltFordeltRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
            assertThat(r.getHjemmelForRefusjonskravfrist()).isEqualTo(Hjemmel.F_22_13_6);
            assertThat(r.getRefusjonskravFristUtfall()).isEqualTo(Utfall.UDEFINERT);
        }
    }

    @Test
    void samtidige_kilder_bruker_aktiv_kilde_og_prioriterer_saksbehandler_i_hull_og_ved_opphør() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, FOM.plusDays(10)).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000, refusjon(108000, 777, 666, 555, Hjemmel.F_22_13_6, Utfall.GODKJENT));
        var imPeriode = periode(FOM, FOM.plusDays(2), "1000", RefusjonUtfall.INNVILGET);
        var sbhPeriode = periode(FOM.plusDays(6), FOM.plusDays(8), "2000", RefusjonUtfall.INNVILGET);
        var grunnlag = new FordelingGrunnlag(List.of(saksbehandlerRefusjonsandel(AG, REF, sbhPeriode),
            inntektsmelding(AG, null, imPeriode), inntektsmelding(AG, REF, sbhPeriode)));
        var perioder = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), grunnlag)
            .getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(4);
        var im = arbeidsforhold(perioder.getFirst(), 0).getRefusjon().orElseThrow();
        assertThat(im.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(12000));
        assertThat(im.getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.fra(777));
        assertThat(im.getFordeltRefusjonPrÅr()).isEqualTo(Beløp.fra(666));
        assertThat(im.getManueltFordeltRefusjonPrÅr()).isEqualTo(Beløp.fra(555));
        assertThat(im.getHjemmelForRefusjonskravfrist()).isEqualTo(Hjemmel.F_22_13_6);
        assertThat(im.getRefusjonskravFristUtfall()).isEqualTo(Utfall.GODKJENT);
        var sbh = arbeidsforhold(perioder.get(2), 0).getRefusjon().orElseThrow();
        assertThat(sbh.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(108000));
        assertThat(sbh.getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.fra(24000));
        assertThat(sbh.getFordeltRefusjonPrÅr()).isEqualTo(Beløp.fra(666));
        assertThat(sbh.getManueltFordeltRefusjonPrÅr()).isEqualTo(Beløp.fra(555));
        assertThat(sbh.getHjemmelForRefusjonskravfrist()).isEqualTo(Hjemmel.F_22_13_6);
        assertThat(sbh.getRefusjonskravFristUtfall()).isEqualTo(Utfall.GODKJENT);
        for (var indeks : List.of(1, 3)) {
            var r = arbeidsforhold(perioder.get(indeks), 0).getRefusjon().orElseThrow();
            assertThat(r.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(108000));
            assertThat(r.getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
            assertThat(r.getFordeltRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
            assertThat(r.getManueltFordeltRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
            assertThat(r.getHjemmelForRefusjonskravfrist()).isEqualTo(Hjemmel.F_22_13_6);
            assertThat(r.getRefusjonskravFristUtfall()).isEqualTo(Utfall.UDEFINERT);
        }
    }

    @Test
    void saksbehandler_tom_tidslinje_og_umatchende_andel_bevarer_bg_feltene() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var ref2 = InternArbeidsforholdRefDto.namedRef("andre");
        var ag2 = Arbeidsgiver.virksomhet("888888888");
        leggTilAndel(p, AG, REF, 1L, 100000, refusjon(999, 777, 666, 555, Hjemmel.F_22_13_6, Utfall.GODKJENT));
        leggTilAndel(p, AG, ref2, 2L, 100000, refusjon(999, 777, 666, 555, Hjemmel.F_22_13_6, Utfall.GODKJENT));
        leggTilAndel(p, ag2, REF, 3L, 100000, refusjon(999, 777, 666, 555, Hjemmel.F_22_13_6, Utfall.GODKJENT));
        var grunnlag = new FordelingGrunnlag(List.of(inntektsmelding(AG, REF), inntektsmelding(AG, ref2),
            saksbehandlerRefusjonsandel(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)),
            saksbehandlerRefusjonsandel(AG, ref2)));
        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), grunnlag)
            .getBeregningsgrunnlagPerioder().getFirst();
        assertThat(arbeidsforhold(resultat, 0).getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(999));
        assertThat(arbeidsforhold(resultat, 0).getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.fra(12000));
        assertThat(arbeidsforhold(resultat, 1).getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(999));
        assertThat(arbeidsforhold(resultat, 1).getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(arbeidsforhold(resultat, 2).getRefusjon().orElseThrow()).usingRecursiveComparison()
            .isEqualTo(refusjon(999, 777, 666, 555, Hjemmel.F_22_13_6, Utfall.GODKJENT));
    }

    @Test
    void null_og_zero_aktive_segmenter_oppretter_ingen_ny_andel() {
        var bg = bg();
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var senere = FOM.plusDays(5);

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), new FordelingGrunnlag(List.of(
            inntektsmelding(AG, REF, periode(FOM, senere.minusDays(1), "0", RefusjonUtfall.INNVILGET)))));

        assertThat(resultat.getBeregningsgrunnlagPerioder()).allSatisfy(x ->
            assertThat(x.getBeregningsgrunnlagPrStatusOgAndelList()).isEmpty());
    }

    @Test
    void ny_andel_opprettes_ikke_når_arbeidsperioden_er_avsluttet_før_skjæringstidspunkt() {
        var bg = bg();
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var tidligereArbeidsperiode = Intervall.fraOgMedTilOgMed(FOM.minusMonths(2), FOM.minusDays(1));

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg, tidligereArbeidsperiode),
            new FordelingGrunnlag(List.of(
                inntektsmelding(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)))));

        assertThat(resultat.getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPrStatusOgAndelList())
            .isEmpty();
    }

    @Test
    void ny_andel_uten_arbeidsperiode_opprettes_uten_arbeidsdatoer() {
        var bg = bg();
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg),
            new FordelingGrunnlag(List.of(
                inntektsmelding(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)))));

        var arbeidsforhold = arbeidsforhold(resultat.getBeregningsgrunnlagPerioder().getFirst(), 0);
        assertThat(arbeidsforhold.getArbeidsperiodeFom()).isNull();
        assertThat(arbeidsforhold.getArbeidsperiodeTom()).isEmpty();
    }

    @Test
    void arbeidsperiode_som_slutter_på_skjæringstidspunkt_er_ikke_avsluttet_før_skjæringstidspunkt() {
        var bg = bg();
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var arbeidsperiode = Intervall.fraOgMedTilOgMed(FOM.minusDays(1), FOM);

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg, arbeidsperiode),
            new FordelingGrunnlag(List.of(
                inntektsmelding(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.INNVILGET)))));

        var arbeidsforhold = arbeidsforhold(resultat.getBeregningsgrunnlagPerioder().getFirst(), 0);
        assertThat(arbeidsforhold.getArbeidsperiodeFom()).isEqualTo(FOM.minusDays(1));
        assertThat(arbeidsforhold.getArbeidsperiodeTom()).contains(FOM);
    }

    @Test
    void positivt_avslått_nytt_krav_oppretter_andel_uten_hjemmel_og_med_null_innvilget_beløp() {
        var bg = bg();
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);

        var resultat = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg),
            new FordelingGrunnlag(List.of(
                inntektsmelding(AG, REF, periode(FOM, null, "1000", RefusjonUtfall.AVSLÅTT)))));

        var r = arbeidsforhold(resultat.getBeregningsgrunnlagPerioder().getFirst(), 0).getRefusjon().orElseThrow();
        assertThat(r.getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(12000));
        assertThat(r.getInnvilgetRefusjonskravPrÅr()).isEqualTo(Beløp.ZERO);
        assertThat(r.getRefusjonskravFristUtfall()).isEqualTo(Utfall.UNDERKJENT);
        assertThat(r.getHjemmelForRefusjonskravfrist()).isNull();
    }

    @Test
    void avviste_inndata_endrer_ikke_opprinnelig_beregningsgrunnlag() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        leggTilAndel(p, AG, REF, 1L, 100000);
        var før = BeregningsgrunnlagDto.builder(bg).build();

        assertThatThrownBy(() -> PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg),
            new FordelingGrunnlag(List.of(
                inntektsmelding(AG, REF, periode(FOM, null, "-1", RefusjonUtfall.INNVILGET))))))
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(bg).usingRecursiveComparison().isEqualTo(før);
    }

    private static BeregningsgrunnlagDto periodiserMedTidslinje(Refusjonsperiode refusjonsperiode) {
        var bg = bg();
        BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        return PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg),
            new FordelingGrunnlag(List.of(inntektsmelding(AG, REF, refusjonsperiode))));
    }

    private static BeregningsgrunnlagDto bg() {
        return BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(FOM).medGrunnbeløp(Beløp.fra(90000)).build();
    }

    private static BeregningsgrunnlagInput input(BeregningsgrunnlagDto bg, Intervall... arbeidsperioder) {
        var iay = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        for (var arbeidsperiode : arbeidsperioder) {
            BeregningIAYTestUtil.byggArbeidForBehandling(FOM, arbeidsperiode, REF, AG, iay);
        }
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(bg).build(BeregningsgrunnlagTilstand.FORESLÅTT);
        return new BeregningsgrunnlagInput(new KoblingReferanseMock(FOM, FOM.minusDays(1)), iay.build(), null, List.of(),
            new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false)).medBeregningsgrunnlagGrunnlag(grunnlag);
    }

    private static void leggTilAndel(BeregningsgrunnlagPeriodeDto p, Arbeidsgiver ag, InternArbeidsforholdRefDto ref,
                                    Long andelsnr, int beregnet) {
        leggTilAndel(p, ag, ref, andelsnr, beregnet,
            new Refusjon(Beløp.fra(999), Beløp.fra(777), Beløp.fra(666), Beløp.fra(555),
                Hjemmel.F_22_13_6, Utfall.UNDERKJENT));
    }

    private static void leggTilAndel(BeregningsgrunnlagPeriodeDto p, Arbeidsgiver ag, InternArbeidsforholdRefDto ref,
                                     Long andelsnr, int beregnet, Refusjon refusjon) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(andelsnr).medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID).medBeregnetPrÅr(Beløp.fra(beregnet))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ag).medArbeidsforholdRef(ref)
                .medArbeidsperiodeFom(FOM.minusYears(1)).medNaturalytelseBortfaltPrÅr(Beløp.fra(500))
                .medRefusjon(refusjon)).build(p);
    }

    private static BGAndelArbeidsforholdDto arbeidsforhold(BeregningsgrunnlagPeriodeDto p, int indeks) {
        return p.getBeregningsgrunnlagPrStatusOgAndelList().get(indeks).getBgAndelArbeidsforhold().orElseThrow();
    }

    private static Refusjonsandel refusjonsandel(Arbeidsgiver ag, InternArbeidsforholdRefDto ref, Refusjonsperiode... perioder) {
        return inntektsmelding(ag, ref, perioder);
    }

    private static Refusjonsandel refusjonsandelForSbh(Arbeidsgiver ag, InternArbeidsforholdRefDto ref, Refusjonsperiode... perioder) {
        return inntektsmeldingForSbh(ag, ref, RefusjonKilde.SAKSBEHANDLER, perioder);
    }


    private static Refusjonsandel saksbehandlerRefusjonsandel(Arbeidsgiver ag, InternArbeidsforholdRefDto ref,
                                                            Refusjonsperiode... perioder) {
        return new Refusjonsandel(ag, ref, RefusjonKilde.SAKSBEHANDLER, null, List.of(perioder));
    }

    private static Refusjonsandel inntektsmelding(Arbeidsgiver ag, InternArbeidsforholdRefDto ref,
                                                  Refusjonsperiode... perioder) {
        return new Refusjonsandel(ag, ref, RefusjonKilde.INNTEKTSMELDING,
            new JournalpostId("102"), List.of(perioder));
    }

    private static Refusjonsandel inntektsmeldingForSbh(Arbeidsgiver ag, InternArbeidsforholdRefDto ref, RefusjonKilde kilde,
                                                Refusjonsperiode... perioder) {
        return new Refusjonsandel(ag, ref, kilde,
            null, List.of(perioder));
    }

    private static Refusjon refusjon(int krav, int saksbehandlet, int fordelt, int manuelt, Hjemmel hjemmel,
                                    Utfall utfall) {
        return new Refusjon(Beløp.fra(krav), Beløp.fra(saksbehandlet), Beløp.fra(fordelt),
            Beløp.fra(manuelt), hjemmel, utfall);
    }

    private static Refusjonsperiode periode(LocalDate fom, LocalDate tom, String beløp, RefusjonUtfall utfall) {
        return new Refusjonsperiode(Intervall.fraOgMedTilOgMed(fom, tom == null ? TIDENES_ENDE : tom),
            Beløp.fra(new BigDecimal(beløp)), utfall, null);
    }
}
