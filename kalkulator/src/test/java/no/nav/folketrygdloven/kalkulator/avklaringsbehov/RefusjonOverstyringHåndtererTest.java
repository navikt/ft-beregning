package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.OverstyrRefusjonDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonOverstyringAndel;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonOverstyringAndel.RefusjonOverstyringPeriode;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
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
import no.nav.folketrygdloven.kalkulator.steg.refusjon.PeriodiserBeregningsgrunnlagForRefusjon;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.JournalpostId;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

class RefusjonOverstyringHåndtererTest {

    private static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet("999999999");
    private static final Arbeidsgiver ANNEN_ARBEIDSGIVER = Arbeidsgiver.virksomhet("888888888");
    private static final InternArbeidsforholdRefDto REF = InternArbeidsforholdRefDto.nullRef();
    private static final LocalDate FOM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TOM = LocalDate.of(2026, 1, 31);

    private final Refusjonsandel inntektsmelding = new Refusjonsandel(ARBEIDSGIVER, REF, RefusjonKilde.INNTEKTSMELDING,
        new JournalpostId("1"), List.of(periode(10000)));
    private final Refusjonsandel gammelSaksbehandlerandel = new Refusjonsandel(ARBEIDSGIVER, REF, RefusjonKilde.SAKSBEHANDLER, null,
        List.of(periode(20000)));

    @Test
    void erstatter_saksbehandlers_andel_og_beholder_inntektsmeldingen() {
        var forrige = new FordelingGrunnlag(List.of(inntektsmelding, gammelSaksbehandlerandel));

        var nytt = RefusjonOverstyringHåndterer.lagNyttFordelingGrunnlag(forrige,
            overstyring(overstyrtAndel(ARBEIDSGIVER, RefusjonKilde.SAKSBEHANDLER, 15000)));

        assertThat(nytt.refusjonsandeler()).hasSize(2).contains(inntektsmelding).doesNotContain(gammelSaksbehandlerandel);
        assertThat(saksbehandlerandel(nytt).refusjonsperioder()).containsExactly(periode(15000));
        assertThat(saksbehandlerandel(nytt).journalpostId()).isNull();
        assertThat(forrige.refusjonsandeler()).containsExactly(inntektsmelding, gammelSaksbehandlerandel);
    }

    @Test
    void legger_til_saksbehandlers_andel_når_den_ikke_finnes_fra_før() {
        var forrige = new FordelingGrunnlag(List.of(inntektsmelding));

        var nytt = RefusjonOverstyringHåndterer.lagNyttFordelingGrunnlag(forrige,
            overstyring(overstyrtAndel(ANNEN_ARBEIDSGIVER, RefusjonKilde.SAKSBEHANDLER, 5000)));

        assertThat(nytt.refusjonsandeler()).hasSize(2).contains(inntektsmelding);
        assertThat(saksbehandlerandel(nytt).arbeidsgiver()).isEqualTo(ANNEN_ARBEIDSGIVER);
    }

    @Test
    void ignorerer_overstyrte_andeler_med_kilde_inntektsmelding() {
        var forrige = new FordelingGrunnlag(List.of(inntektsmelding));

        var nytt = RefusjonOverstyringHåndterer.lagNyttFordelingGrunnlag(forrige,
            overstyring(overstyrtAndel(ARBEIDSGIVER, RefusjonKilde.INNTEKTSMELDING, 99999)));

        assertThat(nytt.refusjonsandeler()).containsExactly(inntektsmelding);
    }

    @Test
    void siste_overstyrte_andel_vinner_ved_duplikater() {
        var forrige = new FordelingGrunnlag(List.of());

        var nytt = RefusjonOverstyringHåndterer.lagNyttFordelingGrunnlag(forrige, overstyring(
            overstyrtAndel(ARBEIDSGIVER, RefusjonKilde.SAKSBEHANDLER, 15000),
            overstyrtAndel(ARBEIDSGIVER, RefusjonKilde.SAKSBEHANDLER, 12000)));

        assertThat(nytt.refusjonsandeler()).hasSize(1);
        assertThat(saksbehandlerandel(nytt).refusjonsperioder()).containsExactly(periode(12000));
    }

