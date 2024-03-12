package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkHarRefusjonSomOverstigerBeregningsgrunnlag.ID)
class SjekkHarRefusjonSomOverstigerBeregningsgrunnlag extends LeafSpecification<FordelModell> {

    static final String ID = "FP_BR 22.3.1";
    static final String BESKRIVELSE = "Har arbeidstaker som søker refusjon som overstiger beregningsgrunnlag?";

    SjekkHarRefusjonSomOverstigerBeregningsgrunnlag() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(FordelModell grunnlag) {
	    var arbeidsandeler = grunnlag.getInput().getAlleAndelerForStatus(AktivitetStatus.AT);
	    var arbeidsforholdSomHarRefusjonStørreEnnBG = arbeidsandeler.stream()
            .filter(this::harRefusjonskravStørreEnnBg).toList();
	    var resultat = arbeidsforholdSomHarRefusjonStørreEnnBG.isEmpty() ? nei() : ja();
	    for (FordelAndelModell arbeidsforhold : arbeidsforholdSomHarRefusjonStørreEnnBG) {
            resultat.setEvaluationProperty("refusjonPrÅr." + arbeidsforhold.getArbeidsgiverId().orElseThrow(), arbeidsforhold.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO));
            resultat.setEvaluationProperty("bruttoPrÅr." + arbeidsforhold.getArbeidsgiverId().orElseThrow(), arbeidsforhold.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO));
        }
        return resultat;
    }

    private boolean harRefusjonskravStørreEnnBg(FordelAndelModell andel) {
        BigDecimal refusjonskrav = andel.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO);
        return refusjonskrav.compareTo(andel.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO)) > 0;
    }
}
