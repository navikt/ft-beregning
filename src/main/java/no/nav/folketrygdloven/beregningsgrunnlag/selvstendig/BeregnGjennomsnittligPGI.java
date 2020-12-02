package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import static no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.FinnGjennomsnittligPGI.finnGjennomsnittligPGI;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnGjennomsnittligPGI.ID)
public class BeregnGjennomsnittligPGI extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.2";
    static final String BESKRIVELSE = "Beregn gjennomsnittlig PGI oppjustert til G";

    public BeregnGjennomsnittligPGI() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
	    Map<String, Object> resultater = new HashMap<>();
	    BigDecimal gjennomsnittligPGI = finnGjennomsnittligPGI(bgps.getBeregningsperiode().getTom(), grunnlag.getBeregningsgrunnlag().getGrunnbeløpsatser(), grunnlag.getInntektsgrunnlag(), grunnlag.getGrunnbeløp(), resultater);
	    resultater.put("GjennomsnittligPGI", gjennomsnittligPGI);
        BeregningsgrunnlagPrStatus.builder(bgps)
            .medGjennomsnittligPGI(gjennomsnittligPGI)
            .build();

        return beregnet(resultater);
    }


}
