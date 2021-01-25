package no.nav.folketrygdloven.skjæringstidspunkt.status;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RegelFastsettStatusVedSkjæringtidspunktPSBTest {
    private static final String ORGNR = "7654";
    private static final Arbeidsforhold ARBEIDSFORHOLD =  Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR);
    private LocalDate skjæringstidspunktForBeregning;
    private AktivitetStatusModell regelmodell;

    @BeforeEach
    public void setup() {
        skjæringstidspunktForBeregning = LocalDate.of(2018, Month.JANUARY, 15);
        regelmodell = new AktivitetStatusModell();
	    regelmodell.setFinnBeregningstidspunkt((stp) -> stp);
	    regelmodell.setSkjæringstidspunktForBeregning(skjæringstidspunktForBeregning);
    }

    @Test
    public void skal_fastsette_status_ATFL_når_aktivitet_er_arbeidsinntekt(){

	    // Arrange
	    AktivPeriode aktivPeriode = new AktivPeriode(Aktivitet.ARBEIDSTAKERINNTEKT, Periode.of(skjæringstidspunktForBeregning.minusWeeks(2), skjæringstidspunktForBeregning.plusWeeks(3)), ARBEIDSFORHOLD);
	    regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);

	    // Act
	    Evaluation evaluation = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);

	    // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.ATFL);
        assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
        BeregningsgrunnlagPrStatus bgPrStatus = regelmodell.getBeregningsgrunnlagPrStatusListe().get(0);
        assertThat(bgPrStatus.getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
        assertThat(bgPrStatus.getArbeidsforholdList()).hasSize(1);
        assertThat(bgPrStatus.getArbeidsforholdList().get(0).getOrgnr()).isEqualTo(ORGNR);
    }

	@Test
	public void skal_fastsette_status_ATFL_når_aktivitet_er_frilans(){

		// Arrange
		AktivPeriode aktivPeriode = AktivPeriode.forFrilanser(Periode.of(skjæringstidspunktForBeregning.minusWeeks(2), skjæringstidspunktForBeregning.plusWeeks(3)));
		regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);

		// Act
		Evaluation evaluation = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);

		// Assert
		@SuppressWarnings("unused")
		String sporing = EvaluationSerializer.asJson(evaluation);

		assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.ATFL);
		assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
		BeregningsgrunnlagPrStatus bgPrStatus = regelmodell.getBeregningsgrunnlagPrStatusListe().get(0);
		assertThat(bgPrStatus.getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
		assertThat(bgPrStatus.getArbeidsforholdList()).hasSize(1);
		assertThat(bgPrStatus.getArbeidsforholdList().get(0).erFrilanser()).isTrue();
	}

	@Test
	public void skal_fastsette_status_ATFL_når_aktivitet_er_frilans_og_arbeidsforhold_slutter_dagen_før(){

		// Arrange
		AktivPeriode aktivPeriode1 = AktivPeriode.forFrilanser(Periode.of(skjæringstidspunktForBeregning.minusWeeks(2), skjæringstidspunktForBeregning));
		AktivPeriode aktivPeriode2 = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForBeregning.minusWeeks(2), skjæringstidspunktForBeregning.minusDays(1)), ARBEIDSFORHOLD.getOrgnr(), ARBEIDSFORHOLD.getArbeidsforholdId());
		regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode1);
		regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);

		// Act
		Evaluation evaluation = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);

		// Assert
		@SuppressWarnings("unused")
		String sporing = EvaluationSerializer.asJson(evaluation);

		assertThat(regelmodell.getAktivitetStatuser()).containsOnly(AktivitetStatus.ATFL);
		assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
		BeregningsgrunnlagPrStatus bgPrStatus = regelmodell.getBeregningsgrunnlagPrStatusListe().get(0);
		assertThat(bgPrStatus.getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
		assertThat(bgPrStatus.getArbeidsforholdList()).hasSize(1);
		assertThat(bgPrStatus.getArbeidsforholdList().get(0).erFrilanser()).isTrue();
	}

}
