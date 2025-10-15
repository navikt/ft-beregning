package no.nav.folketrygdloven.kalkulator.guitjenester.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.avklaringsbehov.AvklaringsbehovDto;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonAndelTilVurderingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.TidligereUtbetalingDto;

class VurderRefusjonDtoTjenesteTest2 {

    private static final Skjæringstidspunkt STP = Skjæringstidspunkt.builder()
        .medSkjæringstidspunktBeregning(LocalDate.of(2020, 1, 1))
        .medSkjæringstidspunktOpptjening(LocalDate.of(2020, 1, 1))
        .build();
    private static final Intervall BG_PERIODE = Intervall.fraOgMedTilOgMed(STP.getSkjæringstidspunktBeregning(), TIDENES_ENDE);
    private static final String ORGNR = "999999999";
    private static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet(ORGNR);
    private final KoblingReferanse koblingReferanse = new KoblingReferanseMock().medSkjæringstidspunkt(STP);
    private String internRef;
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPeriodeDto bgPeriode;
    private BeregningsgrunnlagDto beregningsgrunnlagOrginal;
    private BeregningsgrunnlagPeriodeDto bgPeriodeOrginal;
    private InntektArbeidYtelseGrunnlagDto iay;
    private BeregningsgrunnlagGUIInput input;
    private BeregningRefusjonOverstyringerDto.Builder refusjonOverstyringerBuilder;

