package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RuleDocumentation(BeregnPrArbeidsforholdFraAOrdningenFRISINN.ID)
class BeregnPrArbeidsforholdFraAOrdningenFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FRISINN 2.3";
    static final String BESKRIVELSE = "Rapportert inntekt = snitt av mnd-inntekter i beregningsperioden * 12";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;
    private static final LocalDate TOM = LocalDate.of(2020,2,29);

    BeregnPrArbeidsforholdFraAOrdningenFRISINN(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        Objects.requireNonNull(arbeidsforhold, "arbeidsforhold");
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        int periodeLengdeMND = 12;
        List<BigDecimal> inntekter = grunnlag.getInntektsgrunnlag().getPeriodeinntekter(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, TOM, periodeLengdeMND);
        BigDecimal sum = inntekter.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
            .medBeregnetPrÅr(sum)
            .build();
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("beregnetPrÅr", arbeidsforhold.getBeregnetPrÅr());
        resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
        return beregnet(resultater);
    }
}
