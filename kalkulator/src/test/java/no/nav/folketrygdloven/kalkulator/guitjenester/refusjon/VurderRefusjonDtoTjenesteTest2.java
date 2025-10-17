package no.nav.folketrygdloven.kalkulator.guitjenester.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.avklaringsbehov.AvklaringsbehovDto;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonAndelTilVurderingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.TidligereUtbetalingDto;

class VurderRefusjonDtoTjenesteTest2 {

    private static final Skjæringstidspunkt STP = Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(LocalDate.of(2020, 1, 1)).build();
    public static final LocalDate STP_BEREGNING = STP.getSkjæringstidspunktBeregning();
    private static final Intervall BG_PERIODE = Intervall.fraOgMedTilOgMed(STP_BEREGNING, TIDENES_ENDE);
    private static final Intervall BG_PERIODE_FORRIGE = Intervall.fraOgMedTilOgMed(BG_PERIODE.getFomDato().minusYears(1),
        BG_PERIODE.getFomDato().minusDays(1));
    private static final String ORGNR = "999999999";
    private static final String ORGNR2 = "999999991";
    private static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet(ORGNR);
    private static final Arbeidsgiver ARBEIDSGIVER2 = Arbeidsgiver.virksomhet(ORGNR2);
    private static final Beløp REDUSERT_REFUSJON = Beløp.fra(500000);
    private static final Beløp BRUTTO_LAV = Beløp.fra(400000);
    private static final Beløp BRUTTO_HØY = Beløp.fra(500000);
    private static final Beløp REFUSJON_LAV = Beløp.fra(11111);
    private static final Beløp REFUSJON_HØY = Beløp.fra(33333);

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPeriodeDto bgPeriode;
    private BeregningsgrunnlagDto beregningsgrunnlagForrige;
    private BeregningsgrunnlagPeriodeDto bgPeriodeForrige;
    private BeregningRefusjonOverstyringerDto.Builder refusjonOverstyringerBuilder;

    @BeforeEach
    void setup() {
        beregningsgrunnlag = byggBGMedAktivitetStatusOgPeriode(Beløp.fra(125000));
        beregningsgrunnlagForrige = byggBGMedAktivitetStatusOgPeriode(null);
        bgPeriode = byggBGPeriode(beregningsgrunnlag);
        bgPeriodeForrige = byggBGPeriode(beregningsgrunnlagForrige);
    }

