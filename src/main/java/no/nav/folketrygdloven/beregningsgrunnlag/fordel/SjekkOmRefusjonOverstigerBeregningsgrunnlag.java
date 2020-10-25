package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class SjekkOmRefusjonOverstigerBeregningsgrunnlag extends LeafSpecification<BeregningsgrunnlagPeriode> {

    private static final String ID = "FP_BR 22.3.7";
    private static final String BESKRIVELSE = "Sjekk om refusjon overstiger beregningsgrunnlag for arbeidsforhold.";
    private Arbeidsforhold arbeidsforhold;

    SjekkOmRefusjonOverstigerBeregningsgrunnlag(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold.getArbeidsforhold();
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BigDecimal refusjonskravPrÅr = finnSamletBeløpFraArbeidsforhold(grunnlag, BeregningsgrunnlagPrArbeidsforhold::getGjeldendeRefusjonPrÅr);
        BigDecimal bruttoInkludertNaturalytelsePrÅr = finnSamletBeløpFraArbeidsforhold(grunnlag, BeregningsgrunnlagPrArbeidsforhold::getBruttoInkludertNaturalytelsePrÅr);
        BigDecimal refusjonBruttoBgDiff = refusjonskravPrÅr.subtract(bruttoInkludertNaturalytelsePrÅr);
        SingleEvaluation resultat = refusjonBruttoBgDiff.compareTo(BigDecimal.ZERO) > 0 ? ja() : nei();
        resultat.setEvaluationProperty("refusjonskravPrÅr." + arbeidsforhold.getArbeidsgiverId(), refusjonskravPrÅr);
        resultat.setEvaluationProperty("bruttoInklNaturalytelsePrÅr." + arbeidsforhold.getArbeidsgiverId(), bruttoInkludertNaturalytelsePrÅr);
        return resultat;
    }

    private BigDecimal finnSamletBeløpFraArbeidsforhold(BeregningsgrunnlagPeriode grunnlag, Function<BeregningsgrunnlagPrArbeidsforhold, Optional<BigDecimal>> getBeløpOptional) {
        return finnGrunnlagFraArbeidsforhold(grunnlag).stream().map(getBeløpOptional)
            .filter(Optional::isPresent).map(Optional::get).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private List<BeregningsgrunnlagPrArbeidsforhold> finnGrunnlagFraArbeidsforhold(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().stream()
        .filter(a -> a.getArbeidsforhold().equals(arbeidsforhold))
            .collect(Collectors.toList());
    }
}
