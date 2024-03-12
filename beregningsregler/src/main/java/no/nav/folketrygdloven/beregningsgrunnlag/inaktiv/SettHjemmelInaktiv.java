package no.nav.folketrygdloven.beregningsgrunnlag.inaktiv;

import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel.K9_HJEMMEL_MIDLERTIDIG_INAKTIV;

import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

/**
 * Setter hjemmel for bruker som er midlertidig utenfor inntektsgivende arbeid.
 *
 * Hjemmel som brukes er §8-47, og det tas ikke hensyn til om bruker skal beregnes fra a) eller b).
 *
 */
public class SettHjemmelInaktiv extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "INAKTIV_SETT_HJEMMEL";
    public static final String BESKRIVELSE = "Sett hjemmel";

	public SettHjemmelInaktiv() {
        super(ID, BESKRIVELSE);
	}

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
	    grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.MIDL_INAKTIV).setHjemmel(K9_HJEMMEL_MIDLERTIDIG_INAKTIV);
	    resultater.put("hjemmel", K9_HJEMMEL_MIDLERTIDIG_INAKTIV);
        return beregnet(resultater);
    }

}
