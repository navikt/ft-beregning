package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SettGrenseverdiTilNull extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FRISINN 6.10";
    public static final String BESKRIVELSE = "Sett grenseverdi til 0";

    public SettGrenseverdiTilNull() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
        BigDecimal grenseverdi = BigDecimal.ZERO;
        grunnlag.setGrenseverdi(grenseverdi);
        resultater.put("grenseverdi", grenseverdi);
        grunnlag.setGrenseverdi(grenseverdi);
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;

    }

}
