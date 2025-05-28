package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.fp.ForeldrepengerGrunnlag;
import no.nav.fpsak.nare.evaluation.Resultat;

class SjekkOmBesteberegningTest {

    private Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("12345");

    @Test
    void skalReturnereNeiNårIkkeDagpengerStatus() {
        //Arrange
        var grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), new Inntektsgrunnlag(), List.of(AktivitetStatus.ATFL), List.of(arbeidsforhold));
        var periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        var resultat = new SjekkOmBesteberegning().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    void skalReturnereNeiNårDagpengerStatusMenIkkeBesteberegning() {
        //Arrange
        var grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), new Inntektsgrunnlag(), List.of(AktivitetStatus.ATFL, AktivitetStatus.DP), List.of(arbeidsforhold));
        var periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        var resultat = new SjekkOmBesteberegning().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    void skalReturnereJaNårDagpengerStatusOgBesteberegning() {
        //Arrange
        var grunnlag = Beregningsgrunnlag.builder(settoppGrunnlagMedEnPeriode(LocalDate.now(), new Inntektsgrunnlag(), List.of(AktivitetStatus.ATFL, AktivitetStatus.DP), List.of(arbeidsforhold)))
            .medYtelsesSpesifiktGrunnlag(new ForeldrepengerGrunnlag(true)).build();

        var periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        var dagpengerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        BeregningsgrunnlagPrStatus.builder(dagpengerStatus).medFastsattAvSaksbehandler(true);
        var arbeidstakerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidstakerStatus).medFastsattAvSaksbehandler(true);
        //Act
        var resultat = new SjekkOmBesteberegning().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.JA);
        assertThat(grunnlag.getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

    @Test
    void skalReturnereNeiNårFastsattDagpengerStatusOgIkkeFastsattATFLSTatus() { //Skal vanligvis ikke skje
        //Arrange
        var grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), new Inntektsgrunnlag(), List.of(AktivitetStatus.ATFL, AktivitetStatus.DP), List.of(arbeidsforhold));
        var periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        var dagpengerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        BeregningsgrunnlagPrStatus.builder(dagpengerStatus).medFastsattAvSaksbehandler(true);
        var arbeidstakerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidstakerStatus).medFastsattAvSaksbehandler(false);
        //Act
        var resultat = new SjekkOmBesteberegning().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.NEI);
    }

}
