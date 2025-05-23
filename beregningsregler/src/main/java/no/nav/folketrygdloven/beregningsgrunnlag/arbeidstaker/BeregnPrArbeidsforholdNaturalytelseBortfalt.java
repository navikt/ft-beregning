package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnPrArbeidsforholdNaturalytelseBortfalt.ID)
class BeregnPrArbeidsforholdNaturalytelseBortfalt extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 15.4";
    static final String BESKRIVELSE = "Beregn naturalytelse -> naturalytelseverdi * 12";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    BeregnPrArbeidsforholdNaturalytelseBortfalt(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

        var fom = grunnlag.getSkjæringstidspunkt();
        var tom = grunnlag.getBeregningsgrunnlagPeriode().getFom();
        var inntektsgrunnlag = grunnlag.getInntektsgrunnlag();
        var beløp = inntektsgrunnlag.finnTotaltNaturalytelseBeløpMedOpphørsdatoIPeriodeForArbeidsforhold(arbeidsforhold.getArbeidsforhold(), fom, tom)
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Skal ikke være mulig å havne i denne regelen uten beløp."));
        var naturalytelseBortfaltPrÅr = beløp.multiply(BigDecimal.valueOf(12));

        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
            .medNaturalytelseBortfaltPrÅr(naturalytelseBortfaltPrÅr)
            .build();

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("naturalytelseBortfaltPrÅr", naturalytelseBortfaltPrÅr);
        resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
        return beregnet(resultater);
    }
}
