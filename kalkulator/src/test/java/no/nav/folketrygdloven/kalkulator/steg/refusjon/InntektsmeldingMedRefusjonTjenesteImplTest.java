package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.felles.frist.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
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
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonsperiodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

class InntektsmeldingMedRefusjonTjenesteImplTest {
    private static final String ORGNR = "974760673";
    private static final String ORGNR2 = "915933149";

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);

    @Test
    void skal_finne_arbeidsgiver_som_har_søkt_for_sent_med_flere_arbeidsforhold_et_som_tilkommer_etter_skjæringstidspunktet() {
        // Arrange
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        var im1 = byggIM(arbeidsgiver, 1000, SKJÆRINGSTIDSPUNKT, 1000);
        var im2 = byggIM(arbeidsgiver, 1000, SKJÆRINGSTIDSPUNKT.plusMonths(1), 1000);
        var aktivitetAggregat = leggTilAktivitet(registerBuilder, ORGNR, List.of(im1.getArbeidsforholdRef()));
        var grunnlag = byggGrunnlag(aktivitetAggregat, List.of(arbeidsgiver));
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(registerBuilder)
                .medInntektsmeldinger(List.of(im1, im2)).build();
        var kravperioder = lagKravperioder(Map.of(arbeidsgiver, List.of(im1, im2)), Map.of(arbeidsgiver, SKJÆRINGSTIDSPUNKT.plusMonths(4)), SKJÆRINGSTIDSPUNKT);

        // Act
        var arbeidsgivereSomHarSøktForSent = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
                koblingReferanse,
                iayGrunnlag,
                grunnlag.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER),
                kravperioder,
                FagsakYtelseType.FORELDREPENGER);

        // Assert
        assertThat(arbeidsgivereSomHarSøktForSent).hasSize(1);
        assertThat(arbeidsgivereSomHarSøktForSent.iterator().next()).isEqualTo(arbeidsgiver);
    }

    @Test
    void skal_finne_arbeidsgivere_som_har_søkt_for_sent() {
        // Arrange
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktivitetAggregat = leggTilAktivitet(registerBuilder, List.of(ORGNR, ORGNR2));
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);
        var grunnlag = byggGrunnlag(aktivitetAggregat, List.of(arbeidsgiver, arbeidsgiver2));
        var im1 = byggIM(arbeidsgiver, 10, SKJÆRINGSTIDSPUNKT, 10);
        var im2 = byggIM(arbeidsgiver2, 10, SKJÆRINGSTIDSPUNKT, 10);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(registerBuilder)
                .medInntektsmeldinger(List.of(im1, im2)).build();
        var kravperioder = lagKravperioder(Map.of(arbeidsgiver, List.of(im1), arbeidsgiver2, List.of(im2)), Map.of(arbeidsgiver, SKJÆRINGSTIDSPUNKT.plusMonths(4), arbeidsgiver2, SKJÆRINGSTIDSPUNKT.plusMonths(2)), SKJÆRINGSTIDSPUNKT);

        // Act
        var arbeidsgivereSomHarSøktForSent = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
                koblingReferanse,
                iayGrunnlag,
                grunnlag.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER),
                kravperioder,
                FagsakYtelseType.FORELDREPENGER);

        // Assert
        assertThat(arbeidsgivereSomHarSøktForSent).hasSize(1);
        assertThat(arbeidsgivereSomHarSøktForSent.iterator().next()).isEqualTo(arbeidsgiver);
    }


    @Test
    void skal_returnere_tomt_set_om_ingen_inntektsmeldinger_er_mottatt() {
        // Arrange
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktivitetAggregat = leggTilAktivitet(registerBuilder, List.of(ORGNR, ORGNR2));
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);
        var grunnlag = byggGrunnlag(aktivitetAggregat, List.of(arbeidsgiver, arbeidsgiver2));
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(registerBuilder)
                .medInntektsmeldinger(List.of()).build();
        var kravperioder = lagKravperioder(Map.of(arbeidsgiver, List.of(), arbeidsgiver2, List.of()), Map.of(), SKJÆRINGSTIDSPUNKT);

        // Act
        var arbeidsgivereSomHarSøktForSent = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
                koblingReferanse,
                iayGrunnlag,
                grunnlag.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER),
                kravperioder,
                FagsakYtelseType.FORELDREPENGER);

        // Assert
        assertThat(arbeidsgivereSomHarSøktForSent).isEmpty();
    }

    @Test
    void skal_returnere_tomt_set_om_ingen_inntektsmeldinger_er_mottatt_for_sent() {
        // Arrange
        Map<Arbeidsgiver, LocalDate> førsteInnsendingMap = new HashMap<>();
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktivitetAggregat = leggTilAktivitet(registerBuilder, List.of(ORGNR, ORGNR2));
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        var arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);
        førsteInnsendingMap.put(arbeidsgiver, SKJÆRINGSTIDSPUNKT.plusMonths(1));
        var im1 = byggIM(arbeidsgiver, 10, SKJÆRINGSTIDSPUNKT, 10);
        var im2 = byggIM(arbeidsgiver2, 10, SKJÆRINGSTIDSPUNKT, 10);
        førsteInnsendingMap.put(arbeidsgiver2, SKJÆRINGSTIDSPUNKT.plusMonths(2));
        var grunnlag = byggGrunnlag(aktivitetAggregat, List.of(arbeidsgiver, arbeidsgiver2));
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(registerBuilder)
                .medInntektsmeldinger(List.of(im1, im2)).build();
        var kravperioder = lagKravperioder(Map.of(arbeidsgiver, List.of(im1), arbeidsgiver2, List.of(im2)), Map.of(arbeidsgiver, SKJÆRINGSTIDSPUNKT.plusMonths(1), arbeidsgiver2, SKJÆRINGSTIDSPUNKT.plusMonths(2)), SKJÆRINGSTIDSPUNKT);

        // Act
        var arbeidsgivereSomHarSøktForSent = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
                koblingReferanse,
                iayGrunnlag,
                grunnlag.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER),
                kravperioder,
                FagsakYtelseType.FORELDREPENGER);

        // Assert
        assertThat(arbeidsgivereSomHarSøktForSent).isEmpty();
    }

    @Test
    void skal_bruke_tidligere_im_selv_om_den_har_ulik_id() {
        // Arrange
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktivitetAggregat = leggTilAktivitet(registerBuilder, List.of(ORGNR));
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        var im1 = byggIM(arbeidsgiver, 10, SKJÆRINGSTIDSPUNKT, 10);
        var im2 = byggIM(arbeidsgiver, 10, SKJÆRINGSTIDSPUNKT, 10);
        var grunnlag = byggGrunnlag(aktivitetAggregat, List.of(arbeidsgiver, arbeidsgiver));
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(registerBuilder)
            .medInntektsmeldinger(List.of(im1, im2)).build();
        var kravperioder = lagKravperioder(Map.of(arbeidsgiver, List.of(im1, im2)), Map.of(arbeidsgiver, SKJÆRINGSTIDSPUNKT.minusMonths(3)), SKJÆRINGSTIDSPUNKT);

        // Act
        var arbeidsgivereSomHarSøktForSent = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
            koblingReferanse,
            iayGrunnlag,
            grunnlag.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER),
            kravperioder,
            FagsakYtelseType.FORELDREPENGER);

        // Assert
        assertThat(arbeidsgivereSomHarSøktForSent).isEmpty();
    }

    private BeregningsgrunnlagGrunnlagDtoBuilder byggGrunnlag(BeregningAktivitetAggregatDto aktivitetAggregat, List<Arbeidsgiver> arbeidsgivere) {
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(aktivitetAggregat)
                .medBeregningsgrunnlag(lagBeregningsgrunnlag(arbeidsgivere));
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(List<Arbeidsgiver> ags) {

        var bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)).build();
        var periode = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
                .build(bg);
        ags.forEach(ag -> BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ag))
                .build(periode)
        );
        return bg;
    }


    private BeregningAktivitetAggregatDto leggTilAktivitet(InntektArbeidYtelseAggregatBuilder iayAggregatBuilder, String orgnr, List<InternArbeidsforholdRefDto> internArbeidsforholdRefDto) {
        var arbeidsperiode1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        var aktivitetAggregatBuilder = BeregningAktivitetAggregatDto.builder()
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT);
        for (var ref : internArbeidsforholdRefDto) {
            var ag = leggTilYrkesaktivitet(arbeidsperiode1, aktørArbeidBuilder, orgnr, ref);
            aktivitetAggregatBuilder.leggTilAktivitet(lagAktivitet(arbeidsperiode1, ag, ref));
        }
        iayAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        return aktivitetAggregatBuilder.build();
    }

    private BeregningAktivitetAggregatDto leggTilAktivitet(InntektArbeidYtelseAggregatBuilder iayAggregatBuilder, List<String> orgnr) {
        var arbeidsperiode1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        var aktivitetAggregatBuilder = BeregningAktivitetAggregatDto.builder()
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT);
        for (var nr : orgnr) {
            var ref = InternArbeidsforholdRefDto.nullRef();
            var ag = leggTilYrkesaktivitet(arbeidsperiode1, aktørArbeidBuilder, nr, ref);
            aktivitetAggregatBuilder.leggTilAktivitet(lagAktivitet(arbeidsperiode1, ag, ref));
        }
        iayAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        return aktivitetAggregatBuilder.build();
    }

    private BeregningAktivitetDto lagAktivitet(Intervall arbeidsperiode1, Arbeidsgiver ag, InternArbeidsforholdRefDto ref) {
        return BeregningAktivitetDto.builder()
                .medArbeidsgiver(ag)
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                .medArbeidsforholdRef(ref)
                .medPeriode(Intervall.fraOgMedTilOgMed(arbeidsperiode1.getFomDato(), arbeidsperiode1.getTomDato())).build();
    }

    private Arbeidsgiver leggTilYrkesaktivitet(Intervall arbeidsperiode, InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder, String orgnr, InternArbeidsforholdRefDto arbeidsforholdId) {
        var arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        var aaBuilder1 = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(arbeidsperiode);
        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdId(arbeidsforholdId)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(aaBuilder1);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yaBuilder);
        return arbeidsgiver;
    }



    private List<KravperioderPrArbeidsforholdDto> lagKravperioder(Map<Arbeidsgiver, List<InntektsmeldingDto>> agMap, Map<Arbeidsgiver, LocalDate> førsteInnsendingMap, LocalDate stp) {
        return agMap.entrySet().stream()
            .map(entry -> {
                var refusjonPerioder = entry.getValue()
                    .stream()
                    .map(im -> lagPerioderForKrav(im, førsteInnsendingMap.get(entry.getKey()), stp))
                    .toList();
                var perioder = refusjonPerioder.stream().map(PerioderForKravDto::getPerioder).flatMap(List::stream).toList();
                return new KravperioderPrArbeidsforholdDto(entry.getKey(), refusjonPerioder, perioder.stream().map(RefusjonsperiodeDto::periode).collect(
                    Collectors.toList()));
            }).toList();
    }

    private static PerioderForKravDto lagPerioderForKrav(InntektsmeldingDto im, LocalDate innsendingsdato, LocalDate skjæringstidspunktBeregning) {
        var perioderForKravDto = new PerioderForKravDto(innsendingsdato, lagPerioder(im, skjæringstidspunktBeregning));
        return perioderForKravDto;
    }

    private static List<RefusjonsperiodeDto> lagPerioder(InntektsmeldingDto im, LocalDate skjæringstidspunktBeregning) {
        var alleSegmenter = new ArrayList<LocalDateSegment<Beløp>>();
        if (!(im.getRefusjonBeløpPerMnd() == null || im.getRefusjonBeløpPerMnd().erNullEller0())) {
            alleSegmenter.add(new LocalDateSegment<>(skjæringstidspunktBeregning,
                TIDENES_ENDE, im.getRefusjonBeløpPerMnd()));
        }

        alleSegmenter.addAll(im.getEndringerRefusjon().stream().map(e ->
            new LocalDateSegment<>(e.getFom(), TIDENES_ENDE, e.getRefusjonsbeløp())
        ).collect(Collectors.toList()));

        var refusjonTidslinje = new LocalDateTimeline<>(alleSegmenter, (interval, lhs, rhs) -> {
            if (lhs.getFom().isBefore(rhs.getFom())) {
                return new LocalDateSegment<>(interval, rhs.getValue());
            }
            return new LocalDateSegment<>(interval, lhs.getValue());
        });

        return refusjonTidslinje.stream()
            .map(r -> new RefusjonsperiodeDto(Intervall.fraOgMedTilOgMed(r.getFom(), r.getTom()), r.getValue()))
            .collect(Collectors.toList());

    }

    private InntektsmeldingDto byggIM(Arbeidsgiver ag, Integer inntekt, LocalDate startdato, Integer refusjon) {
        return InntektsmeldingDtoBuilder.builder()
            .medRefusjon(Beløp.fra(refusjon))
            .medBeløp(Beløp.fra(inntekt))
            .medStartDatoPermisjon(startdato)
            .medArbeidsgiver(ag)
            .medArbeidsforholdId(InternArbeidsforholdRefDto.nyRef())
            .build();
    }
}
