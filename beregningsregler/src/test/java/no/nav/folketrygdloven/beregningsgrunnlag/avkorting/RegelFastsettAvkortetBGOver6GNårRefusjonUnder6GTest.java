package no.nav.folketrygdloven.beregningsgrunnlag.avkorting;


import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserFastsettBeregningsgrunnlag.verifiserBeregningsgrunnlagAvkortetPrÅr;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserFastsettBeregningsgrunnlag.verifiserBeregningsgrunnlagAvkortetPrÅrFrilanser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.RegelFullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
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
 * Testen kjøres når Regelflyt 8 blir implementert
 */

class RegelFastsettAvkortetBGOver6GNårRefusjonUnder6GTest {

    private static final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);
    private static final long SEKS_G = 90000 * 6;

    private static final String ORGNR1 = "123";
    private static final String ORGNR2 = "456";
    private static final String ORGNR3 = "789";
    private static final String ORGNR4 = "101112";


    @Test
    //Scenario 1: Arbeidsgiver har refusjonskrav er lik 6G
    void skalBeregneNårRefusjonKravErLik6G() {
        //Arrange
        var bruttoBG = 672000d;
        double refusjonsKrav = SEKS_G;
        double forventetRedusert = SEKS_G;
        var forventetRedusertBrukersAndel = forventetRedusert - refusjonsKrav;

        var grunnlag = lagBeregningsgrunnlag(1, Collections.singletonList(bruttoBG), Collections.singletonList(refusjonsKrav))
            .getBeregningsgrunnlagPerioder().get(0);

        //Act
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        verifiserBrukersAndel(grunnlag, Collections.singletonList(forventetRedusertBrukersAndel));
        verifiserArbeidsgiversAndel(grunnlag, Collections.singletonList(refusjonsKrav), Collections.singletonList(refusjonsKrav));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, forventetRedusert);
    }

    @Test
    //Scenario 2: Arbeidsgiver har refusjonskrav mindre enn 6G
    void skalBeregneNårRefusjonKravErMindreEnn6G() {
        //Arrange
        var bruttoBG = 672000d;
        var refusjonsKrav = 300000d;
        double forventetRedusert = SEKS_G;
        var forventetRedusertBrukersAndel = forventetRedusert - refusjonsKrav;

        var grunnlag = lagBeregningsgrunnlag(1, Collections.singletonList(bruttoBG), Collections.singletonList(refusjonsKrav))
            .getBeregningsgrunnlagPerioder().get(0);
        //Act
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        verifiserBrukersAndel(grunnlag, Collections.singletonList(forventetRedusertBrukersAndel));
        verifiserArbeidsgiversAndel(grunnlag, Collections.singletonList(refusjonsKrav), Collections.singletonList(refusjonsKrav));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, forventetRedusert);
    }

    @Test
    //Scenario 3: Arbeidsgiver har ikke refusjonskrav
    void skalBeregneNårRefusjonKravErLikNull() {
        //Arrange
        var bruttoBG = 672000d;
        var refusjonsKrav = 0d;
        double forventetRedusert = SEKS_G;
        var forventetRedusertBrukersAndel = forventetRedusert - refusjonsKrav;

        var grunnlag = lagBeregningsgrunnlag(1, Collections.singletonList(bruttoBG), Collections.singletonList(refusjonsKrav))
            .getBeregningsgrunnlagPerioder().get(0);
        //Act
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        verifiserBrukersAndel(grunnlag, Collections.singletonList(forventetRedusertBrukersAndel));
        verifiserArbeidsgiversAndel(grunnlag, Collections.singletonList(refusjonsKrav), Collections.singletonList(refusjonsKrav));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, forventetRedusert);
    }

    @Test
    //Scenario 6: Brutto beregningsgrunnlag > 6G i ett av arbeidsforholdene, denne arbeidsgivers refusjonskrav er 6G, de andre arbeidsgiverne har ikke refusjonskrav
    void skalBeregneNårRefusjonKravErNullForEnAvDeToArbeidsgiverne() {
        //Arrange
        var bruttoBG1 = 896000d;
        var bruttoBG2 = 448000d;
        double refusjonsKrav1 = SEKS_G;
        var refusjonsKrav2 = 0d;

        double forventetRedusert = SEKS_G;
        double forventetRedusert1 = SEKS_G;
        var forventetRedusert2 = forventetRedusert - forventetRedusert1;
        var forventetRedusertBrukersAndel1 = forventetRedusert1 - refusjonsKrav1;
        var forventetRedusertBrukersAndel2 = forventetRedusert2 - refusjonsKrav2;

        var grunnlag = lagBeregningsgrunnlag(2, List.of(bruttoBG1, bruttoBG2),
            List.of(refusjonsKrav1, refusjonsKrav2))
            .getBeregningsgrunnlagPerioder().get(0);
        //Act
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        verifiserBrukersAndel(grunnlag, List.of(forventetRedusertBrukersAndel1, forventetRedusertBrukersAndel2));
        verifiserArbeidsgiversAndel(grunnlag, List.of(refusjonsKrav1, refusjonsKrav2), List.of(refusjonsKrav1, refusjonsKrav2));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, forventetRedusert);
    }

    @Test
    //Scenario 9: En arbeidsgiver har refusjonskrav < 6G, de andre arbeidsgiverne har ikke refusjonskrav
    void skalBeregneNårRefusjonKravErNullForToAvDeTreArbeidsgiverne() {
        //Arrange
        var bruttoBG1 = 100000d;
        var bruttoBG2 = 400000d;
        var bruttoBG3 = 250000d;
        var refusjonsKrav1 = 0d;
        var refusjonsKrav2 = 400000d;
        var refusjonsKrav3 = 0d;

        double forventetRedusert = SEKS_G;
        var forventetRedusert2 = refusjonsKrav2;
        var forventetRedusert1 = (forventetRedusert - refusjonsKrav2) * bruttoBG1 / (bruttoBG1 + bruttoBG3);
        var forventetRedusert3 = (forventetRedusert - refusjonsKrav2) * bruttoBG3 / (bruttoBG1 + bruttoBG3);
        var forventetRedusertBrukersAndel1 = forventetRedusert1 - refusjonsKrav1;
        var forventetRedusertBrukersAndel2 = forventetRedusert2 - refusjonsKrav2;
        var forventetRedusertBrukersAndel3 = forventetRedusert3 - refusjonsKrav3;

        var grunnlag = lagBeregningsgrunnlag(3, List.of(bruttoBG1, bruttoBG2, bruttoBG3),
            List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
            .getBeregningsgrunnlagPerioder().get(0);
        //Act
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        verifiserBrukersAndel(grunnlag, List.of(forventetRedusertBrukersAndel1, forventetRedusertBrukersAndel2, forventetRedusertBrukersAndel3));
        verifiserArbeidsgiversAndel(grunnlag, List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
            List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, forventetRedusert);
    }

    @Test
    //Scenario 10: Flere arbeidsgiver har refusjonskrav < brutto beregningsgrunnlag for arbeidsgiveren, totalt refusjonskrav < 6G
    void skalBeregneNårRefusjonKravErNullForEnAvDeTreArbeidsgiverne() {
        //Arrange
        var bruttoBG1 = 400000d;
        var bruttoBG2 = 500000d;
        var bruttoBG3 = 250000d;
        var refusjonsKrav1 = 300000d;
        var refusjonsKrav2 = 150000d;
        var refusjonsKrav3 = 0d;

        double forventetRedusert = SEKS_G;
        var forventetRedusert1 = refusjonsKrav1;
        var forventetRedusert2 = (forventetRedusert - refusjonsKrav1) * bruttoBG2 / (bruttoBG2 + bruttoBG3);
        var forventetRedusert3 = (forventetRedusert - refusjonsKrav1) * bruttoBG3 / (bruttoBG2 + bruttoBG3);
        var forventetRedusertBrukersAndel1 = forventetRedusert1 - refusjonsKrav1;
        var forventetRedusertBrukersAndel2 = forventetRedusert2 - refusjonsKrav2;
        var forventetRedusertBrukersAndel3 = forventetRedusert3 - refusjonsKrav3;

        var grunnlag = lagBeregningsgrunnlag(3, List.of(bruttoBG1, bruttoBG2, bruttoBG3),
            List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
            .getBeregningsgrunnlagPerioder().get(0);
        //Act
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        verifiserBrukersAndel(grunnlag, List.of(forventetRedusertBrukersAndel1, forventetRedusertBrukersAndel2, forventetRedusertBrukersAndel3));
        verifiserArbeidsgiversAndel(grunnlag, List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
            List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, forventetRedusert);
    }

    @Test
    //Scenario 10: Flere arbeidsgiver har refusjonskrav < brutto beregningsgrunnlag for arbeidsgiveren, totalt refusjonskrav < 6G
    void skalBeregneNårAlleArbeidsgivereHarRefusjonKravMindreEnn6G() {
        //Arrange
        var bruttoBG1 = 400000d;
        var bruttoBG2 = 500000d;
        var bruttoBG3 = 250000d;
        var refusjonsKrav1 = 250000d;
        var refusjonsKrav2 = 50000d;
        var refusjonsKrav3 = 220000d;

        double forventetRedusert = SEKS_G;
        var forventetRedusert1 = refusjonsKrav1;
        var forventetRedusert2 = (forventetRedusert - refusjonsKrav1 - refusjonsKrav3);
        var forventetRedusert3 = refusjonsKrav3;
        var forventetRedusertArbeidsgiver1 = refusjonsKrav1;
        var forventetRedusertBrukersAndel1 = forventetRedusert1 - forventetRedusertArbeidsgiver1;
        var forventetRedusertArbeidsgiver2 = refusjonsKrav2;
        var forventetRedusertBrukersAndel2 = forventetRedusert2 - forventetRedusertArbeidsgiver2;
        var forventetRedusertArbeidsgiver3 = refusjonsKrav3;
        var forventetRedusertBrukersAndel3 = forventetRedusert3 - forventetRedusertArbeidsgiver3;

        var grunnlag = lagBeregningsgrunnlag(3, List.of(bruttoBG1, bruttoBG2, bruttoBG3),
            List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
            .getBeregningsgrunnlagPerioder().get(0);
        //Act
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        verifiserBrukersAndel(grunnlag, List.of(forventetRedusertBrukersAndel1, forventetRedusertBrukersAndel2, forventetRedusertBrukersAndel3));
        verifiserArbeidsgiversAndel(grunnlag, List.of(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
            List.of(forventetRedusertArbeidsgiver1, forventetRedusertArbeidsgiver2, forventetRedusertArbeidsgiver3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, forventetRedusert);
    }

    @Test
    // Scenario 11: Flere arbeidsforhold og totalt brutto BG > 6G , total refusjonskrav < 6G,
    // totalt BG for beregningsgrunnlagsandeler fra arbeidsforhold < 6G
    // arbeidsforhold arbeidstaker og dagpenger
    void skalBeregneAvkortningAvUlikeBeregningsgrunnlagsandelerMedEnAndelUtenArbeidsforhold(){ // NOSONAR
        var bruttoBG1 = 300000d;
        var refusjonsKrav1 = 20000d;
        var bruttoBG2 = 263000d;

        var bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));
        var bgpsATFL = lagBeregningsgrunnlagPrStatus(ORGNR1, bruttoBG1, 1, AktivitetStatus.ATFL, refusjonsKrav1);
        var bgpsDP = lagBeregningsgrunnlagPrStatus(ORGNR2, bruttoBG2, 2, AktivitetStatus.DP, null);
        var periode = bgBuilder
            .medBeregningsgrunnlagPrStatus(bgpsATFL)
            .medBeregningsgrunnlagPrStatus(bgpsDP)
            .build();
        var beregningsgrunnlag = opprettGrunnlag(periode);

        var forventetAvkortet = SEKS_G - bruttoBG1;

        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.DP, forventetAvkortet);
    }

    @Test
    // Scenario 12: Flere arbeidsforhold og totalt brutto BG > 6G , total refusjonskrav < 6G,
    // totalt BG for beregningsgrunnlagsandeler fra arbeidsforhold < 6G
    // arbeidsforhold arbeidstaker
    // dagpenger
    // arbeidsavklaringspenger
    void skalBeregneAvkortningAvUlikeBeregningsgrunnlagsandelerMedToAndelUtenArbeidsforhold() { // NOSONAR
        var bruttoBG1 = 300000d;
        var bruttoBG2 = 130000d;
        var bruttoBG3 = 120000d;

        var bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));
        var bgpsATFL = lagBeregningsgrunnlagPrStatus(ORGNR1,bruttoBG1, 1, AktivitetStatus.ATFL);
        var bgpsDP = lagBeregningsgrunnlagPrStatus(ORGNR2,bruttoBG2, 2, AktivitetStatus.DP);
        var bgpsAAP = lagBeregningsgrunnlagPrStatus(ORGNR3,bruttoBG3, 3, AktivitetStatus.AAP);
        var periode = bgBuilder
            .medBeregningsgrunnlagPrStatus(bgpsATFL)
            .medBeregningsgrunnlagPrStatus(bgpsDP)
            .medBeregningsgrunnlagPrStatus(bgpsAAP)
            .build();
        var beregningsgrunnlag = opprettGrunnlag(periode);

        var forventetAvkortet = 130000d;
        var forventetAvkortet2 = SEKS_G - bruttoBG1 - bruttoBG2;

        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.DP, forventetAvkortet);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.AAP, forventetAvkortet2);
    }

    @Test
    // Scenario 13: Flere arbeidsforhold og totalt brutto BG > 6G , total refusjonskrav < 6G,
    // totalt BG for beregningsgrunnlagsandeler fra arbeidsforhold < 6G
    // arbeidsforhold arbeidstaker
    // dagpenger
    // arbeidsavklaringspenger
    // selvstendig næringsdrivende
    void skalBeregneAvkortningAvUlikeBeregningsgrunnlagsandelerMedFlereAndelUtenArbeidsforhold() { // NOSONAR
        var bruttoBG1 = 300000d;
        var bruttoBG2 = 130000d;
        var bruttoBG3 = 110000d;
        double bruttoBG4 = 100000;

        var bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));
        var bgpsATFL = lagBeregningsgrunnlagPrStatus(ORGNR1,bruttoBG1, 1, AktivitetStatus.ATFL);
        var bgpsDP = lagBeregningsgrunnlagPrStatus(ORGNR2,bruttoBG2, 2, AktivitetStatus.DP);
        var bgpsAAP = lagBeregningsgrunnlagPrStatus(ORGNR3,bruttoBG3, 3, AktivitetStatus.AAP);
        var bgpsSN = lagBeregningsgrunnlagPrStatus(ORGNR4,bruttoBG4, 4, AktivitetStatus.SN);
        var periode = bgBuilder
            .medBeregningsgrunnlagPrStatus(bgpsATFL)
            .medBeregningsgrunnlagPrStatus(bgpsDP)
            .medBeregningsgrunnlagPrStatus(bgpsAAP)
            .medBeregningsgrunnlagPrStatus(bgpsSN)
            .build();
        var beregningsgrunnlag = opprettGrunnlag(periode);

        var forventetAvkortet = 130000d;
        var forventetAvkortet2 = SEKS_G - bruttoBG1 - bruttoBG2;
        var forventetAvkortet3 = 0d;

        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.DP, forventetAvkortet);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.AAP, forventetAvkortet2);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.SN, forventetAvkortet3);
    }

    @Test
    // Scenario 14:En arbeidsforhold , frilanser og dagpenger.
    // totalt brutto BG > 6G , total refusjonskrav < 6G,
    // totalt BG for beregningsgrunnlagsandeler fra arbeidsforhold < 6G
    // Brutto beregningsgrunnlag for andelen fra frilanser >= beregningsgrunnlag uten arbeidsforhold til fordeling
    void skalBeregneAvkortningAvFrilanserMedBruttoBGForAndelenStørreEnnBGUtenArbeidsforhold() { // NOSONAR
        var bruttoAT = 300000d;
        var refusjonsKrav = 20000d;
        var bruttoFL = 260000d;
        double bruttoDP = 60000;

        var bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));
        var bgpsATFL = lagBGPrStatusATFL(ORGNR1, bruttoAT, bruttoFL, 1, refusjonsKrav*12);
        var bgpsDP = lagBeregningsgrunnlagPrStatus(ORGNR2, bruttoDP, 2, AktivitetStatus.DP);
        var periode = bgBuilder
            .medBeregningsgrunnlagPrStatus(bgpsATFL)
            .medBeregningsgrunnlagPrStatus(bgpsDP)
            .build();
        var beregningsgrunnlag = opprettGrunnlag(periode);

        var forventetAvkortetFrilanser = SEKS_G - bruttoAT;
        var forventetAvkortetDP = 0d;

        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		verifiserBeregningsgrunnlagAvkortetPrÅrFrilanser(grunnlag, null,  forventetAvkortetFrilanser);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.DP, forventetAvkortetDP);
    }

    @Test
    // Scenario 15: En arbeidsforhold , frilanser og dagpenger.
    // totalt brutto BG > 6G , total refusjonskrav < 6G,
    // totalt BG for beregningsgrunnlagsandeler fra arbeidsforhold < 6G
    // Brutto beregningsgrunnlag for andelen fra frilanser <= beregningsgrunnlag uten arbeidsforhold til fordeling
    void skalBeregneAvkortningAvFrilanserMedBruttoBGForAndelenMindreEnnBGUtenArbeidsforhold() { // NOSONAR
        var bruttoAT = 300000d;
        var refusjonsKrav = 20000d;
        var bruttoFL = 200000d;
        double bruttoDP = 60000;

        var bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));
        var bgpsATFL = lagBGPrStatusATFL(ORGNR1, bruttoAT, bruttoFL, 1, refusjonsKrav);
        var bgpsDP = lagBeregningsgrunnlagPrStatus(ORGNR2, bruttoDP, 2, AktivitetStatus.DP);
        var periode = bgBuilder
            .medBeregningsgrunnlagPrStatus(bgpsATFL)
            .medBeregningsgrunnlagPrStatus(bgpsDP)
            .build();
        var beregningsgrunnlag = opprettGrunnlag(periode);

        var forventetAvkortetFrilanser = bruttoFL;
        var forventetAvkortetDP = SEKS_G - bruttoAT - bruttoFL;

        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		verifiserBeregningsgrunnlagAvkortetPrÅrFrilanser(grunnlag, null,  forventetAvkortetFrilanser);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.DP, forventetAvkortetDP);
    }


    private BeregningsgrunnlagPrStatus lagBeregningsgrunnlagPrStatus(String orgNr, double brutto, int andelNr,
                                                                     AktivitetStatus aktivitetStatus) {
        return lagBeregningsgrunnlagPrStatus(orgNr, brutto, andelNr, aktivitetStatus, null);
    }


    private BeregningsgrunnlagPrStatus lagBeregningsgrunnlagPrStatus(String orgNr, double brutto, int andelNr,
                                                                     AktivitetStatus aktivitetStatus, Double refusjonskrav) {
        var afBuilder1 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgNr))
            .medBruttoPrÅr(BigDecimal.valueOf(brutto))
            .medRefusjonPrÅr(refusjonskrav == null ? null : BigDecimal.valueOf(refusjonskrav))
            .medAndelNr(andelNr)
            .build();
        return BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(aktivitetStatus)
            .medAndelNr(aktivitetStatus.equals(AktivitetStatus.ATFL) ? null : Integer.toUnsignedLong(andelNr))
            .medArbeidsforhold(afBuilder1)
            .build();
    }

    private BeregningsgrunnlagPrStatus lagBGPrStatusATFL(String orgNr, double bruttoAT, double bruttoFL, int andelNr, Double refusjonPrÅr) {
        var afBuilderAT = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgNr))
            .medBruttoPrÅr(BigDecimal.valueOf(bruttoAT))
            .medRefusjonPrÅr(refusjonPrÅr == null ? null : BigDecimal.valueOf(refusjonPrÅr))
            .medAndelNr(andelNr)
            .build();
        var afBuilderFL = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.builder().medAktivitet(Aktivitet.FRILANSINNTEKT).medOrgnr(orgNr).build())
            .medBruttoPrÅr(BigDecimal.valueOf(bruttoFL))
            .medAndelNr(andelNr)
            .build();
        return BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(afBuilderAT)
            .medArbeidsforhold(afBuilderFL)
            .build();
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag(int antallArbeidsforhold, List<Double> bruttoBG, List<Double> refusjonsKrav) {
        assertThat(bruttoBG).hasSize(antallArbeidsforhold);
        assertThat(refusjonsKrav).hasSize(antallArbeidsforhold);
        var bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));
        long andelNr = 1;
        var afBuilder1 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR1))
            .medBruttoPrÅr(BigDecimal.valueOf(bruttoBG.get(0)))
            .medRefusjonPrÅr(BigDecimal.valueOf(refusjonsKrav.get(0)))
            .medAndelNr(andelNr++)
            .build();
        if (antallArbeidsforhold == 1) {
            var bgpsATFL = BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.ATFL)
                .medArbeidsforhold(afBuilder1)
                .build();
            var periode = bgBuilder
                .medBeregningsgrunnlagPrStatus(bgpsATFL)
                .build();
            return opprettGrunnlag(periode);
        }

        var afBuilder2 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2))
            .medBruttoPrÅr(BigDecimal.valueOf(bruttoBG.get(1)))
            .medRefusjonPrÅr(BigDecimal.valueOf(refusjonsKrav.get(1)))
            .medAndelNr(andelNr++)
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
            .medBruttoPrÅr(BigDecimal.valueOf(bruttoBG.get(2)))
            .medRefusjonPrÅr(BigDecimal.valueOf(refusjonsKrav.get(2)))
            .medAndelNr(andelNr++)
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
            .medBruttoPrÅr(BigDecimal.valueOf(bruttoBG.get(3)))
            .medRefusjonPrÅr(BigDecimal.valueOf(refusjonsKrav.get(3)))
            .medAndelNr(andelNr++)
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
        arbeidsforhold.forEach(af -> inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
            .medArbeidsgiver(af.getArbeidsforhold())
            .medMåned(skjæringstidspunkt)
            .medInntekt(af.getBruttoPrÅr().get())
            .build()));
        return Beregningsgrunnlag.builder()
            .medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL, null)))
            .medBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode.oppdater(periode).medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100).build())
            .medGrunnbeløp(GRUNNBELØP)
            .build();
    }

    private void verifiserBrukersAndel(BeregningsgrunnlagPeriode grunnlag, List<Double> avkortet) {
        var brukersAvkortetAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().stream().
            map(BeregningsgrunnlagPrArbeidsforhold::getAvkortetBrukersAndelPrÅr).collect(Collectors.toList());
        assertThat(brukersAvkortetAndel).hasSameSizeAs(avkortet);
        for (var ix = 0; ix < avkortet.size(); ix++) {
            assertThat(brukersAvkortetAndel.get(ix).doubleValue()).isCloseTo(avkortet.get(ix), within(0.01));
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

    private void verifiserEnkeltAndel(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, Double maksimalrefusjon, Double redusertrefusjon) {
        assertThat(arbeidsforhold.getMaksimalRefusjonPrÅr().doubleValue()).isCloseTo(maksimalrefusjon, within(0.01));
        assertThat(arbeidsforhold.getRedusertRefusjonPrÅr().doubleValue()).isCloseTo(redusertrefusjon, within(0.01));
    }
}
