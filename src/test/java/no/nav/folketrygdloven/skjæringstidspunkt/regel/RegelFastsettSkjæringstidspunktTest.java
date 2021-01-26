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
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFP;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class RegelFastsettSkjæringstidspunktTest {
    private static final String ARBEIDSFORHOLD = "5678";
    private static final String ARBEIDSFORHOLD2 = "5679";
    private final LocalDate skjæringstidspunktForOpptjening = LocalDate.of(2017, Month.DECEMBER, 5);
    private AktivitetStatusModell regelmodell;

    @BeforeEach
    public void setup() {
        regelmodell = new AktivitetStatusModell();
        regelmodell.setSkjæringstidspunktForOpptjening(skjæringstidspunktForOpptjening);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikFredagNårEnAktivitetSlutterFredagOgSkjæringstidspunktOpptjeningErPåSøndagForForeldrepenger() {
        // Arrange
        LocalDate søndag = LocalDate.of(2019, 10, 6);
        LocalDate lørdag = LocalDate.of(2019, 10, 5);
        LocalDate fredag = LocalDate.of(2019, 10, 4);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(søndag.minusMonths(5), søndag.plusMonths(2)), ARBEIDSFORHOLD, null);
        AktivPeriode aktivPeriode2 = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(søndag.minusMonths(5), fredag), ARBEIDSFORHOLD2, null);
        regelmodell = new AktivitetStatusModellFP();
        regelmodell.setSkjæringstidspunktForOpptjening(søndag);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(fredag);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikLørdagNårEnAktivitetSlutterFredagOgSkjæringstidspunktOpptjeningErPåSøndagIkkeForeldrepenger() {
        // Arrange
        LocalDate søndag = LocalDate.of(2019, 10, 6);
        LocalDate lørdag = LocalDate.of(2019, 10, 5);
        LocalDate fredag = LocalDate.of(2019, 10, 4);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(søndag.minusMonths(5), søndag.plusMonths(2)), ARBEIDSFORHOLD, null);
        AktivPeriode aktivPeriode2 = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(søndag.minusMonths(5), fredag), ARBEIDSFORHOLD2, null);
        regelmodell = new AktivitetStatusModell();
        regelmodell.setSkjæringstidspunktForOpptjening(søndag);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(lørdag);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikLørdagNårEnAktivitetSlutterLørdagOgSkjæringstidspunktOpptjeningErPåMandagForForeldrepenger() {
        // Arrange
        LocalDate mandag = LocalDate.of(2019, 10, 7);
        LocalDate lørdag = LocalDate.of(2019, 10, 5);
        LocalDate søndag = LocalDate.of(2019, 10, 6);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(mandag.minusMonths(5), mandag.plusMonths(2)), ARBEIDSFORHOLD, null);
        AktivPeriode aktivPeriode2 = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(mandag.minusMonths(5), lørdag), ARBEIDSFORHOLD2, null);
        regelmodell = new AktivitetStatusModellFP();
        regelmodell.setSkjæringstidspunktForOpptjening(mandag);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(lørdag);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikSøndagNårEnAktivitetSlutterLørdagOgSkjæringstidspunktOpptjeningErPåMandagIkkeForeldrepenger() {
        // Arrange
        LocalDate mandag = LocalDate.of(2019, 10, 7);
        LocalDate lørdag = LocalDate.of(2019, 10, 5);
        LocalDate søndag = LocalDate.of(2019, 10, 6);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(mandag.minusMonths(5), mandag.plusMonths(2)), ARBEIDSFORHOLD, null);
        AktivPeriode aktivPeriode2 = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(mandag.minusMonths(5), lørdag), ARBEIDSFORHOLD2, null);
        regelmodell = new AktivitetStatusModell();
        regelmodell.setSkjæringstidspunktForOpptjening(mandag);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(søndag);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikFredagNårEnAktivitetSlutterFredagOgSkjæringstidspunktOpptjeningErPåMandagForeldrepenger() {
        // Arrange
        LocalDate mandag = LocalDate.of(2019, 10, 7);
        LocalDate fredag = LocalDate.of(2019, 10, 4);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(mandag.minusMonths(5), mandag.plusMonths(2)), ARBEIDSFORHOLD, null);
        AktivPeriode aktivPeriode2 = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(mandag.minusMonths(5), fredag), ARBEIDSFORHOLD2, null);
        regelmodell = new AktivitetStatusModellFP();
        regelmodell.setSkjæringstidspunktForOpptjening(mandag);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(fredag);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikSøndagNårEnAktivitetSlutterFredagOgSkjæringstidspunktOpptjeningErPåMandagIkkeForeldrepenger() {
        // Arrange
        LocalDate mandag = LocalDate.of(2019, 10, 7);
	    LocalDate søndag = LocalDate.of(2019, 10, 6);
	    LocalDate fredag = LocalDate.of(2019, 10, 4);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(mandag.minusMonths(5), mandag.plusMonths(2)), ARBEIDSFORHOLD, null);
        AktivPeriode aktivPeriode2 = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(mandag.minusMonths(5), fredag), ARBEIDSFORHOLD2, null);
        regelmodell = new AktivitetStatusModell();
        regelmodell.setSkjæringstidspunktForOpptjening(mandag);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(søndag);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikFredagNårEnAktivitetSlutterFredagOgEnAnnenBegynnerPåLørdagOgSkjæringstidspunktOpptjeningErPåMandagForeldrepenger() {
        // Arrange
        LocalDate mandag = LocalDate.of(2019, 10, 7);
        LocalDate lørdag = LocalDate.of(2019, 10, 5);
        LocalDate fredag = LocalDate.of(2019, 10, 4);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(lørdag, lørdag.plusMonths(2)), ARBEIDSFORHOLD, null);
        AktivPeriode aktivPeriode2 = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(fredag.minusMonths(5), fredag), ARBEIDSFORHOLD2, null);
        regelmodell = new AktivitetStatusModellFP();
        regelmodell.setSkjæringstidspunktForOpptjening(mandag);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(fredag);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikSøndagNårEnAktivitetSlutterFredagOgEnAnnenBegynnerPåLørdagOgSkjæringstidspunktOpptjeningErPåMandagIkkeForeldrepenger() {
        // Arrange
        LocalDate mandag = LocalDate.of(2019, 10, 7);
        LocalDate lørdag = LocalDate.of(2019, 10, 5);
	    LocalDate søndag = LocalDate.of(2019, 10, 6);
	    LocalDate fredag = LocalDate.of(2019, 10, 4);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(lørdag, lørdag.plusMonths(2)), ARBEIDSFORHOLD, null);
        AktivPeriode aktivPeriode2 = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(fredag.minusMonths(5), fredag), ARBEIDSFORHOLD2, null);
        regelmodell = new AktivitetStatusModell();
        regelmodell.setSkjæringstidspunktForOpptjening(mandag);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);
        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(søndag);
    }


    @Test
    public void skalFastsetteSisteAktivitetsdag() {
        // Arrange
        LocalDate sisteAktivitetsdag = skjæringstidspunktForOpptjening.minusWeeks(1);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteAktivitetsdag), ARBEIDSFORHOLD, null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(6), sisteAktivitetsdag.plusDays(10)), ARBEIDSFORHOLD, null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(7), sisteAktivitetsdag.minusDays(10)), ARBEIDSFORHOLD, null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        // Act
        LocalDate sisteDag = regelmodell.sisteAktivitetsdato();
        // Assert
        assertThat(regelmodell.getAktivePerioder()).hasSize(1);
        assertThat(sisteDag).isEqualTo(skjæringstidspunktForOpptjening);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikDagenFørOpptjening() {
        // Arrange
        LocalDate sisteAktivitetsdag = skjæringstidspunktForOpptjening.minusDays(1);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteAktivitetsdag), ARBEIDSFORHOLD, null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening.minusDays(1));
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikDagenFørOpptjeningForVedvarendeAktivitet() {
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), null), ARBEIDSFORHOLD, null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening.minusDays(1));
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikSisteAktivitetsdag() {
        // Arrange
        LocalDate sisteAktivitetsdag = LocalDate.of(2017, Month.OCTOBER, 14);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteAktivitetsdag), ARBEIDSFORHOLD, null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(sisteAktivitetsdag);
    }

    @Test
    public void skalFlytteSkjæringstidspunktSisteAktivitetsdagFørMilitær() {
        // Arrange
        LocalDate sisteAktivitetsdag = skjæringstidspunktForOpptjening.minusMonths(1);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteAktivitetsdag), ARBEIDSFORHOLD, null);
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

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(sisteAktivitetsdag);
        assertThat(regelmodell.getAktivePerioder()).hasSize(3);
        assertThat(regelmodell.getAktivePerioder().stream().map(AktivPeriode::getAktivitet).collect(Collectors.toList()))
            .containsExactlyInAnyOrder(Aktivitet.ARBEIDSTAKERINNTEKT, Aktivitet.MILITÆR_ELLER_SIVILTJENESTE, Aktivitet.SYKEPENGER_MOTTAKER);
    }

    @Test
    public void skalIkkeFlytteSkjæringstidspunktNårMilitærMedAnnenAktivitetPåStpForOpptjening() {
        // Arrange
        LocalDate sisteArbeidsdag = skjæringstidspunktForOpptjening.minusWeeks(1);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteArbeidsdag), ARBEIDSFORHOLD, null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        aktivPeriode = AktivPeriode.forAndre(Aktivitet.MILITÆR_ELLER_SIVILTJENESTE, Periode.of(skjæringstidspunktForOpptjening.minusWeeks(8), sisteArbeidsdag));
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
        assertThat(regelmodell.getAktivePerioder()).hasSize(2);

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(sisteArbeidsdag);
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

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening.minusDays(1));
        assertThat(regelmodell.getAktivePerioder()).hasSize(1);
    }
}
