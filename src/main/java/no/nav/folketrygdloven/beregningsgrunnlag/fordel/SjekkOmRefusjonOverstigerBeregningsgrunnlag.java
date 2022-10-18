package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class SjekkOmRefusjonOverstigerBeregningsgrunnlag extends LeafSpecification<FordelModell> {

    private static final String ID = "FP_BR 22.3.7";
    private static final String BESKRIVELSE = "Sjekk om refusjon overstiger beregningsgrunnlag for arbeidsforhold.";
    private Arbeidsforhold arbeidsforhold;

    SjekkOmRefusjonOverstigerBeregningsgrunnlag(FordelAndelModell andelMedHøyereRefEnnBG) {
        super(ID, BESKRIVELSE);
	    this.arbeidsforhold = andelMedHøyereRefEnnBG.getArbeidsforhold().orElseThrow();
    }

    @Override
    public Evaluation evaluate(FordelModell modell) {
        BigDecimal refusjonskravPrÅr = finnSamletBeløpFraArbeidsforhold(modell.getInput(), FordelAndelModell::getGjeldendeRefusjonPrÅr);
        BigDecimal bruttoInkludertNaturalytelsePrÅr = finnSamletBeløpFraArbeidsforhold(modell.getInput(), FordelAndelModell::getBruttoInkludertNaturalytelsePrÅr);
        BigDecimal refusjonBruttoBgDiff = refusjonskravPrÅr.subtract(bruttoInkludertNaturalytelsePrÅr);
        SingleEvaluation resultat = refusjonBruttoBgDiff.compareTo(BigDecimal.ZERO) > 0 ? ja() : nei();
        resultat.setEvaluationProperty("refusjonskravPrÅr." + arbeidsforhold.getArbeidsgiverId(), refusjonskravPrÅr);
        resultat.setEvaluationProperty("bruttoInklNaturalytelsePrÅr." + arbeidsforhold.getArbeidsgiverId(), bruttoInkludertNaturalytelsePrÅr);
        return resultat;
    }

    private BigDecimal finnSamletBeløpFraArbeidsforhold(FordelPeriodeModell grunnlag, Function<FordelAndelModell, Optional<BigDecimal>> getBeløpOptional) {
        return finnGrunnlagFraArbeidsforhold(grunnlag).stream().map(getBeløpOptional)
            .filter(Optional::isPresent).map(Optional::get).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private List<FordelAndelModell> finnGrunnlagFraArbeidsforhold(FordelPeriodeModell grunnlag) {
        return grunnlag.getAlleAndelerForStatus(AktivitetStatus.AT).stream()
        .filter(a -> Objects.requireNonNull(a.getArbeidsforhold().orElse(null)).equals(arbeidsforhold))
            .collect(Collectors.toList());
    }
}
