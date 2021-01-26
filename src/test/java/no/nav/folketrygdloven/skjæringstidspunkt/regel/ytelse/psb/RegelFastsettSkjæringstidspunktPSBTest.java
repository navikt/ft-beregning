package no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse.psb;

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

public class RegelFastsettSkjæringstidspunktPSBTest {
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
    public void skalBeregneSkjæringstidspunktLikOpptjening() {
        // Arrange
        LocalDate sisteAktivitetsdag = skjæringstidspunktForOpptjening.minusDays(1);
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), sisteAktivitetsdag), ARBEIDSFORHOLD, null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunktPSB().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening);
    }

    @Test
    public void skalBeregneSkjæringstidspunktLikOpptjeningForVedvarendeAktivitet() {
        // Arrange
        AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(skjæringstidspunktForOpptjening.minusMonths(5), null), ARBEIDSFORHOLD, null);
        regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);

        // Act
        Evaluation evaluation = new RegelFastsettSkjæringstidspunktPSB().evaluer(regelmodell);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(regelmodell.getSkjæringstidspunktForBeregning()).isEqualTo(skjæringstidspunktForOpptjening);
    }



}