    @Test
    void skal_oppdatere_eksisterende_saksbehandlerrefusjon_og_overstyre_ny_andel_mens_inntektsmelding_bevares() {
        var bg = bg();
        var p = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        var ref1 = InternArbeidsforholdRefDto.namedRef("første");
        var ref2 = InternArbeidsforholdRefDto.namedRef("andre");
        var ref3 = InternArbeidsforholdRefDto.namedRef("tredje");
        leggTilAndel(p, ARBEIDSGIVER, ref1, 1L, 100000);
        leggTilAndel(p, ARBEIDSGIVER, ref2, 2L, 200000);
        leggTilAndel(p, ARBEIDSGIVER, ref3, 3L, 300000);
        var gammelOverstyring = saksbehandlerAndel(ARBEIDSGIVER, ref1, periode(FOM, null, "1000"));
        var inntektsmeldingAndel = new Refusjonsandel(ARBEIDSGIVER, ref3, RefusjonKilde.INNTEKTSMELDING, new JournalpostId("101"),
            List.of(periode(FOM, null, "3000")));
        var forrigeFordelingsgrunnlag = new FordelingGrunnlag(List.of(gammelOverstyring, inntektsmeldingAndel));
        var eksisterendeBg = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input(bg), forrigeFordelingsgrunnlag);
        assertThat(arbeidsforhold(eksisterendeBg.getBeregningsgrunnlagPerioder().getFirst(), 0).getSaksbehandletRefusjonPrÅr())
            .isEqualTo(Beløp.fra(12000));
        var håndterInput = new HåndterBeregningsgrunnlagInput(
            input(eksisterendeBg).medFordelingGrunnlag(forrigeFordelingsgrunnlag), BeregningsgrunnlagTilstand.FASTSATT_INN);
        var endringsdato = FOM.plusMonths(1);
        var dto = new OverstyrRefusjonDto(List.of(
            new RefusjonOverstyringAndel(ARBEIDSGIVER, ref1, RefusjonKilde.SAKSBEHANDLER, List.of(
                new RefusjonOverstyringPeriode(FOM, endringsdato.minusDays(1), BigDecimal.valueOf(4000), RefusjonUtfall.INNVILGET, null),
                new RefusjonOverstyringPeriode(endringsdato, TIDENES_ENDE, BigDecimal.valueOf(5000), RefusjonUtfall.INNVILGET, null))),
            new RefusjonOverstyringAndel(ARBEIDSGIVER, ref2, RefusjonKilde.SAKSBEHANDLER, List.of(
                new RefusjonOverstyringPeriode(FOM, TIDENES_ENDE, BigDecimal.valueOf(6000), RefusjonUtfall.INNVILGET, null)))));

        var resultat = RefusjonOverstyringHåndterer.håndter(håndterInput, dto);

