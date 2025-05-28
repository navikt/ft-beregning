package no.nav.folketrygdloven.beregningsgrunnlag.avkorting;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserFastsettBeregningsgrunnlag.verifiserBeregningsgrunnlagAvkortetPrÅr;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.RegelFullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;

/**
 * Testen kjøres når Regelflyt 13 blir implementert
 */

class RegelFastsettAvkortetBGNårRefusjonOver6GTest {

    private final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
    private final long seksG = GRUNNBELØP_2017 * 6;

    private static final String ORGNR1 = "123";
    private static final String ORGNR2 = "456";
    private static final String ORGNR3 = "789";
    private static final String ORGNR4 = "101112";

    @Test
    //Scenario 8: Alle arbeidsgivere har refusjonskrav lik brukers brutto beregningsgrunnlag
    void skalBeregneNårRefusjonKravLikBruttoBGForBeggeToArbeidsgivere() {
        //Arrange
        var bruttoBG1 = 448000d;
        var bruttoBG2 = 336000d;
        var refusjonsKrav1 = 448000d;
        var refusjonsKrav2 = 336000d;

        var forventetRedusert1 = seksG * bruttoBG1 / (bruttoBG1 + bruttoBG2);
        var forventetRedusert2 = seksG * bruttoBG2 / (bruttoBG1 + bruttoBG2);

        var grunnlag = lagBeregningsgrunnlag(2, List.of(bruttoBG1, bruttoBG2), List.of(refusjonsKrav1, refusjonsKrav2))
            .getBeregningsgrunnlagPerioder().get(0);

        //Act
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        verifiserBrukersAndel(grunnlag, List.of(0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, List.of(refusjonsKrav1, refusjonsKrav2),
            List.of(forventetRedusert1, forventetRedusert2));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }

    @Test
    //Scenario 8: Alle arbeidsgivere har refusjonskrav lik brukers brutto beregningsgrunnlag
    void skalBeregneNårRefusjonKravLikBruttoBGForAlleTreArbeidsgivere() {
        //Arrange
        var bruttoBG1 = 100000d;
        var bruttoBG2 = 400000d;
        var bruttoBG3 = 250000d;
        var refusjonsKrav1 = 100000d;
        var refusjonsKrav2 = 400000d;
        var refusjonsKrav3 = 250000d;
        var forventetRedusert1 = seksG * bruttoBG1 / (bruttoBG1 + bruttoBG2 + bruttoBG3);
        var forventetRedusert2 = seksG * bruttoBG2 / (bruttoBG1 + bruttoBG2 + bruttoBG3);
        var forventetRedusert3 = seksG * bruttoBG3 / (bruttoBG1 + bruttoBG2 + bruttoBG3);

        var grunnlag = lagBeregningsgrunnlag(3, List.of(bruttoBG1, bruttoBG2, bruttoBG3),
            List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
            .getBeregningsgrunnlagPerioder().get(0);

        //Act
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        verifiserBrukersAndel(grunnlag, List.of(0d, 0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
            List.of(forventetRedusert1, forventetRedusert2, forventetRedusert3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }

    @Test
    //Scenario 8: En av de tre arbeidsgiverne har refusjonskrav mindre enn brukers brutto beregningsgrunnlag ndre
    void skalBeregneNårRefusjonKravErIkkeLikBGForEnAvDeTreArbeidsgivere() {
        //Arrange
        var bruttoBG1 = 100000d;
        var bruttoBG2 = 400000d;
        var bruttoBG3 = 250000d;
        var refusjonsKrav1 = 100000d;
        var refusjonsKrav2 = 280000d;
        var refusjonsKrav3 = 250000d;

        var fordelingRunde2 = seksG - refusjonsKrav2;
        var forventetRedusert1 = fordelingRunde2 * bruttoBG1 / (bruttoBG1 + bruttoBG3);
        var forventetRedusert2 = refusjonsKrav2;
        var forventetRedusert3 = fordelingRunde2 * bruttoBG3 / (bruttoBG1 + bruttoBG3);

        var grunnlag = lagBeregningsgrunnlag(3, List.of(bruttoBG1, bruttoBG2, bruttoBG3),
            List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
            .getBeregningsgrunnlagPerioder().get(0);

        //Act
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        verifiserBrukersAndel(grunnlag, List.of(0d, 0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
            List.of(forventetRedusert1, forventetRedusert2, forventetRedusert3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }

    @Test
    //Scenario 11: Alle arbeidsgivere har refusjonskrav mindre enn brutto beregningsgrunnlag for arbeidsgiveren
    void skalBeregneNårRefusjonKravErNullForEnAvDeTreArbeidsgivere() {
        //Arrange
        var bruttoBG1 = 600000d;
        var bruttoBG2 = 750000d;
        var bruttoBG3 = 250000d;
        double refusjonsKrav1 = seksG;
        double refusjonsKrav2 = seksG;
        var refusjonsKrav3 = 0d;

        double fordelingRunde2 = seksG;
        var forventetRedusert1 = fordelingRunde2 * bruttoBG1 / (bruttoBG1 + bruttoBG2);
        var forventetRedusert2 = fordelingRunde2 * bruttoBG2 / (bruttoBG1 + bruttoBG2);
        double forventetRedusert3 = 0;

        var grunnlag = lagBeregningsgrunnlag(3, List.of(bruttoBG1, bruttoBG2, bruttoBG3),
            List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
            .getBeregningsgrunnlagPerioder().get(0);

        //Act
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        verifiserBrukersAndel(grunnlag, List.of(0d, 0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
            List.of(forventetRedusert1, forventetRedusert2, forventetRedusert3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }

    @Test
    //Scenario 11A: Alle arbeidsgivere har refusjonskrav mindre enn brutto beregningsgrunnlag for arbeidsgiveren
    void skalBeregneNårRefusjonKravLavereEnnBGForFireArbeidsforhold() {
        //Arrange
        var bruttoBG1 = 400000d;
        var bruttoBG2 = 500000d;
        var bruttoBG3 = 300000d;
        var bruttoBG4 = 100000d;
        var refusjonsKrav1 = 200000d;
        var refusjonsKrav2 = 150000d;
        var refusjonsKrav3 = 300000d;
        var refusjonsKrav4 = 100000d;

        var fordelingRunde2 = seksG - (refusjonsKrav1 + refusjonsKrav2);
        var forventetRedusert1 = refusjonsKrav1;
        var forventetRedusert2 = refusjonsKrav2;
        var forventetRedusert3 = fordelingRunde2 * bruttoBG3 / (bruttoBG3 + bruttoBG4);
        var forventetRedusert4 = fordelingRunde2 * bruttoBG4 / (bruttoBG3 + bruttoBG4);

        var grunnlag = lagBeregningsgrunnlag(4, List.of(bruttoBG1, bruttoBG2, bruttoBG3, bruttoBG4),
            List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3, refusjonsKrav4))
            .getBeregningsgrunnlagPerioder().get(0);

        //Act
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        verifiserBrukersAndel(grunnlag, List.of(0d, 0d, 0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3, refusjonsKrav4),
            List.of(forventetRedusert1, forventetRedusert2, forventetRedusert3, forventetRedusert4));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }

    @Test
    //Scenario 12: Flere arbeidsgivere har refusjonskrav og totalt refusjonskrav større enn 6G
    void skalBeregneNårMaksRefusjonKravErMerEnnFordeltRefusjonForToArbeidsgivere() {
        //Arrange
        var bruttoBG1 = 600000d;
        var bruttoBG2 = 750000d;
        var bruttoBG3 = 250000d;
        double refusjonsKrav1 = seksG;
        var refusjonsKrav2 = 200000d;
        var refusjonsKrav3 = 200000d;
        var fordelingRunde2 = seksG - refusjonsKrav2;
        var forventetRedusert1 = fordelingRunde2 * bruttoBG1 / (bruttoBG1 + bruttoBG3);
        var forventetRedusert2 = refusjonsKrav2;
        var forventetRedusert3 = fordelingRunde2 * bruttoBG3 / (bruttoBG1 + bruttoBG3);

        var grunnlag = lagBeregningsgrunnlag(3, List.of(bruttoBG1, bruttoBG2, bruttoBG3),
            List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
            .getBeregningsgrunnlagPerioder().get(0);

        //Act
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        verifiserBrukersAndel(grunnlag, List.of(0d, 0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
            List.of(forventetRedusert1, forventetRedusert2, forventetRedusert3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }

    @Test
    //Scenario 12: Flere arbeidsgivere har refusjonskrav og totalt refusjonskrav større enn 6G
    void skalBeregneNårMaksRefusjonKravErMerEnnFordeltRefusjonForEttArbeidsgiver() {
        //Arrange
        var bruttoBG1 = 600000d;
        var bruttoBG2 = 750000d;
        var bruttoBG3 = 250000d;
        var refusjonsKrav1 = 200000d;
        var refusjonsKrav2 = 200000d;
        var refusjonsKrav3 = 200000d;
        var fordelingRunde2 = seksG - (refusjonsKrav1 + refusjonsKrav2);
        var forventetRedusert1 = refusjonsKrav1;
        var forventetRedusert2 = refusjonsKrav2;
        var forventetRedusert3 = fordelingRunde2;

        var grunnlag = lagBeregningsgrunnlag(3, List.of(bruttoBG1, bruttoBG2, bruttoBG3),
            List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
            .getBeregningsgrunnlagPerioder().get(0);

        //Act
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        verifiserBrukersAndel(grunnlag, List.of(0d, 0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
            List.of(forventetRedusert1, forventetRedusert2, forventetRedusert3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }

    @Test
    //Scenario 12: Flere arbeidsgivere har refusjonskrav og totalt refusjonskrav større enn 6G
    void skalBeregneNårMaksRefusjonKravEr6GForToAvDeTreArbeidsgiverne() {
        //Arrange
        var bruttoBG1 = 600000d;
        var bruttoBG2 = 750000d;
        var bruttoBG3 = 250000d;
        double refusjonsKrav1 = seksG;
        double refusjonsKrav2 = seksG;
        var refusjonsKrav3 = 50000d;

        var fordelingRunde2 = seksG - refusjonsKrav3;
        var forventetRedusert1 = fordelingRunde2 * bruttoBG1 / (bruttoBG1 + bruttoBG2);
        var forventetRedusert2 = fordelingRunde2 * bruttoBG2 / (bruttoBG1 + bruttoBG2);
        var forventetRedusert3 = refusjonsKrav3;

        var grunnlag = lagBeregningsgrunnlag(3, List.of(bruttoBG1, bruttoBG2, bruttoBG3),
            List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
            .getBeregningsgrunnlagPerioder().get(0);

        //Act
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        verifiserBrukersAndel(grunnlag, List.of(0d, 0d, 0d));
        verifiserArbeidsgiversAndel(grunnlag, List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
            List.of(forventetRedusert1, forventetRedusert2, forventetRedusert3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, seksG);
    }


    private Beregningsgrunnlag lagBeregningsgrunnlag(int antallArbeidsforhold, List<Double> bruttoBG, List<Double> refusjonsKrav) {

        assertThat(bruttoBG).hasSize(antallArbeidsforhold);
        assertThat(refusjonsKrav).hasSize(antallArbeidsforhold);
        var bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));
        var afBuilder1 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR1))
            .medAndelNr(1)
            .medBruttoPrÅr(BigDecimal.valueOf(bruttoBG.get(0)))
            .medRefusjonPrÅr(BigDecimal.valueOf(refusjonsKrav.get(0)))
            .build();
        var afBuilder2 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2))
            .medAndelNr(2)
            .medBruttoPrÅr(BigDecimal.valueOf(bruttoBG.get(1)))
            .medRefusjonPrÅr(BigDecimal.valueOf(refusjonsKrav.get(1)))
            .build();

        if (antallArbeidsforhold == 2) {
            var bgpsATFL = BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.ATFL)
                .medArbeidsforhold(afBuilder1)
                .medArbeidsforhold(afBuilder2)
                .build();
            var periode = bgBuilder
                .medBeregningsgrunnlagPrStatus(bgpsATFL)
                .build();
            return opprettGrunnlag(periode);
        }

        var afBuilder3 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR3))
            .medAndelNr(3)
            .medBruttoPrÅr(BigDecimal.valueOf(bruttoBG.get(2)))
            .medRefusjonPrÅr(BigDecimal.valueOf(refusjonsKrav.get(2)))
            .build();
        if (antallArbeidsforhold == 3) {
            var bgpsATFL = BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.ATFL)
                .medArbeidsforhold(afBuilder1)
                .medArbeidsforhold(afBuilder2)
                .medArbeidsforhold(afBuilder3)
                .build();
            var periode = bgBuilder
                .medBeregningsgrunnlagPrStatus(bgpsATFL)
                .build();
            return opprettGrunnlag(periode);
        }
        var afBuilder4 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR4))
            .medAndelNr(4)
            .medBruttoPrÅr(BigDecimal.valueOf(bruttoBG.get(3)))
            .medRefusjonPrÅr(BigDecimal.valueOf(refusjonsKrav.get(3)))
            .build();
        var bgpsATFL = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(afBuilder1)
            .medArbeidsforhold(afBuilder2)
            .medArbeidsforhold(afBuilder3)
            .medArbeidsforhold(afBuilder4)
            .build();
        var periode = bgBuilder
            .medBeregningsgrunnlagPrStatus(bgpsATFL)
            .build();
        return opprettGrunnlag(periode);
    }

    private Beregningsgrunnlag opprettGrunnlag(BeregningsgrunnlagPeriode periode) {
        var arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        var inntektsgrunnlag = new Inntektsgrunnlag();
        arbeidsforhold.forEach(af -> {
            var månedsinntekt = Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
                .medArbeidsgiver(af.getArbeidsforhold())
                .medMåned(skjæringstidspunkt)
                .medInntekt(af.getBruttoPrÅr().get())
                .build();
            inntektsgrunnlag.leggTilPeriodeinntekt(månedsinntekt);
        });
        return Beregningsgrunnlag.builder()
                .medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL, null)))
                .medBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode.oppdater(periode).medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100).build())
                .medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
                .build();
    }

    private void verifiserBrukersAndel(BeregningsgrunnlagPeriode grunnlag, List<Double> beløp) {
        var brukersAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().stream().
            map(BeregningsgrunnlagPrArbeidsforhold::getAvkortetBrukersAndelPrÅr).collect(Collectors.toList());
        assertThat(brukersAndel).hasSameSizeAs(beløp);
        for (var ix = 0; ix < beløp.size(); ix++) {
            assertThat(brukersAndel.get(ix).doubleValue()).isCloseTo(beløp.get(ix), within(0.01));
        }
    }

    private void verifiserArbeidsgiversAndel(BeregningsgrunnlagPeriode grunnlag, List<Double> maksimalRefusjon, List<Double> redusertRefusjon) {
        var arbeidsgiversAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        assertThat(arbeidsgiversAndel).hasSameSizeAs(maksimalRefusjon);
        assertThat(arbeidsgiversAndel).hasSameSizeAs(redusertRefusjon);
        for (var ix = 0; ix < arbeidsgiversAndel.size(); ix++) {
            verifiserEnkeltAndel(arbeidsgiversAndel.get(ix), maksimalRefusjon.get(ix), redusertRefusjon.get(ix));
        }
    }

    private void verifiserEnkeltAndel(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, double maksimalrefusjon, double redusertrefusjon) {
        assertThat(arbeidsforhold.getMaksimalRefusjonPrÅr().doubleValue()).isEqualTo(maksimalrefusjon, within(0.01));
        assertThat(arbeidsforhold.getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(redusertrefusjon, within(0.01));
    }
}
