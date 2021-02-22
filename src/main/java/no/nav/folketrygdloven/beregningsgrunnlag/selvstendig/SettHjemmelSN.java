package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_SELVSTENDIG;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel.K9_HJEMMEL_MIDLERTIDIG_INAKTIV;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SettHjemmelSN.ID)
public class SettHjemmelSN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_SETT_HJEMMEL_SN";
    private static final String BESKRIVELSE = "Sett hjemmel";

	public SettHjemmelSN() {
        super(ID, BESKRIVELSE);
	}

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
	    if (grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL) == null) {
		    var ytelsesSpesifiktGrunnlag = grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlagHvisFinnes();
		    BeregningsgrunnlagHjemmel hjemmel;
		    if (ytelsesSpesifiktGrunnlag.map(YtelsesSpesifiktGrunnlag::erKap9Ytelse).orElse(false)) {
			    hjemmel =  K9_HJEMMEL_BARE_SELVSTENDIG;
		    } else {
			    hjemmel = K14_HJEMMEL_BARE_SELVSTENDIG;
		    }
		    grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.SN).setHjemmel(hjemmel);
		    resultater.put("hjemmel", hjemmel);
	    }
        return beregnet(resultater);
    }

}
