package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettSammenligningsgrunnlagIPeriode;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;

class FastsettSammenligningsgrunnlagTest {

    /*
    Scenarioer i testene er tatt fra https://confluence.adeo.no/display/MODNAV/3b+Fastsette+sammenligningsgrunnlagsperiode#FunksjonellogUX
     */

    //Eksempel 1
    @Test
    void sammenligningsgrunnlagFørFristMedSisteInntektIkkeRapportert() {
        //Arrange
        var behandlingsdato = LocalDate.of(2019, 4, 3);
        var skjæringstidspunkt = LocalDate.of(2019, 5, 1);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        var periode = Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2019, 2, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.ATFL);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlag().evaluate(grunnlag, behandlingsdato);
        //Assert
        var sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.AT_FL);
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 3, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 2, 28));
    }

    //Eksempel 2
    @Test
    void sammenligningsgrunnlagFørFristMedSisteInntektRapportert() {
        //Arrange
        var behandlingsdato = LocalDate.of(2019, 4, 3);
        var skjæringstidspunkt = LocalDate.of(2019, 5, 1);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        var periode = Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2019, 4, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.ATFL);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlag().evaluate(grunnlag, behandlingsdato);
        //Assert
        var sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.AT_FL);
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 3, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 2, 28));
    }

    //Eksempel 3
    @Test
    void sammenligningsgrunnlagFørIHelgFristMedSisteInntektIkkeRapportert() {
        //Arrange
        var behandlingsdato = LocalDate.of(2019, 1, 7);
        var skjæringstidspunkt = LocalDate.of(2019, 2, 1);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        var periode = Periode.of(LocalDate.of(2017, 10, 1), LocalDate.of(2018, 11, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.ATFL);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlag().evaluate(grunnlag, behandlingsdato);
        //Assert
        var sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.AT_FL);
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2017, 12, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2018, 11, 30));
    }

    @Test
    void sammenligningsgrunnlagEtterFristMedSisteInntektIkkeRapportert() {
        //Arrange
        var behandlingsdato = LocalDate.of(2019, 10, 8);
        var skjæringstidspunkt = LocalDate.of(2019, 11, 1);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        var periode = Periode.of(LocalDate.of(2018, 10, 1), LocalDate.of(2019, 8, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.ATFL);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlag().evaluate(grunnlag, behandlingsdato);
        //Assert
        var sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.AT_FL);
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 10, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 9, 30));
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(275000));
    }

    @Test
    void sammenligningsgrunnlagEtterFristMedSisteInntektRapportert() {
        //Arrange
	    var behandlingsdato = LocalDate.of(2019, 10, 8);
        var skjæringstidspunkt = LocalDate.of(2019, 11, 1);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        var periode = Periode.of(LocalDate.of(2018, 10, 1), LocalDate.of(2019, 9, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.ATFL);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlag().evaluate(grunnlag, behandlingsdato);
        //Assert
        var sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.AT_FL);
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 10, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 9, 30));
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
    }

    //Eksempel 4
    @Test
    void sammenligningsgrunnlagBehandlingEtterStp() {
        //Arrange
        var behandlingsdato = LocalDate.of(2019, 5, 11);
        var skjæringstidspunkt = LocalDate.of(2019, 5, 1);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        var periode = Periode.of(LocalDate.of(2017, 10, 1), LocalDate.of(2018, 12, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.ATFL);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlag().evaluate(grunnlag, behandlingsdato);
        //Assert
        var sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.AT_FL);
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(200000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 5, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 4, 30));
    }

    @Test
    void sammenligningsgrunnlagBehandlingLengeEtterStp() {
        //Arrange
        var behandlingsdato = LocalDate.of(2019, 6, 11);
        var skjæringstidspunkt = LocalDate.of(2019, 5, 1);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        var periode = Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2019, 12, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.ATFL);
        var periode2 = Periode.of(LocalDate.of(2019,5,1), LocalDate.of(2019, 6, 30));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode2, BigDecimal.valueOf(333333), AktivitetStatus.ATFL);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlag().evaluate(grunnlag, behandlingsdato);
        //Assert
        var sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.AT_FL);
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 5, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 4, 30));
    }

    @Test
    void skalIkkeLageNyttSammenligningsgrunnlagNårAlleredeEksisterer() {
        //Arrange
        var skjæringstidspunkt = LocalDate.of(2018, 10, 10);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        var periode = Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 12, 31));
        var sg = SammenligningsGrunnlag.builder()
            .medSammenligningsperiode(periode)
	        .medSammenligningstype(SammenligningGrunnlagType.AT_FL)
            .medRapportertPrÅr(BigDecimal.valueOf(55)).build();
        Beregningsgrunnlag.builder(beregningsgrunnlag).leggTilSammenligningsgrunnlagPrStatus(sg);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlag().evaluate(grunnlag);
        //Assert
        var hentetSg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.AT_FL);
        assertThat(hentetSg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(55));
        assertThat(hentetSg.getSammenligningsperiode().getFom()).isEqualTo(periode.getFom());
        assertThat(hentetSg.getSammenligningsperiode().getTom()).isEqualTo(periode.getTom());
    }
}
