package no.nav.folketrygdloven.beregningsgrunnlag.vurder.frisinn;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØPLISTE;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2019;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;

class RegelVurderBeregningsgrunnlagFRISINNTest {


    public static final String ORGNR = "999999999";
    private final Offset<Double> offset = Offset.offset(0.01);

    private static final LocalDate skjæringstidspunkt = LocalDate.of(2020, Month.MARCH, 15);

    @Test
    void skalOppretteRegelmerknadForAvslagNårSNErUnderTreKvartG() {
        //Arrange
        var beregnetPrÅr = GRUNNBELØP_2019 * 0.74;
        var beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅr, null, null);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.merknader().stream().map(RegelMerknad::utfallÅrsak)).containsOnly(BeregningUtfallÅrsak.AVSLAG_UNDER_TREKVART_G);
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr, offset);
    }

    @Test
    void skalIkkeOppretteRegelmerknadForAvslagNårSNErLikTreKvartG() {
        //Arrange
        var beregnetPrÅr = GRUNNBELØP_2019 * 0.75;
        var beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅr, null, null);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.merknader()).isEmpty();
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr, offset);
    }

    @Test
    void skalIkkeOppretteRegelmerknadForAvslagNårSNErOverTreKvartG() {
        //Arrange
        var beregnetPrÅr = GRUNNBELØP_2019 * 0.76;
        var beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅr, null, null);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.merknader()).isEmpty();
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr, offset);
    }

    @Test
    void skalOppretteRegelmerknadForAvslagNårFLErUnderTreKvartG() {
        //Arrange
        var beregnetPrÅr = GRUNNBELØP_2019 * 0.74;
        var beregningsgrunnlag = lagBeregningsgrunnlag(null, beregnetPrÅr, null);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var resultat = kjørRegel(grunnlag);

        //Assert
	    assertThat(resultat.merknader().stream().map(RegelMerknad::utfallÅrsak)).containsOnly(BeregningUtfallÅrsak.AVSLAG_UNDER_TREKVART_G);
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr, offset);
    }

    @Test
    void skalIkkeOppretteRegelmerknadForAvslagNårFLErLikTreKvartG() {
        //Arrange
        var beregnetPrÅr = GRUNNBELØP_2019 * 0.75;
        var beregningsgrunnlag = lagBeregningsgrunnlag(null, beregnetPrÅr, null);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.merknader()).isEmpty();
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr, offset);
    }

    @Test
    void skalIkkeOppretteRegelmerknadForAvslagNårFLErOverTreKvartG() {
        //Arrange
        var beregnetPrÅr = GRUNNBELØP_2019 * 0.76;
        var beregningsgrunnlag = lagBeregningsgrunnlag(null, beregnetPrÅr, null);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.merknader()).isEmpty();
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr, offset);
    }

    @Test
    void skalOppretteRegelmerknadForAvslagNårFLOgSNErUnderTreKvartG() {
        //Arrange
        var beregnetPrÅrFL = GRUNNBELØP_2019 * 0.5;
        var beregnetPrÅrSN = GRUNNBELØP_2019 * 0.15;

        var beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅrSN, beregnetPrÅrFL, null);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var resultat = kjørRegel(grunnlag);

        //Assert
	    assertThat(resultat.merknader().stream().map(RegelMerknad::utfallÅrsak)).containsOnly(BeregningUtfallÅrsak.AVSLAG_UNDER_TREKVART_G);
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅrFL + beregnetPrÅrSN, offset);
    }

    @Test
    void skalIkkeOppretteRegelmerknadForAvslagNårFLOgSNErLikTreKvartG() {
        //Arrange
        var beregnetPrÅrFL = GRUNNBELØP_2019 * 0.5;
        var beregnetPrÅrSN = GRUNNBELØP_2019 * 0.25;

        var beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅrSN, beregnetPrÅrFL, null);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.merknader()).isEmpty();
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅrFL + beregnetPrÅrSN, offset);
    }

    @Test
    void skalIkkeOppretteRegelmerknadForAvslagNårFLOgSNErOverTreKvartGFLErUnder() {
        //Arrange
        var beregnetPrÅrFL = GRUNNBELØP_2019 * 0.5;
        double beregnetPrÅrSN = GRUNNBELØP_2019;
        var beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅrSN, beregnetPrÅrFL, null);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.merknader()).isEmpty();
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅrFL + beregnetPrÅrSN, offset);
    }

    @Test
    void skalIkkeOppretteRegelmerknadForAvslagNårFLOgSNErOverTreKvartGBeggeErOver() {
        //Arrange
        double beregnetPrÅrFL = GRUNNBELØP_2019;
        double beregnetPrÅrSN = GRUNNBELØP_2019;
        var beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅrSN, beregnetPrÅrFL, null);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.merknader()).isEmpty();
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅrFL + beregnetPrÅrSN, offset);
    }

    @Test
    void skalOppretteRegelmerknadForAvslagNårSNErUnderTreKvartGTotalsumErOver() {
        //Arrange
        var beregnetPrÅrSN = GRUNNBELØP_2019 * 0.74;
        var beregnetPrÅrAT = GRUNNBELØP_2019 * 0.5;
        var beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅrSN, null, beregnetPrÅrAT);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var resultat = kjørRegel(grunnlag);

        //Assert
	    assertThat(resultat.merknader().stream().map(RegelMerknad::utfallÅrsak)).containsOnly(BeregningUtfallÅrsak.AVSLAG_UNDER_TREKVART_G);
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅrSN + beregnetPrÅrAT, offset);
    }

    @Test
    void skalOppretteRegelmerknadForAvslagNårFLErUnderTreKvartGTotalsumErOver() {
        //Arrange
        var beregnetPrÅrFL = GRUNNBELØP_2019 * 0.74;
        var beregnetPrÅrAT = GRUNNBELØP_2019 * 0.5;
        var beregningsgrunnlag = lagBeregningsgrunnlag(null, beregnetPrÅrFL, beregnetPrÅrAT);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var resultat = kjørRegel(grunnlag);

        //Assert
	    assertThat(resultat.merknader().stream().map(RegelMerknad::utfallÅrsak)).containsOnly(BeregningUtfallÅrsak.AVSLAG_UNDER_TREKVART_G);
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅrFL + beregnetPrÅrAT, offset);
    }

    @Test
    void skalOppretteRegelmerknadForAvslagNårFLErUnderTreKvartGTATErOver() {
        //Arrange
        var beregnetPrÅrFL = GRUNNBELØP_2019 * 0.74;
        double beregnetPrÅrAT = GRUNNBELØP_2019;
        var beregningsgrunnlag = lagBeregningsgrunnlag(null, beregnetPrÅrFL, beregnetPrÅrAT);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var resultat = kjørRegel(grunnlag);

        //Assert
	    assertThat(resultat.merknader().stream().map(RegelMerknad::utfallÅrsak)).containsOnly(BeregningUtfallÅrsak.AVSLAG_UNDER_TREKVART_G);
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅrFL + beregnetPrÅrAT, offset);
    }

    private RegelResultat kjørRegel(BeregningsgrunnlagPeriode grunnlag) {
        return new RegelVurderBeregningsgrunnlagFRISINN().evaluerRegel(grunnlag);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag(Double snInntektPrÅr, Double frilansInntektPrÅr, Double arbeidsinntektPrÅr) {
        var periodeBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(new Periode(skjæringstidspunkt, null));
        byggSN(snInntektPrÅr, periodeBuilder);
        byggATFL(frilansInntektPrÅr, arbeidsinntektPrÅr, periodeBuilder);
        var periode = periodeBuilder.build();
        return Beregningsgrunnlag.builder()
            .medInntektsgrunnlag(new Inntektsgrunnlag())
            .medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2019))
            .medUregulertGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2019))
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .medGrunnbeløpSatser(GRUNNBELØPLISTE)
            .medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL_SN, null)))
            .medBeregningsgrunnlagPeriode(periode)
            .build();
    }

    private void byggSN(Double snInntektPrÅr, BeregningsgrunnlagPeriode.Builder periodeBuilder) {
        if (snInntektPrÅr != null) {
            periodeBuilder.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.SN)
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.valueOf(snInntektPrÅr))
                .build());
        }
    }

    private void byggATFL(Double frilansInntektPrÅr, Double arbeidsinntektPrÅr, BeregningsgrunnlagPeriode.Builder periodeBuilder) {
        if (frilansInntektPrÅr != null || arbeidsinntektPrÅr != null) {
            var atflStatusBuilder = BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.ATFL);
            if (frilansInntektPrÅr != null) {
                atflStatusBuilder.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                    .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                    .medBeregnetPrÅr(BigDecimal.valueOf(frilansInntektPrÅr))
                    .medAndelNr(2L)
                    .build());
            }
            if (arbeidsinntektPrÅr != null) {
                atflStatusBuilder.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                    .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR))
                    .medBeregnetPrÅr(BigDecimal.valueOf(arbeidsinntektPrÅr))
                    .medAndelNr(3L)
                    .build());
            }
            periodeBuilder.medBeregningsgrunnlagPrStatus(atflStatusBuilder.build());
        }
    }

}
