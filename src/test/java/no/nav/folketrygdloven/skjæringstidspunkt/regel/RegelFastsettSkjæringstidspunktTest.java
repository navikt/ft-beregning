package no.nav.folketrygdloven.skjæringstidspunkt.regel;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class RegelFastsettSkjæringstidspunktTest {
    private static final String ARBEIDSFORHOLD = "5678";
    private final LocalDate skjæringstidspunktForOpptjening = LocalDate.of(2017, Month.DECEMBER, 5);
    private AktivitetStatusModell regelmodell;

    @BeforeEach
    public void setup() {
        regelmodell = new AktivitetStatusModell();
        regelmodell.setSkjæringstidspunktForOpptjening(skjæringstidspunktForOpptjening);
    }

    @Test
    public void skalFastsetteSisteAktivitetsdag() {
        // Arrange
        LocalDate sisteAktivitetsdag = skjæringstidspunktForOpptjening.minusWeeks(1);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteAktivitetsdag), ARBEIDSFORHOLD, null, false);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(6), sisteAktivitetsdag.plusDays(10)), ARBEIDSFORHOLD, null, false);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(7), sisteAktivitetsdag.minusDays(10)), ARBEIDSFORHOLD, null, false);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        // Act
        LocalDate sisteDag = regelmodell.sisteAktivitetsdato();
        // Assert
        assertThat(regelmodell.getAktivePerioder()).hasSize(1);
        assertThat(sisteDag).isEqualTo(skjæringstidspunktForOpptjening);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikOpptjening() {
        // Arrange
        LocalDate sisteAktivitetsdag = skjæringstidspunktForOpptjening.minusDays(1);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteAktivitetsdag), ARBEIDSFORHOLD, null, false);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikOpptjeningForVedvarendeAktivitet() {
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), null), ARBEIDSFORHOLD, null, false);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikDagenEtterAktivitet() {
        // Arrange
        LocalDate sisteAktivitetsdag = LocalDate.of(2017, Month.OCTOBER, 14);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteAktivitetsdag), ARBEIDSFORHOLD, null, false);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(sisteAktivitetsdag.plusDays(1));
    }

    @Test
    public void skalFlytteSkjæringstidspunktTilDagenEtterAktivitetFørMilitær() {
        // Arrange
        LocalDate sisteAktivitetsdag = skjæringstidspunktForOpptjening.minusMonths(1);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteAktivitetsdag), ARBEIDSFORHOLD, null, false);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.MILITÆR_ELLER_SIVILTJENESTE, Periode.of(skjæringstidspunktForOpptjening.minusMonths(1), skjæringstidspunktForOpptjening));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.SYKEPENGER_MOTTAKER, Periode.of(skjæringstidspunktForOpptjening.minusMonths(6), skjæringstidspunktForOpptjening.minusMonths(6)));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(3);

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(sisteAktivitetsdag.plusDays(1));
        assertThat(regelmodell.getAktivePerioder()).hasSize(3);
        assertThat(regelmodell.getAktivePerioder().stream().map(AktivPeriode::getAktivitet).collect(Collectors.toList()))
            .containsExactlyInAnyOrder(Aktivitet.ARBEIDSTAKERINNTEKT, Aktivitet.MILITÆR_ELLER_SIVILTJENESTE, Aktivitet.SYKEPENGER_MOTTAKER);
    }

    @Test
    public void skalIkkeFlytteSkjæringstidspunktNårMilitærMedAnnenAktivitetPåStpForOpptjening() {
        // Arrange
        LocalDate sisteArbeidsdag = skjæringstidspunktForOpptjening.minusWeeks(1);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteArbeidsdag), ARBEIDSFORHOLD, null, false);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.MILITÆR_ELLER_SIVILTJENESTE, Periode.of(skjæringstidspunktForOpptjening.minusWeeks(8), sisteArbeidsdag));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(sisteArbeidsdag.plusDays(1));
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);
    }

    @Test
    public void skalIkkeFlytteSkjæringstidspunktNårMilitærErEnesteAktivitet() {
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forAndre(Aktivitet.MILITÆR_ELLER_SIVILTJENESTE, Periode.of(skjæringstidspunktForOpptjening.minusMonths(8), skjæringstidspunktForOpptjening));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(1);

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening);
        assertThat(regelmodell.getAktivePerioder()).hasSize(1);
    }
}
