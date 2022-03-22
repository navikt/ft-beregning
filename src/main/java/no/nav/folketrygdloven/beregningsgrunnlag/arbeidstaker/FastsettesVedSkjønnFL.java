package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.math.RoundingMode;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsakKoder;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;

@RuleDocumentation(FastsettesVedSkjønnFL.ID)
class FastsettesVedSkjønnFL extends IkkeBeregnet {

	static final String ID = BeregningUtfallÅrsakKoder.AVVIK_25;

    FastsettesVedSkjønnFL() {
        super(new BeregningUtfallMerknad(BeregningUtfallÅrsak.FASTSETT_AVVIK_OVER_25_PROSENT_FRILANS));
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL);
        BigDecimal avvikProsent = sg.getAvvikProsent();
        return nei(ruleReasonRef, String.valueOf(avvikProsent.setScale(0, RoundingMode.HALF_EVEN)));
    }
}
