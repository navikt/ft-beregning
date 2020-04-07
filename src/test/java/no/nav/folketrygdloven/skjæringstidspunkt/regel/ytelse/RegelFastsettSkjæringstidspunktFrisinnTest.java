package no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFRISINN;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class RegelFastsettSkjæringstidspunktFrisinnTest {
    private static final String ARBEIDSFORHOLD = "5678";
    private static final String ARBEIDSFORHOLD2 = "5679";
    private final LocalDate skjæringstidspunktForOpptjening = LocalDate.of(2017, Month.DECEMBER, 5);
    private AktivitetStatusModellFRISINN regelmodell;

    @BeforeEach
    public void setup() {
        regelmodell = new AktivitetStatusModellFRISINN();
        regelmodell.setSkjæringstidspunktForOpptjening(skjæringstidspunktForOpptjening);
    }

    @Test
    public void skal_ikke_sette_stp_hvis_ikke_frilans_eller_sn() {
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusYears(1), skjæringstidspunktForOpptjening.plusDays(10)), ARBEIDSFORHOLD, null);
        AktivPeriode aktivPeriode2 = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusYears(1), skjæringstidspunktForOpptjening.plusDays(1)), ARBEIDSFORHOLD2, null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunktFrisinn().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isNull();
    }

    @Test
    public void skal_sette_stp_hvis_kun_sn() {
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forAndre(Aktivitet.NÆRINGSINNTEKT, Periode.of(skjæringstidspunktForOpptjening.minusYears(1), skjæringstidspunktForOpptjening.plusDays(10)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunktFrisinn().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening);
    }

    @Test
    public void skal_ikke_sette_stp_sn_avsluttes_før_stp() {
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forAndre(Aktivitet.NÆRINGSINNTEKT, Periode.of(skjæringstidspunktForOpptjening.minusYears(1), skjæringstidspunktForOpptjening.minusDays(1)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunktFrisinn().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isNull();
    }

    @Test
    public void skal_sette_stp_hvis_sn_og_at() {
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forAndre(Aktivitet.NÆRINGSINNTEKT, Periode.of(skjæringstidspunktForOpptjening.minusYears(1), skjæringstidspunktForOpptjening));
        AktivPeriode aktivPeriode2 = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusYears(1), skjæringstidspunktForOpptjening.plusDays(10)), ARBEIDSFORHOLD, null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunktFrisinn().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening);
    }
    @Test
    public void skal_sette_stp_hvis_kun_fl() {
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forFrilanser(Periode.of(skjæringstidspunktForOpptjening.minusYears(1), skjæringstidspunktForOpptjening.plusDays(10)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunktFrisinn().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening);
    }

    @Test
    public void skal_ikke_sette_stp_fl_avsluttes_før_stp() {
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forFrilanser(Periode.of(skjæringstidspunktForOpptjening.minusYears(1), skjæringstidspunktForOpptjening.minusDays(1)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunktFrisinn().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isNull();
    }

    @Test
    public void skal_sette_stp_hvis_fl_og_at() {
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forFrilanser(Periode.of(skjæringstidspunktForOpptjening.minusYears(1), skjæringstidspunktForOpptjening));
        AktivPeriode aktivPeriode2 = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusYears(1), skjæringstidspunktForOpptjening.plusDays(10)), ARBEIDSFORHOLD, null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunktFrisinn().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening);
    }

    @Test
    public void skal_sette_stp_sn_varer_over_stp_fl_slutter_før_stp() {
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forFrilanser(Periode.of(skjæringstidspunktForOpptjening.minusYears(1), skjæringstidspunktForOpptjening.minusDays(1)));
        AktivPeriode aktivPeriode2 = AktivPeriode.forAndre(Aktivitet.NÆRINGSINNTEKT, Periode.of(skjæringstidspunktForOpptjening.minusYears(1), skjæringstidspunktForOpptjening.plusDays(10)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunktFrisinn().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening);
    }

}
