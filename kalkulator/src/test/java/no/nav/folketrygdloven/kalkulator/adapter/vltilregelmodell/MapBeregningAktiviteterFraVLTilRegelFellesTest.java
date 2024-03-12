package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

class MapBeregningAktiviteterFraVLTilRegelFellesTest {
    private static final String ORGNR = "974242931";
    private static final String ORGNR2 = "999999999";
    private static final AktørId aktørId = AktørId.dummy();
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, 1, 1);
    private static final KoblingReferanseMock KOBLING_REFERANSE = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    public static final String ARBEIDSGIVER_ORGNR = "123456789";
    public static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
    public static final InternArbeidsforholdRefDto NULL_REF = InternArbeidsforholdRefDto.nullRef();
    public static final InternArbeidsforholdRefDto ARBEIDSFORHOLD_ID = InternArbeidsforholdRefDto.nyRef();
    private MapBeregningAktiviteterFraVLTilRegelFelles mapper = new MapBeregningAktiviteterFraVLTilRegelFelles();
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);

    @Test
    void skal_mappe_et_arbeidsforhold_med_inntektsmelding_uten_referanse() {
        // Arrange
        LocalDate ansettelsesDato = SKJÆRINGSTIDSPUNKT.minusYears(1);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = lagIAY(ansettelsesDato, NULL_REF, Collections.emptyList());
        var opptjeningAktiviteterDto = lagOpptjeningsAktivitet(ansettelsesDato, NULL_REF);

        // Act
        AktivitetStatusModell aktivitetStatusModell = mapForSkjæringstidspunkt(iayGrunnlagBuilder, opptjeningAktiviteterDto);

        // Assert
        var beregningsModell = aktivitetStatusModell.getAktivePerioder();
        assertThat(beregningsModell).hasSize(1);
        var aktivitet = beregningsModell.get(0);
        assertThat(aktivitet.getArbeidsforhold().getOrgnr()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(aktivitet.getArbeidsforhold().getArbeidsforholdId()).isNull();
    }

    @Test
    void skal_mappe_et_arbeidsforhold_med_inntektsmelding_med_referanse() {
        // Arrange
        LocalDate ansettelsesDato = SKJÆRINGSTIDSPUNKT.minusYears(1);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = lagIAY(ansettelsesDato, ARBEIDSFORHOLD_ID, Collections.emptyList());
        var opptjeningAktiviteterDto = lagOpptjeningsAktivitet(ansettelsesDato, ARBEIDSFORHOLD_ID);

        // Act
        AktivitetStatusModell aktivitetStatusModell = mapForSkjæringstidspunkt(iayGrunnlagBuilder, opptjeningAktiviteterDto);

        // Assert
        var beregningsModell = aktivitetStatusModell.getAktivePerioder();
        assertThat(beregningsModell).hasSize(1);
        var aktivitet = beregningsModell.get(0);
        assertThat(aktivitet.getArbeidsforhold().getOrgnr()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(aktivitet.getArbeidsforhold().getArbeidsforholdId()).isEqualTo(ARBEIDSFORHOLD_ID.getReferanse());
        assertThat(aktivitet.getPeriode().getTom()).isEqualTo(TIDENES_ENDE);
    }

    @Test
    void skal_mappe_et_arbeidsforhold_med_full_permisjon() {
        // Arrange
        LocalDate ansettelsesDato = SKJÆRINGSTIDSPUNKT.minusYears(1);
        var permisjonsPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.plusDays(1));
        List<PermisjonDtoBuilder> permisjonDtoBuilders = List.of(PermisjonDtoBuilder.ny().medPeriode(permisjonsPeriode).medProsentsats(Stillingsprosent.HUNDRED).medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.ANNEN_PERMISJON_LOVFESTET));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = lagIAY(ansettelsesDato, NULL_REF, permisjonDtoBuilders);
        var opptjeningAktiviteterDto = lagOpptjeningsAktivitet(ansettelsesDato, NULL_REF);

        // Act
        AktivitetStatusModell aktivitetStatusModell = mapForSkjæringstidspunkt(iayGrunnlagBuilder, opptjeningAktiviteterDto);

        // Assert
        var beregningsModell = aktivitetStatusModell.getAktivePerioder();
        assertThat(beregningsModell).hasSize(1);
        var aktivitet = beregningsModell.get(0);
        assertThat(aktivitet.getArbeidsforhold().getOrgnr()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(aktivitet.getArbeidsforhold().getArbeidsforholdId()).isNull();
        assertThat(aktivitet.getPeriode().getTom()).isEqualTo(permisjonsPeriode.getFomDato().minusDays(1));
    }

    @Test
    void skal_mappe_et_arbeidsforhold_med_delvis_permisjon() {
        // Arrange
        LocalDate ansettelsesDato = SKJÆRINGSTIDSPUNKT.minusYears(1);
        var permisjonsPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.plusDays(1));
        List<PermisjonDtoBuilder> permisjonDtoBuilders = List.of(PermisjonDtoBuilder.ny().medPeriode(permisjonsPeriode).medProsentsats(Stillingsprosent.fra(60)));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = lagIAY(ansettelsesDato, NULL_REF, permisjonDtoBuilders);
        var opptjeningAktiviteterDto = lagOpptjeningsAktivitet(ansettelsesDato, NULL_REF);

        // Act
        AktivitetStatusModell aktivitetStatusModell = mapForSkjæringstidspunkt(iayGrunnlagBuilder, opptjeningAktiviteterDto);

        // Assert
        var beregningsModell = aktivitetStatusModell.getAktivePerioder();
        assertThat(beregningsModell).hasSize(1);
        var aktivitet = beregningsModell.get(0);
        assertThat(aktivitet.getArbeidsforhold().getOrgnr()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(aktivitet.getArbeidsforhold().getArbeidsforholdId()).isNull();
        assertThat(aktivitet.getPeriode().getTom()).isEqualTo(TIDENES_ENDE);
    }


    private AktivitetStatusModell mapForSkjæringstidspunkt(KoblingReferanse ref, OpptjeningAktiviteterDto opptjeningAktiviteter,
                                                           InntektsmeldingDto inntektsmelding) {
        var alleYA = opptjeningAktiviteter.getOpptjeningPerioder().stream()
                .map(op -> lagYA(op, null, null))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medInntektsmeldinger(inntektsmelding)
                .medData(lagData(alleYA))
                .build();
        var input = new FastsettBeregningsaktiviteterInput(ref, iayGrunnlag, opptjeningAktiviteter, List.of(), null);
        return new MapBeregningAktiviteterFraVLTilRegelFelles().mapForSkjæringstidspunkt(input);
    }

    private AktivitetStatusModell mapForSkjæringstidspunkt(KoblingReferanse ref, OpptjeningAktiviteterDto opptjeningAktiviteter,
                                                           List<InntektsmeldingDto> inntektsmeldinger) {
        var alleYA = opptjeningAktiviteter.getOpptjeningPerioder().stream()
                .map(op -> lagYA(op, null, null))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medInntektsmeldinger(inntektsmeldinger)
                .medData(lagData(alleYA))
                .build();
        var input = new FastsettBeregningsaktiviteterInput(ref, iayGrunnlag, opptjeningAktiviteter, List.of(), null);
        return new MapBeregningAktiviteterFraVLTilRegelFelles().mapForSkjæringstidspunkt(input);
    }

    private AktivitetStatusModell mapForSkjæringstidspunktArbeidIIAY(KoblingReferanse ref, OpptjeningAktiviteterDto opptjeningAktiviteter,
                                                                     List<InntektsmeldingDto> inntektsmeldinger) {
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medInntektsmeldinger(inntektsmeldinger)
                .build();
        var input = new FastsettBeregningsaktiviteterInput(ref, iayGrunnlag, opptjeningAktiviteter, List.of(), null);
        return new MapBeregningAktiviteterFraVLTilRegelFelles().mapForSkjæringstidspunkt(input);
    }

    private InntektArbeidYtelseAggregatBuilder lagData(List<YrkesaktivitetDto> yrkesaktiviteter) {
        var arbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        yrkesaktiviteter.forEach(arbeidBuilder::leggTilYrkesaktivitet);
        return InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER).leggTilAktørArbeid(arbeidBuilder);
    }

    private InntektArbeidYtelseAggregatBuilder lagData(List<YrkesaktivitetDto> yrkesaktiviteter, List<YtelseDtoBuilder> ytelser) {
        var arbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        yrkesaktiviteter.forEach(arbeidBuilder::leggTilYrkesaktivitet);
        var aktørYtelseBuilder = InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty());
        ytelser.forEach(aktørYtelseBuilder::leggTilYtelse);
        return InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER).leggTilAktørArbeid(arbeidBuilder).leggTilAktørYtelse(aktørYtelseBuilder);
    }

    private Optional<YrkesaktivitetDto> lagYA(OpptjeningAktiviteterDto.OpptjeningPeriodeDto opp, PermisjonsbeskrivelseType type, Intervall permisjonperiode) {
        if (opp.getArbeidsgiver().isPresent()) {
            var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty()).medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                    .medArbeidsgiver(opp.getArbeidsgiver().get())
                    .medArbeidsforholdId(opp.getArbeidsforholdId());

            var aktivitetsAvtaleBuilder = yaBuilder.getAktivitetsAvtaleBuilder();
            aktivitetsAvtaleBuilder.medPeriode(opp.getPeriode()).medErAnsettelsesPeriode(true);
            yaBuilder.leggTilAktivitetsAvtale(aktivitetsAvtaleBuilder);

            if (permisjonperiode != null) {
                var permisjonBuilder = PermisjonDtoBuilder.ny()
                        .medPeriode(permisjonperiode)
                        .medProsentsats(Stillingsprosent.HUNDRED)
                        .medPermisjonsbeskrivelseType(type);
                yaBuilder.leggTilPermisjon(permisjonBuilder);
            }
            return Optional.of(yaBuilder.build());
        }
        return Optional.empty();
    }

    @Test
    public void skal_mappe_arbeidsforhold_med_virksomhetarbeidsgiver_fra_opptjening_med_info_i_iay() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusMonths(12);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        Intervall periode = Intervall.fraOgMedTilOgMed(fom, tom);

        var opptjeningAktivitet = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.ARBEID, periode, ARBEIDSGIVER_ORGNR, arbId);

        // Act
        AktivitetStatusModell modell = mapForSkjæringstidspunkt(koblingReferanse, opptjeningAktivitet, List.of());

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(1);
        AktivPeriode aktivPeriode = modell.getAktivePerioder().get(0);
        assertThat(aktivPeriode.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode.getPeriode().getTom()).isEqualTo(tom);
        Arbeidsforhold arbeidsforhold = aktivPeriode.getArbeidsforhold();
        assertThat(arbeidsforhold.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
        assertThat(arbeidsforhold.getReferanseType())
                .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.ORG_NR);
        assertThat(arbeidsforhold.getOrgnr()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(arbeidsforhold.getArbeidsforholdId()).isEqualTo(null);
    }

    @Test
    public void skal_mappe_arbeidsforhold_med_virksomhetarbeidsgiver_fra_opptjening_med_info_i_iay_med_inntektsmelding() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();

        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusMonths(12);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        var periode = Intervall.fraOgMedTilOgMed(fom, tom);

        var opptjeningAktivitet = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.ARBEID, periode, ARBEIDSGIVER_ORGNR, arbId);

        var inntektsmelding = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ARBEIDSGIVER_ORGNR, arbId, SKJÆRINGSTIDSPUNKT);

        // Act
        AktivitetStatusModell modell = mapForSkjæringstidspunkt(koblingReferanse, opptjeningAktivitet, inntektsmelding);

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(1);
        AktivPeriode aktivPeriode = modell.getAktivePerioder().get(0);
        assertThat(aktivPeriode.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode.getPeriode().getTom()).isEqualTo(tom);
        Arbeidsforhold arbeidsforhold = aktivPeriode.getArbeidsforhold();
        assertThat(arbeidsforhold.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
        assertThat(arbeidsforhold.getReferanseType())
                .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.ORG_NR);
        assertThat(arbeidsforhold.getOrgnr()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(arbeidsforhold.getArbeidsforholdId()).isEqualTo(arbId.getReferanse());
    }

    @Test
    public void skal_mappe_arbeidsforhold_med_virksomhetarbeidsgiver_fra_opptjening_uten_info_i_iay() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusMonths(12);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        var periode = Intervall.fraOgMedTilOgMed(fom, tom);

        var opptjeningAktiviteter = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.ARBEID, periode, ORGNR, null);

        // Act
        AktivitetStatusModell modell = mapForSkjæringstidspunkt(koblingReferanse, opptjeningAktiviteter, Collections.emptyList());

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(1);
        AktivPeriode aktivPeriode = modell.getAktivePerioder().get(0);
        assertThat(aktivPeriode.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode.getPeriode().getTom()).isEqualTo(tom);
        Arbeidsforhold arbeidsforhold = aktivPeriode.getArbeidsforhold();
        assertThat(arbeidsforhold.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
        assertThat(arbeidsforhold.getReferanseType())
                .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.ORG_NR);
        assertThat(arbeidsforhold.getOrgnr()).isEqualTo(ORGNR);
        assertThat(arbeidsforhold.getArbeidsforholdId()).isNull();
    }

    @Test
    public void skal_mappe_2_arbeidsforhold_med_permisjon_fra_det_ene() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusMonths(12);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        var periode = Intervall.fraOgMedTilOgMed(fom, tom);

        var opptjeningAktiviteter = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, ORGNR, null, null);
        var opptjeningAktiviteter2 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, ORGNR2, null, null);
        var alleAktiviteter = new OpptjeningAktiviteterDto(opptjeningAktiviteter, opptjeningAktiviteter2);
        var ya1 = lagYA(opptjeningAktiviteter, null, null).orElseThrow();
        var ya2 = lagYA(opptjeningAktiviteter2, PermisjonsbeskrivelseType.ANNEN_PERMISJON_LOVFESTET, Intervall.fraOgMedTilOgMed(periode.getFomDato(), SKJÆRINGSTIDSPUNKT.plusYears(12))).orElseThrow();

        // Act
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medInntektsmeldinger(Collections.emptyList())
                .medData(lagData(Arrays.asList(ya1, ya2)))
                .build();
        var input = new FastsettBeregningsaktiviteterInput(koblingReferanse, iayGrunnlag, alleAktiviteter, List.of(), null);
        AktivitetStatusModell modell = new MapBeregningAktiviteterFraVLTilRegelFelles().mapForSkjæringstidspunkt(input);

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(2);

        AktivPeriode aktivPeriode1 = modell.getAktivePerioder().stream().filter(p -> p.getArbeidsforhold().getOrgnr().equals(ORGNR)).findFirst().orElseThrow();
        assertThat(aktivPeriode1.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode1.getPeriode().getTom()).isEqualTo(tom);
        Arbeidsforhold arbeidsforhold = aktivPeriode1.getArbeidsforhold();
        assertThat(arbeidsforhold.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
        assertThat(arbeidsforhold.getReferanseType())
                .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.ORG_NR);
        assertThat(arbeidsforhold.getOrgnr()).isEqualTo(ORGNR);
        assertThat(arbeidsforhold.getArbeidsforholdId()).isNull();

        AktivPeriode aktivPeriode2 = modell.getAktivePerioder().stream().filter(p -> p.getArbeidsforhold().getOrgnr().equals(ORGNR2)).findFirst().orElseThrow();
        assertThat(aktivPeriode2.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode2.getPeriode().getTom()).isEqualTo(fom);
        Arbeidsforhold arbeidsforhold2 = aktivPeriode2.getArbeidsforhold();
        assertThat(arbeidsforhold2.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
        assertThat(arbeidsforhold2.getReferanseType())
                .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.ORG_NR);
        assertThat(arbeidsforhold2.getOrgnr()).isEqualTo(ORGNR2);
        assertThat(arbeidsforhold2.getArbeidsforholdId()).isNull();
    }

    @Test
    public void skal_mappe_2_arbeidsforhold_med_permisjon_fra_det_ene_i_14_dager() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusMonths(12);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        var periode = Intervall.fraOgMedTilOgMed(fom, tom);

        var opptjeningAktiviteter = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, ORGNR, null, null);
        var opptjeningAktiviteter2 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, ORGNR2, null, null);
        var alleAktiviteter = new OpptjeningAktiviteterDto(opptjeningAktiviteter, opptjeningAktiviteter2);
        var ya1 = lagYA(opptjeningAktiviteter, null, null).orElseThrow();
        var ya2 = lagYA(opptjeningAktiviteter2, PermisjonsbeskrivelseType.ANNEN_PERMISJON_LOVFESTET, Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusDays(15), SKJÆRINGSTIDSPUNKT.minusDays(1))).orElseThrow();

        // Act
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medInntektsmeldinger(Collections.emptyList())
                .medData(lagData(Arrays.asList(ya1, ya2)))
                .build();
        var input = new FastsettBeregningsaktiviteterInput(koblingReferanse, iayGrunnlag, alleAktiviteter, List.of(), null);
        AktivitetStatusModell modell = new MapBeregningAktiviteterFraVLTilRegelFelles().mapForSkjæringstidspunkt(input);

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(2);

        AktivPeriode aktivPeriode1 = modell.getAktivePerioder().stream().filter(p -> p.getArbeidsforhold().getOrgnr().equals(ORGNR)).findFirst().orElseThrow();
        assertThat(aktivPeriode1.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode1.getPeriode().getTom()).isEqualTo(tom);
        Arbeidsforhold arbeidsforhold = aktivPeriode1.getArbeidsforhold();
        assertThat(arbeidsforhold.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
        assertThat(arbeidsforhold.getReferanseType())
                .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.ORG_NR);
        assertThat(arbeidsforhold.getOrgnr()).isEqualTo(ORGNR);
        assertThat(arbeidsforhold.getArbeidsforholdId()).isNull();

        AktivPeriode aktivPeriode2 = modell.getAktivePerioder().stream().filter(p -> p.getArbeidsforhold().getOrgnr().equals(ORGNR2)).findFirst().orElseThrow();
        assertThat(aktivPeriode2.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode2.getPeriode().getTom()).isEqualTo(SKJÆRINGSTIDSPUNKT.minusDays(16));
        Arbeidsforhold arbeidsforhold2 = aktivPeriode2.getArbeidsforhold();
        assertThat(arbeidsforhold2.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
        assertThat(arbeidsforhold2.getReferanseType())
                .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.ORG_NR);
        assertThat(arbeidsforhold2.getOrgnr()).isEqualTo(ORGNR2);
        assertThat(arbeidsforhold2.getArbeidsforholdId()).isNull();
    }


    @Test
    public void skal_mappe_2_arbeidsforhold_med_permisjon_under_14_dager_fra_det_ene() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusMonths(12);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        var periode = Intervall.fraOgMedTilOgMed(fom, tom);

        var opptjeningAktiviteter = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, ORGNR, null, null);
        var opptjeningAktiviteter2 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, ORGNR2, null, null);
        var alleAktiviteter = new OpptjeningAktiviteterDto(opptjeningAktiviteter, opptjeningAktiviteter2);
        var ya1 = lagYA(opptjeningAktiviteter, null, null).orElseThrow();
        var ya2 = lagYA(opptjeningAktiviteter2, PermisjonsbeskrivelseType.VELFERDSPERMISJON, Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusDays(14), SKJÆRINGSTIDSPUNKT.minusDays(1))).orElseThrow();

        // Act
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medInntektsmeldinger(Collections.emptyList())
                .medData(lagData(Arrays.asList(ya1, ya2)))
                .build();
        var input = new FastsettBeregningsaktiviteterInput(koblingReferanse, iayGrunnlag, alleAktiviteter, List.of(), null);
        AktivitetStatusModell modell = new MapBeregningAktiviteterFraVLTilRegelFelles().mapForSkjæringstidspunkt(input);

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(2);

        AktivPeriode aktivPeriode1 = modell.getAktivePerioder().stream().filter(p -> p.getArbeidsforhold().getOrgnr().equals(ORGNR)).findFirst().orElseThrow();
        assertThat(aktivPeriode1.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode1.getPeriode().getTom()).isEqualTo(tom);
        Arbeidsforhold arbeidsforhold = aktivPeriode1.getArbeidsforhold();
        assertThat(arbeidsforhold.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
        assertThat(arbeidsforhold.getReferanseType())
                .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.ORG_NR);
        assertThat(arbeidsforhold.getOrgnr()).isEqualTo(ORGNR);
        assertThat(arbeidsforhold.getArbeidsforholdId()).isNull();

        AktivPeriode aktivPeriode2 = modell.getAktivePerioder().stream().filter(p -> p.getArbeidsforhold().getOrgnr().equals(ORGNR2)).findFirst().orElseThrow();
        assertThat(aktivPeriode2.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode2.getPeriode().getTom()).isEqualTo(tom);
        Arbeidsforhold arbeidsforhold2 = aktivPeriode2.getArbeidsforhold();
        assertThat(arbeidsforhold2.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
        assertThat(arbeidsforhold2.getReferanseType())
                .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.ORG_NR);
        assertThat(arbeidsforhold2.getOrgnr()).isEqualTo(ORGNR2);
        assertThat(arbeidsforhold2.getArbeidsforholdId()).isNull();
    }

    @Test
    public void skal_mappe_2_arbeidsforhold_med_permisjon_som_ikke_er_relevant_fra_det_ene() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusMonths(12);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        var periode = Intervall.fraOgMedTilOgMed(fom, tom);

        var opptjeningAktiviteter = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, ORGNR, null, null);
        var opptjeningAktiviteter2 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, ORGNR2, null, null);
        var opptjeningAktiviteter3 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.FORELDREPENGER, periode, null, null, null);

        var alleAktiviteter = new OpptjeningAktiviteterDto(opptjeningAktiviteter, opptjeningAktiviteter2, opptjeningAktiviteter3);
        var ya1 = lagYA(opptjeningAktiviteter, null, null).orElseThrow();
        var ya2 = lagYA(opptjeningAktiviteter2, PermisjonsbeskrivelseType.PERMISJON_MED_FORELDREPENGER, Intervall.fraOgMedTilOgMed(periode.getFomDato(), SKJÆRINGSTIDSPUNKT.plusYears(12))).orElseThrow();
        var foreldrepengerYtelse = YtelseDtoBuilder.ny().medYtelseType(YtelseType.FORELDREPENGER).medPeriode(periode);

        // Act
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medInntektsmeldinger(Collections.emptyList())
                .medData(lagData(Arrays.asList(ya1, ya2), List.of(foreldrepengerYtelse)))
                .build();
        var input = new FastsettBeregningsaktiviteterInput(koblingReferanse, iayGrunnlag, alleAktiviteter, List.of(), null);
        AktivitetStatusModell modell = new MapBeregningAktiviteterFraVLTilRegelFelles().mapForSkjæringstidspunkt(input);

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(3);

        AktivPeriode aktivPeriode1 = modell.getAktivePerioder().stream().filter(p -> p.getArbeidsforhold().getOrgnr().equals(ORGNR)).findFirst().orElseThrow();
        assertThat(aktivPeriode1.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode1.getPeriode().getTom()).isEqualTo(tom);
        Arbeidsforhold arbeidsforhold = aktivPeriode1.getArbeidsforhold();
        assertThat(arbeidsforhold.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
        assertThat(arbeidsforhold.getReferanseType())
                .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.ORG_NR);
        assertThat(arbeidsforhold.getOrgnr()).isEqualTo(ORGNR);
        assertThat(arbeidsforhold.getArbeidsforholdId()).isNull();

        AktivPeriode aktivPeriode2 = modell.getAktivePerioder().stream().filter(p -> p.getArbeidsforhold().getOrgnr().equals(ORGNR2)).findFirst().orElseThrow();
        assertThat(aktivPeriode2.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode2.getPeriode().getTom()).isEqualTo(tom);
        Arbeidsforhold arbeidsforhold2 = aktivPeriode2.getArbeidsforhold();
        assertThat(arbeidsforhold2.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
        assertThat(arbeidsforhold2.getReferanseType())
                .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.ORG_NR);
        assertThat(arbeidsforhold2.getOrgnr()).isEqualTo(ORGNR2);
        assertThat(arbeidsforhold2.getArbeidsforholdId()).isNull();
    }

    @Test
    public void skal_mappe_arbeidsforhold_fra_samme_arbeidsgiver_med_inntektsmelding_i_iay() {
        // Arrange
        String orgnr = ORGNR;
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        var arbId2 = InternArbeidsforholdRefDto.nyRef();
        var arbId3 = InternArbeidsforholdRefDto.nyRef();

        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusMonths(12);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.minusDays(1);

        var periode = Intervall.fraOgMedTilOgMed(fom, tom);

        var opptj1 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, orgnr, null, arbId1);
        var opptj2 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, orgnr, null, arbId2);
        var opptj3 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, orgnr, null, arbId3);

        var im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr, arbId2, SKJÆRINGSTIDSPUNKT);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr, arbId1, SKJÆRINGSTIDSPUNKT);
        var im3 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr, arbId3, SKJÆRINGSTIDSPUNKT);

        List<InternArbeidsforholdRefDto> arbeidsforholdRef = List.of(arbId1, arbId2, arbId3);

        // Sjekke at vi har riktig antall arbeidsforholdref
        assertThat(arbeidsforholdRef).hasSize(3);

        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(opptj1, opptj2, opptj3));
        // Act
        AktivitetStatusModell modell = mapForSkjæringstidspunkt(koblingReferanse, opptjeningAktiviteter, List.of(im1, im2, im3));

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(3);
        for (InternArbeidsforholdRefDto ref : arbeidsforholdRef) {
            Optional<AktivPeriode> aktivPeriodeOpt = modell.getAktivePerioder().stream()
                    .filter(ap -> Objects.equals(ap.getArbeidsforhold().getArbeidsforholdId(), ref.getReferanse()))
                    .findFirst();
            assertThat(aktivPeriodeOpt.isPresent()).isTrue();
            AktivPeriode aktivPeriode = aktivPeriodeOpt.get();
            assertThat(aktivPeriode.getPeriode().getFom()).isEqualTo(fom);
            assertThat(aktivPeriode.getPeriode().getTom()).isEqualTo(tom);
            Arbeidsforhold arbeidsforhold = aktivPeriode.getArbeidsforhold();
            assertThat(arbeidsforhold.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
            assertThat(arbeidsforhold.getReferanseType())
                    .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.ORG_NR);
            assertThat(arbeidsforhold.getOrgnr()).isEqualTo(orgnr);
        }
    }

    @Test
    public void skal_mappe_til_kun_en_aktivitet_med_fleire_arbeidsforhold_for_samme_arbeidsgiver_i_iay_uten_inntektsmelding() {
        // Arrange
        String orgnr = ORGNR;
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        var arbId2 = InternArbeidsforholdRefDto.nyRef();
        var arbId3 = InternArbeidsforholdRefDto.nyRef();

        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusMonths(12);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        var periode = Intervall.fraOgMedTilOgMed(fom, tom);

        var opptj1 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, orgnr, null, arbId1);
        var opptj2 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, orgnr, null, arbId2);
        var opptj3 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, orgnr, null, arbId3);

        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(opptj1, opptj2, opptj3));

        // Act
        AktivitetStatusModell modell = mapForSkjæringstidspunkt(koblingReferanse, opptjeningAktiviteter, List.of());

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(1);
        AktivPeriode aktivPeriode = modell.getAktivePerioder().get(0);
        assertThat(aktivPeriode.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode.getPeriode().getTom()).isEqualTo(tom);
        Arbeidsforhold arbeidsforhold = aktivPeriode.getArbeidsforhold();
        assertThat(arbeidsforhold.getArbeidsforholdId()).isNull();
        assertThat(arbeidsforhold.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
        assertThat(arbeidsforhold.getReferanseType())
                .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.ORG_NR);
        assertThat(arbeidsforhold.getOrgnr()).isEqualTo(ORGNR);
    }

    @Test
    public void skal_mappe_til_kun_en_aktivitet_med_fleire_arbeidsforhold_i_iay_for_samme_arbeidsgiver_med_felles_inntektsmelding() {
        // Arrange
        String orgnr = ORGNR;
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        var arbId2 = InternArbeidsforholdRefDto.nyRef();
        var arbId3 = InternArbeidsforholdRefDto.nyRef();

        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusMonths(12);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        var periode = Intervall.fraOgMedTilOgMed(fom, tom);

        var opptj1 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, orgnr, null, arbId1);
        var opptj2 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, orgnr, null, arbId2);
        var opptj3 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, periode, orgnr, null, arbId3);

        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR, null, SKJÆRINGSTIDSPUNKT);
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(opptj1, opptj2, opptj3));

        // Act
        AktivitetStatusModell modell = mapForSkjæringstidspunkt(koblingReferanse, opptjeningAktiviteter, List.of(im1));

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(1);
        AktivPeriode aktivPeriode = modell.getAktivePerioder().get(0);
        assertThat(aktivPeriode.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode.getPeriode().getTom()).isEqualTo(tom);
        Arbeidsforhold arbeidsforhold = aktivPeriode.getArbeidsforhold();
        assertThat(arbeidsforhold.getArbeidsforholdId()).isNull();
        assertThat(arbeidsforhold.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
        assertThat(arbeidsforhold.getReferanseType())
                .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.ORG_NR);
        assertThat(arbeidsforhold.getOrgnr()).isEqualTo(ORGNR);
    }

    @Test
    public void skal_mappe_arbeidsforhold_med_privatpersonarbeidsgiver_fra_opptjening() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusMonths(12);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        var periode = Intervall.fraOgMedTilOgMed(fom, tom);

        var opptjeningAktivitet = OpptjeningAktiviteterDto.fraAktørId(OpptjeningAktivitetType.ARBEID, periode, aktørId.getId());

        // Act
        AktivitetStatusModell modell = mapForSkjæringstidspunkt(koblingReferanse, opptjeningAktivitet, Collections.emptyList());

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(1);
        AktivPeriode aktivPeriode = modell.getAktivePerioder().get(0);
        assertThat(aktivPeriode.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode.getPeriode().getTom()).isEqualTo(tom);
        Arbeidsforhold arbeidsforhold = aktivPeriode.getArbeidsforhold();
        assertThat(arbeidsforhold.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
        assertThat(arbeidsforhold.getReferanseType())
                .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.AKTØR_ID);
        assertThat(arbeidsforhold.getAktørId()).isEqualTo(aktørId.getId());
        assertThat(arbeidsforhold.getArbeidsforholdId()).isNull();
    }

    @Test
    public void skal_mappe_frilansaktivitet_for_opptjening() {
        // Arrange

        var periode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.minusMonths(6));

        var opptjeningAktiviteter = OpptjeningAktiviteterDto.fra(OpptjeningAktivitetType.FRILANS, periode);

        // Act
        AktivitetStatusModell modell = mapForSkjæringstidspunkt(koblingReferanse, opptjeningAktiviteter, Collections.emptyList());

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(1);
        assertFrilansPeriode(modell, periode);
    }

    @Test
    public void skal_mappe_arbeid_uten_matchende_yrkesaktivitet() {
        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusMonths(12);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        var periode = Intervall.fraOgMedTilOgMed(fom, tom);

        var opptjeningAktivitet = OpptjeningAktiviteterDto.fraAktørId(OpptjeningAktivitetType.ARBEID, periode, aktørId.getId());

        // Act
        AktivitetStatusModell modell = mapForSkjæringstidspunktArbeidIIAY(koblingReferanse, opptjeningAktivitet, Collections.emptyList());

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(1);
        AktivPeriode aktivPeriode = modell.getAktivePerioder().get(0);
        assertThat(aktivPeriode.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode.getPeriode().getTom()).isEqualTo(tom);
        Arbeidsforhold arbeidsforhold = aktivPeriode.getArbeidsforhold();
        assertThat(arbeidsforhold.getAktivitet()).isEqualByComparingTo(Aktivitet.ARBEIDSTAKERINNTEKT);
        assertThat(arbeidsforhold.getReferanseType())
                .isEqualByComparingTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.AKTØR_ID);
        assertThat(arbeidsforhold.getAktørId()).isEqualTo(aktørId.getId());
        assertThat(arbeidsforhold.getArbeidsforholdId()).isNull();
    }

    @Test
    public void skal_mappe_alle_SN_fra_opptjening_til_ein_aktivitet() {
        // Arrange
        var opptj1 = OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.NÆRING,
                Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.minusMonths(6)), "674367833");
        var opptj2 = OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.NÆRING,
                Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(4), SKJÆRINGSTIDSPUNKT.minusMonths(2)), "5465464545");
        var opptj3 = OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.NÆRING,
                Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.plusMonths(4)), "543678342");

        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(opptj1, opptj2, opptj3));

        // Act
        AktivitetStatusModell modell = mapForSkjæringstidspunkt(koblingReferanse, opptjeningAktiviteter, List.of());

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(1);
        var forventetPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.plusMonths(4));
        assertNæringPeriode(modell, forventetPeriode);
    }

    @Test
    public void skal_mappe_sykepenger_fra_opptjening_til_ein_aktivitet() {
        // Arrange
        var opptj1 = OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.SYKEPENGER,
                Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.minusMonths(6)), "674367833");
        var opptj2 = OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.SYKEPENGER,
                Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(4), SKJÆRINGSTIDSPUNKT.minusMonths(2)), "5465464545");
        var opptj3 = OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.SYKEPENGER,
                Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.plusMonths(4)), "543678342");

        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(opptj1, opptj2, opptj3));

        // Act
        AktivitetStatusModell modell = mapForSkjæringstidspunkt(koblingReferanse, opptjeningAktiviteter, List.of());

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(1);
        var forventetPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.plusMonths(4));
        assertSykepengerPeriode(modell, forventetPeriode);
    }

    @Test
    public void skal_ikkje_mappe_etterutdanning() {
        // Arrange
        var periode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT);

        var opptj1 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.VENTELØNN_VARTPENGER, periode);
        var opptj2 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.VIDERE_ETTERUTDANNING, periode);

        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(opptj1, opptj2));

        // Act
        AktivitetStatusModell modell = mapForSkjæringstidspunkt(koblingReferanse, opptjeningAktiviteter, Collections.emptyList());

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(1);
        assertThat(modell.getAktivePerioder().get(0).getAktivitet()).isEqualTo(Aktivitet.VENTELØNN_VARTPENGER);
    }

    @Test
    public void skal_mappe_arbeidsforhold_med_virksomhetarbeidsgiver_fra_iay_som_ikkje_finnes_i_aareg() {

        // Arrange
        String orgnr1 = ORGNR;
        var arbId = InternArbeidsforholdRefDto.nyRef();
        LocalDate fom = SKJÆRINGSTIDSPUNKT.minusMonths(12);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        var opptj1 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMedTilOgMed(fom, tom), orgnr1, null, arbId);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr1, arbId, SKJÆRINGSTIDSPUNKT);

        // Bygg arbeid som ikkje ligger i opptjening
        // Arbeidsforhold starter på skjæringstidspunkt for opptjening. Skal ikkje vere med i mappinga.
        String orgnr2 = "23478497234";
        var arbId2 = InternArbeidsforholdRefDto.nyRef();
        LocalDate fom2 = SKJÆRINGSTIDSPUNKT;
        LocalDate tom2 = SKJÆRINGSTIDSPUNKT.plusMonths(2);
        var opptj2 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMedTilOgMed(fom2, tom2), orgnr2, null, arbId2);
        var im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr2, arbId2, SKJÆRINGSTIDSPUNKT);

        // Arbeidsforhold starter før skjæringstidspunktet og slutter etter. Skal vere med i mapping.
        String orgnr3 = "874893579834";
        var arbId3 = InternArbeidsforholdRefDto.nyRef();
        LocalDate fom3 = SKJÆRINGSTIDSPUNKT.minusMonths(3);
        LocalDate tom3 = SKJÆRINGSTIDSPUNKT.plusMonths(1);
        var opptj3 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMedTilOgMed(fom3, tom3), orgnr3, null, arbId3);
        var im3 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr3, arbId3, SKJÆRINGSTIDSPUNKT);

        // Arbeidsforhold starter etter skjæringstidspunktet. Skal ikkje vere med i mappinga.
        String orgnr4 = "789458734893";
        var arbId4 = InternArbeidsforholdRefDto.nyRef();
        LocalDate fom4 = SKJÆRINGSTIDSPUNKT.plusMonths(1);
        LocalDate tom4 = SKJÆRINGSTIDSPUNKT.plusMonths(2);
        var opptj4 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMedTilOgMed(fom4, tom4), orgnr4, null, arbId4);
        var im4 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr4, arbId4, SKJÆRINGSTIDSPUNKT);

        // Arbeidsforhold starter før skjæringstidspunktet og slutter dagen før skjæringstidspunktet. Skal vere med i mappinga.
        String orgnr5 = "435348734893";
        var arbId5 = InternArbeidsforholdRefDto.nyRef();
        LocalDate fom5 = SKJÆRINGSTIDSPUNKT.minusMonths(6);
        LocalDate tom5 = SKJÆRINGSTIDSPUNKT.minusDays(1);
        var opptj5 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMedTilOgMed(fom5, tom5), orgnr5, null, arbId5);
        var im5 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr5, arbId5, SKJÆRINGSTIDSPUNKT);

        // Arbeidsforhold starter før skjæringstidspunktet og slutter på skjæringstidspunktet. Skal vere med i mappinga.
        String orgnr6 = "543534348734893";
        var arbId6 = InternArbeidsforholdRefDto.nyRef();
        LocalDate fom6 = SKJÆRINGSTIDSPUNKT.minusMonths(6);
        LocalDate tom6 = SKJÆRINGSTIDSPUNKT;
        var opptj6 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMedTilOgMed(fom6, tom6), orgnr6, null, arbId6);
        var im6 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr6, arbId6, SKJÆRINGSTIDSPUNKT);

        // Act
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(opptj1, opptj2, opptj3, opptj4, opptj5, opptj6));

        // Act
        AktivitetStatusModell modell = mapForSkjæringstidspunkt(koblingReferanse, opptjeningAktiviteter, List.of(im1, im2, im3, im4, im5, im6));

        // Assert
        assertThat(modell.getSkjæringstidspunktForOpptjening()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(modell.getAktivePerioder()).hasSize(4);
        assertAktivPeriode(modell, orgnr1, fom, tom, arbId.getReferanse());
        assertAktivPeriode(modell, orgnr3, fom3, tom3, arbId3.getReferanse());
        assertAktivPeriode(modell, orgnr5, fom5, tom5, arbId5.getReferanse());
        assertAktivPeriode(modell, orgnr6, fom6, tom6, arbId6.getReferanse());
    }

    private void assertSykepengerPeriode(AktivitetStatusModell modell, Intervall periode) {
        Optional<AktivPeriode> aktivPeriodeOpt = modell.getAktivePerioder().stream()
                .filter(ap -> ap.getPeriode().equals(Periode.of(periode.getFomDato(), periode.getTomDato()))).findFirst();
        assertThat(aktivPeriodeOpt.isPresent()).isTrue();
        AktivPeriode aktivPeriode = aktivPeriodeOpt.get();
        assertThat(aktivPeriode.getAktivitet()).isEqualByComparingTo(Aktivitet.SYKEPENGER_MOTTAKER);
        assertThat(aktivPeriode.getArbeidsforhold()).isNull();
    }

    private void assertNæringPeriode(AktivitetStatusModell modell, Intervall periode) {
        Optional<AktivPeriode> aktivPeriodeOpt = modell.getAktivePerioder().stream()
                .filter(ap -> ap.getPeriode().equals(Periode.of(periode.getFomDato(), periode.getTomDato()))).findFirst();
        assertThat(aktivPeriodeOpt.isPresent()).isTrue();
        AktivPeriode aktivPeriode = aktivPeriodeOpt.get();
        assertThat(aktivPeriode.getAktivitet()).isEqualByComparingTo(Aktivitet.NÆRINGSINNTEKT);
        assertThat(aktivPeriode.getArbeidsforhold()).isNull();
    }

    private void assertFrilansPeriode(AktivitetStatusModell modell, Intervall periode) {
        Optional<AktivPeriode> aktivPeriodeOpt = modell.getAktivePerioder().stream()
                .filter(ap -> ap.getPeriode().equals(Periode.of(periode.getFomDato(), periode.getTomDato()))).findFirst();
        assertThat(aktivPeriodeOpt.isPresent()).isTrue();
        AktivPeriode aktivPeriode = aktivPeriodeOpt.get();
        assertThat(aktivPeriode.getAktivitet()).isEqualByComparingTo(Aktivitet.FRILANSINNTEKT);
        assertThat(aktivPeriode.getArbeidsforhold().getAktivitet()).isEqualByComparingTo(Aktivitet.FRILANSINNTEKT);
        assertThat(aktivPeriode.getArbeidsforhold().getOrgnr()).isNull();
        assertThat(aktivPeriode.getArbeidsforhold().getAktørId()).isNull();
        assertThat(aktivPeriode.getArbeidsforhold().getArbeidsforholdId()).isNull();
    }

    private void assertAktivPeriode(AktivitetStatusModell modell, String orgnr, LocalDate fom, LocalDate tom, String arbRef) {
        Optional<AktivPeriode> aktivPeriodeOpt = modell.getAktivePerioder().stream()
                .filter(ap -> Objects.equals(ap.getArbeidsforhold().getReferanseType(),
                        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType.ORG_NR) &&
                        Objects.equals(ap.getArbeidsforhold().getOrgnr(), orgnr) &&
                        Objects.equals(ap.getArbeidsforhold().getArbeidsforholdId(), arbRef) &&
                        Objects.equals(ap.getArbeidsforhold().getAktivitet(), Aktivitet.ARBEIDSTAKERINNTEKT))
                .findFirst();
        assertThat(aktivPeriodeOpt.isPresent()).isTrue();
        AktivPeriode aktivPeriode = aktivPeriodeOpt.get();
        assertThat(aktivPeriode.getPeriode().getFom()).isEqualTo(fom);
        assertThat(aktivPeriode.getPeriode().getTom()).isEqualTo(tom);
    }


    private AktivitetStatusModell mapForSkjæringstidspunkt(InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder, OpptjeningAktiviteterDto opptjeningAktiviteterDto) {
        var beregningsgrunnlaginput = new BeregningsgrunnlagInput(KOBLING_REFERANSE, iayGrunnlagBuilder.build(), opptjeningAktiviteterDto, null, null);
        var stegInput = new StegProsesseringInput(beregningsgrunnlaginput, BeregningsgrunnlagTilstand.OPPRETTET);
        var input = new FastsettBeregningsaktiviteterInput(stegInput);
        return mapper.mapForSkjæringstidspunkt(input);
    }

    private OpptjeningAktiviteterDto lagOpptjeningsAktivitet(LocalDate ansettelsesDato, InternArbeidsforholdRefDto nullRef) {
        return new OpptjeningAktiviteterDto(List.of(OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMed(ansettelsesDato), ARBEIDSGIVER_ORGNR, null, nullRef)));
    }

    private InntektArbeidYtelseGrunnlagDtoBuilder lagIAY(LocalDate ansettelsesDato, InternArbeidsforholdRefDto arbeidsforholdReferanse, List<PermisjonDtoBuilder> permisjoner) {
        var register = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = register.getAktørArbeidBuilder();
        aktørArbeidBuilder.leggTilYrkesaktivitet(lagYrkesaktivitet(ansettelsesDato, permisjoner));
        register.leggTilAktørArbeid(aktørArbeidBuilder);

        InntektsmeldingDto inntektsmelding = InntektsmeldingDtoBuilder.builder().medArbeidsgiver(ARBEIDSGIVER).medArbeidsforholdId(arbeidsforholdReferanse).medBeløp(Beløp.fra(300000)).build();
        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of(inntektsmelding));
        iayGrunnlagBuilder.medData(register);
        return iayGrunnlagBuilder;
    }

    private YrkesaktivitetDto lagYrkesaktivitet(LocalDate ansettelsesDato, List<PermisjonDtoBuilder> permisjoner) {
        YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        AktivitetsAvtaleDtoBuilder aktivitetsavtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        Intervall periode = Intervall.fraOgMedTilOgMed(ansettelsesDato, TIDENES_ENDE);
        lagAktivitetsavtale(aktivitetsavtaleBuilder, periode);

        AktivitetsAvtaleDtoBuilder ansettelsesPeriode = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder(periode, true);

        YrkesaktivitetDtoBuilder yrkesaktivitetDtoBuilder = yrkesaktivitetBuilder.medArbeidsgiver(ARBEIDSGIVER)
                .medArbeidsforholdId(ARBEIDSFORHOLD_ID)
                .leggTilAktivitetsAvtale(aktivitetsavtaleBuilder)
                .leggTilAktivitetsAvtale(ansettelsesPeriode)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        permisjoner.forEach(yrkesaktivitetDtoBuilder::leggTilPermisjon);

        return yrkesaktivitetDtoBuilder
                .build();
    }

    private void lagAktivitetsavtale(AktivitetsAvtaleDtoBuilder aktivitetsavtaleBuilder, Intervall periode) {
        aktivitetsavtaleBuilder.medPeriode(periode)
                .medErAnsettelsesPeriode(false);
    }
}
