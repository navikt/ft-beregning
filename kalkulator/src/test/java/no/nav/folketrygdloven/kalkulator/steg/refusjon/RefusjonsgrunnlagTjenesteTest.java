package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Arbeidsgiver;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagModell.Arbeidsforholdsinformasjon;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagModell.Kravperioder;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagModell.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Utfall;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon.MapRefusjonsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsandel;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsperiode;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.steg.BeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.JournalpostId;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektsmeldingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

class RefusjonsgrunnlagTjenesteTest {

    private static final LocalDate FOM = LocalDate.of(2026, 1, 1);
    private static final InternArbeidsforholdRefDto REF = InternArbeidsforholdRefDto.namedRef("første");
    private final RefusjonsgrunnlagTjeneste tjeneste = new RefusjonsgrunnlagTjeneste();

    @Test
    void skal_periodisere_refusjon_på_eksisterende_andel() {
        var arbeidsgiver = virksomhet("999999999");
        var input = input(inntektsmelding(arbeidsgiver, REF, "101", "1234.56")
            .leggTil(new RefusjonDto(Beløp.fra(2000), FOM.plusMonths(3)))
            .medRefusjon(Beløp.fra(new BigDecimal("1234.56")), FOM.plusMonths(4).minusDays(1)).build());
        var bg = input.getBeregningsgrunnlag();
        var første = bg.getBeregningsgrunnlagPerioder().getFirst();
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID).medBeregnetPrÅr(Beløp.fra(400000))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(REF).medArbeidsperiodeFom(FOM.minusYears(1))).build(første);
        BeregningsgrunnlagPeriodeDto.oppdater(første).medBeregningsgrunnlagPeriode(FOM, FOM.plusMonths(2).minusDays(1)).build();
        BeregningsgrunnlagPeriodeDto.kopier(første).medBeregningsgrunnlagPeriode(FOM.plusMonths(2), null)
            .leggTilPeriodeÅrsak(PeriodeÅrsak.GRADERING).build(bg);
        var aktiviteter = BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(FOM)
            .leggTilAktivitet(BeregningAktivitetDto.builder().medArbeidsgiver(arbeidsgiver).medArbeidsforholdRef(REF)
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID).medPeriode(Intervall.fraOgMed(FOM.minusYears(1))).build())
            .build();
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.of(input.getBeregningsgrunnlagGrunnlag()))
            .medRegisterAktiviteter(aktiviteter).build(BeregningsgrunnlagTilstand.FORESLÅTT);
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        input.leggTilToggle(BeregningsgrunnlagInput.REFUSJON_TOGGLE, true);
        var stegInput = new VurderRefusjonBeregningsgrunnlagInput(new StegProsesseringInput(input, BeregningsgrunnlagTilstand.FORESLÅTT));

        var resultat = new BeregningsgrunnlagTjeneste().vurderRefusjonskravForBeregninggrunnlag(stegInput);

        var perioder = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(perioder).extracting(BeregningsgrunnlagPeriodeDto::getPeriode).containsExactly(
            Intervall.fraOgMedTilOgMed(FOM, FOM.plusMonths(2).minusDays(1)),
            Intervall.fraOgMedTilOgMed(FOM.plusMonths(2), FOM.plusMonths(3).minusDays(1)),
            Intervall.fraOgMedTilOgMed(FOM.plusMonths(3), FOM.plusMonths(4).minusDays(1)),
            Intervall.fraOgMed(FOM.plusMonths(4)));
        assertThat(perioder).extracting(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().getFirst()
            .getBgAndelArbeidsforhold().orElseThrow().getRefusjonskravPrÅr()).containsExactly(
                Beløp.fra(new BigDecimal("14814.72")), Beløp.fra(new BigDecimal("14814.72")),
                Beløp.fra(24000), Beløp.ZERO);
        assertThat(perioder).allSatisfy(p -> {
            assertThat(p.getBeregningsgrunnlagPrStatusOgAndelList()).singleElement().satisfies(andel -> {
                assertThat(andel.getArbeidsgiver()).contains(arbeidsgiver);
                assertThat(andel.getArbeidsforholdRef()).contains(REF);
                assertThat(andel.getBeregnetPrÅr()).isEqualTo(Beløp.fra(400000));
            });
        });
        assertThat(resultat.getFordelingGrunnlag().orElseThrow().refusjonsandeler()).singleElement().satisfies(andel -> {
            assertThat(andel.arbeidsgiver()).isEqualTo(arbeidsgiver);
            assertThat(andel.arbeidsforholdRef()).isEqualTo(REF);
            assertThat(andel.journalpostId()).isEqualTo(new JournalpostId("101"));
            assertThat(andel.kilde()).isEqualTo(RefusjonKilde.INNTEKTSMELDING);
            assertThat(andel.refusjonsperioder()).containsExactly(
                periode(FOM, FOM.plusMonths(3).minusDays(1), "1234.56"),
                periode(FOM.plusMonths(3), FOM.plusMonths(4).minusDays(1), "2000"),
                periode(FOM.plusMonths(4), new Periode(FOM, null).getTom(), "0"));
        });
        assertThat(bg.getBeregningsgrunnlagPerioder()).hasSize(2);
        assertThat(første.getBeregningsgrunnlagPrStatusOgAndelList().getFirst()
            .getBgAndelArbeidsforhold().orElseThrow().getRefusjon()).isEmpty();
    }

    @Test
    void skal_opprette_og_periodisere_ny_andel_for_refusjon() {
        var arbeidsgiver = virksomhet("999999999");
        var input = input(inntektsmelding(arbeidsgiver, REF, "101", "1234.56")
            .leggTil(new RefusjonDto(Beløp.fra(2000), FOM.plusMonths(3)))
            .medRefusjon(Beløp.fra(new BigDecimal("1234.56")), FOM.plusMonths(4).minusDays(1)).build());
        var bg = input.getBeregningsgrunnlag();
        var første = bg.getBeregningsgrunnlagPerioder().getFirst();
        BeregningsgrunnlagPeriodeDto.oppdater(første).medBruttoPrÅr(Beløp.ZERO).build();
        BeregningsgrunnlagPeriodeDto.oppdater(første).medBeregningsgrunnlagPeriode(FOM, FOM.plusMonths(2).minusDays(1)).build();
        BeregningsgrunnlagPeriodeDto.kopier(første).medBeregningsgrunnlagPeriode(FOM.plusMonths(2), null)
            .leggTilPeriodeÅrsak(PeriodeÅrsak.GRADERING).build(bg);
        var aktiviteter = BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(FOM)
            .leggTilAktivitet(BeregningAktivitetDto.builder().medArbeidsgiver(arbeidsgiver).medArbeidsforholdRef(REF)
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID).medPeriode(Intervall.fraOgMed(FOM.minusYears(1))).build())
            .build();
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.of(input.getBeregningsgrunnlagGrunnlag()))
            .medRegisterAktiviteter(aktiviteter).build(BeregningsgrunnlagTilstand.FORESLÅTT);
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        var resultat = tjeneste.fastsettRefusjonsgrunnlag(input);

        var perioder = resultat.beregningsgrunnlag().getBeregningsgrunnlagPerioder();
        assertThat(perioder).extracting(BeregningsgrunnlagPeriodeDto::getPeriode).containsExactly(
            Intervall.fraOgMedTilOgMed(FOM, FOM.plusMonths(2).minusDays(1)),
            Intervall.fraOgMedTilOgMed(FOM.plusMonths(2), FOM.plusMonths(3).minusDays(1)),
            Intervall.fraOgMedTilOgMed(FOM.plusMonths(3), FOM.plusMonths(4).minusDays(1)),
            Intervall.fraOgMed(FOM.plusMonths(4)));
        assertThat(perioder).extracting(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().size())
            .containsExactly(1, 1, 1, 0);
        assertThat(perioder.subList(0, 3)).extracting(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().getFirst()
            .getBgAndelArbeidsforhold().orElseThrow().getRefusjonskravPrÅr()).containsExactly(
                Beløp.fra(new BigDecimal("14814.72")), Beløp.fra(new BigDecimal("14814.72")), Beløp.fra(24000));
        assertThat(perioder.subList(0, 3)).allSatisfy(p -> {
            assertThat(p.getBeregningsgrunnlagPrStatusOgAndelList()).singleElement().satisfies(a -> {
                assertThat(a.getArbeidsgiver()).contains(arbeidsgiver);
                assertThat(a.getArbeidsforholdRef()).contains(REF);
                assertThat(a.getKilde()).isEqualTo(AndelKilde.PROSESS_PERIODISERING);
            });
        });
        var andel = perioder.getFirst()
            .getBeregningsgrunnlagPrStatusOgAndelList().getFirst().getBgAndelArbeidsforhold().orElseThrow();
        assertThat(andel.getArbeidsperiodeFom()).isEqualTo(FOM.minusYears(1));
        assertThat(andel.getHjemmelForRefusjonskravfrist()).isNull();
        assertThat(resultat.fordelingGrunnlag().refusjonsandeler()).singleElement().satisfies(refusjonsandel -> {
            assertThat(refusjonsandel.arbeidsgiver()).isEqualTo(arbeidsgiver);
            assertThat(refusjonsandel.arbeidsforholdRef()).isEqualTo(REF);
            assertThat(refusjonsandel.journalpostId()).isEqualTo(new JournalpostId("101"));
            assertThat(refusjonsandel.kilde()).isEqualTo(RefusjonKilde.INNTEKTSMELDING);
            assertThat(refusjonsandel.refusjonsperioder()).containsExactly(
                periode(FOM, FOM.plusMonths(3).minusDays(1), "1234.56"),
                periode(FOM.plusMonths(3), FOM.plusMonths(4).minusDays(1), "2000"),
                periode(FOM.plusMonths(4), new Periode(FOM, null).getTom(), "0"));
        });
        assertThat(bg.getBeregningsgrunnlagPerioder()).hasSize(2);
        assertThat(første.getBeregningsgrunnlagPrStatusOgAndelList()).isEmpty();
    }

    @Test
    void skal_opprette_ny_andel_for_inntektsmelding_med_referanse_når_bg_andelen_mangler_referanse() {
        var arbeidsgiver = virksomhet("999999999");
        var utenReferanse = InternArbeidsforholdRefDto.nullRef();
        var input = input(
            inntektsmelding(arbeidsgiver, utenReferanse, "101", "1000").build(),
            inntektsmelding(arbeidsgiver, REF, "102", "2000").build());
        var førstePeriode = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().getFirst();
        BeregningsgrunnlagPrStatusOgAndelDto.ny().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID).medBeregnetPrÅr(Beløp.fra(400000))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(utenReferanse).medArbeidsperiodeFom(FOM.minusYears(1))).build(førstePeriode);
        var resultat = tjeneste.fastsettRefusjonsgrunnlag(input);

        var andeler = resultat.beregningsgrunnlag().getBeregningsgrunnlagPerioder().getFirst()
            .getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(2);
        assertThat(andeler).filteredOn(a -> a.getArbeidsforholdRef().orElseThrow().equals(utenReferanse))
            .singleElement().satisfies(a -> {
                assertThat(a.getBeregnetPrÅr()).isEqualTo(Beløp.fra(400000));
                assertThat(a.getBgAndelArbeidsforhold().orElseThrow().getRefusjonskravPrÅr())
                    .isEqualTo(Beløp.fra(12000));
            });
        assertThat(andeler).filteredOn(a -> a.getArbeidsforholdRef().orElseThrow().equals(REF))
            .singleElement().satisfies(a -> {
                assertThat(a.getKilde()).isEqualTo(AndelKilde.PROSESS_PERIODISERING);
                assertThat(a.getBgAndelArbeidsforhold().orElseThrow().getRefusjonskravPrÅr())
                    .isEqualTo(Beløp.fra(24000));
            });
        assertThat(resultat.fordelingGrunnlag().refusjonsandeler()).hasSize(2);
        assertThat(førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList()).singleElement()
            .satisfies(a -> assertThat(a.getBgAndelArbeidsforhold().orElseThrow().getRefusjon()).isEmpty());
    }

    @Test
    void skal_bevare_endring_og_opphør_med_månedsbeløp() {
        var im = inntektsmelding(virksomhet("999999999"), REF, "JP-101", "30000.25")
            .leggTil(new RefusjonDto(Beløp.fra(new BigDecimal("1234.56")), FOM.plusMonths(1)))
            .medRefusjon(Beløp.fra(new BigDecimal("30000.25")), FOM.plusMonths(2).minusDays(1))
            .build();
        var input = input(im);

        var regelinput = MapRefusjonsgrunnlagFraVLTilRegel.map(input).orElseThrow();
        assertThat(regelinput.refusjonskravListe()).singleElement().satisfies(krav -> {
            assertThat(krav.journalpostId()).isEqualTo("JP-101");
            assertThat(krav.kravperioder()).containsExactly(
                kravperiode(FOM, FOM.plusMonths(1).minusDays(1), "30000.25"),
                kravperiode(FOM.plusMonths(1), FOM.plusMonths(2).minusDays(1), "1234.56"),
                kravperiode(FOM.plusMonths(2), null, "0"));
        });
        var resultat = tjeneste.fastsettRefusjonsgrunnlag(input);

        assertThat(resultat.fordelingGrunnlag().refusjonsandeler()).singleElement().satisfies(andel -> {
            assertThat(andel.arbeidsforholdRef()).isEqualTo(REF);
            assertThat(andel.journalpostId()).isEqualTo(new JournalpostId("JP-101"));
            assertThat(andel.kilde()).isEqualTo(RefusjonKilde.INNTEKTSMELDING);
            assertThat(andel.refusjonsperioder()).containsExactly(
                periode(FOM, FOM.plusMonths(1).minusDays(1), "30000.25"),
                periode(FOM.plusMonths(1), FOM.plusMonths(2).minusDays(1), "1234.56"),
                periode(FOM.plusMonths(2), new Periode(FOM, null).getTom(), "0"));
        });
        assertThat(resultat.beregningsgrunnlag()).isNotSameAs(input.getBeregningsgrunnlag());
        assertThat(resultat.beregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(3);
        assertThat(input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
    }

    @Test
    void skal_lage_en_andel_per_krav_og_bevare_arbeidsforhold_og_journalposter() {
        var annenRef = InternArbeidsforholdRefDto.namedRef("andre");
        var input = input(
            inntektsmelding(virksomhet("999999999"), REF, "101", "1000").build(),
            inntektsmelding(virksomhet("999999999"), annenRef, "102", "2000").build(),
            inntektsmelding(no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver.person(new AktørId("1111111111111")),
                InternArbeidsforholdRefDto.nullRef(), "103", "3000").build(),
            inntektsmelding(virksomhet("888888888"), REF, "104", "4000").build());

        var andeler = tjeneste.fastsettRefusjonsgrunnlag(input).fordelingGrunnlag().refusjonsandeler().stream()
            .sorted(java.util.Comparator.comparing(a -> a.journalpostId().getId())).toList();

        assertThat(andeler).hasSize(4);
        assertThat(andeler.get(0).arbeidsgiver().getOrgnr()).isEqualTo("999999999");
        assertThat(andeler.get(0).arbeidsforholdRef()).isEqualTo(REF);
        assertThat(andeler.get(0).journalpostId()).isEqualTo(new JournalpostId("101"));
        assertThat(andeler.get(0).refusjonsperioder()).containsExactly(
            periode(FOM, new Periode(FOM, null).getTom(), "1000"));
        assertThat(andeler.get(1).arbeidsforholdRef()).isEqualTo(annenRef);
        assertThat(andeler.get(1).journalpostId()).isEqualTo(new JournalpostId("102"));
        assertThat(andeler.get(1).refusjonsperioder()).containsExactly(periode(FOM, new Periode(FOM, null).getTom(), "2000"));
        assertThat(andeler.get(2).arbeidsgiver().getAktørId().getId()).isEqualTo("1111111111111");
        assertThat(andeler.get(2).arbeidsforholdRef()).isEqualTo(InternArbeidsforholdRefDto.nullRef());
        assertThat(andeler.get(2).journalpostId()).isEqualTo(new JournalpostId("103"));
        assertThat(andeler.get(2).refusjonsperioder()).containsExactly(periode(FOM, new Periode(FOM, null).getTom(), "3000"));
        assertThat(andeler.get(3).arbeidsgiver().getOrgnr()).isEqualTo("888888888");
        assertThat(andeler.get(3).arbeidsforholdRef()).isEqualTo(REF);
        assertThat(andeler.get(3).journalpostId()).isEqualTo(new JournalpostId("104"));
        assertThat(andeler.get(3).refusjonsperioder()).containsExactly(periode(FOM, new Periode(FOM, null).getTom(), "4000"));
    }

    @Test
    void skal_gi_tomt_fordelingsgrunnlag_når_det_ikke_er_krav() {
        var resultat = tjeneste.fastsettRefusjonsgrunnlag(input());

        assertThat(resultat.fordelingGrunnlag().refusjonsandeler()).isEmpty();
    }

    @Test
    void skal_avvise_input_med_tomt_fordelingsgrunnlag() {
        var input = input().medFordelingGrunnlag(new FordelingGrunnlag(List.of()));

        assertThatThrownBy(() -> tjeneste.fastsettRefusjonsgrunnlag(input))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Fordelingsgrunnlag skal ikke være satt ved fastsetting av refusjonsgrunnlag.");
    }

    @Test
    void skal_avvise_input_med_fordelingsgrunnlag_som_inneholder_refusjon() {
        var refusjonsandel = new Refusjonsandel(virksomhet("999999999"), REF, RefusjonKilde.INNTEKTSMELDING,
            new JournalpostId("101"), List.of(periode(FOM, new Periode(FOM, null).getTom(), "1000")));
        var input = input().medFordelingGrunnlag(new FordelingGrunnlag(List.of(refusjonsandel)));

        assertThatThrownBy(() -> tjeneste.fastsettRefusjonsgrunnlag(input))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Fordelingsgrunnlag skal ikke være satt ved fastsetting av refusjonsgrunnlag.");
    }

    @Test
    void skal_avvise_duplikate_arbeidsforhold_uavhengig_av_journalpost_startdato_og_perioder() {
        var arbeidsforhold = new Arbeidsforholdsinformasjon(Arbeidsgiver.medOrgnr("999999999"), REF.getReferanse(), null);
        var sammeArbeidsforhold = new Arbeidsforholdsinformasjon(Arbeidsgiver.medOrgnr("999999999"), REF.getReferanse(), FOM);
        var duplikateKrav = List.of(
            krav(arbeidsforhold, "101", kravperiode(FOM, FOM.plusDays(9), "1000")),
            krav(sammeArbeidsforhold, "102", kravperiode(FOM.plusDays(10), null, "2000")));

        assertThatThrownBy(() -> new RefusjonsgrunnlagModell(duplikateKrav, FOM))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Flere refusjonskrav med samme arbeidsgiver og arbeidsforholdsreferanse.");
    }

    @Test
    void skal_avvise_duplikater_uten_arbeidsforholdsreferanse() {
        var arbeidsforhold = new Arbeidsforholdsinformasjon(Arbeidsgiver.medOrgnr("999999999"), null, null);
        var sammeArbeidsforhold = new Arbeidsforholdsinformasjon(Arbeidsgiver.medOrgnr("999999999"), null, FOM);
        var duplikateKrav = List.of(
            krav(arbeidsforhold, "101", kravperiode(FOM, null, "1000")),
            krav(sammeArbeidsforhold, "102", kravperiode(FOM, null, "2000")));

        assertThatThrownBy(() -> new RefusjonsgrunnlagModell(duplikateKrav, FOM))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("samme arbeidsgiver og arbeidsforholdsreferanse");
    }

    @Test
    void skal_skille_arbeidsgivertyper_med_samme_identifikator() {
        var virksomhet = new Arbeidsforholdsinformasjon(Arbeidsgiver.medOrgnr("999999999"), REF.getReferanse(), null);
        var person = new Arbeidsforholdsinformasjon(Arbeidsgiver.medAktørId("999999999"), REF.getReferanse(), null);
        var input = new RefusjonsgrunnlagModell(List.of(
            krav(virksomhet, "101", kravperiode(FOM, null, "1000")),
            krav(person, "102", kravperiode(FOM, null, "2000"))), FOM);

        assertThat(KalkulusRegler.fastsettRefusjonsgrunnlag(input).refusjonsandeler()).hasSize(2);
    }

    @Test
    void skal_bevare_journalpost_og_beløp_i_regelresultatets_perioder() {
        var arbeidsforhold = new Arbeidsforholdsinformasjon(Arbeidsgiver.medOrgnr("999999999"), REF.getReferanse(), null);
        var input = new RefusjonsgrunnlagModell(List.of(krav(arbeidsforhold, "101",
            kravperiode(FOM.plusDays(10), null, "0"),
            kravperiode(FOM, FOM.plusDays(9), "1234.56"))), FOM);

        var resultat = KalkulusRegler.fastsettRefusjonsgrunnlag(input);

        assertThat(resultat.refusjonsandeler()).singleElement().satisfies(andel -> {
            assertThat(andel.arbeidsgiver()).isSameAs(arbeidsforhold.arbeidsgiver());
            assertThat(andel.internArbeidsforholdId()).isEqualTo(REF.getReferanse());
            assertThat(andel.journalpostId()).isEqualTo("101");
            assertThat(andel.refusjonKilde())
                .isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonKilde.INNTEKTSMELDING);
            assertThat(andel.refusjonskrav()).extracting(p -> p.periode().getFom()).containsExactly(FOM, FOM.plusDays(10));
            assertThat(andel.refusjonskrav()).extracting(p -> p.refusjonPrMnd())
                .containsExactly(new BigDecimal("1234.56"), BigDecimal.ZERO);
            assertThat(andel.refusjonskrav()).allSatisfy(p -> {
                assertThat(p.journalpostId()).isEqualTo("101");
                assertThat(p.utfall()).isEqualTo(Utfall.GODKJENT);
            });
        });
    }

    @Test
    void skal_kreve_journalpost_for_inntektsmelding() {
        var input = input(inntektsmelding(virksomhet("999999999"), REF, null, "1000").build());

        assertThatThrownBy(() -> tjeneste.fastsettRefusjonsgrunnlag(input))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Journalpost er påkrevd");
    }

    @Test
    void skal_returnere_uendret_bg_for_ytelser_uten_refusjonsperiodisering() {
        var input = input();
        var ikkeStøttet = new BeregningsgrunnlagInput(new KoblingReferanseMock(FOM, FagsakYtelseType.FRISINN),
            input.getIayGrunnlag(), null, List.of(), null).medBeregningsgrunnlagGrunnlag(input.getBeregningsgrunnlagGrunnlag());

        var resultat = tjeneste.fastsettRefusjonsgrunnlag(ikkeStøttet);

        assertThat(resultat.beregningsgrunnlag()).isSameAs(input.getBeregningsgrunnlag());
        assertThat(resultat.fordelingGrunnlag().refusjonsandeler()).isEmpty();
    }

    @Test
    void skal_bruke_gyldige_utbetalingsperioder_for_svangerskapspenger() {
        sjekkGyldigeUtbetalingsperioder(FagsakYtelseType.SVANGERSKAPSPENGER);
    }

    @Test
    void skal_bruke_gyldige_utbetalingsperioder_for_omsorgspenger() {
        sjekkGyldigeUtbetalingsperioder(FagsakYtelseType.OMSORGSPENGER);
    }

    @Test
    void skal_bruke_gyldige_utbetalingsperioder_for_pleiepenger_sykt_barn() {
        sjekkGyldigeUtbetalingsperioder(FagsakYtelseType.PLEIEPENGER_SYKT_BARN);
    }

    @Test
    void skal_bruke_gyldige_utbetalingsperioder_for_opplæringspenger() {
        sjekkGyldigeUtbetalingsperioder(FagsakYtelseType.OPPLÆRINGSPENGER);
    }

    @Test
    void skal_bruke_gyldige_utbetalingsperioder_for_pleiepenger_nærstående() {
        sjekkGyldigeUtbetalingsperioder(FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE);
    }

    private void sjekkGyldigeUtbetalingsperioder(FagsakYtelseType ytelseType) {
        var arbeidsgiver = virksomhet("999999999");
        var originalInput = input(inntektsmelding(arbeidsgiver, REF, "JP-101", "1234.56").build());
        var aktivitet = new AktivitetDto(arbeidsgiver, REF, UttakArbeidType.ORDINÆRT_ARBEID);
        var utbetalingsperioder = List.of(
            new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(FOM.plusDays(2), FOM.plusDays(10)), Utbetalingsgrad.fra(BigDecimal.valueOf(50))),
            new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(FOM.plusDays(21), FOM.plusDays(30)), Utbetalingsgrad.fra(BigDecimal.valueOf(100))));
        var ytelsegrunnlag = new SvangerskapspengerGrunnlag(List.of(new UtbetalingsgradPrAktivitetDto(aktivitet, utbetalingsperioder)));
        var input = new BeregningsgrunnlagInput(new KoblingReferanseMock(FOM, ytelseType), originalInput.getIayGrunnlag(),
            null, List.of(), ytelsegrunnlag).medBeregningsgrunnlagGrunnlag(originalInput.getBeregningsgrunnlagGrunnlag());

        var resultat = tjeneste.fastsettRefusjonsgrunnlag(input);

        assertThat(resultat.beregningsgrunnlag()).isNotSameAs(input.getBeregningsgrunnlag());
        assertThat(resultat.fordelingGrunnlag().refusjonsandeler()).singleElement().satisfies(andel -> {
            assertThat(andel.journalpostId()).isEqualTo(new JournalpostId("JP-101"));
            assertThat(andel.refusjonsperioder()).containsExactly(
                periode(FOM.plusDays(2), FOM.plusDays(10), "1234.56"),
                periode(FOM.plusDays(11), FOM.plusDays(20), "0"),
                periode(FOM.plusDays(21), FOM.plusDays(30), "1234.56"),
                periode(FOM.plusDays(31), new Periode(FOM, null).getTom(), "0"));
        });
    }

    @Test
    void skal_nullstille_beløp_under_utbetalingsterskelen() {
        sjekkUtbetalingsterskel("10.82", "0");
    }

    @Test
    void skal_nullstille_beløp_på_utbetalingsterskelen() {
        sjekkUtbetalingsterskel("10.83", "0");
    }

    @Test
    void skal_bevare_beløp_over_utbetalingsterskelen() {
        sjekkUtbetalingsterskel("10.84", "10.84");
    }

    private void sjekkUtbetalingsterskel(String beløp, String forventetBeløp) {
        var input = input(inntektsmelding(virksomhet("999999999"), REF, "101", beløp)
            .leggTil(new RefusjonDto(Beløp.fra(new BigDecimal("20")), FOM.plusDays(10))).build());

        assertThat(tjeneste.fastsettRefusjonsgrunnlag(input).fordelingGrunnlag().refusjonsandeler())
            .singleElement().satisfies(andel -> assertThat(andel.refusjonsperioder()).containsExactly(
                periode(FOM, FOM.plusDays(9), forventetBeløp),
                periode(FOM.plusDays(10), new Periode(FOM, null).getTom(), "20")));
    }

    @Test
    void skal_ikke_bruke_overstyrt_startdato_for_matchende_arbeidsforhold() {
        var arbeidsgiver = virksomhet("999999999");
        var originalInput = input(inntektsmelding(arbeidsgiver, REF, "101", "1000").build());
        var overstyringer = BeregningRefusjonOverstyringerDto.builder()
            .leggTilOverstyring(new BeregningRefusjonOverstyringDto(arbeidsgiver, null,
                List.of(new BeregningRefusjonPeriodeDto(REF, FOM.plusDays(10))), null)).build();
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.of(originalInput.getBeregningsgrunnlagGrunnlag()))
            .medRefusjonOverstyring(overstyringer).build(BeregningsgrunnlagTilstand.FORESLÅTT);
        var input = originalInput.medBeregningsgrunnlagGrunnlag(grunnlag);

        assertThat(tjeneste.fastsettRefusjonsgrunnlag(input).fordelingGrunnlag().refusjonsandeler())
            .singleElement().satisfies(andel -> assertThat(andel.refusjonsperioder()).containsExactly(
                periode(FOM, new Periode(FOM, null).getTom(), "1000")));
    }

    @Test
    void skal_ikke_bruke_senere_permisjonsstart_for_refusjonskrav() {
        var arbeidsgiver = virksomhet("999999999");
        var startdatoPermisjon = FOM.plusDays(10);
        var inntektsmelding = inntektsmelding(arbeidsgiver, REF, "101", "1000")
            .medStartDatoPermisjon(startdatoPermisjon)
            .medInntektsmeldingType(InntektsmeldingType.REFUSJONSKRAV)
            .build();

        assertThat(tjeneste.fastsettRefusjonsgrunnlag(input(inntektsmelding)).fordelingGrunnlag().refusjonsandeler())
            .singleElement().satisfies(andel -> assertThat(andel.refusjonsperioder()).containsExactly(
                periode(FOM, new Periode(FOM, null).getTom(), "1000")));
    }

    @Test
    void skal_flytte_endringer_til_neste_gyldige_utbetalingsperiode_og_bevare_opphør() {
        var arbeidsgiver = virksomhet("999999999");
        var originalInput = input(inntektsmelding(arbeidsgiver, REF, "101", "1000")
            .leggTil(new RefusjonDto(Beløp.fra(2000), FOM.minusDays(1)))
            .leggTil(new RefusjonDto(Beløp.fra(3000), FOM.plusDays(15)))
            .medRefusjon(Beløp.fra(1000), FOM.plusDays(25)).build());
        var aktivitet = new AktivitetDto(arbeidsgiver, REF, UttakArbeidType.ORDINÆRT_ARBEID);
        var utbetalingsperioder = List.of(
            new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(FOM, FOM.plusDays(10)), Utbetalingsgrad.fra(BigDecimal.valueOf(100))),
            new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(FOM.plusDays(21), FOM.plusDays(30)), Utbetalingsgrad.fra(BigDecimal.valueOf(100))));
        var ytelsegrunnlag = new SvangerskapspengerGrunnlag(List.of(new UtbetalingsgradPrAktivitetDto(aktivitet, utbetalingsperioder)));
        var input = new BeregningsgrunnlagInput(new KoblingReferanseMock(FOM, FagsakYtelseType.SVANGERSKAPSPENGER),
            originalInput.getIayGrunnlag(), null, List.of(), ytelsegrunnlag)
            .medBeregningsgrunnlagGrunnlag(originalInput.getBeregningsgrunnlagGrunnlag());

        assertThat(tjeneste.fastsettRefusjonsgrunnlag(input).fordelingGrunnlag().refusjonsandeler())
            .singleElement().satisfies(andel -> assertThat(andel.refusjonsperioder()).containsExactly(
                periode(FOM, FOM.plusDays(10), "2000"),
                periode(FOM.plusDays(11), FOM.plusDays(20), "0"),
                periode(FOM.plusDays(21), FOM.plusDays(25), "3000"),
                periode(FOM.plusDays(26), new Periode(FOM, null).getTom(), "0")));
    }

    private static BeregningsgrunnlagInput input(InntektsmeldingDto... inntektsmeldinger) {
        var iay = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        for (var im : inntektsmeldinger) {
            BeregningIAYTestUtil.byggArbeidForBehandling(FOM, Intervall.fraOgMed(FOM.minusYears(1)),
                im.getArbeidsforholdRef(), im.getArbeidsgiver(), iay);
        }
        iay.medInntektsmeldinger(inntektsmeldinger);
        var bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(FOM).medGrunnbeløp(Beløp.fra(90000))
            .leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(FOM, null))
            .build();
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(bg).build(BeregningsgrunnlagTilstand.FORESLÅTT);
        return new BeregningsgrunnlagInput(new KoblingReferanseMock(FOM, FOM.minusDays(1)), iay.build(), null, List.of(),
            new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false)).medBeregningsgrunnlagGrunnlag(grunnlag);
    }

    private static no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver virksomhet(String orgnr) {
        return no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver.virksomhet(orgnr);
    }

    private static InntektsmeldingDtoBuilder inntektsmelding(no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver arbeidsgiver,
                                                            InternArbeidsforholdRefDto ref, String journalpostId, String beløp) {
        return InntektsmeldingDtoBuilder.builder().medArbeidsgiver(arbeidsgiver).medArbeidsforholdId(ref)
            .medJournalpostId(journalpostId == null ? null : new JournalpostId(journalpostId))
            .medStartDatoPermisjon(FOM).medBeløp(Beløp.fra(30000)).medRefusjon(Beløp.fra(new BigDecimal(beløp)));
    }

    private static Refusjonskrav krav(Arbeidsforholdsinformasjon arbeidsforhold, String journalpostId, Kravperioder... perioder) {
        return new Refusjonskrav(arbeidsforhold, journalpostId, List.of(perioder));
    }

    private static Kravperioder kravperiode(LocalDate fom, LocalDate tom, String månedsbeløp) {
        return new Kravperioder(new Periode(fom, tom), new BigDecimal(månedsbeløp));
    }

    private static Refusjonsperiode periode(LocalDate fom, LocalDate tom, String månedsbeløp) {
        return new Refusjonsperiode(Intervall.fraOgMedTilOgMed(fom, tom),
            Beløp.fra(new BigDecimal(månedsbeløp)), RefusjonUtfall.INNVILGET, null);
    }
}
