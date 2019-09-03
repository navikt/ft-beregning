package no.nav.foreldrepenger.beregningsgrunnlag.fastsette;

import java.math.BigDecimal;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkSumMaxRefusjonskravStørreEnn6G.ID)
class SjekkSumMaxRefusjonskravStørreEnn6G extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR XX";
    static final String BESKRIVELSE = "Er totalt refusjonskrav større enn 6G/grenseverdi";

    SjekkSumMaxRefusjonskravStørreEnn6G() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus atfl = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (atfl == null) {
            return nei();
        }
        BigDecimal grenseverdi = grunnlag.getGrenseverdi();
        BigDecimal sum = atfl.getArbeidsforholdSomSkalBrukes().stream()
            .filter(bpaf -> bpaf.getMaksimalRefusjonPrÅr() != null)
            .map(BeregningsgrunnlagPrArbeidsforhold::getMaksimalRefusjonPrÅr)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
        SingleEvaluation resultat = sum.compareTo(grenseverdi) > 0 ? ja() : nei();
        resultat.setEvaluationProperty("totaltRefusjonskravPrÅr", sum);
        resultat.setEvaluationProperty("grunnbeløp", grunnlag.getGrunnbeløp());
        resultat.setEvaluationProperty("grenseverdi", grenseverdi);
        return resultat;
    }
}
