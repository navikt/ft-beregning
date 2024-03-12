package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;

public class NyIArbeidslivetTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);
    private static final String ORGNR = "123456780";

    @Test
    public void skalGiTilfelleDeromBrukerErSNOgNyIArbeidslivet() {
        // Arrange
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medOppgittOpptjening(lagEgenNæring(true)).build();
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlagMedArbeidOgNæring();

        // Act
        boolean resultat = NyIArbeidslivetTjeneste.erNyIArbeidslivetMedAktivitetStatusSN(bg, iayGrunnlag);

        // Assert
        assertThat(resultat).isTrue();
    }

    @Test
    public void skalIkkeGiTilfelleDeromBrukerIkkeErNyIArbeidslivet() {
        // Arrange
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medOppgittOpptjening(lagEgenNæring(false)).build();
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlagMedArbeidOgNæring();

        // Act
        boolean resultat = NyIArbeidslivetTjeneste.erNyIArbeidslivetMedAktivitetStatusSN(bg, iayGrunnlag);

        // Assert
        assertThat(resultat).isFalse();
    }

    @Test
    public void skalIkkeGiTilfelleForNyIArbeidslivetSNDeromBrukerIkkeErSN() {
        // Arrange
        InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag = lagAktørArbeid();
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlagMedArbeid();

        // Act
        boolean resultat = NyIArbeidslivetTjeneste.erNyIArbeidslivetMedAktivitetStatusSN(bg, inntektArbeidYtelseGrunnlag);

        // Assert
        assertThat(resultat).isFalse();
    }

    private OppgittOpptjeningDtoBuilder lagEgenNæring(boolean nyIArbeidslivet) {
        return OppgittOpptjeningDtoBuilder.ny().leggTilEgneNæringer(Collections.singletonList(OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny()
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(6), SKJÆRINGSTIDSPUNKT_OPPTJENING))
        .medNyIArbeidslivet(nyIArbeidslivet)));
    }


    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedArbeidOgNæring() {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.Builder.oppdater(Optional.empty())
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.KOMBINERT_AT_SN))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .build();
        BeregningsgrunnlagPeriodeDto bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3), SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                .medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef())
                .medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR))
                .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(12))
                .medArbeidsperiodeTom(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1)))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(bgPeriode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build(bgPeriode);
        return beregningsgrunnlag;
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedArbeid() {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.Builder.oppdater(Optional.empty())
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .build();
        BeregningsgrunnlagPeriodeDto bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3), SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                .medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef())
                .medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR))
                .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(12))
                .medArbeidsperiodeTom(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1)))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(bgPeriode);
        return beregningsgrunnlag;
    }


    private InntektArbeidYtelseGrunnlagDto lagAktørArbeid() {
        InntektArbeidYtelseAggregatBuilder oppdatere = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = oppdatere.getAktørArbeidBuilder();
        leggTilAktivitet(InternArbeidsforholdRefDto.nullRef(), ORGNR, Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1)), aktørArbeidBuilder);
        return InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty()).medData(oppdatere).build();
    }

    private void leggTilAktivitet(InternArbeidsforholdRefDto arbId, String orgnr, Intervall periode, InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder) {
        YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsforholdId(arbId)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder(periode, true));
        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
    }

}
