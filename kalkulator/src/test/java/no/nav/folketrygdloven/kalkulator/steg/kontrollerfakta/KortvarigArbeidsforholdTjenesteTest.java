package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.KortvarigArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;


public class KortvarigArbeidsforholdTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);

    @Test
    public void skalIkkjeGiKortvarigForArbeidsforholdPå6Mnd() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        Intervall periode = Intervall.fraOgMedTilOgMed(
            LocalDate.of(2018, 8, 5),
            LocalDate.of(2019, 2, 4));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlaBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, LocalDate.of(2018, 8, 5),
            LocalDate.of(2019, 2, 4), arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlaBuilder);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeid(arbId, orgnr, periode, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarige = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(beregningsgrunnlag, iayGrunnlaBuilder.build());

        // Assert
        assertThat(kortvarige).isEmpty();
    }

    @Test
    public void skalIkkjeGiKortvarigForArbeidsforholdPå6MndIMånederMedUlikDato1() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        Intervall periode = Intervall.fraOgMedTilOgMed(
            LocalDate.of(2018, 8, 29),
            LocalDate.of(2019, 2, 28));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlaBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, LocalDate.of(2018, 8, 29),
            LocalDate.of(2019, 2, 28), arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlaBuilder);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeid(arbId, orgnr, periode, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarige = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(beregningsgrunnlag, iayGrunnlaBuilder.build());

        // Assert
        assertThat(kortvarige).isEmpty();
    }

    @Test
    public void skalIkkjeGiKortvarigForArbeidsforholdPå6MndIMånederMedUlikDato2() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        Intervall periode = Intervall.fraOgMedTilOgMed(
            LocalDate.of(2018, 8, 31),
            LocalDate.of(2019, 2, 28));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlaBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, LocalDate.of(2018, 8, 31),
            LocalDate.of(2019, 2, 28), arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlaBuilder);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeid(arbId, orgnr, periode, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarige = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(beregningsgrunnlag, iayGrunnlaBuilder.build());

        // Assert
        assertThat(kortvarige).isEmpty();
    }

    @Test
    public void skalIkkjeGiKortvarigForArbeidsforholdPå6MndIMånederMedUlikDato3() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        Intervall periode = Intervall.fraOgMedTilOgMed(
            LocalDate.of(2018, 9, 1),
            LocalDate.of(2019, 2, 28));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlaBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, LocalDate.of(2018, 9, 1),
            LocalDate.of(2019, 2, 28), arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlaBuilder);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeid(arbId, orgnr, periode, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarige = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(beregningsgrunnlag, iayGrunnlaBuilder.build());
        // Assert
        assertThat(kortvarige).isEmpty();
    }

    @Test
    public void skalIkkjeGiKortvarigForArbeidsforholdPå6MndIMånederMedUlikDato4() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        Intervall periode = Intervall.fraOgMedTilOgMed(
            LocalDate.of(2018, 8, 30),
            LocalDate.of(2019, 2, 28));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlaBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(
                LocalDate.of(2018, 9, 30),
            LocalDate.of(2018, 8, 30),
            LocalDate.of(2019, 2, 28),
            arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlaBuilder);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeid(arbId, orgnr, periode, LocalDate.of(2018, 9, 30));

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarige = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(beregningsgrunnlag, iayGrunnlaBuilder.build());

        // Assert
        assertThat(kortvarige).isEmpty();
    }

    @Test
    public void skalIkkjeGiKortvarigForArbeidsforholdSomStarterPåSkjæringstidspunktet() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        Intervall periode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(1));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlaBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(
                SKJÆRINGSTIDSPUNKT_OPPTJENING,
            SKJÆRINGSTIDSPUNKT_OPPTJENING,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(1),
            arbId, Arbeidsgiver.virksomhet(orgnr),
            iayGrunnlaBuilder);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeid(arbId, orgnr, periode, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarige = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(beregningsgrunnlag, iayGrunnlaBuilder.build());

        // Assert
        assertThat(kortvarige).isEmpty();
    }

    @Test
    public void skalIkkjeGiKortvarigForArbeidsforholdSomStarterEtterSkjæringstidspunktet() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        Intervall periode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(1));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlaBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(
                SKJÆRINGSTIDSPUNKT_OPPTJENING,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(1),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(1),
            arbId, Arbeidsgiver.virksomhet(orgnr),
            iayGrunnlaBuilder);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeid(arbId, orgnr, periode, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarige = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(beregningsgrunnlag, iayGrunnlaBuilder.build());

        // Assert
        assertThat(kortvarige).isEmpty();
    }


    @Test
    public void skalGiKortvarigForArbeidsforholdPå6MndMinusEinDag() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        Intervall periode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlaBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(
                SKJÆRINGSTIDSPUNKT_OPPTJENING,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2),
            arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlaBuilder);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeid(arbId, orgnr, periode, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarige = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(
                beregningsgrunnlag, iayGrunnlaBuilder.build());

        // Assert
        List<YrkesaktivitetDto> kortvarigeYrkesaktiviteter = new ArrayList<>(kortvarige.values());
        assertThat(kortvarigeYrkesaktiviteter).hasSize(1);
    }

    @Test
    public void skalGiKortvarigForArbeidsforholdSomStarterDagenFørSkjæringstidspunktet() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlaBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(
                SKJÆRINGSTIDSPUNKT_OPPTJENING,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2),
            arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlaBuilder);
        Intervall periode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeid(arbId, orgnr, periode, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarige = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(
                beregningsgrunnlag, iayGrunnlaBuilder.build());

        // Assert
        List<YrkesaktivitetDto> kortvarigeYrkesaktiviteter = new ArrayList<>(kortvarige.values());
        assertThat(kortvarigeYrkesaktiviteter).hasSize(1);
    }

    @Test
    public void skalGiKortvarigVedKombinasjonMedDagpenger() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        Intervall periode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlaBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(
                SKJÆRINGSTIDSPUNKT_OPPTJENING,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2),
            arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlaBuilder);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeidOgDagpenger(arbId, orgnr, periode, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarige = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(
                beregningsgrunnlag, iayGrunnlaBuilder.build());

        // Assert
        assertThat(kortvarige.values()).hasSize(1);
    }


    @Test
    public void skalGiToKortvarigeArbeidsforhold() {
        // Arrange
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        var arbId2 = InternArbeidsforholdRefDto.nyRef();
        String orgnr1 = "123456780";
        String orgnr2 = "123456644";
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlaBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(
                SKJÆRINGSTIDSPUNKT_OPPTJENING,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(10),
            arbId1, Arbeidsgiver.virksomhet(orgnr1), iayGrunnlaBuilder);
        BeregningIAYTestUtil.byggArbeidForBehandling(
                SKJÆRINGSTIDSPUNKT_OPPTJENING,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2).minusDays(2),
            arbId2, Arbeidsgiver.virksomhet(orgnr2), iayGrunnlaBuilder);
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = iayGrunnlaBuilder.build();
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeid(iayGrunnlag.getAktørArbeidFraRegister().orElseThrow().hentAlleYrkesaktiviteter(), SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarige = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(beregningsgrunnlag, iayGrunnlag);

        // Assert
        List<YrkesaktivitetDto> kortvarigeYrkesaktiviteter = new ArrayList<>(kortvarige.values());
        assertThat(kortvarigeYrkesaktiviteter).hasSize(2);
    }

    @Test
    public void skalIkkeGiKortvarigArbeidsforholdDersomBrukerErSN() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        // Lager et kortvarig arbeidsforhold
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlaBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2),
            arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlaBuilder);
        BeregningIAYTestUtil.lagOppgittOpptjeningForSN(SKJÆRINGSTIDSPUNKT_OPPTJENING, false, iayGrunnlaBuilder);
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = iayGrunnlaBuilder.build();
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeidOgSN(iayGrunnlag.getAktørArbeidFraRegister().orElseThrow().hentAlleYrkesaktiviteter(), SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        boolean resultat = KortvarigArbeidsforholdTjeneste.harKortvarigeArbeidsforholdOgErIkkeSN(beregningsgrunnlag, iayGrunnlag);

        // Assert
        assertThat(resultat).isFalse();
    }

    @Test
    public void skalGiKortvarigArbeidsforholdDersomBrukerIkkeErSN() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        // Lager et kortvarig arbeidsforhold
        Intervall periode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlaBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2),
            arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlaBuilder);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeid(arbId, orgnr, periode, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        boolean resultat = KortvarigArbeidsforholdTjeneste.harKortvarigeArbeidsforholdOgErIkkeSN(beregningsgrunnlag, iayGrunnlaBuilder.build());

        // Assert
        assertThat(resultat).isTrue();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedArbeid(InternArbeidsforholdRefDto arbId, String orgnr, Intervall periode, LocalDate skjæringstidspunktOpptjening) {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.Builder.oppdater(Optional.empty())
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .medSkjæringstidspunkt(skjæringstidspunktOpptjening)
            .build();
        BeregningsgrunnlagPeriodeDto bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(skjæringstidspunktOpptjening, null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                .medArbeidsforholdRef(arbId)
                .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr))
                .medArbeidsperiodeFom(periode.getFomDato())
                .medArbeidsperiodeTom(periode.getTomDato()))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(bgPeriode);
        return beregningsgrunnlag;
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedArbeidOgDagpenger(InternArbeidsforholdRefDto arbId, String orgnr, Intervall periode, LocalDate skjæringstidspunktOpptjening) {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.Builder.oppdater(Optional.empty())
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER))
            .medSkjæringstidspunkt(skjæringstidspunktOpptjening)
            .build();
        BeregningsgrunnlagPeriodeDto bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(skjæringstidspunktOpptjening, null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                .medArbeidsforholdRef(arbId)
                .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr))
                .medArbeidsperiodeFom(periode.getFomDato())
                .medArbeidsperiodeTom(periode.getTomDato()))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(bgPeriode);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.DAGPENGER)
            .build(bgPeriode);
        return beregningsgrunnlag;
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedArbeid(Collection<YrkesaktivitetDto> yrkesaktivitetList, LocalDate skjæringstidspunktOpptjening) {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.Builder.oppdater(Optional.empty())
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .medSkjæringstidspunkt(skjæringstidspunktOpptjening)
            .build();
        BeregningsgrunnlagPeriodeDto bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(skjæringstidspunktOpptjening, null)
            .build(beregningsgrunnlag);
        yrkesaktivitetList.forEach(ya ->
            BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                    .medArbeidsforholdRef(ya.getArbeidsforholdRef())
                    .medArbeidsgiver(ya.getArbeidsgiver())
                    .medArbeidsperiodeFom(getPeriode(ya).getFomDato())
                    .medArbeidsperiodeTom(getPeriode(ya).getTomDato()))
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(bgPeriode)
        );
        return beregningsgrunnlag;
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedArbeidOgSN(Collection<YrkesaktivitetDto> yrkesaktivitetList, LocalDate skjæringstidspunktOpptjening) {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.Builder.oppdater(Optional.empty())
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.KOMBINERT_AT_SN))
            .medSkjæringstidspunkt(skjæringstidspunktOpptjening)
            .build();
        BeregningsgrunnlagPeriodeDto bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(skjæringstidspunktOpptjening, null)
            .build(beregningsgrunnlag);
        yrkesaktivitetList.stream().filter(a -> a.getArbeidType().equals(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)).forEach(ya ->
            BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                    .medArbeidsforholdRef(ya.getArbeidsforholdRef())
                    .medArbeidsgiver(ya.getArbeidsgiver())
                    .medArbeidsperiodeFom(getPeriode(ya).getFomDato())
                    .medArbeidsperiodeTom(getPeriode(ya).getTomDato()))
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(bgPeriode)
        );
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build(bgPeriode);
        return beregningsgrunnlag;
    }

    private Intervall getPeriode(YrkesaktivitetDto ya) {
        Intervall periode = ya.getAlleAktivitetsAvtaler().stream().filter(AktivitetsAvtaleDto::erAnsettelsesPeriode).findFirst().orElseThrow().getPeriode();
        return Intervall.fraOgMedTilOgMed(periode.getFomDato(), periode.getTomDato());
    }

}
