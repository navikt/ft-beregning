package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.math.RoundingMode;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsakKoder;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;

@RuleDocumentation(FastsettesVedSkjønnEtterTidsbegrensetArbeidsforhold.ID)
class FastsettesVedSkjønnEtterTidsbegrensetArbeidsforhold extends IkkeBeregnet {

    static final String ID = BeregningUtfallÅrsakKoder.AVVIK_25_TIDBEGRENSET;

    FastsettesVedSkjønnEtterTidsbegrensetArbeidsforhold() {
        super(new BeregningUtfallMerknad(BeregningUtfallÅrsak.FASTSETT_AVVIK_TIDSBEGRENSET));
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.AT_FL);
        BigDecimal avvikProsent = sg.getAvvikProsent();
        return nei(ruleReasonRef, String.valueOf(avvikProsent.setScale(0, RoundingMode.HALF_EVEN)));
    }
}
