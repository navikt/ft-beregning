package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØPLISTE;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2019;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnPeriode;

class RegelVurderRegisterinntektMotOppgittInntektATFLFRISINNTest {
    private static final LocalDate STP = LocalDate.of(2020,4,1);
    private static final Arbeidsforhold ARBFOR_MED_REF = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999", "ARBFOR-REF");
    private static final Arbeidsforhold ARBFOR_UTEN_REF = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999");

    private Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();

    @Test
    void skal_bare_returnere_beregnet_om_oppgitt_inntekt_ikke_finnes() {
        var beregnetPrÅr = BigDecimal.valueOf(100_000);
        var arbfor = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medBeregnetPrÅr(beregnetPrÅr).medAndelNr(1L).build();
        var andel = BeregningsgrunnlagPrStatus.builder().medArbeidsforhold(arbfor).medAktivitetStatus(AktivitetStatus.ATFL).build();
        var beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, andel);

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag).build());

        // Assert
        assertArbfor(arbfor, beregnetPrÅr, beregnetPrÅr, null);
    }

    @Test
    void skal_ikke_endre_inntekt_om_oppgitt_inntekt_er_lavere() {
        var beregnetPrÅr = BigDecimal.valueOf(100_000);
        var arbfor = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medBeregnetPrÅr(beregnetPrÅr).medAndelNr(1L).build();
        var andel = BeregningsgrunnlagPrStatus.builder().medArbeidsforhold(arbfor).medAktivitetStatus(AktivitetStatus.ATFL).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggSøknadsinntekt(Periode.of(STP, LocalDate.of(2020,4,30)), BigDecimal.valueOf(5000)));
        var beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, andel);

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag).build());

        // Assert
        assertArbfor(arbfor, beregnetPrÅr, beregnetPrÅr, null);
    }

    @Test
    void skal_endre_inntekt_dersom_oppgitt_inntekt_er_høyere_enn_beregnet() {
        var beregnetPrÅr = BigDecimal.valueOf(100_000);
        var oppgitt = BigDecimal.valueOf(20000);
        var arbfor = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medBeregnetPrÅr(beregnetPrÅr).medAndelNr(1L).build();
        var andel = BeregningsgrunnlagPrStatus.builder().medArbeidsforhold(arbfor).medAktivitetStatus(AktivitetStatus.ATFL).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggSøknadsinntekt(Periode.of(STP, LocalDate.of(2020,4,30)), oppgitt));
        var beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, andel);

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag).build());

        // Assert
        var overstyrt = BigDecimal.valueOf(236363.64);
        assertArbfor(arbfor, overstyrt, beregnetPrÅr, overstyrt);
    }

    @Test
    void skal_summere_grunnlag_for_at_ved_flere_arbfor() {
        var beregnetPrÅr = BigDecimal.valueOf(100_000);
        var oppgitt = BigDecimal.valueOf(20000);
        var arbfor = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medBeregnetPrÅr(beregnetPrÅr).medAndelNr(1L).build();
        var arbfor2 = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_UTEN_REF).medBeregnetPrÅr(beregnetPrÅr).medAndelNr(2L).build();
        var andel = BeregningsgrunnlagPrStatus.builder().medArbeidsforhold(arbfor).medArbeidsforhold(arbfor2).medAktivitetStatus(AktivitetStatus.ATFL).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggSøknadsinntekt(Periode.of(STP, LocalDate.of(2020,4,30)), oppgitt));
        var beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, andel);

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag).build());

        // Assert
        var overstyrt = BigDecimal.valueOf(236363.64);
        var prAndel = overstyrt.divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_EVEN);
        assertArbfor(arbfor, prAndel, beregnetPrÅr, prAndel);
        assertArbfor(arbfor2, prAndel, beregnetPrÅr, prAndel);
    }

    @Test
    void flere_andeler_overstiger_oppgitt_skal_ikke_bruke_oppgitt() {
        var beregnetPrÅr1 = BigDecimal.valueOf(100_000);
        var beregnetPrÅr2 = BigDecimal.valueOf(200_000);
        var oppgitt = BigDecimal.valueOf(20000);
        var arbfor = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medBeregnetPrÅr(beregnetPrÅr1).medAndelNr(1L).build();
        var arbfor2 = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_UTEN_REF).medBeregnetPrÅr(beregnetPrÅr2).medAndelNr(2L).build();
        var andel = BeregningsgrunnlagPrStatus.builder().medArbeidsforhold(arbfor).medArbeidsforhold(arbfor2).medAktivitetStatus(AktivitetStatus.ATFL).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggSøknadsinntekt(Periode.of(STP, LocalDate.of(2020,4,30)), oppgitt));
        var beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, andel);

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag).build());

        // Assert
        assertArbfor(arbfor, beregnetPrÅr1, beregnetPrÅr1, null);
        assertArbfor(arbfor2, beregnetPrÅr2, beregnetPrÅr2, null);
    }

    @Test
    void flere_andeler_overstiger_ikke_oppgitt_skal_fordele_oppgitt_riktig() {
        var beregnetPrÅr1 = BigDecimal.valueOf(40_000);
        var beregnetPrÅr2 = BigDecimal.valueOf(60_000);
        var oppgitt = BigDecimal.valueOf(20000);
        var arbfor = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medBeregnetPrÅr(beregnetPrÅr1).medAndelNr(1L).build();
        var arbfor2 = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_UTEN_REF).medBeregnetPrÅr(beregnetPrÅr2).medAndelNr(2L).build();
        var andel = BeregningsgrunnlagPrStatus.builder().medArbeidsforhold(arbfor).medArbeidsforhold(arbfor2).medAktivitetStatus(AktivitetStatus.ATFL).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggSøknadsinntekt(Periode.of(STP, LocalDate.of(2020,4,30)), oppgitt));
        var beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, andel);

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag).build());

        // Assert
        var overstyrtTotal = BigDecimal.valueOf(236363.64);
        var overstyrt1 = overstyrtTotal.multiply(BigDecimal.valueOf(0.4));
        var overstyrt2 = overstyrtTotal.multiply(BigDecimal.valueOf(0.6));
        assertArbfor(arbfor, overstyrt1, beregnetPrÅr1, overstyrt1);
        assertArbfor(arbfor2, overstyrt2, beregnetPrÅr2, overstyrt2);
    }

    @Test
    void flere_andeler_med_samlet_inntekt_0_skal_fordele_oppgitt_likt() {
        var beregnetPrÅr1 = BigDecimal.valueOf(0);
        var beregnetPrÅr2 = BigDecimal.valueOf(0);
        var oppgitt = BigDecimal.valueOf(20000);
        var arbfor = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medBeregnetPrÅr(beregnetPrÅr1).medAndelNr(1L).build();
        var arbfor2 = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_UTEN_REF).medBeregnetPrÅr(beregnetPrÅr2).medAndelNr(2L).build();
        var andel = BeregningsgrunnlagPrStatus.builder().medArbeidsforhold(arbfor).medArbeidsforhold(arbfor2).medAktivitetStatus(AktivitetStatus.ATFL).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggSøknadsinntekt(Periode.of(STP, LocalDate.of(2020,4,30)), oppgitt));
        var beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, andel);

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag).build());

        // Assert
        var overstyrtTotal = BigDecimal.valueOf(236363.64);
        var overstyrtPr = overstyrtTotal.divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_EVEN);
        assertArbfor(arbfor, overstyrtPr, beregnetPrÅr1, overstyrtPr);
        assertArbfor(arbfor2, overstyrtPr, beregnetPrÅr2, overstyrtPr);
    }


    private void assertArbfor(BeregningsgrunnlagPrArbeidsforhold arbfor, BigDecimal brutto, BigDecimal beregnet, BigDecimal overstyrt) {
        assertThat(arbfor.getBruttoPrÅr().get().doubleValue()).isCloseTo(brutto.doubleValue(), within(0.01d));
        assertThat(arbfor.getBeregnetPrÅr().doubleValue()).isCloseTo(beregnet.doubleValue(), within(0.01d));
        if (overstyrt == null) {
            assertThat(arbfor.getOverstyrtPrÅr()).isNull();
        } else {
            assertThat(arbfor.getOverstyrtPrÅr().doubleValue()).isCloseTo(overstyrt.doubleValue(), within(0.01d));
        }
    }

    private Periodeinntekt byggSøknadsinntekt(Periode periode, BigDecimal beløp) {
        return Periodeinntekt.builder()
            .medPeriode(periode)
            .medInntekt(beløp)
            .medInntektskildeOgPeriodeType(Inntektskilde.SØKNAD)
            .medAktivitetStatus(AktivitetStatus.AT)
            .build();
    }

    private void kjørRegel(Beregningsgrunnlag beregningsgrunnlag) {
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        new RegelVurderRegisterinntektMotOppgittInntektATFLFRISINN().evaluate(grunnlag);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag(Inntektsgrunnlag ig, BeregningsgrunnlagPrStatus... andeler) {
        var periodeBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(new Periode(STP, null));
        var andelsliste = Arrays.asList(andeler);
        andelsliste.forEach(periodeBuilder::medBeregningsgrunnlagPrStatus);
        var periode = periodeBuilder.build();
        var frisinnPerioder = Collections.singletonList(new FrisinnPeriode(periode.getBeregningsgrunnlagPeriode(), true, false));
        var frisinnGrunnlag = new FrisinnGrunnlag(frisinnPerioder, List.of(periode.getBeregningsgrunnlagPeriode()), STP);
        return Beregningsgrunnlag.builder()
            .medInntektsgrunnlag(ig)
            .medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2019))
            .medSkjæringstidspunkt(STP)
            .medGrunnbeløpSatser(GRUNNBELØPLISTE)
            .medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL_SN, null)))
            .medBeregningsgrunnlagPeriode(periode)
            .medYtelsesSpesifiktGrunnlag(frisinnGrunnlag)
            .build();
    }


}
