package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnPrArbeidsforholdNaturalytelseTilkommet.ID)
class BeregnPrArbeidsforholdNaturalytelseTilkommet extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 15.6";
    static final String BESKRIVELSE = "Beregn naturalytelse -> naturalytelseverdi * 12";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    BeregnPrArbeidsforholdNaturalytelseTilkommet(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

        var fom = grunnlag.getSkjæringstidspunkt();
        var tom = grunnlag.getBeregningsgrunnlagPeriode().getFom();
        var inntektsgrunnlag = grunnlag.getInntektsgrunnlag();
        var beløp = inntektsgrunnlag.finnTotaltNaturalytelseBeløpTilkommetIPeriodeForArbeidsforhold(arbeidsforhold.getArbeidsforhold(), fom, tom)
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Skal ikke være mulig å havne i denne regelen uten beløp."));
        var naturalytelseTilkommetPrÅr = beløp.multiply(BigDecimal.valueOf(12));

        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
            .medNaturalytelseTilkommetPrÅr(naturalytelseTilkommetPrÅr)
            .build();

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("naturalytelseTilkommetPrÅr", naturalytelseTilkommetPrÅr);
        resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
        return beregnet(resultater);
    }
}
