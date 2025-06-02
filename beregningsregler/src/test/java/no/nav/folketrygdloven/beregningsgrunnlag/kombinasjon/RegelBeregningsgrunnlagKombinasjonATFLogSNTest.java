package no.nav.folketrygdloven.beregningsgrunnlag.kombinasjon;

import static java.util.Collections.singletonList;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppÅrsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.årsinntekterFor3SisteÅr;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBeregnet;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.fortsettForeslå.RegelFortsettForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;

class RegelBeregningsgrunnlagKombinasjonATFLogSNTest {
    private LocalDate skjæringstidspunkt;
    private String orgnr;

    @BeforeEach
    void setup() {
        skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
        orgnr = "987";
    }

    @Test
    void skalBeregneAndelGrunnlagSNHvorSNStørreEnnATFL() { // NOSONAR
        // Arrange
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var arbeidsforholStartdato = skjæringstidspunkt.minusYears(2);
        var arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, orgnr);
        var refusjonskrav = BigDecimal.valueOf(4.0d * GSNITT_2017 / 12);
        var inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
                årsinntekterFor3SisteÅr(5,  3,  4), Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, List.of(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

        var grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, singletonList(AktivitetStatus.ATFL_SN),
            singletonList(arbeidsforhold), singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);

        // Act
        @SuppressWarnings("unused") var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
	    @SuppressWarnings("unused") var regelResultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

	    // Assert
        var forventetSNBeløp = ((4.0d * GRUNNBELØP_2017) - (12 * månedsinntekt.doubleValue())); // Differanse siden SN > ATFL: SN - ATFL
        verifiserBeregningsgrunnlagBeregnet(grunnlag, forventetSNBeløp + 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, forventetSNBeløp, 4.0d * GRUNNBELØP_2017);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
    }

    @Test
    void skalBeregneAndelGrunnlagSNHvorSNMindreEnnATFL() { // NOSONAR
        // Arrange
        var månedsinntekt = BigDecimal.valueOf(5.0 * GRUNNBELØP_2017 / 12);
        var arbeidsforholStartdato = skjæringstidspunkt.minusYears(2);
        var arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, orgnr);
        var refusjonskrav = månedsinntekt;
        var inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
                årsinntekterFor3SisteÅr(3.0,  2.0,  2.5), Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, List.of(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

        var grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, singletonList(AktivitetStatus.ATFL_SN),
            singletonList(arbeidsforhold),  singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);

        // Act
	    @SuppressWarnings("unused") var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
	    @SuppressWarnings("unused") var regelResultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
        // Assert
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, 0.0d, 2.5d * GRUNNBELØP_2017);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
    }
}