    @Test
    void skal_ikke_lage_dto_når_det_ikke_finnes_andeler_med_økt_ref() {
        byggBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriode, ARBEIDSGIVER);
        var input = ferdigstillInput();

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isEmpty();
    }

    @Test
    void skal_lage_dto_når_tilkommetIM_har_refusjonskrav_tidligere_utbetalt_bruker() {
        byggBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriode, ARBEIDSGIVER);
        byggBGAndelPåPeriode(bgPeriodeForrige, ARBEIDSGIVER, REFUSJON_LAV, BRUTTO_HØY, REDUSERT_REFUSJON, Beløp.ZERO);
        var input = ferdigstillInput();

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), BG_PERIODE.getFomDato(), STP_BEREGNING, false);
    }

    @Test
    void skal_lage_dto_når_tilkommetIM_har_refusjonskrav_tidligere_utbetalt_refusjon() {
        byggBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriode, ARBEIDSGIVER);
        byggBGAndelPåPeriode(bgPeriodeForrige, ARBEIDSGIVER, REFUSJON_LAV, BRUTTO_HØY, Beløp.ZERO, REDUSERT_REFUSJON);
        var input = ferdigstillInput();

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), BG_PERIODE.getFomDato(), STP_BEREGNING, true);
    }

    @Test
    void skal_lage_dto_når_tilkommetIM_har_refusjonskrav_tidligere_utbetalt_refusjon_for_annet_orgnr() {
        byggBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriode, ARBEIDSGIVER2);
        byggBGAndelPåPeriode(bgPeriodeForrige, ARBEIDSGIVER, REFUSJON_LAV, BRUTTO_HØY, Beløp.ZERO, REDUSERT_REFUSJON);
        var input = ferdigstillInput();

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
        var tidligsteRefusjonFom = BG_PERIODE.getFomDato().plusMonths(1);
        byggBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriode, ARBEIDSGIVER);
        byggBGAndelPåPeriode(bgPeriodeForrige, ARBEIDSGIVER, REFUSJON_LAV, BRUTTO_HØY, Beløp.ZERO, REDUSERT_REFUSJON);
        byggTidligereRefusjonoverstyring(ARBEIDSGIVER, tidligsteRefusjonFom);
        var input = ferdigstillInput();

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), BG_PERIODE.getFomDato(), tidligsteRefusjonFom, true);
    }

    @Test
    void ved_flere_andeler_skal_kun_andel_i_første_periode_brukes() {
        byggBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriode, ARBEIDSGIVER);
        var bgPeriodeEldst = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(BG_PERIODE_FORRIGE.getFomDato(), BG_PERIODE_FORRIGE.getTomDato())
            .build(beregningsgrunnlag);
        byggBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriodeEldst, ARBEIDSGIVER);

        byggBGAndelPåPeriode(bgPeriodeForrige, ARBEIDSGIVER, REFUSJON_LAV, BRUTTO_HØY, Beløp.ZERO, REDUSERT_REFUSJON);
        var bgPeriodeForrigeEldst = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(BG_PERIODE_FORRIGE.getFomDato(), BG_PERIODE_FORRIGE.getTomDato())
            .build(beregningsgrunnlagForrige);
        byggBGAndelPåPeriode(bgPeriodeForrigeEldst, ARBEIDSGIVER, REFUSJON_LAV, BRUTTO_HØY, Beløp.ZERO, REDUSERT_REFUSJON);

        var tidligsteRefusjonFom = BG_PERIODE_FORRIGE.getFomDato().plusMonths(1);
        byggTidligereRefusjonoverstyring(ARBEIDSGIVER, tidligsteRefusjonFom);
        var input = ferdigstillInput();

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), BG_PERIODE_FORRIGE.getFomDato(), tidligsteRefusjonFom, true);
    }

    @Test
    void skal_ignorere_tidligere_avklaring_fra_fakta_om_beregning_når_dette_gjeder_annet_arbfor() {
        byggBGAndelHøyRefusjonLavBruttoPåPeriode(bgPeriode, ARBEIDSGIVER);
        byggBGAndelPåPeriode(bgPeriodeForrige, ARBEIDSGIVER, REFUSJON_LAV, BRUTTO_HØY, Beløp.ZERO, REDUSERT_REFUSJON);
        var tidligsteRefusjonFom = BG_PERIODE.getFomDato().plusMonths(1);
        byggTidligereRefusjonoverstyring(ARBEIDSGIVER2, tidligsteRefusjonFom);
        var input = ferdigstillInput();

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), BG_PERIODE.getFomDato(), STP_BEREGNING, true);
    }

    private void assertAndeler(List<RefusjonAndelTilVurderingDto> andeler,
                               LocalDate refusjonFom,
                               LocalDate tidligsteMuligeRefusjon,
                               boolean erTildeltRefusjon) {
        var matchetAndel = getMatchetAndel(andeler, ORGNR);
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

    private BeregningsgrunnlagGUIInput ferdigstillInput() {
        BeregningsgrunnlagPeriodeDto.oppdater(bgPeriodeForrige).build();
        BeregningsgrunnlagPeriodeDto.oppdater(bgPeriode).build();
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        var avklaringsbehov = new AvklaringsbehovDto(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV, null, null, null, null, null);
        var koblingReferanse = new KoblingReferanseMock().medSkjæringstidspunkt(STP);

        var forrigeBeregningsgrunnlagGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlagForrige)
            .build(BeregningsgrunnlagTilstand.VURDERT_REFUSJON);

        var beregningsgrunnlagGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .medRefusjonOverstyring(refusjonOverstyringerBuilder != null ? refusjonOverstyringerBuilder.build() : null)
            .build(BeregningsgrunnlagTilstand.VURDERT_REFUSJON);

        return new BeregningsgrunnlagGUIInput(koblingReferanse, iayGrunnlag, List.of(), null).medBeregningsgrunnlagGrunnlagFraForrigeBehandling(
                List.of(forrigeBeregningsgrunnlagGrunnlag))
            .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag)
            .medAvklaringsbehov(List.of(avklaringsbehov));
    }

    private void byggBGAndelHøyRefusjonLavBruttoPåPeriode(BeregningsgrunnlagPeriodeDto periode, Arbeidsgiver arbeidsgiver) {
        byggBGAndelPåPeriode(periode, arbeidsgiver, REFUSJON_HØY, BRUTTO_LAV, null, null);
    }

    private void byggBGAndelPåPeriode(BeregningsgrunnlagPeriodeDto periode,
                                      Arbeidsgiver arbeidsgiver,
                                      Beløp refusjon,
                                      Beløp brutto,
                                      Beløp redusertBruker,
                                      Beløp redusertAG) {
        var bga = BGAndelArbeidsforholdDto.builder()
            .medArbeidsperiodeFom(STP_BEREGNING.minusYears(1))
            .medArbeidsperiodeTom(STP_BEREGNING.plusYears(2))
            .medRefusjon(new Refusjon(refusjon, null, null, null, null, null))
            .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAndelsnr(1L)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregningsperiode(STP_BEREGNING, STP_BEREGNING.plusMonths(1))
            .medFordeltPrÅr(brutto)
            .medRedusertBrukersAndelPrÅr(redusertBruker)
            .medRedusertRefusjonPrÅr(redusertAG)
            .build(periode);
    }

    private void byggTidligereRefusjonoverstyring(Arbeidsgiver arbeidsgiver, LocalDate tidligsteRefusjonsstart) {
        var refusjonOverstyring = new BeregningRefusjonOverstyringDto(arbeidsgiver, tidligsteRefusjonsstart, false);
        refusjonOverstyringerBuilder = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(refusjonOverstyring);
    }

    private BeregningsgrunnlagDto byggBGMedAktivitetStatusOgPeriode(Beløp grunnbeløp) {
        return BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(STP_BEREGNING)
            .medGrunnbeløp(grunnbeløp)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .build();
    }

    private BeregningsgrunnlagPeriodeDto byggBGPeriode(BeregningsgrunnlagDto grunnlag) {
        return BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(BG_PERIODE.getFomDato(), BG_PERIODE.getTomDato()).build(grunnlag);
    }
}

