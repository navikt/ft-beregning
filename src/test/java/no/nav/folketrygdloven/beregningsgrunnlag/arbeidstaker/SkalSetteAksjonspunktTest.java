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
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

public class SkalSetteAksjonspunktTest {
    private Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("12345");
    private long gVerdi = 99858L;

    @Test
    public void skalSetteAksjonspunktNårDetUtbetalesPengerDirekteTilBrukerOgRefusjonSkalSjekkesFørAvviksvurdering() {
        //Arrange
        BigDecimal maksRefusjonForPeriode = BigDecimal.valueOf(400_000);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(450_000);

        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(beregnetPrÅr, maksRefusjonForPeriode, true);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation resultat = new SkalSetteAksjonspunkt().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.JA);
    }

    @Test
    public void skalIkkeSetteAksjonspunktNårRefusjonTilsvarer6GOgRefusjonSkalSjekkesFørAvviksvurdering() {
        //Arrange
        BigDecimal maksRefusjonForPeriode = BigDecimal.valueOf(gVerdi*6);
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(650_000);

        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(beregnetPrÅr, maksRefusjonForPeriode, true);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation resultat = new SkalSetteAksjonspunkt().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void skalIkkeSetteAksjonspunktNårRefusjonTilsvarerBeregnetOgRefusjonSkalSjekkesFørAvviksvurdering() {
        //Arrange
        BigDecimal maksRefusjonForPeriode = BigDecimal.valueOf(350_000);
        BigDecimal beregnetPrÅr = maksRefusjonForPeriode;

        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(beregnetPrÅr, maksRefusjonForPeriode, true);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation resultat = new SkalSetteAksjonspunkt().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void skalSetteAksjonspunktNårRefusjonIkkeSkalSjekkesFørAvviksvurdering() {
        //Arrange
        BigDecimal maksRefusjonForPeriode = BigDecimal.valueOf(350_000);
        BigDecimal beregnetPrÅr = maksRefusjonForPeriode;

        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(beregnetPrÅr, maksRefusjonForPeriode, false);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation resultat = new SkalSetteAksjonspunkt().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.JA);
    }

    @Test
    public void skalSetteAksjonspunktNårBrukerErFrilanser() {
        //Arrange
        BigDecimal maksRefusjonForPeriode = BigDecimal.valueOf(350_000);
        BigDecimal beregnetPrÅr = maksRefusjonForPeriode;

        BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medBeregnetPrÅr(beregnetPrÅr)
                .medArbeidsforhold(arbeidsforhold)
                .medAndelNr(1L).build())
            .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                .medBeregnetPrÅr(beregnetPrÅr)
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .medAndelNr(2L).build())
            .build();
        BeregningsgrunnlagPeriode bgPeriode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(now(), null))
            .medBeregningsgrunnlagPrStatus(beregningsgrunnlagPrStatus)
            .build();

        Beregningsgrunnlag beregningsgrunnlag =  Beregningsgrunnlag.builder()
            .medInntektsgrunnlag(new Inntektsgrunnlag())
            .medSkjæringstidspunkt(now())
            .medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
            .medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_ARBEIDSTAKER)))
            .medBeregningsgrunnlagPeriode(bgPeriode)
            .medYtelsesSpesifiktGrunnlag(new OmsorgspengerGrunnlag(maksRefusjonForPeriode))
            .medGrunnbeløpSatser(List.of(
                new Grunnbeløp(LocalDate.of(2019, 5, 1), LocalDate.MAX, gVerdi, GSNITT_2019)))
            .build();

        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation resultat = new SkalSetteAksjonspunkt().evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.JA);
    }

    private Beregningsgrunnlag opprettBeregningsgrunnlag(BigDecimal beregnetPrÅr, BigDecimal maksRefusjonForPeriode, boolean avviksVurdere){
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
            .medYtelsesSpesifiktGrunnlag(avviksVurdere ? new OmsorgspengerGrunnlag(maksRefusjonForPeriode) : null)
            .medGrunnbeløpSatser(List.of(
                new Grunnbeløp(LocalDate.of(2019, 5, 1), LocalDate.MAX, gVerdi, GSNITT_2019)))
            .build();
    }
}
