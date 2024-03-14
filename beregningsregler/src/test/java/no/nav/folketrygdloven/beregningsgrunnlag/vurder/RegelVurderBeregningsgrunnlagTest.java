package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilArbeidsforholdMedInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak.AVSLAG_UNDER_HALV_G;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType.BEREGNET;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;

class RegelVurderBeregningsgrunnlagTest {

    private static Long generatedId = 1L;
    private final Offset<Double> offset = Offset.offset(0.01);

    private static final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);

    @Test
    void skalOppretteRegelmerknadForAvslagNårBruttoInntektPrÅrMindreEnnHalvG() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2017 * 0.49;
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.merknader().stream().map(RegelMerknad::utfallÅrsak)).containsOnly(AVSLAG_UNDER_HALV_G);
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr, offset);
    }

    @Test
    void skalOppretteRegelmerknadForAvslagForFlereArbeidsforholdNårBruttoInntektPrÅrMindreEnnHalvG() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2017 * 0.25;
        double beregnetPrÅr2 = GRUNNBELØP_2017 * 0.24; //Totalt under 0,5G
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0);
        leggTilArbeidsforhold(beregningsgrunnlag, beregnetPrÅr2, 0);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.merknader().stream().map(RegelMerknad::utfallÅrsak)).containsOnly(AVSLAG_UNDER_HALV_G);
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr + beregnetPrÅr2, offset);
    }

	@Test
	void skalIkkeAvslåOmsorgspengerTilArbeidsgiverUndeEnHalvG() {
		//Arrange
		double beregnetPrÅr = GRUNNBELØP_2017 * 0.25;
		double beregnetPrÅr2 = GRUNNBELØP_2017 * 0.24; //Totalt under 0,5G
		Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0);

		Beregningsgrunnlag.builder(beregningsgrunnlag)
						.medYtelsesSpesifiktGrunnlag(new OmsorgspengerGrunnlag(false, false));
		leggTilArbeidsforhold(beregningsgrunnlag, beregnetPrÅr2, 0);
		BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

		//Act
		RegelResultat resultat = kjørRegel(grunnlag);

		//Assert
		assertThat(resultat.merknader()).isEmpty();
		assertThat(resultat.beregningsresultat()).isEqualTo(BEREGNET);
		assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr + beregnetPrÅr2, offset);
	}

	@Test
	void skalAvslåOmsorgspengerTilBrukerUndeEnHalvG() {
		//Arrange
		double beregnetPrÅr = GRUNNBELØP_2017 * 0.25;
		double beregnetPrÅr2 = GRUNNBELØP_2017 * 0.24; //Totalt under 0,5G
		Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0);

		Beregningsgrunnlag.builder(beregningsgrunnlag)
				.medYtelsesSpesifiktGrunnlag(new OmsorgspengerGrunnlag(false, true));
		leggTilArbeidsforhold(beregningsgrunnlag, beregnetPrÅr2, 0);
		BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

		//Act
		RegelResultat resultat = kjørRegel(grunnlag);

		//Assert
		assertThat(resultat.merknader().stream().map(RegelMerknad::utfallÅrsak)).containsOnly(AVSLAG_UNDER_HALV_G);
		assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr + beregnetPrÅr2, offset);
	}

    private RegelResultat kjørRegel(BeregningsgrunnlagPeriode grunnlag) {
        return new RegelVurderBeregningsgrunnlag().evaluerRegel(grunnlag);
    }

    private Beregningsgrunnlag opprettBeregningsgrunnlag(LocalDate skjæringstidspunkt, double beregnetPrÅr, double refusjonskravPrÅr) {
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.valueOf(beregnetPrÅr / 12), BigDecimal.valueOf(refusjonskravPrÅr / 12));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
	    BeregningsgrunnlagPeriode.builder(grunnlag);

        BeregningsgrunnlagPrArbeidsforhold.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0))
            .medBeregnetPrÅr(BigDecimal.valueOf(beregnetPrÅr)).build();
        return beregningsgrunnlag;
    }

    private void leggTilArbeidsforhold(Beregningsgrunnlag grunnlag, double beregnetPrÅr, double refusjonskrav) {
        BeregningsgrunnlagPeriode bgPeriode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        String nyttOrgnr = generateId().toString();
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(nyttOrgnr);
        leggTilArbeidsforholdMedInntektsmelding(bgPeriode, skjæringstidspunkt, BigDecimal.valueOf(beregnetPrÅr / 12), BigDecimal.valueOf(refusjonskrav / 12), arbeidsforhold, BigDecimal.ZERO, null);
        BeregningsgrunnlagPrStatus atfl = bgPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        BeregningsgrunnlagPrArbeidsforhold bgpaf = atfl.getArbeidsforhold().stream()
            .filter(af -> af.getArbeidsforhold().getOrgnr().equals(nyttOrgnr)).findFirst().get();
        BeregningsgrunnlagPrArbeidsforhold.builder(bgpaf)
            .medBeregnetPrÅr(BigDecimal.valueOf(beregnetPrÅr))
            .build();
    }

    private static Long generateId() {
        return generatedId++;
    }

}
