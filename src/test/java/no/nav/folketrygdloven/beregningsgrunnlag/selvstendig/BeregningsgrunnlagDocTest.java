package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDescriptionDigraph;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import no.nav.fpsak.nare.specification.Specification;

public class BeregningsgrunnlagDocTest {

    private static final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
    private BeregningsgrunnlagPeriode regelmodell;

    @BeforeEach
    void setup() {
        regelmodell = BeregningsgrunnlagPeriode.builder()
                .medPeriode(Periode.of(skjæringstidspunkt, null))
                .medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
                        .medAktivitetStatus(AktivitetStatus.SN)
                        .medAndelNr(1L)
                        .build())
                .build();
        Beregningsgrunnlag.builder()
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .medInntektsgrunnlag(new Inntektsgrunnlag())
                .medAktivitetStatuser(Arrays.asList(new AktivitetStatusMedHjemmel(AktivitetStatus.SN, null)))
                .medBeregningsgrunnlagPeriode(regelmodell)
                .medGrunnbeløpSatser(Arrays.asList(new Grunnbeløp(LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2099,  Month.DECEMBER,  31), 90000L, 90000L)))
                .build();
    }

    @Test
    void test_documentation_beregningsgrunnlagSN() { // NOSONAR
        Specification<BeregningsgrunnlagPeriode> beregning = new RegelBeregningsgrunnlagSN().getSpecification();

        String json = EvaluationSerializer.asJson(beregning);
	    assertThat(json).isNotEmpty();

    }
}
