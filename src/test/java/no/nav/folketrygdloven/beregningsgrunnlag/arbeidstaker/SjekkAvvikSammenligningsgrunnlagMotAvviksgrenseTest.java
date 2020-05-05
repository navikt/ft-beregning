package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import static java.time.LocalDate.now;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2019;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlagPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

public class SjekkAvvikSammenligningsgrunnlagMotAvviksgrenseTest {
    private Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("12345");
    private long gVerdi = 99858L;

    @Test
    public void skalSetteAksjonspunktkNårDetUtbetalesPengerDirekteTilBrukerOgRefusjonSkalSjekkesFørAvviksvurderingOgAvvik() {
        //Arrange
        BigDecimal maksRefusjonForPeriode = BigDecimal.valueOf(400_000);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(450_000);
        BigDecimal avvik = BigDecimal.valueOf(26);

        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(beregnetPrÅr, maksRefusjonForPeriode, true, avvik);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation resultat = new SjekkAvvikSammenligningsgrunnlagMotAvviksgrense().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.JA);
    }

    @Test
    public void skalIkkeSetteAksjonspunktNårRefusjonTilsvarer6GOgRefusjonSkalSjekkesFørAvviksvurderingOgAvvik() {
        //Arrange
        BigDecimal maksRefusjonForPeriode = BigDecimal.valueOf(gVerdi*6);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(650_000);
        BigDecimal avvik = BigDecimal.valueOf(26);

        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(beregnetPrÅr, maksRefusjonForPeriode, true, avvik);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation resultat = new SjekkAvvikSammenligningsgrunnlagMotAvviksgrense().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void skalIkkeSetteAksjonspunktNårRefusjonTilsvarerBeregnetOgRefusjonSkalSjekkesFørAvviksvurderingOgAvvik() {
        //Arrange
        BigDecimal maksRefusjonForPeriode = BigDecimal.valueOf(350_000);
        BigDecimal beregnetPrÅr = maksRefusjonForPeriode;
        BigDecimal avvik = BigDecimal.valueOf(26);

        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(beregnetPrÅr, maksRefusjonForPeriode, true, avvik);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation resultat = new SjekkAvvikSammenligningsgrunnlagMotAvviksgrense().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void skalIkkeSetteAksjonspunktNårOmsorgspengerOgUtbetalesDirekteTilBrukerOgIkkeAvvik() {
        //Arrange
        BigDecimal maksRefusjonForPeriode = BigDecimal.valueOf(400_000);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(450_000);
        BigDecimal avvikProsent = BigDecimal.valueOf(24);

        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(beregnetPrÅr, maksRefusjonForPeriode, true, avvikProsent);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation resultat = new SjekkAvvikSammenligningsgrunnlagMotAvviksgrense().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void skalIkkeSetteAksjonspunktNårIkkeOmsorgspengerOgIkkeAvvik() {
        //Arrange
        BigDecimal maksRefusjonForPeriode = BigDecimal.valueOf(350_000);
        BigDecimal beregnetPrÅr = maksRefusjonForPeriode;
        BigDecimal avvikProsent = BigDecimal.valueOf(20);

        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(beregnetPrÅr, maksRefusjonForPeriode, false, avvikProsent);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation resultat = new SjekkAvvikSammenligningsgrunnlagMotAvviksgrense().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void skalSetteAksjonspunktNårIkkeOmsorgspengerOgAvvik() {
        //Arrange
        BigDecimal maksRefusjonForPeriode = BigDecimal.valueOf(350_000);
        BigDecimal beregnetPrÅr = maksRefusjonForPeriode;
        BigDecimal avvikProsent = BigDecimal.valueOf(27);

        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(beregnetPrÅr, maksRefusjonForPeriode, false, avvikProsent);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation resultat = new SjekkAvvikSammenligningsgrunnlagMotAvviksgrense().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.JA);
    }

    @Test
    public void skalReturnereNeiNårAvvikErAkkurat25Prosent() {
        //Arrange
        BigDecimal maksRefusjonForPeriode = BigDecimal.valueOf(350_000);
        BigDecimal beregnetPrÅr = maksRefusjonForPeriode;
        BigDecimal avvikProsent = BigDecimal.valueOf(25);

        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(beregnetPrÅr, maksRefusjonForPeriode, false, avvikProsent);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation resultat = new SjekkAvvikSammenligningsgrunnlagMotAvviksgrense().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void skalReturnereJaNårAvvikErAkkurat25Prosent() {
        //Arrange
        BigDecimal maksRefusjonForPeriode = BigDecimal.valueOf(350_000);
        BigDecimal beregnetPrÅr = maksRefusjonForPeriode;
        BigDecimal avvikProsent = BigDecimal.valueOf(25.001);

        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(beregnetPrÅr, maksRefusjonForPeriode, false, avvikProsent);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation resultat = new SjekkAvvikSammenligningsgrunnlagMotAvviksgrense().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.JA);
    }


    private Beregningsgrunnlag opprettBeregningsgrunnlag(BigDecimal beregnetPrÅr, BigDecimal maksRefusjonForPeriode, boolean avviksVurdere, BigDecimal sgAvvik){
        BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medBeregnetPrÅr(beregnetPrÅr)
                .medArbeidsforhold(arbeidsforhold)
                .medAndelNr(1L).build())
            .build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(now(), null))
            .medBeregningsgrunnlagPrStatus(beregningsgrunnlagPrStatus)
            .build();

        return Beregningsgrunnlag.builder()
            .medInntektsgrunnlag(new Inntektsgrunnlag())
            .medSkjæringstidspunkt(now())
            .medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
            .medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_ARBEIDSTAKER)))
            .medBeregningsgrunnlagPeriode(periode)
            .medSammenligningsgrunnlag(SammenligningsGrunnlag.builder().medAvvikProsent(sgAvvik).build())
            .medYtelsesSpesifiktGrunnlag(avviksVurdere ? new OmsorgspengerGrunnlag(maksRefusjonForPeriode) : null)
            .medGrunnbeløpSatser(List.of(
                new Grunnbeløp(LocalDate.of(2019, 5, 1), LocalDate.MAX, gVerdi, GSNITT_2019)))
            .build();
    }
}
