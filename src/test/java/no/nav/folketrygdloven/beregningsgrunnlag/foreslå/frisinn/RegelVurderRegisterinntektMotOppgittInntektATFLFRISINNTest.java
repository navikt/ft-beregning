package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

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
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.threeten.extra.Interval;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØPLISTE;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2019;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RegelVurderRegisterinntektMotOppgittInntektATFLFRISINNTest {
    private static final LocalDate STP = LocalDate.of(2020,4,1);
    private static final Arbeidsforhold ARBFOR_MED_REF = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999", "ARBFOR-REF");
    private static final Arbeidsforhold ARBFOR_UTEN_REF = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999");

    private Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();

    @Test
    public void skal_bare_returnere_beregnet_om_oppgitt_inntekt_ikke_finnes() {
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold arbfor = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medBeregnetPrÅr(beregnetPrÅr).medAndelNr(1L).build();
        BeregningsgrunnlagPrStatus andel = BeregningsgrunnlagPrStatus.builder().medArbeidsforhold(arbfor).medAktivitetStatus(AktivitetStatus.ATFL).build();
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, andel);

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag).build());

        // Assert
        assertArbfor(arbfor, beregnetPrÅr, beregnetPrÅr, null);
    }

    @Test
    public void skal_ikke_endre_inntekt_om_oppgitt_inntekt_er_lavere() {
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(100_000);
        BeregningsgrunnlagPrArbeidsforhold arbfor = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medBeregnetPrÅr(beregnetPrÅr).medAndelNr(1L).build();
        BeregningsgrunnlagPrStatus andel = BeregningsgrunnlagPrStatus.builder().medArbeidsforhold(arbfor).medAktivitetStatus(AktivitetStatus.ATFL).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggSøknadsinntekt(Periode.of(STP, LocalDate.of(2020,4,30)), BigDecimal.valueOf(5000)));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, andel);

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag).build());

        // Assert
        assertArbfor(arbfor, beregnetPrÅr, beregnetPrÅr, null);
    }

    @Test
    public void skal_endre_inntekt_dersom_oppgitt_inntekt_er_høyere_enn_beregnet() {
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(100_000);
        BigDecimal oppgitt = BigDecimal.valueOf(20000);
        BeregningsgrunnlagPrArbeidsforhold arbfor = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medBeregnetPrÅr(beregnetPrÅr).medAndelNr(1L).build();
        BeregningsgrunnlagPrStatus andel = BeregningsgrunnlagPrStatus.builder().medArbeidsforhold(arbfor).medAktivitetStatus(AktivitetStatus.ATFL).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggSøknadsinntekt(Periode.of(STP, LocalDate.of(2020,4,30)), oppgitt));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, andel);

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag).build());

        // Assert
        BigDecimal overstyrt = BigDecimal.valueOf(236363.64);
        assertArbfor(arbfor, overstyrt, beregnetPrÅr, overstyrt);
    }

    @Test
    public void skal_summere_grunnlag_for_at_ved_flere_arbfor() {
        BigDecimal beregnetPrÅr = BigDecimal.valueOf(100_000);
        BigDecimal oppgitt = BigDecimal.valueOf(20000);
        BeregningsgrunnlagPrArbeidsforhold arbfor = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medBeregnetPrÅr(beregnetPrÅr).medAndelNr(1L).build();
        BeregningsgrunnlagPrArbeidsforhold arbfor2 = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_UTEN_REF).medBeregnetPrÅr(beregnetPrÅr).medAndelNr(2L).build();
        BeregningsgrunnlagPrStatus andel = BeregningsgrunnlagPrStatus.builder().medArbeidsforhold(arbfor).medArbeidsforhold(arbfor2).medAktivitetStatus(AktivitetStatus.ATFL).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggSøknadsinntekt(Periode.of(STP, LocalDate.of(2020,4,30)), oppgitt));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, andel);

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag).build());

        // Assert
        BigDecimal overstyrt = BigDecimal.valueOf(236363.64);
        BigDecimal prAndel = overstyrt.divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_EVEN);
        assertArbfor(arbfor, prAndel, beregnetPrÅr, prAndel);
        assertArbfor(arbfor2, prAndel, beregnetPrÅr, prAndel);
    }

    @Test
    public void flere_andeler_overstiger_oppgitt_skal_ikke_bruke_oppgitt() {
        BigDecimal beregnetPrÅr1 = BigDecimal.valueOf(100_000);
        BigDecimal beregnetPrÅr2 = BigDecimal.valueOf(200_000);
        BigDecimal oppgitt = BigDecimal.valueOf(20000);
        BeregningsgrunnlagPrArbeidsforhold arbfor = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medBeregnetPrÅr(beregnetPrÅr1).medAndelNr(1L).build();
        BeregningsgrunnlagPrArbeidsforhold arbfor2 = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_UTEN_REF).medBeregnetPrÅr(beregnetPrÅr2).medAndelNr(2L).build();
        BeregningsgrunnlagPrStatus andel = BeregningsgrunnlagPrStatus.builder().medArbeidsforhold(arbfor).medArbeidsforhold(arbfor2).medAktivitetStatus(AktivitetStatus.ATFL).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggSøknadsinntekt(Periode.of(STP, LocalDate.of(2020,4,30)), oppgitt));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, andel);

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag).build());

        // Assert
        assertArbfor(arbfor, beregnetPrÅr1, beregnetPrÅr1, null);
        assertArbfor(arbfor2, beregnetPrÅr2, beregnetPrÅr2, null);
    }

    @Test
    public void flere_andeler_overstiger_ikke_oppgitt_skal_fordele_oppgitt_riktig() {
        BigDecimal beregnetPrÅr1 = BigDecimal.valueOf(40_000);
        BigDecimal beregnetPrÅr2 = BigDecimal.valueOf(60_000);
        BigDecimal oppgitt = BigDecimal.valueOf(20000);
        BeregningsgrunnlagPrArbeidsforhold arbfor = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medBeregnetPrÅr(beregnetPrÅr1).medAndelNr(1L).build();
        BeregningsgrunnlagPrArbeidsforhold arbfor2 = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_UTEN_REF).medBeregnetPrÅr(beregnetPrÅr2).medAndelNr(2L).build();
        BeregningsgrunnlagPrStatus andel = BeregningsgrunnlagPrStatus.builder().medArbeidsforhold(arbfor).medArbeidsforhold(arbfor2).medAktivitetStatus(AktivitetStatus.ATFL).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggSøknadsinntekt(Periode.of(STP, LocalDate.of(2020,4,30)), oppgitt));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, andel);

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag).build());

        // Assert
        BigDecimal overstyrtTotal = BigDecimal.valueOf(236363.64);
        BigDecimal overstyrt1 = overstyrtTotal.multiply(BigDecimal.valueOf(0.4));
        BigDecimal overstyrt2 = overstyrtTotal.multiply(BigDecimal.valueOf(0.6));
        assertArbfor(arbfor, overstyrt1, beregnetPrÅr1, overstyrt1);
        assertArbfor(arbfor2, overstyrt2, beregnetPrÅr2, overstyrt2);
    }

    @Test
    public void flere_andeler_med_samlet_inntekt_0_skal_fordele_oppgitt_likt() {
        BigDecimal beregnetPrÅr1 = BigDecimal.valueOf(0);
        BigDecimal beregnetPrÅr2 = BigDecimal.valueOf(0);
        BigDecimal oppgitt = BigDecimal.valueOf(20000);
        BeregningsgrunnlagPrArbeidsforhold arbfor = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medBeregnetPrÅr(beregnetPrÅr1).medAndelNr(1L).build();
        BeregningsgrunnlagPrArbeidsforhold arbfor2 = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_UTEN_REF).medBeregnetPrÅr(beregnetPrÅr2).medAndelNr(2L).build();
        BeregningsgrunnlagPrStatus andel = BeregningsgrunnlagPrStatus.builder().medArbeidsforhold(arbfor).medArbeidsforhold(arbfor2).medAktivitetStatus(AktivitetStatus.ATFL).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggSøknadsinntekt(Periode.of(STP, LocalDate.of(2020,4,30)), oppgitt));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, andel);

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag).build());

        // Assert
        BigDecimal overstyrtTotal = BigDecimal.valueOf(236363.64);
        BigDecimal overstyrtPr = overstyrtTotal.divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_EVEN);
        assertArbfor(arbfor, overstyrtPr, beregnetPrÅr1, overstyrtPr);
        assertArbfor(arbfor2, overstyrtPr, beregnetPrÅr2, overstyrtPr);
    }


    private void assertArbfor(BeregningsgrunnlagPrArbeidsforhold arbfor, BigDecimal brutto, BigDecimal beregnet, BigDecimal overstyrt) {
        assertThat(arbfor.getBruttoPrÅr().doubleValue()).isCloseTo(brutto.doubleValue(), within(0.01d));
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
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        new RegelVurderRegisterinntektMotOppgittInntektATFLFRISINN().evaluate(grunnlag);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag(Inntektsgrunnlag ig, BeregningsgrunnlagPrStatus... andeler) {
        BeregningsgrunnlagPeriode.Builder periodeBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(new Periode(STP, null));
        List<BeregningsgrunnlagPrStatus> andelsliste = Arrays.asList(andeler);
        andelsliste.forEach(periodeBuilder::medBeregningsgrunnlagPrStatus);
        BeregningsgrunnlagPeriode periode = periodeBuilder.build();
        FrisinnGrunnlag frisinnGrunnlag = new FrisinnGrunnlag(true, STP);
        return Beregningsgrunnlag.builder()
            .medInntektsgrunnlag(ig)
            .medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2019))
            .medSkjæringstidspunkt(STP)
            .medAntallGMinstekravVilkår(BigDecimal.valueOf(0.75))
            .medGrunnbeløpSatser(GRUNNBELØPLISTE)
            .medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL_SN, null)))
            .medBeregningsgrunnlagPeriode(periode)
            .medYtelsesSpesifiktGrunnlag(frisinnGrunnlag)
            .build();
    }


}