        assertThat(resultat.fordelingGrunnlag().refusjonsandeler()).containsExactlyInAnyOrder(
            saksbehandlerAndel(ARBEIDSGIVER, ref1,
                periode(FOM, endringsdato.minusDays(1), "4000"),
                periode(endringsdato, null, "5000")),
            saksbehandlerAndel(ARBEIDSGIVER, ref2, periode(FOM, null, "6000")),
            inntektsmeldingAndel);
        var perioder = resultat.beregningsgrunnlagGrunnlag().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(perioder).extracting(BeregningsgrunnlagPeriodeDto::getPeriode).containsExactly(
            Intervall.fraOgMedTilOgMed(FOM, endringsdato.minusDays(1)), Intervall.fraOgMed(endringsdato));
        assertThat(perioder).extracting(periode -> arbeidsforhold(periode, 0).getSaksbehandletRefusjonPrÅr())
            .containsExactly(Beløp.fra(48000), Beløp.fra(60000));
        assertThat(perioder).allSatisfy(periode -> {
            assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(3);
            assertThat(arbeidsforhold(periode, 1).getSaksbehandletRefusjonPrÅr()).isEqualTo(Beløp.fra(72000));
            assertThat(arbeidsforhold(periode, 2).getRefusjonskravPrÅr()).isEqualTo(Beløp.fra(36000));
        });
        assertThat(forrigeFordelingsgrunnlag.refusjonsandeler()).containsExactly(gammelOverstyring, inntektsmeldingAndel);
        assertThat(arbeidsforhold(eksisterendeBg.getBeregningsgrunnlagPerioder().getFirst(), 0).getSaksbehandletRefusjonPrÅr())
            .isEqualTo(Beløp.fra(12000));
    }

    @Test
    void skal_bevare_inntektsmeldingsbeløp_og_oppdatere_saksbehandlet_refusjon_med_endring_og_opphør() {
        var bg = bg();
        var opprinneligPeriode = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null).build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(1L).medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID).medBeregnetPrÅr(Beløp.fra(100000))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER).medArbeidsforholdRef(REF)
                .medArbeidsperiodeFom(FOM.minusYears(1))
                .medRefusjon(Refusjon.medRefusjonskravPrÅr(Beløp.fra(12000), null, Utfall.GODKJENT)))
            .build(opprinneligPeriode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(2L).medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID).medBeregnetPrÅr(Beløp.fra(200000))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ANNEN_ARBEIDSGIVER).medArbeidsforholdRef(REF)
                .medArbeidsperiodeFom(FOM.minusYears(1))
                .medRefusjon(new Refusjon(Beløp.fra(24000), Beløp.fra(36000), null, null, null, Utfall.GODKJENT)))
            .build(opprinneligPeriode);
        var inntektsmeldingAndel = new Refusjonsandel(ARBEIDSGIVER, REF, RefusjonKilde.INNTEKTSMELDING, new JournalpostId("101"),
            List.of(periode(FOM, null, "1000")));
        var overstyrtInntektsmelding = new Refusjonsandel(ANNEN_ARBEIDSGIVER, REF, RefusjonKilde.INNTEKTSMELDING, new JournalpostId("102"),
            List.of(periode(FOM, null, "2000")));
        var gammelOverstyring = saksbehandlerAndel(ANNEN_ARBEIDSGIVER, REF,
            new Refusjonsperiode(Intervall.fraOgMed(FOM), Beløp.fra(3000), RefusjonUtfall.INNVILGET,
                RefusjonUtfallÅrsak.AVTALT_MED_ARBEIDSGIVER));
        var forrigeFordelingsgrunnlag = new FordelingGrunnlag(List.of(inntektsmeldingAndel, overstyrtInntektsmelding, gammelOverstyring));
        var håndterInput = new HåndterBeregningsgrunnlagInput(
            input(bg).medFordelingGrunnlag(forrigeFordelingsgrunnlag), BeregningsgrunnlagTilstand.FASTSATT_INN);
        var endringsdato = FOM.plusMonths(1);
        var opphørsdato = FOM.plusMonths(2);
        var dto = new OverstyrRefusjonDto(List.of(
            new RefusjonOverstyringAndel(ARBEIDSGIVER, REF, RefusjonKilde.INNTEKTSMELDING, List.of(
                new RefusjonOverstyringPeriode(FOM, TIDENES_ENDE, BigDecimal.valueOf(1000), RefusjonUtfall.INNVILGET, null))),
            new RefusjonOverstyringAndel(ANNEN_ARBEIDSGIVER, REF, RefusjonKilde.INNTEKTSMELDING, List.of(
                new RefusjonOverstyringPeriode(FOM, TIDENES_ENDE, BigDecimal.valueOf(2000), RefusjonUtfall.INNVILGET, null))),
            new RefusjonOverstyringAndel(ANNEN_ARBEIDSGIVER, REF, RefusjonKilde.SAKSBEHANDLER, List.of(
                new RefusjonOverstyringPeriode(FOM, TIDENES_ENDE, BigDecimal.valueOf(3000), RefusjonUtfall.INNVILGET,
                    RefusjonUtfallÅrsak.AVTALT_MED_ARBEIDSGIVER))),
            new RefusjonOverstyringAndel(ANNEN_ARBEIDSGIVER, REF, RefusjonKilde.SAKSBEHANDLER, List.of(
                new RefusjonOverstyringPeriode(FOM, TIDENES_ENDE, BigDecimal.valueOf(5000), RefusjonUtfall.INNVILGET,
                    RefusjonUtfallÅrsak.AVTALT_MED_ARBEIDSGIVER))),
            new RefusjonOverstyringAndel(ARBEIDSGIVER, REF, RefusjonKilde.SAKSBEHANDLER, List.of(
                new RefusjonOverstyringPeriode(FOM, endringsdato.minusDays(1), BigDecimal.valueOf(1000), RefusjonUtfall.INNVILGET,
                    RefusjonUtfallÅrsak.AVTALT_MED_ARBEIDSGIVER),
                new RefusjonOverstyringPeriode(endringsdato, opphørsdato.minusDays(1), BigDecimal.valueOf(4000), RefusjonUtfall.INNVILGET,
                    RefusjonUtfallÅrsak.AVTALT_MED_ARBEIDSGIVER),
                new RefusjonOverstyringPeriode(opphørsdato, TIDENES_ENDE, BigDecimal.ZERO, RefusjonUtfall.INNVILGET,
                    RefusjonUtfallÅrsak.AVTALT_MED_ARBEIDSGIVER)))));

        var resultat = RefusjonOverstyringHåndterer.håndter(håndterInput, dto);

        var perioder = resultat.beregningsgrunnlagGrunnlag().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(perioder).extracting(BeregningsgrunnlagPeriodeDto::getPeriode).containsExactly(
            Intervall.fraOgMedTilOgMed(FOM, endringsdato.minusDays(1)),
            Intervall.fraOgMedTilOgMed(endringsdato, opphørsdato.minusDays(1)), Intervall.fraOgMed(opphørsdato));
        assertThat(perioder).allSatisfy(p -> assertThat(p.getBeregningsgrunnlagPrStatusOgAndelList())
            .extracting(BeregningsgrunnlagPrStatusOgAndelDto::getAndelsnr).containsExactly(1L, 2L));
        assertThat(resultat.fordelingGrunnlag().refusjonsandeler()).hasSize(4)
            .contains(inntektsmeldingAndel, overstyrtInntektsmelding).doesNotContain(gammelOverstyring);
        assertThat(resultat.fordelingGrunnlag().refusjonsandeler())
            .filteredOn(a -> a.kilde() == RefusjonKilde.SAKSBEHANDLER).hasSize(2)
            .allSatisfy(a -> assertThat(a.refusjonsperioder()).isNotEmpty().allSatisfy(p ->
                assertThat(p.utfallÅrsak()).isEqualTo(RefusjonUtfallÅrsak.AVTALT_MED_ARBEIDSGIVER)));
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

    private static BeregningsgrunnlagDto bg() {
        return BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(FOM).medGrunnbeløp(Beløp.fra(90000)).build();
    }

    private static BeregningsgrunnlagInput input(BeregningsgrunnlagDto bg) {
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(bg).build(BeregningsgrunnlagTilstand.FORESLÅTT);
        return new BeregningsgrunnlagInput(new KoblingReferanseMock(FOM, FOM.minusDays(1)),
            InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), null, List.of(),
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

    private static Refusjonsandel saksbehandlerAndel(Arbeidsgiver ag, InternArbeidsforholdRefDto ref, Refusjonsperiode... perioder) {
        return new Refusjonsandel(ag, ref, RefusjonKilde.SAKSBEHANDLER, null, List.of(perioder));
    }

    private static Refusjonsperiode periode(LocalDate fom, LocalDate tom, String beløp) {
        return new Refusjonsperiode(Intervall.fraOgMedTilOgMed(fom, tom == null ? TIDENES_ENDE : tom),
            Beløp.fra(new BigDecimal(beløp)), RefusjonUtfall.INNVILGET, null);
    }

    private static Refusjonsandel saksbehandlerandel(FordelingGrunnlag grunnlag) {
        return grunnlag.refusjonsandeler().stream().filter(a -> a.kilde() == RefusjonKilde.SAKSBEHANDLER).findFirst().orElseThrow();
    }

    private static OverstyrRefusjonDto overstyring(RefusjonOverstyringAndel... andeler) {
        return new OverstyrRefusjonDto(List.of(andeler));
    }

    private static RefusjonOverstyringAndel overstyrtAndel(Arbeidsgiver arbeidsgiver, RefusjonKilde kilde, int refusjonPrMnd) {
        return new RefusjonOverstyringAndel(arbeidsgiver, REF, kilde,
            List.of(new RefusjonOverstyringPeriode(FOM, TOM, BigDecimal.valueOf(refusjonPrMnd), RefusjonUtfall.INNVILGET, null)));
    }

    private static Refusjonsperiode periode(int refusjonPrMnd) {
        return new Refusjonsperiode(Intervall.fraOgMedTilOgMed(FOM, TOM), Beløp.fra(BigDecimal.valueOf(refusjonPrMnd)),
            RefusjonUtfall.INNVILGET, null);
    }
}
