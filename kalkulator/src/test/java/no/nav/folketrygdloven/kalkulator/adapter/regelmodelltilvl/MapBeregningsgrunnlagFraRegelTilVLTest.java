package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.GRUNNBELØPLISTE;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.MINUS_DAYS_10;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.MINUS_DAYS_20;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.MINUS_DAYS_5;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.MINUS_YEARS_1;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.MINUS_YEARS_2;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.MINUS_YEARS_3;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.buildRegelBGPeriode;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.buildRegelBeregningsgrunnlag;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.buildRegelSammenligningsG;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.buildVLBGAktivitetStatus;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.buildVLBGPStatus;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.buildVLBGPStatusForSN;
import static no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper.buildVLBGPeriode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.adapter.RegelMapperTestDataHelper;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulus.mappers.JsonMapper;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

public class MapBeregningsgrunnlagFraRegelTilVLTest {

    private static final String ORGNR = "974652269";

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock();

    @Test
    public void testMappingBGForArbeidstaker() {
        final BeregningsgrunnlagDto vlBG = buildVLBGForATOgFL();

        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatGrunnlag = buildRegelBGForAT();
        final BeregningsgrunnlagDto mappedBG = new MapBeregningsgrunnlagFraRegelTilVL()
                .mapForeslåBeregningsgrunnlag(resultatGrunnlag, vlBG);

        final BeregningsgrunnlagPeriodeDto vlBGP = mappedBG.getBeregningsgrunnlagPerioder()
                .get(0);

        assertThat(vlBGP.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        final BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus1 = vlBGP.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertVLBGPStatusAT(vlBGPStatus1);
        final BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus2 = vlBGP.getBeregningsgrunnlagPrStatusOgAndelList().get(1);
        assertVLBGPStatusFL(vlBGPStatus2);
        assertVLSammenligningsgrunnlagPrStatus(mappedBG.getSammenligningsgrunnlagPrStatusListe().get(0), SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL);
    }

    @Test
    public void skal_mappe_beregningsgrunnlag_når_arbeidsgiver_er_privatperson() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2018, 1, 1);
        LocalDate førsteUttaksdag = skjæringstidspunkt.plusWeeks(2);
        AktørId aktørId = AktørId.dummy();
        AktivitetStatusModell regelmodell = lagRegelModell(skjæringstidspunkt, Arbeidsforhold.nyttArbeidsforholdHosPrivatperson(aktørId.getId()));
        String inputSkjæringstidspunkt = toJson(regelmodell);
        RegelResultat regelResultat = new RegelResultat(ResultatBeregningType.BEREGNET, "1.2.3", inputSkjæringstidspunkt, "sporing");
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(skjæringstidspunkt, skjæringstidspunkt.minusYears(1), skjæringstidspunkt, null, Arbeidsgiver.person(aktørId), iayGrunnlagBuilder);

        // Act
        BeregningsgrunnlagDto beregningsgrunnlag = MapBGSkjæringstidspunktOgStatuserFraRegelTilVL
                .mapForSkjæringstidspunktOgStatuser(koblingReferanse.medSkjæringstidspunkt(Skjæringstidspunkt.builder().medFørsteUttaksdato(førsteUttaksdag).medSkjæringstidspunktOpptjening(førsteUttaksdag).build()), regelmodell,
                        List.of(regelResultat, regelResultat), iayGrunnlagBuilder.build(), GRUNNBELØPLISTE);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndelDto andel = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        // Andel asserts
        assertThat(andel.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        // Arbeidsforhold asserts
        assertThat(andel.getBgAndelArbeidsforhold()).isPresent();
        BGAndelArbeidsforholdDto bga = andel.getBgAndelArbeidsforhold().get();
        assertThat(bga.getArbeidsgiver().getErVirksomhet()).isFalse();
        assertThat(bga.getArbeidsgiver().getIdentifikator()).isEqualTo(aktørId.getId());
    }

