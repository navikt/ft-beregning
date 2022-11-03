package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.MidlertidigInaktivType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FinnGrenseverdi.ID)
public class FinnGrenseverdi extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR 6.2";
    public static final String BESKRIVELSE = "Finn grenseverdi";

    public FinnGrenseverdi() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
        BigDecimal sumAvkortetSkalBrukes = BigDecimal.ZERO;
        for (BeregningsgrunnlagPrStatus bps : grunnlag.getBeregningsgrunnlagPrStatusSomSkalBrukes()) {
            if (bps.erArbeidstakerEllerFrilanser()) {
                sumAvkortetSkalBrukes = sumAvkortetSkalBrukes.add(bps.getArbeidsforholdSomSkalBrukes().stream()
                    .map(arb -> arb.getAndelsmessigFørGraderingPrAar().multiply(arb.getUtbetalingsprosent().scaleByPowerOfTen(-2)))
                    .reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
            } else {
                sumAvkortetSkalBrukes = sumAvkortetSkalBrukes.add(bps.getAndelsmessigFørGraderingPrAar().multiply(bps.getUtbetalingsprosent().scaleByPowerOfTen(-2)));
            }
        }
	    var erInaktivTypeA = MidlertidigInaktivType.A.equals(grunnlag.getBeregningsgrunnlag().getMidlertidigInaktivType());
	    var grenseverdi = sumAvkortetSkalBrukes;
	    if (erInaktivTypeA) {
        	grenseverdi = grenseverdi.multiply(grunnlag.getBeregningsgrunnlag().getMidlertidigInaktivTypeAReduksjonsfaktor());
        }
	    resultater.put("grenseverdi", grenseverdi);
        grunnlag.setGrenseverdi(grenseverdi);
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;

    }
}
