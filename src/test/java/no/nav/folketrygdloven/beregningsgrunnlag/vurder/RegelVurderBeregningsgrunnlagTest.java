package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilArbeidsforholdMedInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektsmelding;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import org.assertj.core.data.Offset;
import org.junit.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.vurder.RegelVurderBeregningsgrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;

public class RegelVurderBeregningsgrunnlagTest {

    private static Long generatedId = 1L;
    private final Offset<Double> offset = Offset.offset(0.01);

    private static final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);

    @Test
    public void skalOppretteRegelmerknadForAvslagNårBruttoInntektPrÅrMindreEnnHalvG() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2017 * 0.49;
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode)).containsOnly("1041");
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr, offset);
    }

    @Test
    public void skalOppretteRegelmerknadForAvslagForFlereArbeidsforholdNårBruttoInntektPrÅrMindreEnnHalvG() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2017 * 0.25;
        double beregnetPrÅr2 = GRUNNBELØP_2017 * 0.24; //Totalt under 0,5G
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0);
        leggTilArbeidsforhold(beregningsgrunnlag, beregnetPrÅr2, 0);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode)).containsOnly("1041");
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr + beregnetPrÅr2, offset);
    }

    private RegelResultat kjørRegel(BeregningsgrunnlagPeriode grunnlag) {
        RegelVurderBeregningsgrunnlag regel = new RegelVurderBeregningsgrunnlag(grunnlag);
        Evaluation evaluation = regel.evaluer(grunnlag);
        return RegelmodellOversetter.getRegelResultat(evaluation, "input");
    }

    private Beregningsgrunnlag opprettBeregningsgrunnlag(LocalDate skjæringstidspunkt, double beregnetPrÅr, double refusjonskravPrÅr) {
        return opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, refusjonskravPrÅr, Dekningsgrad.DEKNINGSGRAD_100);
    }

    private Beregningsgrunnlag opprettBeregningsgrunnlag(LocalDate skjæringstidspunkt, double beregnetPrÅr, double refusjonskravPrÅr, Dekningsgrad dekningsgrad) {
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.valueOf(beregnetPrÅr / 12), BigDecimal.valueOf(refusjonskravPrÅr / 12));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Beregningsgrunnlag.builder(beregningsgrunnlag).medDekningsgrad(dekningsgrad);

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
