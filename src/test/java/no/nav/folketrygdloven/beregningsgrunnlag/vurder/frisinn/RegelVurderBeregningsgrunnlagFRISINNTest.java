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


    public static final String ORGNR = "14263547852";
    private static Long generatedId = 1L;
    private final Offset<Double> offset = Offset.offset(0.01);

    private static final LocalDate skjæringstidspunkt = LocalDate.of(2020, Month.MARCH, 15);

    @Test
    public void skalOppretteRegelmerknadForAvslagNårSNErUnderTreKvartG() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2019 * 0.74;
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅr, null, null);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode)).containsOnly("1041");
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr, offset);
    }

    @Test
    public void skalIkkeOppretteRegelmerknadForAvslagNårSNErLikTreKvartG() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2019 * 0.75;
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅr, null, null);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader()).hasSize(0);
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr, offset);
    }

    @Test
    public void skalIkkeOppretteRegelmerknadForAvslagNårSNErOverTreKvartG() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2019 * 0.76;
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅr, null, null);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader()).hasSize(0);
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr, offset);
    }

    @Test
    public void skalOppretteRegelmerknadForAvslagNårFLErUnderTreKvartG() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2019 * 0.74;
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(null, beregnetPrÅr, null);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode)).containsOnly("1041");
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr, offset);
    }

    @Test
    public void skalIkkeOppretteRegelmerknadForAvslagNårFLErLikTreKvartG() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2019 * 0.75;
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(null, beregnetPrÅr, null);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader()).hasSize(0);
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr, offset);
    }

    @Test
    public void skalIkkeOppretteRegelmerknadForAvslagNårFLErOverTreKvartG() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2019 * 0.76;
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(null, beregnetPrÅr, null);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader()).hasSize(0);
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr, offset);
    }

    @Test
    public void skalOppretteRegelmerknadForAvslagNårFLOgSNErUnderTreKvartG() {
        //Arrange
        double beregnetPrÅrFL = GRUNNBELØP_2019 * 0.5;
        double beregnetPrÅrSN = GRUNNBELØP_2019 * 0.15;

        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅrSN, beregnetPrÅrFL, null);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode)).containsOnly("1041");
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅrFL + beregnetPrÅrSN, offset);
    }

    @Test
    public void skalIkkeOppretteRegelmerknadForAvslagNårFLOgSNErLikTreKvartG() {
        //Arrange
        double beregnetPrÅrFL = GRUNNBELØP_2019 * 0.5;
        double beregnetPrÅrSN = GRUNNBELØP_2019 * 0.25;

        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅrSN, beregnetPrÅrFL, null);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader()).hasSize(0);
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅrFL + beregnetPrÅrSN, offset);
    }

    @Test
    public void skalIkkeOppretteRegelmerknadForAvslagNårFLOgSNErOverTreKvartGFLErUnder() {
        //Arrange
        double beregnetPrÅrFL = GRUNNBELØP_2019 * 0.5;
        double beregnetPrÅrSN = GRUNNBELØP_2019;
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅrSN, beregnetPrÅrFL, null);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader()).hasSize(0);
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅrFL + beregnetPrÅrSN, offset);
    }

    @Test
    public void skalIkkeOppretteRegelmerknadForAvslagNårFLOgSNErOverTreKvartGBeggeErOver() {
        //Arrange
        double beregnetPrÅrFL = GRUNNBELØP_2019;
        double beregnetPrÅrSN = GRUNNBELØP_2019;
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅrSN, beregnetPrÅrFL, null);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader()).hasSize(0);
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅrFL + beregnetPrÅrSN, offset);
    }

    @Test
    public void skalOppretteRegelmerknadForAvslagNårSNErUnderTreKvartGTotalsumErOver() {
        //Arrange
        double beregnetPrÅrSN = GRUNNBELØP_2019 * 0.74;
        double beregnetPrÅrAT = GRUNNBELØP_2019 * 0.5;
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(beregnetPrÅrSN, null, beregnetPrÅrAT);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode)).containsOnly("1041");
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅrSN + beregnetPrÅrAT, offset);
    }

    @Test
    public void skalOppretteRegelmerknadForAvslagNårFLErUnderTreKvartGTotalsumErOver() {
        //Arrange
        double beregnetPrÅrFL = GRUNNBELØP_2019 * 0.74;
        double beregnetPrÅrAT = GRUNNBELØP_2019 * 0.5;
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(null, beregnetPrÅrFL, beregnetPrÅrAT);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode)).containsOnly("1041");
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅrFL + beregnetPrÅrAT, offset);
    }

    @Test
    public void skalOppretteRegelmerknadForAvslagNårFLErUnderTreKvartGTATErOver() {
        //Arrange
        double beregnetPrÅrFL = GRUNNBELØP_2019 * 0.74;
        double beregnetPrÅrAT = GRUNNBELØP_2019;
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(null, beregnetPrÅrFL, beregnetPrÅrAT);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode)).containsOnly("1041");
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅrFL + beregnetPrÅrAT, offset);
    }

    private RegelResultat kjørRegel(BeregningsgrunnlagPeriode grunnlag) {
        return new RegelVurderBeregningsgrunnlagFRISINN().evaluerRegel(grunnlag);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag(Double snInntektPrÅr, Double frilansInntektPrÅr, Double arbeidsinntektPrÅr) {
        BeregningsgrunnlagPeriode.Builder periodeBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(new Periode(skjæringstidspunkt, null));
        byggSN(snInntektPrÅr, periodeBuilder);
        byggATFL(frilansInntektPrÅr, arbeidsinntektPrÅr, periodeBuilder);
        BeregningsgrunnlagPeriode periode = periodeBuilder.build();
        return Beregningsgrunnlag.builder()
            .medInntektsgrunnlag(new Inntektsgrunnlag())
            .medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2019))
            .medUregulertGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2019))
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .medAntallGMinstekravVilkår(BigDecimal.valueOf(0.75))
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
            BeregningsgrunnlagPrStatus.Builder atflStatusBuilder = BeregningsgrunnlagPrStatus.builder()
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