    @Test
    public void skal_mappe_beregningsgrunnlag_når_arbeidsgiver_er_virksomhet() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2018, 1, 1);
        LocalDate førsteUttaksdag = skjæringstidspunkt.plusWeeks(2);
        RegelResultat regelResultat = new RegelResultat(ResultatBeregningType.BEREGNET, "1.2.3", "input", "sporing");
        AktivitetStatusModell regelmodell = lagRegelModell(skjæringstidspunkt, Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(skjæringstidspunkt, skjæringstidspunkt.minusYears(1), skjæringstidspunkt, null,
                Arbeidsgiver.virksomhet(ORGNR), iayGrunnlagBuilder);

        // Act
        BeregningsgrunnlagDto beregningsgrunnlag = MapBGSkjæringstidspunktOgStatuserFraRegelTilVL
                .mapForSkjæringstidspunktOgStatuser(koblingReferanse.medSkjæringstidspunkt(Skjæringstidspunkt.builder().medFørsteUttaksdato(førsteUttaksdag).medSkjæringstidspunktOpptjening(førsteUttaksdag).build()), regelmodell,
                        List.of(regelResultat, regelResultat), iayGrunnlagBuilder.build(), GRUNNBELØPLISTE);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndelDto andel = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        // Andel asserts
        assertThat(andel.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        // Arbeidsforhold asserts
        assertThat(andel.getBgAndelArbeidsforhold()).isPresent();
        BGAndelArbeidsforholdDto bga = andel.getBgAndelArbeidsforhold().get();
        assertThat(bga.getArbeidsgiver().getErVirksomhet()).isTrue();
        assertThat(bga.getArbeidsgiver().getIdentifikator()).isEqualTo(ORGNR);
    }

    @Test
    public void skal_mappe_beregningsgrunnlag_for_næring() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2018, 1, 1);
        LocalDate førsteUttaksdag = skjæringstidspunkt.plusWeeks(2);
        RegelResultat regelResultat = new RegelResultat(ResultatBeregningType.BEREGNET, "1.2.3", "input", "sporing");
        AktivitetStatusModell regelmodell = lagRegelModellSN(skjæringstidspunkt);

        // Act
        BeregningsgrunnlagDto beregningsgrunnlag = MapBGSkjæringstidspunktOgStatuserFraRegelTilVL
                .mapForSkjæringstidspunktOgStatuser(koblingReferanse.medSkjæringstidspunkt(Skjæringstidspunkt.builder().medFørsteUttaksdato(førsteUttaksdag).medSkjæringstidspunktOpptjening(førsteUttaksdag).build()), regelmodell,
                        List.of(regelResultat, regelResultat), InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), GRUNNBELØPLISTE);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndelDto andel = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(andel.getAktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(andel.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.NÆRING);
    }


    @Test
    public void skalSetteRiktigSammenligningsgrunnlagPrStatusNårBeregningsgrunnlagInneholderSammenlingningsgrunnlagForFL() {
        final BeregningsgrunnlagDto vlBG = buildVLBGForATOgFL();

        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatGrunnlag = buildRegelBGForFL();
        final BeregningsgrunnlagDto mappedBG = new MapBeregningsgrunnlagFraRegelTilVL()
                .mapForeslåBeregningsgrunnlag(resultatGrunnlag, vlBG);

        assertVLSammenligningsgrunnlagPrStatus(mappedBG.getSammenligningsgrunnlagPrStatusListe().get(0), SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL);
    }

    private void assertVLSammenligningsgrunnlagPrStatus(SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlag, SammenligningsgrunnlagType sammenligningsgrunnlagType) {
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeFom()).isEqualTo(MINUS_YEARS_1);
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeTom()).isEqualTo(MINUS_DAYS_20);
        assertThat(sammenligningsgrunnlag.getRapportertPrÅr().verdi().doubleValue()).isEqualTo(42.0);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagType()).isEqualTo(sammenligningsgrunnlagType);
    }

    private void assertVLBGPStatusSN(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus) {
        assertThat(vlBGPStatus.getAktivitetStatus())
                .isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(vlBGPStatus.getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(vlBGPStatus.getBeregnetPrÅr().verdi().doubleValue()).isEqualTo(400000.42, within(0.01));
        assertThat(vlBGPStatus.getBruttoPrÅr().verdi().doubleValue()).isEqualTo(400000.42, within(0.01));
        assertThat(vlBGPStatus.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(null)).isNull();
        assertThat(vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr)).isEmpty();
        assertThat(vlBGPStatus.getAvkortetPrÅr().verdi().doubleValue()).isEqualTo(789.789, within(0.01));
        assertThat(vlBGPStatus.getRedusertPrÅr().verdi().doubleValue()).isEqualTo(901.901, within(0.01));
        assertThat(vlBGPStatus.getDagsatsArbeidsgiver()).isEqualTo(0L);
    }

    private void assertVLBGPStatusFL(BeregningsgrunnlagPrStatusOgAndelDto bgpsa) {
        assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
        assertThat(bgpsa.getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
        assertThat(bgpsa.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver)).isEmpty();
        assertThat(bgpsa.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)
                .map(InternArbeidsforholdRefDto::gjelderForSpesifiktArbeidsforhold).orElse(false))
                .as("gjelderSpesifiktArbeidsforhold").isFalse();
        assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.FRILANS);
        assertThat(bgpsa.getBeregnetPrÅr().verdi().doubleValue()).isEqualTo(456.456, within(0.01));
        assertThat(bgpsa.getBruttoPrÅr().verdi().doubleValue()).isEqualTo(456.456, within(0.01));
        assertThat(bgpsa.getBgAndelArbeidsforhold()).isEmpty();
        assertThat(bgpsa.getAvkortetPrÅr().verdi().doubleValue()).isEqualTo(34.34, within(0.01));
        assertThat(bgpsa.getRedusertPrÅr().verdi().doubleValue()).isEqualTo(65.65, within(0.01));
        assertThat(bgpsa.getDagsatsArbeidsgiver()).isEqualTo(5L);
    }

    private void assertVLBGPStatusAT(BeregningsgrunnlagPrStatusOgAndelDto bgpsa) {
        assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(bgpsa.getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(bgpsa.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver))
                .hasValueSatisfying(arbeidsgiver -> assertThat(arbeidsgiver.getOrgnr()).isEqualTo(ORGNR));
        assertThat(bgpsa.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)
                .map(InternArbeidsforholdRefDto::gjelderForSpesifiktArbeidsforhold).orElse(false))
                .as("gjelderSpesifiktArbeidsforhold").isFalse();
        assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ARBEID);
        assertThat(bgpsa.getBeregnetPrÅr().verdi().doubleValue()).isEqualTo(123.123, within(0.01));
        assertThat(bgpsa.getBruttoPrÅr().verdi().doubleValue()).isEqualTo(123.123, within(0.01));
        assertThat(bgpsa.getMaksimalRefusjonPrÅr().verdi().doubleValue()).isEqualTo(123.123, within(0.01));
        assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr).get().verdi().doubleValue()).isEqualTo(87.87,
                within(0.01));
        assertThat(bgpsa.getAvkortetPrÅr().verdi().doubleValue()).isEqualTo(57.57, within(0.01));
        assertThat(bgpsa.getRedusertPrÅr().verdi().doubleValue()).isEqualTo(89.89, within(0.01));
        assertThat(bgpsa.getDagsatsArbeidsgiver()).isEqualTo(10L);
    }

    private BeregningsgrunnlagDto buildVLBG() {
        final BeregningsgrunnlagDto vlBG = RegelMapperTestDataHelper
                .buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(vlBG, AktivitetStatus.ARBEIDSTAKER);
        buildVLBGPStatusForSN(buildVLBGPeriode(vlBG));
        return vlBG;
    }

    private BeregningsgrunnlagDto buildVLBGForATOgFL() {
        final BeregningsgrunnlagDto vlBG = RegelMapperTestDataHelper
                .buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(vlBG, AktivitetStatus.ARBEIDSTAKER);
        final BeregningsgrunnlagPeriodeDto vlBGPeriode = buildVLBGPeriode(vlBG);
        buildVLBGPStatus(vlBGPeriode, AktivitetStatus.ARBEIDSTAKER,
                Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_2,
                MINUS_YEARS_1, Arbeidsgiver.virksomhet(ORGNR), OpptjeningAktivitetType.ARBEID);
        buildVLBGPStatus(vlBGPeriode, AktivitetStatus.FRILANSER,
                Inntektskategori.FRILANSER, MINUS_YEARS_3, MINUS_YEARS_2);
        return vlBG;
    }

    private BeregningsgrunnlagDto buildVLBGForATFLogSN() {
        final BeregningsgrunnlagDto vlBG = RegelMapperTestDataHelper
                .buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(vlBG, AktivitetStatus.ARBEIDSTAKER);
        final BeregningsgrunnlagPeriodeDto vlBGPeriode = buildVLBGPeriode(vlBG);
        buildVLBGPStatusForSN(vlBGPeriode);
        buildVLBGPStatus(vlBGPeriode, AktivitetStatus.ARBEIDSTAKER,
                Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_2,
                MINUS_YEARS_1, Arbeidsgiver.virksomhet(ORGNR), OpptjeningAktivitetType.ARBEID);
        buildVLBGPStatus(vlBGPeriode, AktivitetStatus.FRILANSER,
                Inntektskategori.FRILANSER, MINUS_YEARS_3, MINUS_YEARS_2);
        return vlBG;
    }

    private no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag buildRegelBGForSN() {
        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag regelBG = buildRegelBeregningsgrunnlag(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.SN,
                no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE,
                BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG);
        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag.builder(regelBG).build();
        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag.builder(regelBG).build();

        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode regelBGP = regelBG.getBeregningsgrunnlagPerioder().get(0);

        buildRegelBGPeriodeSN(regelBGP);
        return regelBG;
    }

    private no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag buildRegelBGForAT() {
        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag regelBG = buildRegelBeregningsgrunnlag(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL,
                null,
                BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER);
        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag.builder(regelBG).build();
        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag.builder(regelBG).leggTilSammenligningsgrunnlagPrStatus(buildRegelSammenligningsG(SammenligningGrunnlagType.AT_FL)).build();

        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode regelBGP = regelBG.getBeregningsgrunnlagPerioder().get(0);

        buildRegelBGPStatusATFL(regelBGP, 1);
        return regelBG;
    }

    private no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag buildRegelBGForFL() {
        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag regelBG = buildRegelBeregningsgrunnlag(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL,
                null,
                BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER);
        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag.builder(regelBG).build();
        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag.builder(regelBG).leggTilSammenligningsgrunnlagPrStatus(buildRegelSammenligningsG(SammenligningGrunnlagType.AT_FL)).build();

        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode regelBGP = regelBG.getBeregningsgrunnlagPerioder().get(0);

        buildRegelBGPStatusATFL(regelBGP, 1);
        return regelBG;
    }

    private no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag buildRegelBGForATFLogSN() {
        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag regelBG = buildRegelBeregningsgrunnlag(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL_SN,
                null,
                BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG);
        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag.builder(regelBG).build();

        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode regelBGP = regelBG.getBeregningsgrunnlagPerioder().get(0);

        buildRegelBGPeriodeSN(regelBGP);
        buildRegelBGPStatusATFL(regelBGP, 2);
        return regelBG;
    }

    private void buildRegelBGPeriodeSN(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode regelBGP) {
        buildRegelBGPeriode(regelBGP, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.SN, new Periode(MINUS_DAYS_10, MINUS_DAYS_5));
    }

    private void buildRegelBGPStatusATFL(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode regelBGP, long andelNr) {
        final no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus regelBGPStatus = buildRegelBGPeriode(regelBGP, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL, new Periode(MINUS_YEARS_2, MINUS_YEARS_1));
        final BeregningsgrunnlagPrArbeidsforhold regelArbeidsforhold42 = BeregningsgrunnlagPrArbeidsforhold.builder()
                .medArbeidsforhold(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR))
                .medInntektskategori(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.ARBEIDSTAKER)
                .medAndelNr(andelNr++)
                .medFordeltPrÅr(BigDecimal.valueOf(123.123))
                .medBeregnetPrÅr(BigDecimal.valueOf(123.123))
                .medMaksimalRefusjonPrÅr(BigDecimal.valueOf(123.123))
                .medNaturalytelseBortfaltPrÅr(BigDecimal.valueOf(87.87))
                .medAvkortetPrÅr(BigDecimal.valueOf(57.57))
                .medRedusertPrÅr(BigDecimal.valueOf(89.89))
                .medRedusertRefusjonPrÅr(BigDecimal.valueOf(2600.0), BigDecimal.valueOf(260))
                .build();

        final BeregningsgrunnlagPrArbeidsforhold regelArbeidsforhold66 = BeregningsgrunnlagPrArbeidsforhold.builder()
                .medArbeidsforhold(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold.frilansArbeidsforhold())
                .medInntektskategori(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.FRILANSER)
                .medAndelNr(andelNr)
                .medFordeltPrÅr(BigDecimal.valueOf(456.456))
                .medBeregnetPrÅr(BigDecimal.valueOf(456.456))
                .medNaturalytelseBortfaltPrÅr(BigDecimal.valueOf(45.45))
                .medAvkortetPrÅr(BigDecimal.valueOf(34.34))
                .medRedusertPrÅr(BigDecimal.valueOf(65.65))
                .medRedusertRefusjonPrÅr(BigDecimal.valueOf(1300.0), BigDecimal.valueOf(260))
                .build();
        BeregningsgrunnlagPrStatus.builder(regelBGPStatus)
                .medArbeidsforhold(regelArbeidsforhold42)
                .medArbeidsforhold(regelArbeidsforhold66)
                .build();
    }

    private String toJson(AktivitetStatusModell grunnlag) {
        try {
            return JsonMapper.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(grunnlag);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Jackson parsing", e);
        }

    }

    private AktivitetStatusModell lagRegelModell(LocalDate skjæringstidspunkt, Arbeidsforhold arbeidsforhold) {
        AktivitetStatusModell regelmodell = new AktivitetStatusModell();
        regelmodell.setSkjæringstidspunktForBeregning(skjæringstidspunkt);
        regelmodell.setSkjæringstidspunktForOpptjening(skjæringstidspunkt);
        regelmodell.leggTilAktivitetStatus(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL);
        var bgPrStatus = new no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.BeregningsgrunnlagPrStatus(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL, arbeidsforhold);
        regelmodell.leggTilBeregningsgrunnlagPrStatus(bgPrStatus);
        return regelmodell;
    }


    private AktivitetStatusModell lagRegelModellSN(LocalDate skjæringstidspunkt) {
        AktivitetStatusModell regelmodell = new AktivitetStatusModell();
        regelmodell.setSkjæringstidspunktForBeregning(skjæringstidspunkt);
        regelmodell.setSkjæringstidspunktForOpptjening(skjæringstidspunkt);
        regelmodell.leggTilAktivitetStatus(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.SN);
        no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.BeregningsgrunnlagPrStatus bgPrStatus = new no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.BeregningsgrunnlagPrStatus(
                no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.SN);
        regelmodell.leggTilBeregningsgrunnlagPrStatus(bgPrStatus);
        return regelmodell;
    }
}
