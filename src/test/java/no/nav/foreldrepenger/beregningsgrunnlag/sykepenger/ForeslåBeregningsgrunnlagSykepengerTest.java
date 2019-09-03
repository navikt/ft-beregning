package no.nav.foreldrepenger.beregningsgrunnlag.sykepenger;

import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektsmelding;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettSammenligningsgrunnlag;
import static no.nav.foreldrepenger.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBeregnet;
import static no.nav.foreldrepenger.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.foreldrepenger.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class ForeslåBeregningsgrunnlagSykepengerTest {

    private LocalDate skjæringstidspunkt;

    @Before
    public void setup() {
        skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
    }

    @Test
    public void skalBeregneSykepengerGrunnlagMedInntektsmeldingMedNaturalYtelserIArbeidsgiverperioden() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(40000);
        BigDecimal refusjonskrav = BigDecimal.valueOf(10000);
        BigDecimal naturalytelse = BigDecimal.valueOf(2000);
        LocalDate naturalytelseOpphørFom = skjæringstidspunkt.plusDays(7);
        List<Periode> arbeidsgiversPeriode = List.of(Periode.of(skjæringstidspunkt, skjæringstidspunkt.plusDays(14)));
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, månedsinntekt,
            refusjonskrav, naturalytelse, naturalytelseOpphørFom);
        Beregningsgrunnlag.builder(beregningsgrunnlag).medBeregningForSykepenger(true);
        opprettSammenligningsgrunnlag(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, BigDecimal.valueOf(42000));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold bgArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(bgArbeidsforhold).medArbeidsgiverperioder(arbeidsgiversPeriode).build();
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat resultat = RegelmodellOversetter.getRegelResultat(evaluation, "input");

        assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).isEmpty();
        assertThat(bgArbeidsforhold.getNaturalytelseBortfaltPrÅr().get()).isEqualByComparingTo(BigDecimal.valueOf(24000)); //NOSONAR
        assertBeregningsgrunnlag(grunnlag, månedsinntekt, 24000);
    }

    @Test
    public void skalBeregneSykepengerGrunnlagMedInntektsmeldingMedNaturalYtelserUtenforArbeidsgiverperioden() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(40000);
        BigDecimal refusjonskrav = BigDecimal.valueOf(10000);
        BigDecimal naturalytelse = BigDecimal.valueOf(2000);
        LocalDate naturalytelseOpphørFom = skjæringstidspunkt.plusDays(7);
        List<Periode> arbeidsgiversPeriode = List.of(Periode.of(skjæringstidspunkt, skjæringstidspunkt.plusDays(6)),
            Periode.of(skjæringstidspunkt.plusDays(10), skjæringstidspunkt.plusDays(18)));
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, månedsinntekt,
            refusjonskrav, naturalytelse, naturalytelseOpphørFom);
        Beregningsgrunnlag.builder(beregningsgrunnlag).medBeregningForSykepenger(true);
        opprettSammenligningsgrunnlag(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, BigDecimal.valueOf(40000));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold bgArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(bgArbeidsforhold).medArbeidsgiverperioder(arbeidsgiversPeriode).build();
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        RegelResultat resultat = RegelmodellOversetter.getRegelResultat(evaluation, "input");

        assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).isEmpty();
        assertThat(bgArbeidsforhold.getNaturalytelseBortfaltPrÅr()).isEmpty();
        assertBeregningsgrunnlag(grunnlag, månedsinntekt, 0);
    }

    @Test
    public void skalIkkeBeregneSykepengerGrunnlagMedInntektsmeldingMedNaturalYtelserIArbeidsgiverperiodenNårIFPSAK() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(40000);
        BigDecimal refusjonskrav = BigDecimal.valueOf(10000);
        BigDecimal naturalytelse = BigDecimal.valueOf(2000);
        LocalDate naturalytelseOpphørFom = skjæringstidspunkt.plusDays(7);
        List<Periode> arbeidsgiversPeriode = List.of(Periode.of(skjæringstidspunkt, skjæringstidspunkt.plusDays(14)));
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, månedsinntekt,
            refusjonskrav, naturalytelse, naturalytelseOpphørFom);
        opprettSammenligningsgrunnlag(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, BigDecimal.valueOf(40000));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold bgArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(bgArbeidsforhold).medArbeidsgiverperioder(arbeidsgiversPeriode).build();
        // Act
        new RegelForeslåBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        // Assert
        assertThat(bgArbeidsforhold.getNaturalytelseBortfaltPrÅr()).isEmpty();
        assertBeregningsgrunnlag(grunnlag, månedsinntekt, 0);
    }

    private void assertBeregningsgrunnlag(BeregningsgrunnlagPeriode grunnlag, BigDecimal månedsinntekt, int naturalYtelsePrÅr) {
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(naturalYtelsePrÅr));
        assertThat(grunnlag.getSammenligningsGrunnlag().getAvvikPromille()).isEqualTo(0);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }
}

