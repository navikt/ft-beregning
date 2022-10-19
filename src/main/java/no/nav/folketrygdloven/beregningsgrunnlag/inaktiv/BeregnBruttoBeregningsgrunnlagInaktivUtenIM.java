package no.nav.folketrygdloven.beregningsgrunnlag.inaktiv;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

/**
 * Setter beregnet til å vere gjennomsnittlig pensjonsgivende inntekt (PGI) fra de siste tre ferdiglignede år.
 */
public class BeregnBruttoBeregningsgrunnlagInaktivUtenIM extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.8";
    static final String BESKRIVELSE = "Beregn brutto beregningsgrunnlag inaktiv uten inntektsmelding";

    public BeregnBruttoBeregningsgrunnlagInaktivUtenIM() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.BA);
        BigDecimal gjennomsnittligPGI = bgps.getGjennomsnittligPGI() == null ? BigDecimal.ZERO : bgps.getGjennomsnittligPGI();
        BeregningsgrunnlagPrStatus.builder(bgps).medBeregnetPrÅr(gjennomsnittligPGI).build();
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("bruttoBeregningsgrunnlagInaktivUtenIM", gjennomsnittligPGI);
        return beregnet(resultater);
    }

}
