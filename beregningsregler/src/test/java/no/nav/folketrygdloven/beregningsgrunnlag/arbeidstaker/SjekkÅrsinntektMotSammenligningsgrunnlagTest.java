package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

class SjekkÅrsinntektMotSammenligningsgrunnlagTest {
    private Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("12345");

    @Test
    void skalReturnereJaNårSammenligningsGrunnlagEr0() {
        //Arrange
        var grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), new Inntektsgrunnlag(),
            Collections.singletonList(AktivitetStatus.ATFL), Collections.singletonList(arbeidsforhold));
        var sgPrStatus = SammenligningsGrunnlag.builder()
			    .medSammenligningsperiode(null)
			    .medSammenligningstype(SammenligningGrunnlagType.AT_FL)
			    .medRapportertPrÅr(BigDecimal.ZERO).build();
	    Beregningsgrunnlag.builder(grunnlag).leggTilSammenligningsgrunnlagPrStatus(sgPrStatus);
        var periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        var resultat = new SjekkÅrsinntektMotSammenligningsgrunnlag().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.JA);
	    var sgPrStatusResultat = grunnlag.getSammenligningsgrunnlagForStatus(SammenligningGrunnlagType.AT_FL).orElseThrow();
	    assertThat(sgPrStatusResultat.getAvvikPromilleUtenAvrunding()).isEqualByComparingTo(BigDecimal.valueOf(1000));

    }

    @Test
    void skalKasteExceptionNårSammenligningsgrunnlagErNull() {
        //Arrange
        var grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), new Inntektsgrunnlag(),
            Collections.singletonList(AktivitetStatus.ATFL), Collections.singletonList(arbeidsforhold));
        var periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var sjekkÅrsinntektMotSammenligningsgrunnlag = new SjekkÅrsinntektMotSammenligningsgrunnlag();
	    Assertions.assertThrows(IllegalStateException.class, () -> sjekkÅrsinntektMotSammenligningsgrunnlag.evaluate(periode));
    }

    @Test
    void skalReturnereNeiNårAvvikErAkkurat25Prosent() {
        //Arrange
        var grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), new Inntektsgrunnlag(),
            Collections.singletonList(AktivitetStatus.ATFL), Collections.singletonList(arbeidsforhold));
        var periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        var sgPrStatus = SammenligningsGrunnlag.builder()
			    .medSammenligningsperiode(null)
			    .medSammenligningstype(SammenligningGrunnlagType.AT_FL)
			    .medRapportertPrÅr(BigDecimal.valueOf(100000)).build();
	    Beregningsgrunnlag.builder(grunnlag).leggTilSammenligningsgrunnlagPrStatus(sgPrStatus);
        var bgAT = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(bgAT).medBeregnetPrÅr(BigDecimal.valueOf(125000));

        //Act
        var resultat = new SjekkÅrsinntektMotSammenligningsgrunnlag().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.NEI);

	    var sgPrStatusResultat = grunnlag.getSammenligningsgrunnlagForStatus(SammenligningGrunnlagType.AT_FL).orElseThrow();
	    assertThat(sgPrStatusResultat.getAvvikPromilleUtenAvrunding()).isEqualByComparingTo(BigDecimal.valueOf(250));
	    assertThat(sgPrStatusResultat.getAvvikProsent()).isEqualByComparingTo(BigDecimal.valueOf(25));

    }

    @Test
    void skalReturnereJaNårAvvikErAkkuratOver25Prosent() {
        //Arrange
        var grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), new Inntektsgrunnlag(),
            Collections.singletonList(AktivitetStatus.ATFL), Collections.singletonList(arbeidsforhold));
        var periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        var sgPrStatus = SammenligningsGrunnlag.builder()
			    .medSammenligningstype(SammenligningGrunnlagType.AT_FL)
			    .medSammenligningsperiode(null)
			    .medRapportertPrÅr(BigDecimal.valueOf(100000)).build();
	    Beregningsgrunnlag.builder(grunnlag).leggTilSammenligningsgrunnlagPrStatus(sgPrStatus);
        var bgAT = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(bgAT).medBeregnetPrÅr(BigDecimal.valueOf(125001));

        //Act
        var resultat = new SjekkÅrsinntektMotSammenligningsgrunnlag().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.JA);

	    var sgPrStatusResultat = grunnlag.getSammenligningsgrunnlagForStatus(SammenligningGrunnlagType.AT_FL).orElseThrow();
	    assertThat(sgPrStatusResultat.getAvvikPromilleUtenAvrunding()).isEqualByComparingTo(BigDecimal.valueOf(250.01));
	    assertThat(sgPrStatusResultat.getAvvikProsent()).isEqualByComparingTo(BigDecimal.valueOf(25.001));
    }

    @Test
    void skalTesteAtAvvikKanReturneresMedFullNøyaktighet() {
        //Arrange
        var grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), new Inntektsgrunnlag(),
            Collections.singletonList(AktivitetStatus.ATFL), Collections.singletonList(arbeidsforhold));
        var periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        var sgPrStatus = SammenligningsGrunnlag.builder()
			    .medSammenligningstype(SammenligningGrunnlagType.AT_FL)
			    .medSammenligningsperiode(null)
			    .medRapportertPrÅr(BigDecimal.valueOf(100000)).build();
	    Beregningsgrunnlag.builder(grunnlag).leggTilSammenligningsgrunnlagPrStatus(sgPrStatus);
        var bgAT = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(bgAT).medBeregnetPrÅr(BigDecimal.valueOf(125001));

        //Act
        var resultat = new SjekkÅrsinntektMotSammenligningsgrunnlag().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.JA);

	    var sgPrStatusResultat = grunnlag.getSammenligningsgrunnlagForStatus(SammenligningGrunnlagType.AT_FL).orElseThrow();
	    assertThat(sgPrStatusResultat.getAvvikPromilleUtenAvrunding()).isEqualByComparingTo(BigDecimal.valueOf(250.010000000));
    }

}