    @BeforeEach
    void setup() {
        internRef = UUID.randomUUID().toString();
        beregningsgrunnlag = no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(STP.getSkjæringstidspunktBeregning())
            .medGrunnbeløp(Beløp.fra(125000))
            .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
            .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER)
            .medHjemmel(Hjemmel.F_14_7_8_30)
            .build(beregningsgrunnlag);
        bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag);
        beregningsgrunnlagOrginal = no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(STP.getSkjæringstidspunktBeregning())
            .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
            .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER)
            .medHjemmel(Hjemmel.F_14_7_8_30)
            .build(beregningsgrunnlagOrginal);
        bgPeriodeOrginal = buildBeregningsgrunnlagPeriode(beregningsgrunnlagOrginal);
        lagIAYGrunnlag();
    }

    @Test
    void skal_ikke_lage_dto_når_det_ikke_finnes_andeler_med_økt_ref() {
        byggBGAndel(ARBEIDSGIVER, internRef);
        ferdigstillInput();

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isEmpty();
    }

    @Test
    void skal_lage_dto_når_tilkommetIM_har_refusjonskrav_tidligere_utbetalt_bruker() {
        var refusjonFom = BG_PERIODE.getFomDato();
        byggBGAndel(ARBEIDSGIVER, internRef);
        byggBGAndelOriginal(ARBEIDSGIVER, internRef, 500000, 0);
        ferdigstillInput();

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);
        var tidligereUtb = new TidligereUtbetalingDto(bgPeriodeOrginal.getBeregningsgrunnlagPeriodeFom(),
            bgPeriodeOrginal.getBeregningsgrunnlagPeriodeTom(), false);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), ORGNR, internRef, refusjonFom, STP.getSkjæringstidspunktBeregning(), tidligereUtb);
    }

    @Test
    void skal_lage_dto_når_tilkommetIM_har_refusjonskrav_tidligere_utbetalt_refusjon() {
        var refusjonFom = BG_PERIODE.getFomDato();
        byggBGAndel(ARBEIDSGIVER, internRef);
        byggBGAndelOriginal(ARBEIDSGIVER, internRef, 0, 500000);
        ferdigstillInput();

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);
        var tidligereUtb = new TidligereUtbetalingDto(bgPeriodeOrginal.getBeregningsgrunnlagPeriodeFom(),
            bgPeriodeOrginal.getBeregningsgrunnlagPeriodeTom(), true);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), ORGNR, internRef, refusjonFom, STP.getSkjæringstidspunktBeregning(), tidligereUtb);
    }

    @Test
    void skal_lage_dto_når_tilkommetIM_har_refusjonskrav_tidligere_utbetalt_refusjon_for_annet_orgnr() {
        var internRef2 = UUID.randomUUID().toString();
        var orgnr2 = "999999991";
        var refusjonFom = BG_PERIODE.getFomDato();
        byggBGAndel(Arbeidsgiver.virksomhet(orgnr2), internRef2);

        byggBGAndelOriginal(ARBEIDSGIVER, internRef, 0, 500000);
        ferdigstillInput();

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        var matchetAndel = resultat.get()
            .getAndeler()
            .stream()
            .filter(a -> a.getArbeidsgiver().getArbeidsgiverOrgnr().equals(orgnr2) && Objects.equals(a.getInternArbeidsforholdRef(), internRef2))
            .findFirst()
            .orElse(null);
        assertThat(matchetAndel).isNotNull();
        assertThat(matchetAndel.getNyttRefusjonskravFom()).isEqualTo(refusjonFom);
        assertThat(matchetAndel.getTidligsteMuligeRefusjonsdato()).isEqualTo(refusjonFom);

        assertThat(matchetAndel.getTidligereUtbetalinger()).isEmpty();
        assertThat(matchetAndel.getSkalKunneFastsetteDelvisRefusjon()).isTrue();
    }

    @Test
    void skal_sette_tidligste_refusjonsdato_lik_avklaring_fra_fakta_om_beregnign() {
        var refusjonFom = BG_PERIODE.getFomDato();
        byggBGAndel(ARBEIDSGIVER, internRef);
        byggBGAndelOriginal(ARBEIDSGIVER, internRef, 0, 500000);
        var tidligsteRefusjonFom = refusjonFom.plusMonths(1);
        byggTidligereRefusjonoverstyring(ARBEIDSGIVER, tidligsteRefusjonFom, false);
        ferdigstillInput();

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);
        var tidligereUtb = new TidligereUtbetalingDto(bgPeriodeOrginal.getBeregningsgrunnlagPeriodeFom(),
            bgPeriodeOrginal.getBeregningsgrunnlagPeriodeTom(), true);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), ORGNR, internRef, refusjonFom, tidligsteRefusjonFom, tidligereUtb);
    }

    @Test
    void ved_flere_andeler_skal_kun_andel_i_første_periode_brukes() {
        var bgPeriodeEldst = buildBeregningsgrunnlagPeriode2(beregningsgrunnlag);
        var bgPeriodeOriginalEldst = buildBeregningsgrunnlagPeriode2(beregningsgrunnlagOrginal);
        byggBGAndel(ARBEIDSGIVER, internRef);
        byggBGAndel(ARBEIDSGIVER, internRef, bgPeriodeEldst);
        var eldrePeriode = Intervall.fraOgMedTilOgMed(BG_PERIODE.getFomDato().minusYears(1), BG_PERIODE.getFomDato().minusDays(1));
        byggBGAndelOriginal(ARBEIDSGIVER, internRef, 0, 500000);
        byggBGAndelOriginal(ARBEIDSGIVER, internRef, 0, 500000, bgPeriodeOriginalEldst);
        var refusjonFom = eldrePeriode.getFomDato();
        var tidligsteRefusjonFom = refusjonFom.plusMonths(1);
        byggTidligereRefusjonoverstyring(ARBEIDSGIVER, tidligsteRefusjonFom, false);
        ferdigstillInput();

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);
        var tidligereUtb = new TidligereUtbetalingDto(bgPeriodeOrginal.getBeregningsgrunnlagPeriodeFom(),
            bgPeriodeOrginal.getBeregningsgrunnlagPeriodeTom(), true);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), ORGNR, internRef, refusjonFom, tidligsteRefusjonFom, tidligereUtb);
    }

    @Test
    void skal_ignorere_tidligere_avklaring_fra_fakta_om_beregning_når_dette_gjeder_annet_arbfor() {
        var refusjonFom = BG_PERIODE.getFomDato();
        byggBGAndel(ARBEIDSGIVER, internRef);
        byggBGAndelOriginal(ARBEIDSGIVER, internRef, 0, 500000);
        var tidligsteRefusjonFom = refusjonFom.plusMonths(1);
        byggTidligereRefusjonoverstyring(Arbeidsgiver.virksomhet("99999998"), tidligsteRefusjonFom, false);
        ferdigstillInput();

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);
        var tidligereUtb = new TidligereUtbetalingDto(bgPeriodeOrginal.getBeregningsgrunnlagPeriodeFom(),
            bgPeriodeOrginal.getBeregningsgrunnlagPeriodeTom(), true);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), ORGNR, internRef, refusjonFom, STP.getSkjæringstidspunktBeregning(), tidligereUtb);
    }


    private void byggTidligereRefusjonoverstyring(Arbeidsgiver arbeidsgiver, LocalDate tidligsteRefusjonsstart, boolean erFristUtvidet) {
        var refusjonOverstyring = new BeregningRefusjonOverstyringDto(arbeidsgiver, tidligsteRefusjonsstart, erFristUtvidet);
        if (refusjonOverstyringerBuilder == null) {
            refusjonOverstyringerBuilder = BeregningRefusjonOverstyringerDto.builder();
        }
        refusjonOverstyringerBuilder.leggTilOverstyring(refusjonOverstyring);
    }

    private void assertAndeler(List<RefusjonAndelTilVurderingDto> andeler,
                               String orgnr,
                               String internRef,
                               LocalDate refusjonFom,
                               LocalDate tidligsteMuligeRefusjon,
                               TidligereUtbetalingDto... tidligereUtbetaling) {
        var matchetAndel = andeler.stream()
            .filter(a -> a.getArbeidsgiver().getArbeidsgiverOrgnr().equals(orgnr) && Objects.equals(a.getInternArbeidsforholdRef(), internRef))
            .findFirst()
            .orElse(null);
        assertThat(matchetAndel).isNotNull();
        assertThat(matchetAndel.getNyttRefusjonskravFom()).isEqualTo(refusjonFom);
        assertThat(matchetAndel.getTidligsteMuligeRefusjonsdato()).isEqualTo(tidligsteMuligeRefusjon);

        var tidligereUtb = Arrays.asList(tidligereUtbetaling);
        assertThat(matchetAndel.getTidligereUtbetalinger()).containsAll(tidligereUtb);
    }

    private void ferdigstillInput() {
        BeregningsgrunnlagPeriodeDto.oppdater(bgPeriodeOrginal).build();
        BeregningsgrunnlagPeriodeDto.oppdater(bgPeriode).build();
        var forrigeBeregningsgrunnlagGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlagOrginal)
            .build(BeregningsgrunnlagTilstand.VURDERT_REFUSJON);

        var beregningsgrunnlagGrunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlag);
        if (refusjonOverstyringerBuilder != null) {
            beregningsgrunnlagGrunnlagBuilder.medRefusjonOverstyring(refusjonOverstyringerBuilder.build());
        }
        var beregningsgrunnlagGrunnlag = beregningsgrunnlagGrunnlagBuilder.build(BeregningsgrunnlagTilstand.VURDERT_REFUSJON);
        var avklaringsbehov = new AvklaringsbehovDto(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV, null, null, null, null, null);
        input = new BeregningsgrunnlagGUIInput(koblingReferanse, iay, List.of(), null).medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag)
            .medBeregningsgrunnlagGrunnlagFraForrigeBehandling(List.of(forrigeBeregningsgrunnlagGrunnlag))
            .medAvklaringsbehov(List.of(avklaringsbehov));
    }

    private void lagIAYGrunnlag() {
        iay = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
    }

    private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
        return BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(BG_PERIODE.getFomDato(), BG_PERIODE.getTomDato())
            .build(beregningsgrunnlag);
    }

    private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode2(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
        return BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(BG_PERIODE.getFomDato().minusYears(1), BG_PERIODE.getFomDato().minusDays(1))
            .build(beregningsgrunnlag);
    }

    private void byggBGAndel(Arbeidsgiver arbeidsgiver, String ref) {
        byggBGAndel(arbeidsgiver, ref, bgPeriode);
    }

    private void byggBGAndel(Arbeidsgiver arbeidsgiver, String ref, BeregningsgrunnlagPeriodeDto bgPeriode) {
        var bga = BGAndelArbeidsforholdDto.builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsforholdRef(ref)
            .medRefusjon(new Refusjon(Beløp.fra(33333), null, null, null, null, null))
            .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori.ARBEIDSTAKER)
            .medAndelsnr(1L)
            .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER)
            .medBeregningsperiode(LocalDate.now(), LocalDate.now().plusMonths(1))
            .medFordeltPrÅr(Beløp.fra(400000)) // TODO: Endre denne verdien til riktig sum, dette er et tilfeldig tall
            .build(bgPeriode);
    }

    private void byggBGAndelOriginal(Arbeidsgiver arbeidsgiver, String internRef, int redusertBruker, int redusertAG) {
        byggBGAndelOriginal(arbeidsgiver, internRef, redusertBruker, redusertAG, bgPeriodeOrginal);

    }

    private void byggBGAndelOriginal(Arbeidsgiver arbeidsgiver,
                                     String internRef,
                                     int redusertBruker,
                                     int redusertAG,
                                     BeregningsgrunnlagPeriodeDto bgPeriodeOrginal) {
        var bga = BGAndelArbeidsforholdDto.builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsforholdRef(internRef)
            .medRefusjon(new Refusjon(Beløp.fra(11111), null, null, null, null, null))
            .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori.ARBEIDSTAKER)
            .medAndelsnr(1L)
            .medRedusertBrukersAndelPrÅr(Beløp.fra(redusertBruker))
            .medRedusertRefusjonPrÅr(Beløp.fra(redusertAG))
            .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER)
            .medBeregningsperiode(LocalDate.now(), LocalDate.now().plusMonths(1))
            .medFordeltPrÅr(Beløp.fra(500000)) // TODO: Endre denne verdien til riktig sum, dette er et tilfeldig tall
            .build(bgPeriodeOrginal);
    }
}
