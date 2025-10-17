package no.nav.folketrygdloven.kalkulator.guitjenester.refusjon;

import static no.nav.folketrygdloven.kalkulator.OpprettKravPerioderFraInntektsmeldingerForTest.opprett;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.avklaringsbehov.AvklaringsbehovDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.RefusjonskravForSentDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonAndelTilVurderingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.TidligereUtbetalingDto;

class VurderRefusjonDtoTjenesteTest3 {
    private static final String ORGNR1 = "974760673";
    private static final String ORGNR2 = "915933149";
    private static final Arbeidsgiver ARBEIDSGIVER1 = Arbeidsgiver.virksomhet(ORGNR1);
    private static final Arbeidsgiver ARBEIDSGIVER2 = Arbeidsgiver.virksomhet(ORGNR2);

    private static final Beløp GRUNNBELØP = Beløp.fra(125000);
    private static final Beløp REFUSJONSKRAV = Beløp.fra(400000);
    private static final Beløp BRUTTO = Beløp.fra(500000);
    private static final Beløp REFUSJON = Beløp.fra(33333);

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final LocalDate SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID = LocalDate.now().minusMonths(2);
    private static final Intervall BG_PERIODE = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID, TIDENES_ENDE);
    private static final Intervall BG_PERIODE_FORRIGE = Intervall.fraOgMedTilOgMed(BG_PERIODE.getFomDato().minusYears(1),
        BG_PERIODE.getFomDato().minusDays(1));

    private static final AvklaringsbehovDto avklaringsbehovDto = new AvklaringsbehovDto(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV, null, null,
        null, null, null);
    private static final InntektArbeidYtelseAggregatBuilder iayBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(),
        VersjonTypeDto.REGISTER);

    @Test
    void skal_gi_liste_over_arbeidsgivere_som_har_søkt_refusjon_for_sent() {
        Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap = Map.of(ARBEIDSGIVER1, SKJÆRINGSTIDSPUNKT.plusMonths(4), ARBEIDSGIVER2,
            SKJÆRINGSTIDSPUNKT.plusMonths(2));
        var input = lagInputMedBeregningsgrunnlagOgIAY(førsteInnsendingAvRefusjonMap);
        input.leggTilToggle("refusjonsfrist.flytting", true);

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getRefusjonskravForSentListe()).hasSize(1)
            .extracting(RefusjonskravForSentDto::getArbeidsgiverIdent)
            .contains(ARBEIDSGIVER1.getIdentifikator())
            .doesNotContain(ARBEIDSGIVER2.getIdentifikator());
    }

    @Test
    void skal_gi_liste_over_andeler_som_har_tilkommet_refusjon() {
        Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap = Map.of(ARBEIDSGIVER1, SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID);
        var input = lagInputMedBeregningsgrunnlagOgIAYOgForrigeGrunnlag(førsteInnsendingAvRefusjonMap);

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getRefusjonskravForSentListe()).isEmpty();

        var andeler = resultat.get().getAndeler();
        assertThat(andeler).hasSize(1);

        var andel = andeler.getFirst();
        assertThat(andel.getArbeidsgiver().getArbeidsgiverOrgnr()).isEqualTo(ARBEIDSGIVER1.getIdentifikator());
        assertThat(andel.getTidligereUtbetalinger()).hasSize(1);
        assertThat(andel.getTidligereUtbetalinger().getFirst().getFom()).isEqualTo(SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID);
        assertThat(andel.getTidligereUtbetalinger().getFirst().getTom()).isEqualTo(TIDENES_ENDE);
        assertThat(andel.getMaksTillattDelvisRefusjonPrMnd().verdi()).isEqualTo(REFUSJON.verdi());
        assertThat(andel.getNyttRefusjonskravFom()).isEqualTo(SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID);
    }

    @Test
    void skal_gi_liste_over_andeler_som_har_tilkommet_refusjon_og_tidligere_overstyring() {
        Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap = Map.of(ARBEIDSGIVER1, SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID);
        var input = lagInputMedBeregningsgrunnlagOgIAYOgForrigeGrunnlag(førsteInnsendingAvRefusjonMap);

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getRefusjonskravForSentListe()).isEmpty();

        var andeler = resultat.get().getAndeler();
        assertThat(andeler).hasSize(1);

        var andel = andeler.getFirst();
        assertThat(andel.getArbeidsgiver().getArbeidsgiverOrgnr()).isEqualTo(ARBEIDSGIVER1.getIdentifikator());
        assertThat(andel.getMaksTillattDelvisRefusjonPrMnd().verdi()).isEqualTo(REFUSJON.verdi());
        assertThat(andel.getTidligsteMuligeRefusjonsdato()).isEqualTo(SKJÆRINGSTIDSPUNKT.plusMonths(3));
        assertThat(andel.getFastsattNyttRefusjonskravFom()).isEqualTo(SKJÆRINGSTIDSPUNKT.plusMonths(4));
        assertThat(andel.getSkalKunneFastsetteDelvisRefusjon()).isFalse();
    }

    @Test
    void skal_ikke_lage_dto_når_det_ikke_finnes_andeler_med_økt_ref() {
        var beregningsgrunnlag = lagBeregningsgrunnlagDto();
        var bgPeriode = lagBGPeriode(beregningsgrunnlag);
        lagBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriode, ARBEIDSGIVER1);
        var input = ferdigstillInput(beregningsgrunnlag, null, null);

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isEmpty();
    }

    @Test
    void skal_lage_dto_når_tilkommetIM_har_refusjonskrav_tidligere_utbetalt_bruker() {
        var beregningsgrunnlag = lagBeregningsgrunnlagDto();
        var beregningsgrunnlagForrige = lagBeregningsgrunnlagDto();
        var bgPeriode = lagBGPeriode(beregningsgrunnlag);
        var bgPeriodeForrige = lagBGPeriode(beregningsgrunnlagForrige);
        lagBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriode, ARBEIDSGIVER1);
        lagBGAndelLavRefusjonHøyBruttoPåPeriode(bgPeriodeForrige, REFUSJONSKRAV, null);

        var input = ferdigstillInput(beregningsgrunnlag, beregningsgrunnlagForrige, null);

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), bgPeriodeForrige, BG_PERIODE.getFomDato(), SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID, false);
    }

    @Test
    void skal_lage_dto_når_tilkommetIM_har_refusjonskrav_tidligere_utbetalt_refusjon() {
        var beregningsgrunnlag = lagBeregningsgrunnlagDto();
        var beregningsgrunnlagForrige = lagBeregningsgrunnlagDto();
        var bgPeriode = lagBGPeriode(beregningsgrunnlag);
        var bgPeriodeForrige = lagBGPeriode(beregningsgrunnlagForrige);
        lagBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriode, ARBEIDSGIVER1);
        lagBGAndelLavRefusjonHøyBruttoPåPeriode(bgPeriodeForrige, null, REFUSJONSKRAV);
        var input = ferdigstillInput(beregningsgrunnlag, beregningsgrunnlagForrige, null);

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), bgPeriodeForrige, BG_PERIODE.getFomDato(), SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID, true);
    }

    @Test
    void skal_lage_dto_når_tilkommetIM_har_refusjonskrav_tidligere_utbetalt_refusjon_for_annet_orgnr() {
        var beregningsgrunnlag = lagBeregningsgrunnlagDto();
        var beregningsgrunnlagForrige = lagBeregningsgrunnlagDto();
        var bgPeriode = lagBGPeriode(beregningsgrunnlag);
        var bgPeriodeForrige = lagBGPeriode(beregningsgrunnlagForrige);
        lagBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriode, ARBEIDSGIVER2);
        lagBGAndelLavRefusjonHøyBruttoPåPeriode(bgPeriodeForrige, null, REFUSJONSKRAV);
        var input = ferdigstillInput(beregningsgrunnlag, beregningsgrunnlagForrige, null);

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);

        var matchetAndel = getMatchetAndel(resultat.get().getAndeler(), ORGNR2);
        assertThat(matchetAndel).isNotNull();
        assertThat(matchetAndel.getNyttRefusjonskravFom()).isEqualTo(BG_PERIODE.getFomDato());
        assertThat(matchetAndel.getTidligsteMuligeRefusjonsdato()).isEqualTo(BG_PERIODE.getFomDato());

        assertThat(matchetAndel.getTidligereUtbetalinger()).isEmpty();
        assertThat(matchetAndel.getSkalKunneFastsetteDelvisRefusjon()).isTrue();
    }

    @Test
    void skal_sette_tidligste_refusjonsdato_lik_avklaring_fra_fakta_om_beregning() {
        var beregningsgrunnlag = lagBeregningsgrunnlagDto();
        var beregningsgrunnlagForrige = lagBeregningsgrunnlagDto();
        var bgPeriode = lagBGPeriode(beregningsgrunnlag);
        var bgPeriodeForrige = lagBGPeriode(beregningsgrunnlagForrige);
        var tidligsteRefusjonFom = BG_PERIODE.getFomDato().plusMonths(1);
        lagBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriode, ARBEIDSGIVER1);
        lagBGAndelLavRefusjonHøyBruttoPåPeriode(bgPeriodeForrige, null, REFUSJONSKRAV);
        var refusjonOverstyring = lagRefusjonOverstyringBuilder(ARBEIDSGIVER1, tidligsteRefusjonFom, List.of());
        var input = ferdigstillInput(beregningsgrunnlag, beregningsgrunnlagForrige, refusjonOverstyring);

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), bgPeriodeForrige, BG_PERIODE.getFomDato(), tidligsteRefusjonFom, true);
    }

    @Test
    void ved_flere_andeler_skal_kun_andel_i_første_periode_brukes() {
        var beregningsgrunnlag = lagBeregningsgrunnlagDto();
        var beregningsgrunnlagForrige = lagBeregningsgrunnlagDto();
        var bgPeriode = lagBGPeriode(beregningsgrunnlag);
        var bgPeriodeForrige = lagBGPeriode(beregningsgrunnlagForrige);

        lagBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriode, ARBEIDSGIVER1);
        var bgPeriodeEldst = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(BG_PERIODE_FORRIGE.getFomDato(), BG_PERIODE_FORRIGE.getTomDato())
            .build(beregningsgrunnlag);
        lagBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriodeEldst, ARBEIDSGIVER1);
        lagBGAndelLavRefusjonHøyBruttoPåPeriode(bgPeriodeForrige, null, REFUSJONSKRAV);

        var bgPeriodeForrigeEldst = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(BG_PERIODE_FORRIGE.getFomDato(), BG_PERIODE_FORRIGE.getTomDato())
            .build(beregningsgrunnlagForrige);
        lagBGAndelLavRefusjonHøyBruttoPåPeriode(bgPeriodeForrigeEldst, null, REFUSJONSKRAV, false);

        var tidligsteRefusjonFom = BG_PERIODE_FORRIGE.getFomDato().plusMonths(1);
        var refusjonOverstying = lagRefusjonOverstyringBuilder(ARBEIDSGIVER1, tidligsteRefusjonFom, List.of());
        var input = ferdigstillInput(beregningsgrunnlag, beregningsgrunnlagForrige, refusjonOverstying);

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), bgPeriodeForrige, BG_PERIODE_FORRIGE.getFomDato(), tidligsteRefusjonFom, true);
    }

    @Test
    void skal_ignorere_tidligere_avklaring_fra_fakta_om_beregning_når_dette_gjeder_annet_arbfor() {
        var beregningsgrunnlag = lagBeregningsgrunnlagDto();
        var beregningsgrunnlagForrige = lagBeregningsgrunnlagDto();
        var bgPeriode = lagBGPeriode(beregningsgrunnlag);
        var bgPeriodeForrige = lagBGPeriode(beregningsgrunnlagForrige);
        lagBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriode, ARBEIDSGIVER1);
        lagBGAndelLavRefusjonHøyBruttoPåPeriode(bgPeriodeForrige, null, REFUSJONSKRAV);

        var tidligsteRefusjonFom = BG_PERIODE.getFomDato().plusMonths(1);
        var refusjonOverstyring = lagRefusjonOverstyringBuilder(ARBEIDSGIVER2, tidligsteRefusjonFom, List.of());
        var input = ferdigstillInput(beregningsgrunnlag, beregningsgrunnlagForrige, refusjonOverstyring);

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), bgPeriodeForrige, BG_PERIODE.getFomDato(), SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID, true);
    }

    private void assertAndeler(List<RefusjonAndelTilVurderingDto> andeler,
                               BeregningsgrunnlagPeriodeDto bgPeriodeForrige,
                               LocalDate refusjonFom,
                               LocalDate tidligsteMuligeRefusjon,
                               boolean erTildeltRefusjon) {
        var matchetAndel = getMatchetAndel(andeler, ORGNR1);
        assertThat(matchetAndel).isNotNull();
        assertThat(matchetAndel.getNyttRefusjonskravFom()).isEqualTo(refusjonFom);
        assertThat(matchetAndel.getTidligsteMuligeRefusjonsdato()).isEqualTo(tidligsteMuligeRefusjon);

        var tidligereUtbetaling = new TidligereUtbetalingDto(bgPeriodeForrige.getBeregningsgrunnlagPeriodeFom(),
            bgPeriodeForrige.getBeregningsgrunnlagPeriodeTom(), erTildeltRefusjon);
        assertThat(matchetAndel.getTidligereUtbetalinger()).contains(tidligereUtbetaling);
    }

    private RefusjonAndelTilVurderingDto getMatchetAndel(List<RefusjonAndelTilVurderingDto> andeler, String orgnr) {
        return andeler.stream().filter(a -> a.getArbeidsgiver().getArbeidsgiverOrgnr().equals(orgnr)).findFirst().orElse(null);
    }

    private BeregningsgrunnlagGUIInput lagInputMedBeregningsgrunnlagOgIAY(Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap) {
        var iayGrunnlag = lagIayGrunnlagMedInntektsmeldinger(List.of(ORGNR1, ORGNR2), true);
        var aktivitetAggregat = lagBeregningAktivitetAggregat(førsteInnsendingAvRefusjonMap.keySet());
        var beregingsgrunnlagGrunnlag = lagBGGrunnlagOgPerioder(aktivitetAggregat, null, førsteInnsendingAvRefusjonMap.keySet(), SKJÆRINGSTIDSPUNKT,
            Beløp.ZERO, null);
        return new BeregningsgrunnlagGUIInput(new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT), iayGrunnlag,
            opprett(iayGrunnlag, SKJÆRINGSTIDSPUNKT, førsteInnsendingAvRefusjonMap), null).medBeregningsgrunnlagGrunnlag(beregingsgrunnlagGrunnlag)
            .medAvklaringsbehov(List.of(avklaringsbehovDto));
    }

    private BeregningsgrunnlagGUIInput lagInputMedBeregningsgrunnlagOgIAYOgForrigeGrunnlag(Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap) {
        var iayGrunnlag = lagIayGrunnlagMedInntektsmeldinger(List.of(ORGNR1), false);
        var aktivitetAggregat = lagBeregningAktivitetAggregat(førsteInnsendingAvRefusjonMap.keySet());
        var refusjonOverstyring = lagRefusjonOverstyringBuilder(ARBEIDSGIVER1, SKJÆRINGSTIDSPUNKT.plusMonths(3),
            List.of(new BeregningRefusjonPeriodeDto(null, SKJÆRINGSTIDSPUNKT.plusMonths(4))));
        var beregningsgrunnlagGrunnlag = lagBGGrunnlagOgPerioder(aktivitetAggregat, refusjonOverstyring, Set.of(ARBEIDSGIVER1),
            SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID, REFUSJONSKRAV, null);
        var forrigeBeregningsgrunnlagGrunnlag = lagBGGrunnlagOgPerioder(aktivitetAggregat, null, Set.of(ARBEIDSGIVER1),
            SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID, REFUSJONSKRAV, REFUSJONSKRAV);
        return new BeregningsgrunnlagGUIInput(new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID), iayGrunnlag,
            opprett(iayGrunnlag, SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID, førsteInnsendingAvRefusjonMap), null).medBeregningsgrunnlagGrunnlag(
                beregningsgrunnlagGrunnlag)
            .medBeregningsgrunnlagGrunnlagFraForrigeBehandling(forrigeBeregningsgrunnlagGrunnlag)
            .medAvklaringsbehov(List.of(avklaringsbehovDto));
    }

    private BeregningsgrunnlagGUIInput ferdigstillInput(BeregningsgrunnlagDto beregningsgrunnlag,
                                                        BeregningsgrunnlagDto beregningsgrunnlagForrige,
                                                        BeregningRefusjonOverstyringerDto.Builder refusjonOverstyringerBuilder) {
        var forrigeBeregningsgrunnlagGrunnlag = lagBGGrunnlagDto(beregningsgrunnlagForrige, null, null, BeregningsgrunnlagTilstand.VURDERT_REFUSJON);
        var beregningsgrunnlagGrunnlag = lagBGGrunnlagDto(beregningsgrunnlag, null, refusjonOverstyringerBuilder,
            BeregningsgrunnlagTilstand.VURDERT_REFUSJON);

        return new BeregningsgrunnlagGUIInput(new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT), InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(),
            List.of(), null).medBeregningsgrunnlagGrunnlagFraForrigeBehandling(List.of(forrigeBeregningsgrunnlagGrunnlag))
            .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag)
            .medAvklaringsbehov(List.of(avklaringsbehovDto));
    }

    private BeregningsgrunnlagGrunnlagDto lagBGGrunnlagOgPerioder(BeregningAktivitetAggregatDto aktivitetAggregat,
                                                                  BeregningRefusjonOverstyringerDto.Builder refusjonOverstyringerBuilder,
                                                                  Set<Arbeidsgiver> arbeidsgivere,
                                                                  LocalDate skjæringstidspunkt,
                                                                  Beløp refusjonskrav,
                                                                  Beløp redusertAG) {
        var beregningsgrunnlag = lagBeregningsgrunnlagDto(skjæringstidspunkt);
        var bgPeriode = lagBGPeriode(beregningsgrunnlag, skjæringstidspunkt, null);
        int andelsnr = 1;
        for (Arbeidsgiver arbeidsgiver : arbeidsgivere) {
            var bga = lagBGAndelArbeidsforhold(SKJÆRINGSTIDSPUNKT.minusYears(1), SKJÆRINGSTIDSPUNKT.plusYears(2), arbeidsgiver, refusjonskrav);
            lagBGPrStatusOgAndel(bga, bgPeriode, andelsnr++, BRUTTO, null, redusertAG, bgPeriode.getPeriode().getFomDato(),
                bgPeriode.getPeriode().getTomDato());
        }
        BeregningsgrunnlagPeriodeDto.oppdater(bgPeriode).build();
        return lagBGGrunnlagDto(beregningsgrunnlag, aktivitetAggregat, refusjonOverstyringerBuilder,
            BeregningsgrunnlagTilstand.VURDERT_TILKOMMET_INNTEKT_UT);
    }

    private BeregningAktivitetAggregatDto lagBeregningAktivitetAggregat(Set<Arbeidsgiver> arbeidsgivere) {
        var arbeidsperiode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        var aktivitetAggregatBuilder = BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT);
        for (var arbeidsgiver : arbeidsgivere) {
            leggTilYrkesaktivitet(aktørArbeidBuilder, arbeidsperiode, arbeidsgiver);
            lagBeregningAktivitetAggregat(aktivitetAggregatBuilder, arbeidsperiode, arbeidsgiver);
        }
        iayBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        return aktivitetAggregatBuilder.build();
    }

    private InntektArbeidYtelseGrunnlagDto lagIayGrunnlagMedInntektsmeldinger(List<String> orgnrListe, boolean refusjonLikInntekt) {
        List<InntektsmeldingDto> inntektsmeldinger = new ArrayList<>();
        orgnrListe.forEach(orgnr -> {
            inntektsmeldinger.add(BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr, SKJÆRINGSTIDSPUNKT, REFUSJON,
                refusjonLikInntekt ? REFUSJON : REFUSJON.subtraher(Beløp.fra(10000))));
        });
        return InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty()).medData(iayBuilder).medInntektsmeldinger(inntektsmeldinger).build();
    }

    private void lagBGAndelHøyRefusjonLavBruttoPåPeriode(BeregningsgrunnlagPeriodeDto periode, Arbeidsgiver arbeidsgiver) {
        var bga = lagBGAndelArbeidsforhold(SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID, SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID.plusMonths(1), arbeidsgiver, REFUSJON);
        lagBGPrStatusOgAndel(bga, periode, 1, BRUTTO.subtraher(Beløp.fra(100000)), null, null, SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID,
            SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID.plusMonths(1));
        BeregningsgrunnlagPeriodeDto.oppdater(periode).build();
    }

    private void lagBGAndelLavRefusjonHøyBruttoPåPeriode(BeregningsgrunnlagPeriodeDto periode, Beløp redusertBruker, Beløp redusertAG) {
        lagBGAndelLavRefusjonHøyBruttoPåPeriode(periode, redusertBruker, redusertAG, true);
    }

    private void lagBGAndelLavRefusjonHøyBruttoPåPeriode(BeregningsgrunnlagPeriodeDto periode,
                                                         Beløp redusertBruker,
                                                         Beløp redusertAG,
                                                         boolean skalOppdatere) {
        var bga = lagBGAndelArbeidsforhold(SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID, SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID.plusMonths(1), ARBEIDSGIVER1,
            REFUSJON.subtraher(Beløp.fra(10000)));
        lagBGPrStatusOgAndel(bga, periode, 1, BRUTTO, redusertBruker, redusertAG, SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID,
            SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID.plusMonths(1));
        if (skalOppdatere) {
            BeregningsgrunnlagPeriodeDto.oppdater(periode).build();
        }
    }

    private void leggTilYrkesaktivitet(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder,
                                       Intervall arbeidsperiode,
                                       Arbeidsgiver arbeidsgiver) {
        var aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny().medPeriode(arbeidsperiode);
        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aktivitetsAvtaleDtoBuilder);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yaBuilder);
    }

    private void lagBeregningAktivitetAggregat(BeregningAktivitetAggregatDto.Builder aktivitetAggregatBuilder,
                                               Intervall arbeidsperiode,
                                               Arbeidsgiver arbeidsgiver) {
        var beregningAktivitet = BeregningAktivitetDto.builder()
            .medArbeidsgiver(arbeidsgiver)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medPeriode(Intervall.fraOgMedTilOgMed(arbeidsperiode.getFomDato(), arbeidsperiode.getTomDato()))
            .build();
        aktivitetAggregatBuilder.leggTilAktivitet(beregningAktivitet);
    }

    private BeregningRefusjonOverstyringerDto.Builder lagRefusjonOverstyringBuilder(Arbeidsgiver arbeidsgiver,
                                                                                    LocalDate tidligsteRefusjonsstart,
                                                                                    List<BeregningRefusjonPeriodeDto> refusjonPerioder) {
        var refusjonOverstyring = new BeregningRefusjonOverstyringDto(arbeidsgiver, tidligsteRefusjonsstart, refusjonPerioder, false);
        return BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(refusjonOverstyring);
    }

    private BeregningsgrunnlagPeriodeDto lagBGPeriode(BeregningsgrunnlagDto grunnlag) {
        return BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(BG_PERIODE.getFomDato(), BG_PERIODE.getTomDato()).build(grunnlag);
    }

    private BeregningsgrunnlagPeriodeDto lagBGPeriode(BeregningsgrunnlagDto grunnlag, LocalDate fraDato, LocalDate tilDato) {
        return BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(fraDato, tilDato).build(grunnlag);
    }

    private BeregningsgrunnlagGrunnlagDto lagBGGrunnlagDto(BeregningsgrunnlagDto beregningsgrunnlag,
                                                           BeregningAktivitetAggregatDto aktivitetAggregat,
                                                           BeregningRefusjonOverstyringerDto.Builder refusjonOverstyringerBuilder,
                                                           BeregningsgrunnlagTilstand tilstand) {
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .medRegisterAktiviteter(aktivitetAggregat)
            .medRefusjonOverstyring(refusjonOverstyringerBuilder != null ? refusjonOverstyringerBuilder.build() : null)
            .build(tilstand);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagDto() {
        return lagBeregningsgrunnlagDto(SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagDto(LocalDate skjæringstidspunkt) {
        return BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .medGrunnbeløp(GRUNNBELØP)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .build();
    }

    private BGAndelArbeidsforholdDto.Builder lagBGAndelArbeidsforhold(LocalDate fraDato,
                                                                      LocalDate tilDato,
                                                                      Arbeidsgiver arbeidsgiver,
                                                                      Beløp refusjon) {
        return BGAndelArbeidsforholdDto.builder()
            .medArbeidsperiodeFom(fraDato)
            .medArbeidsperiodeTom(tilDato)
            .medRefusjon(new Refusjon(refusjon, null, null, null, null, null))
            .medArbeidsgiver(arbeidsgiver);
    }

    private void lagBGPrStatusOgAndel(BGAndelArbeidsforholdDto.Builder bga,
                                      BeregningsgrunnlagPeriodeDto periode,
                                      int andelsnr,
                                      Beløp brutto,
                                      Beløp redusertBruker,
                                      Beløp redusertAG,
                                      LocalDate fraDato,
                                      LocalDate tilDato) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAndelsnr((long) andelsnr)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregningsperiode(fraDato, tilDato)
            .medFordeltPrÅr(brutto)
            .medRedusertBrukersAndelPrÅr(redusertBruker)
            .medRedusertRefusjonPrÅr(redusertAG)
            .build(periode);
    }
}
