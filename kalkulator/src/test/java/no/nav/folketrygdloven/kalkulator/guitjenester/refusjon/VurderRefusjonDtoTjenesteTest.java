package no.nav.folketrygdloven.kalkulator.guitjenester.refusjon;

import static no.nav.folketrygdloven.kalkulator.OpprettKravPerioderFraInntektsmeldingerForTest.opprett;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.avklaringsbehov.AvklaringsbehovDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
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
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.RefusjonskravForSentDto;

class VurderRefusjonDtoTjenesteTest {

    private static final String ORGNR1 = "974760673";
    private static final String ORGNR2 = "915933149";
    private static final Arbeidsgiver ARBEIDSGIVER1 = Arbeidsgiver.virksomhet(ORGNR1);
    private static final Arbeidsgiver ARBEIDSGIVER2 = Arbeidsgiver.virksomhet(ORGNR2);
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final LocalDate SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID = LocalDate.now().minusMonths(2);
    private static final Beløp REFUSJONSKRAV_BELØP = Beløp.fra(33333);

    @Test
    void skal_gi_liste_over_arbeidsgivere_som_har_søkt_refusjon_for_sent() {
        var forSentInnsendtRefusjon = SKJÆRINGSTIDSPUNKT.plusMonths(4);
        var tidsnokInnsendtRefusjon = SKJÆRINGSTIDSPUNKT.plusMonths(2);
        Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap = new HashMap<>();
        førsteInnsendingAvRefusjonMap.put(ARBEIDSGIVER1, forSentInnsendtRefusjon);
        førsteInnsendingAvRefusjonMap.put(ARBEIDSGIVER2, tidsnokInnsendtRefusjon);
        var input = lagInputMedBeregningsgrunnlagOgIAY(førsteInnsendingAvRefusjonMap);

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getRefusjonskravForSentListe())
            .hasSize(1)
            .extracting(RefusjonskravForSentDto::getArbeidsgiverIdent)
            .contains(ARBEIDSGIVER1.getIdentifikator())
            .doesNotContain(ARBEIDSGIVER2.getIdentifikator());
    }

    @Test
    void skal_gi_liste_over_andeler_som_har_tilkommet_refusjon() {
        Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap = new HashMap<>();
        førsteInnsendingAvRefusjonMap.put(ARBEIDSGIVER1, SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID);
        var input = lagInputMedBeregningsgrunnlagOgIAYOgForrigeGrunnlag(førsteInnsendingAvRefusjonMap);

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getRefusjonskravForSentListe())
            .isEmpty();

        var andeler = resultat.get().getAndeler().stream().toList();
        assertThat(andeler).hasSize(1);

        var andel = andeler.getFirst();
        assertThat(andel.getArbeidsgiver().getArbeidsgiverOrgnr()).isEqualTo(ARBEIDSGIVER1.getIdentifikator());
        assertThat(andel.getTidligereUtbetalinger()).hasSize(1);
        assertThat(andel.getTidligereUtbetalinger().getFirst().getFom()).isEqualTo(SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID);
        assertThat(andel.getTidligereUtbetalinger().getFirst().getTom()).isEqualTo(TIDENES_ENDE);
        assertThat(andel.getMaksTillattDelvisRefusjonPrMnd().verdi()).isEqualTo(REFUSJONSKRAV_BELØP.verdi());
        assertThat(andel.getNyttRefusjonskravFom()).isEqualTo(SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID);
    }

    @Test
    void skal_gi_liste_over_andeler_som_har_tilkommet_refusjon_og_tidligere_overstyring() {
        Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap = new HashMap<>();
        førsteInnsendingAvRefusjonMap.put(ARBEIDSGIVER1, SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID);
        var input = lagInputMedBeregningsgrunnlagOgIAYOgForrigeGrunnlag(førsteInnsendingAvRefusjonMap, Beløp.fra(500000), Beløp.fra(500000));

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getRefusjonskravForSentListe())
            .isEmpty();

        var andeler = resultat.get().getAndeler().stream().toList();
        assertThat(andeler).hasSize(1);

        var andel = andeler.getFirst();
        assertThat(andel.getArbeidsgiver().getArbeidsgiverOrgnr()).isEqualTo(ARBEIDSGIVER1.getIdentifikator());
        assertThat(andel.getMaksTillattDelvisRefusjonPrMnd().verdi()).isEqualTo(Beløp.fra(41667).verdi());
        assertThat(andel.getTidligsteMuligeRefusjonsdato()).isEqualTo(SKJÆRINGSTIDSPUNKT.plusMonths(3));
        assertThat(andel.getFastsattNyttRefusjonskravFom()).isEqualTo(SKJÆRINGSTIDSPUNKT.plusMonths(4));
        assertThat(andel.getSkalKunneFastsetteDelvisRefusjon()).isFalse();
    }

    private static BeregningsgrunnlagGUIInput lagInputMedBeregningsgrunnlagOgIAY(Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap) {
        var arbeidsgivere = førsteInnsendingAvRefusjonMap.keySet();
        var iayBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktivitetAggregat = byggBeregningAktivitetAggregat(iayBuilder, arbeidsgivere);
        var imForOrgnr1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR1, SKJÆRINGSTIDSPUNKT, Beløp.fra(10), Beløp.fra(10));
        var imForOrgnr2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR2, SKJÆRINGSTIDSPUNKT, Beløp.fra(10), Beløp.fra(10));
        var iayGrunnlag = byggIayGrunnlagMedInntektsmeldinger(iayBuilder, List.of(imForOrgnr1, imForOrgnr2));
        var koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, iayGrunnlag,
            opprett(iayGrunnlag, koblingReferanse.getSkjæringstidspunktBeregning(), førsteInnsendingAvRefusjonMap), null);
        var avklaringsbehov = new AvklaringsbehovDto(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV, null, null, null, null, null);

        //beregningsgrunnlag
        var beregningsgrunnlag = lagBeregningsgrunnlag(null, SKJÆRINGSTIDSPUNKT);
        var bgPeriode = lagBgPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT);
        int andelsnr = 1;
        for (Arbeidsgiver arbeidsgiver : arbeidsgivere) {
            lagAndel(bgPeriode, arbeidsgiver, andelsnr++, 0,0, Beløp.ZERO);
        }
        BeregningsgrunnlagPeriodeDto.oppdater(bgPeriode).build();

        input.leggTilToggle("refusjonsfrist.flytting", true);
        return input.medBeregningsgrunnlagGrunnlag(lagBeregningsgrunnlagGrunnlag(aktivitetAggregat, beregningsgrunnlag))
            .medAvklaringsbehov(List.of(avklaringsbehov));
    }

    private static BeregningsgrunnlagGUIInput lagInputMedBeregningsgrunnlagOgIAYOgForrigeGrunnlag(Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap) {
        return lagInputMedBeregningsgrunnlagOgIAYOgForrigeGrunnlag(førsteInnsendingAvRefusjonMap, Beløp.fra(500000), Beløp.fra(400000));
    }

    private static BeregningsgrunnlagGUIInput lagInputMedBeregningsgrunnlagOgIAYOgForrigeGrunnlag(Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap, Beløp refusjonskrav1, Beløp refusjonskrav2) {
        var arbeidsgivere = førsteInnsendingAvRefusjonMap.keySet();
        var iayBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktivitetAggregat = byggBeregningAktivitetAggregat(iayBuilder, arbeidsgivere);
        var imForOrgnr1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR1, SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID, REFUSJONSKRAV_BELØP, Beløp.fra(41666));
        var iayGrunnlag = byggIayGrunnlagMedInntektsmeldinger(iayBuilder, List.of(imForOrgnr1));
        var koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, null);
        var avklaringsbehov = new AvklaringsbehovDto(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV, null, null, null, null, null);

        //beregningsgrunnlag
        var beregningsgrunnlag = lagBeregningsgrunnlag(Beløp.fra(100000), SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID);
        var bgPeriode = lagBgPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID);
        lagAndel(bgPeriode, ARBEIDSGIVER1, 1, 0, 0, refusjonskrav1);
        BeregningsgrunnlagPeriodeDto.oppdater(bgPeriode).build();

        var beregningsgrunnlagGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .medRegisterAktiviteter(aktivitetAggregat)
            .medRefusjonOverstyring(BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(new BeregningRefusjonOverstyringDto(ARBEIDSGIVER1, SKJÆRINGSTIDSPUNKT.plusMonths(3), List.of(lagRefusjonPeriodeDto()), false)).build())
            .build(BeregningsgrunnlagTilstand.VURDERT_TILKOMMET_INNTEKT_UT);

        //forrige beregningsgrunnlag
        var forrigeBeregningsgrunnlag = lagBeregningsgrunnlag(null, SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID);
        var bgPeriodeForrigeGrunnlag = lagBgPeriode(forrigeBeregningsgrunnlag, SKJÆRINGSTIDSPUNKT_TILBAKE_I_TID);
        lagAndel(bgPeriodeForrigeGrunnlag, ARBEIDSGIVER1, 1,0, 500000, refusjonskrav2);
        BeregningsgrunnlagPeriodeDto.oppdater(bgPeriodeForrigeGrunnlag).build();

        var forrigeBeregningsgrunnlagGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(forrigeBeregningsgrunnlag)
            .medRegisterAktiviteter(aktivitetAggregat)
            .build(BeregningsgrunnlagTilstand.VURDERT_TILKOMMET_INNTEKT_UT);

        return new BeregningsgrunnlagGUIInput(koblingReferanse, iayGrunnlag,
            opprett(iayGrunnlag, koblingReferanse.getSkjæringstidspunktBeregning(), førsteInnsendingAvRefusjonMap), ytelsespesifiktGrunnlag)
            .medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag)
            .medBeregningsgrunnlagGrunnlagFraForrigeBehandling(forrigeBeregningsgrunnlagGrunnlag)
            .medAvklaringsbehov(List.of(avklaringsbehov));
    }

    private static BeregningRefusjonPeriodeDto lagRefusjonPeriodeDto() {
        return new BeregningRefusjonPeriodeDto(null, SKJÆRINGSTIDSPUNKT.plusMonths(4));
    }

    private static void lagAndel(BeregningsgrunnlagPeriodeDto bgPeriode, Arbeidsgiver arbeidsgiver, int andelsnr,  int redusertBruker, int redusertAG, Beløp beløp) {
        var bga = BGAndelArbeidsforholdDto
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medRefusjon(new Refusjon(beløp, null, null, null, null, null))
            .medArbeidsgiver(arbeidsgiver);

        var statusOgAndelDtoBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAndelsnr((long)andelsnr)
            .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER)
            .medBeregningsperiode(bgPeriode.getPeriode().getFomDato(), bgPeriode.getPeriode().getTomDato())
            .medFordeltPrÅr(Beløp.fra(500000));
        if (redusertBruker != 0)  {
           statusOgAndelDtoBuilder.medRedusertBrukersAndelPrÅr(Beløp.fra(redusertBruker));
        }
        if (redusertAG != 0)  {
            statusOgAndelDtoBuilder.medRedusertBrukersAndelPrÅr(Beløp.fra(redusertAG));
        }
        statusOgAndelDtoBuilder.build(bgPeriode);
    }

    private static BeregningsgrunnlagPeriodeDto lagBgPeriode(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate fraDato) {
        return BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(fraDato, null).build(beregningsgrunnlag);
    }

    private static BeregningsgrunnlagDto lagBeregningsgrunnlag(Beløp grunnbeløp, LocalDate skjæringstidspunkt) {
        var beregningsgrunnlagDtoBuilder = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER));
        if (grunnbeløp != null) {
            beregningsgrunnlagDtoBuilder.medGrunnbeløp(grunnbeløp);
        }
        return beregningsgrunnlagDtoBuilder.build();
    }

    private static BeregningAktivitetAggregatDto byggBeregningAktivitetAggregat(InntektArbeidYtelseAggregatBuilder iayAggregatBuilder, Set<Arbeidsgiver> arbeidsgivere) {
        var arbeidsperiode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        var aktivitetAggregatBuilder = BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT);
        for (var arbeidsgiver : arbeidsgivere) {
            leggTilYrkesaktivitet(aktørArbeidBuilder, arbeidsperiode, arbeidsgiver);
            byggBeregningAktivitetAggregat(aktivitetAggregatBuilder, arbeidsperiode, arbeidsgiver);
        }
        iayAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        return aktivitetAggregatBuilder.build();
    }

    private static void byggBeregningAktivitetAggregat(BeregningAktivitetAggregatDto.Builder aktivitetAggregatBuilder,
                                                       Intervall arbeidsperiode,
                                                       Arbeidsgiver arbeidsgiver) {
        var beregningAktivitet = BeregningAktivitetDto.builder()
            .medArbeidsgiver(arbeidsgiver)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medPeriode(Intervall.fraOgMedTilOgMed(arbeidsperiode.getFomDato(), arbeidsperiode.getTomDato()))
            .build();
        aktivitetAggregatBuilder.leggTilAktivitet(beregningAktivitet);
    }

    private static void leggTilYrkesaktivitet(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder,
                                              Intervall arbeidsperiode,
                                              Arbeidsgiver arbeidsgiver) {
        var aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny().medPeriode(arbeidsperiode);
        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aktivitetsAvtaleDtoBuilder);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yaBuilder);
    }

    private static BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlagGrunnlag(BeregningAktivitetAggregatDto aktivitetAggregat,
                                                                               BeregningsgrunnlagDto beregningsgrunnlagDto) {

        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(aktivitetAggregat)
            .medBeregningsgrunnlag(beregningsgrunnlagDto)
            .build(BeregningsgrunnlagTilstand.VURDERT_TILKOMMET_INNTEKT_UT);
    }

    private static InntektArbeidYtelseGrunnlagDto byggIayGrunnlagMedInntektsmeldinger(InntektArbeidYtelseAggregatBuilder iayBuilder, List<InntektsmeldingDto> inntektsmeldinger) {
        return InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(iayBuilder)
            .medInntektsmeldinger(inntektsmeldinger)
            .build();
    }
}


