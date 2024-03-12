package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.felles.frist.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
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
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class InntektsmeldingMedRefusjonTjenesteImplTest {
    private static final String ORGNR = "974760673";
    private static final String ORGNR2 = "915933149";

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);

    @Test
    public void skal_finne_arbeidsgiver_som_har_søkt_for_sent_med_flere_arbeidsforhold_et_som_tilkommer_etter_skjæringstidspunktet() {
        // Arrange
        Map<Arbeidsgiver, LocalDate> førsteInnsendingMap = new HashMap<>();
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        InternArbeidsforholdRefDto ref1 = InternArbeidsforholdRefDto.nyRef();
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR, ref1, SKJÆRINGSTIDSPUNKT, 1000, 1000);
        førsteInnsendingMap.put(arbeidsgiver, SKJÆRINGSTIDSPUNKT.plusMonths(4));
        InternArbeidsforholdRefDto ref2 = InternArbeidsforholdRefDto.nyRef();
        InntektsmeldingDto im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR, ref2, SKJÆRINGSTIDSPUNKT.plusMonths(1), 1000, 1000);
        førsteInnsendingMap.put(arbeidsgiver, SKJÆRINGSTIDSPUNKT.plusMonths(4));
        BeregningAktivitetAggregatDto aktivitetAggregat = leggTilAktivitet(registerBuilder, ORGNR, List.of(ref1));
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlag = byggGrunnlag(aktivitetAggregat, List.of(arbeidsgiver));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(registerBuilder)
                .medInntektsmeldinger(List.of(im1, im2)).build();
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(koblingReferanse, grunnlag,
                BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag, førsteInnsendingMap);

        // Act
        Set<Arbeidsgiver> arbeidsgivereSomHarSøktForSent = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
                koblingReferanse,
                input.getIayGrunnlag(),
                input.getBeregningsgrunnlagGrunnlag(),
                input.getKravPrArbeidsgiver(),
                FagsakYtelseType.FORELDREPENGER);

        // Assert
        assertThat(arbeidsgivereSomHarSøktForSent).hasSize(1);
        assertThat(arbeidsgivereSomHarSøktForSent.iterator().next()).isEqualTo(arbeidsgiver);
    }

    @Test
    public void skal_finne_arbeidsgivere_som_har_søkt_for_sent() {
        // Arrange
        Map<Arbeidsgiver, LocalDate> førsteInnsendingMap = new HashMap<>();
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        BeregningAktivitetAggregatDto aktivitetAggregat = leggTilAktivitet(registerBuilder, List.of(ORGNR, ORGNR2));
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        Arbeidsgiver arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR, SKJÆRINGSTIDSPUNKT, Beløp.fra(10), Beløp.fra(10));
        førsteInnsendingMap.put(arbeidsgiver, SKJÆRINGSTIDSPUNKT.plusMonths(4));
        InntektsmeldingDto im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR2, SKJÆRINGSTIDSPUNKT, Beløp.fra(10), Beløp.fra(10));
        førsteInnsendingMap.put(arbeidsgiver2, SKJÆRINGSTIDSPUNKT.plusMonths(2));
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlag = byggGrunnlag(aktivitetAggregat, List.of(arbeidsgiver, arbeidsgiver2));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(registerBuilder)
                .medInntektsmeldinger(List.of(im1, im2)).build();
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(koblingReferanse, grunnlag,
                BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag, førsteInnsendingMap);

        // Act
        Set<Arbeidsgiver> arbeidsgivereSomHarSøktForSent = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
                koblingReferanse,
                input.getIayGrunnlag(),
                input.getBeregningsgrunnlagGrunnlag(),
                input.getKravPrArbeidsgiver(), FagsakYtelseType.FORELDREPENGER);

        // Assert
        assertThat(arbeidsgivereSomHarSøktForSent).hasSize(1);
        assertThat(arbeidsgivereSomHarSøktForSent.iterator().next()).isEqualTo(arbeidsgiver);
    }


    @Test
    public void skal_returnere_tomt_set_om_ingen_inntektsmeldinger_er_mottatt() {
        // Arrange
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        BeregningAktivitetAggregatDto aktivitetAggregat = leggTilAktivitet(registerBuilder, List.of(ORGNR, ORGNR2));
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        Arbeidsgiver arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlag = byggGrunnlag(aktivitetAggregat, List.of(arbeidsgiver, arbeidsgiver2));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(registerBuilder)
                .medInntektsmeldinger(List.of()).build();

        // Act
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(koblingReferanse, grunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag);
        Set<Arbeidsgiver> arbeidsgivereSomHarSøktForSent = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
                koblingReferanse,
                input.getIayGrunnlag(),
                input.getBeregningsgrunnlagGrunnlag(),
                input.getKravPrArbeidsgiver(),
                FagsakYtelseType.FORELDREPENGER);

        // Assert
        assertThat(arbeidsgivereSomHarSøktForSent).isEmpty();
    }

    @Test
    public void skal_returnere_tomt_set_om_ingen_inntektsmeldinger_er_mottatt_for_sent() {
        // Arrange
        Map<Arbeidsgiver, LocalDate> førsteInnsendingMap = new HashMap<>();
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        BeregningAktivitetAggregatDto aktivitetAggregat = leggTilAktivitet(registerBuilder, List.of(ORGNR, ORGNR2));
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        Arbeidsgiver arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR, SKJÆRINGSTIDSPUNKT, Beløp.fra(10), Beløp.fra(10));
        førsteInnsendingMap.put(arbeidsgiver, SKJÆRINGSTIDSPUNKT.plusMonths(1));
        InntektsmeldingDto im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR2, SKJÆRINGSTIDSPUNKT, Beløp.fra(10), Beløp.fra(10));
        førsteInnsendingMap.put(arbeidsgiver2, SKJÆRINGSTIDSPUNKT.plusMonths(2));
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlag = byggGrunnlag(aktivitetAggregat, List.of(arbeidsgiver, arbeidsgiver2));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(registerBuilder)
                .medInntektsmeldinger(List.of(im1, im2)).build();

        // Act
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(koblingReferanse, grunnlag,
                BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag, førsteInnsendingMap);
        Set<Arbeidsgiver> arbeidsgivereSomHarSøktForSent = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
                koblingReferanse,
                input.getIayGrunnlag(),
                input.getBeregningsgrunnlagGrunnlag(),
                input.getKravPrArbeidsgiver(),
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

        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)).build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
                .build(bg);
        ags.forEach(ag -> BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ag))
                .build(periode)
        );
        return bg;
    }


    private BeregningAktivitetAggregatDto leggTilAktivitet(InntektArbeidYtelseAggregatBuilder iayAggregatBuilder, String orgnr, List<InternArbeidsforholdRefDto> internArbeidsforholdRefDto) {
        Intervall arbeidsperiode1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        BeregningAktivitetAggregatDto.Builder aktivitetAggregatBuilder = BeregningAktivitetAggregatDto.builder()
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT);
        for (InternArbeidsforholdRefDto ref : internArbeidsforholdRefDto) {
            Arbeidsgiver ag = leggTilYrkesaktivitet(arbeidsperiode1, aktørArbeidBuilder, orgnr, ref);
            aktivitetAggregatBuilder.leggTilAktivitet(lagAktivitet(arbeidsperiode1, ag, ref));
        }
        iayAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        return aktivitetAggregatBuilder.build();
    }

    private BeregningAktivitetAggregatDto leggTilAktivitet(InntektArbeidYtelseAggregatBuilder iayAggregatBuilder, List<String> orgnr) {
        Intervall arbeidsperiode1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        BeregningAktivitetAggregatDto.Builder aktivitetAggregatBuilder = BeregningAktivitetAggregatDto.builder()
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT);
        for (String nr : orgnr) {
            InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nullRef();
            Arbeidsgiver ag = leggTilYrkesaktivitet(arbeidsperiode1, aktørArbeidBuilder, nr, ref);
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
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        AktivitetsAvtaleDtoBuilder aaBuilder1 = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(arbeidsperiode);
        YrkesaktivitetDtoBuilder yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdId(arbeidsforholdId)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(aaBuilder1);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yaBuilder);
        return arbeidsgiver;
    }

}
