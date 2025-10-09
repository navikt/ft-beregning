package no.nav.folketrygdloven.kalkulator.guitjenester.refusjon;

import static no.nav.folketrygdloven.kalkulator.OpprettKravPerioderFraInntektsmeldingerForTest.opprett;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.avklaringsbehov.AvklaringsbehovDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;

import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.RefusjonskravSomKommerForSentDto;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

class VurderRefusjonDtoTjenesteTest {

    private static final String ORGNR1 = "974760673";
    private static final String ORGNR2 = "915933149";
    private static final Arbeidsgiver ARBEIDSGIVER1 = Arbeidsgiver.virksomhet(ORGNR1);
    private static final Arbeidsgiver ARBEIDSGIVER2 = Arbeidsgiver.virksomhet(ORGNR2);
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

    /* TODO: Flere tester
        - Både tilkommet refusjon og refusjonsfrist
        - Både tilkommet refusjon (overstyring) og refusjonsfrist
        - Bare tilkommet refusjon
        - Bare refusjonsfrist
     */

    @Test
    void skal_gi_liste_over_arbeidsgivere_som_har_søkt_refusjon_for_sent() {
        var forSentInnsendtRefusjon = SKJÆRINGSTIDSPUNKT.plusMonths(4);
        var tidsnokInnsendtRefusjon = SKJÆRINGSTIDSPUNKT.plusMonths(2);
        Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap = new HashMap<>();
        førsteInnsendingAvRefusjonMap.put(ARBEIDSGIVER1, forSentInnsendtRefusjon);
        førsteInnsendingAvRefusjonMap.put(ARBEIDSGIVER2, tidsnokInnsendtRefusjon);
        var input = lagInputMedBeregningsgrunnlagOgIAY(førsteInnsendingAvRefusjonMap);
        input.leggTilToggle("refusjonsfrist.flytting", true);

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getRefusjonskravSomKomForSentListe())
            .hasSize(1)
            .extracting(RefusjonskravSomKommerForSentDto::getArbeidsgiverIdent)
            .contains(ARBEIDSGIVER1.getIdentifikator())
            .doesNotContain(ARBEIDSGIVER2.getIdentifikator());
    }

    @Test
    void skal_gi_liste_over_andeler_som_har_tilkommet_refusjon() {
        Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap = new HashMap<>();
        førsteInnsendingAvRefusjonMap.put(ARBEIDSGIVER1, SKJÆRINGSTIDSPUNKT);
        var input = lagInputMedBeregningsgrunnlagOgIAYOgForrigeGrunnlag(førsteInnsendingAvRefusjonMap);
        input.leggTilToggle("refusjonsfrist.flytting", true);

        var resultat = VurderRefusjonDtoTjeneste.lagRefusjonTilVurderingDto(input);
    }

    private static BeregningsgrunnlagGUIInput lagInputMedBeregningsgrunnlagOgIAY(Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap) {
        var arbeidsgivere = førsteInnsendingAvRefusjonMap.keySet();
        var iayBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktivitetAggregat = byggBeregningAktivitetAggregat(iayBuilder, arbeidsgivere);
        var iayGrunnlag = byggIayGrunnlagMedInntektsmeldinger(iayBuilder);
        var koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, iayGrunnlag,
            opprett(iayGrunnlag, koblingReferanse.getSkjæringstidspunktBeregning(), førsteInnsendingAvRefusjonMap), null);

        var avklaringsbehov = new AvklaringsbehovDto(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV, null, null, null, null, null);
        return input.medBeregningsgrunnlagGrunnlag(lagBeregningsgrunnlagGrunnlag(aktivitetAggregat, arbeidsgivere, null))
            .medAvklaringsbehov(List.of(avklaringsbehov));
    }

    private static BeregningsgrunnlagGUIInput lagInputMedBeregningsgrunnlagOgIAYOgForrigeGrunnlag(Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap) {
        var arbeidsgivere = førsteInnsendingAvRefusjonMap.keySet();
        var iayBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktivitetAggregat = byggBeregningAktivitetAggregat(iayBuilder, arbeidsgivere);
        var iayGrunnlag = byggIayGrunnlagMedInntektsmeldinger(iayBuilder);
        var koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, null);
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, iayGrunnlag,
            opprett(iayGrunnlag, koblingReferanse.getSkjæringstidspunktBeregning(), førsteInnsendingAvRefusjonMap), ytelsespesifiktGrunnlag);
        var avklaringsbehov = new AvklaringsbehovDto(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV, null, null, null, null, null);
        return input
            .medBeregningsgrunnlagGrunnlagFraForrigeBehandling(lagBeregningsgrunnlagGrunnlag(aktivitetAggregat, arbeidsgivere, null))
            .medBeregningsgrunnlagGrunnlag(lagBeregningsgrunnlagGrunnlag(aktivitetAggregat, arbeidsgivere, Beløp.fra(100000)))
            .medAvklaringsbehov(List.of(avklaringsbehov));
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
                                                                               Set<Arbeidsgiver> arbeidsgivere,
                                                                               Beløp grunnbeløp) {

        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(aktivitetAggregat)
            .medBeregningsgrunnlag(lagBeregningsgrunnlag(arbeidsgivere.stream().map(a -> Arbeidsgiver.virksomhet(a.getOrgnr())).toList(), grunnbeløp))
            .build(BeregningsgrunnlagTilstand.VURDERT_TILKOMMET_INNTEKT_UT);
    }

    private static BeregningsgrunnlagDto lagBeregningsgrunnlag(List<Arbeidsgiver> arbeidsgivere, Beløp grunnbeløp) {
        // Vi trenger å legge til dagsats. Denne utledes trolig via medRedusertRefusjonPrÅr, men vi må finne ut hvordan vi kan få denne på beregningsgrunnlag som returneres
        var beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(grunnbeløp)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .build();
        var periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build();
        arbeidsgivere.forEach(arbeidsgiver -> BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver))
            .medRedusertRefusjonPrÅr(Beløp.fra(700000))
            .build(periode));
        var bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);
        return beregningsgrunnlag;
    }

    private static InntektArbeidYtelseGrunnlagDto byggIayGrunnlagMedInntektsmeldinger(InntektArbeidYtelseAggregatBuilder iayBuilder) {
        var startdatoPermisjon = SKJÆRINGSTIDSPUNKT;
        var imForOrgnr1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR1, startdatoPermisjon, Beløp.fra(10), Beløp.fra(10));
        var imForOrgnr2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR2, startdatoPermisjon, Beløp.fra(10), Beløp.fra(10));
        return InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(iayBuilder)
            .medInntektsmeldinger(List.of(imForOrgnr1, imForOrgnr2))
            .build();
    }
}
