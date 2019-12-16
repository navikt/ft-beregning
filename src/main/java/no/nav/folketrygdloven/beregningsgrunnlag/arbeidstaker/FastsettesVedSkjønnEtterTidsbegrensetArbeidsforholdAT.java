package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;

@RuleDocumentation(FastsettesVedSkjønnEtterTidsbegrensetArbeidsforholdAT.ID)
class FastsettesVedSkjønnEtterTidsbegrensetArbeidsforholdAT extends IkkeBeregnet {

    static final String ID = "5038";
    static final String BESKRIVELSE = "Avvik er > 25% og bruker har tidsbegrenset arbeidsforhold i foregående periode, beregningsgrunnlag fastsettes ved skjønn";
    private static final RuleReasonRef AVVIK_MER_ENN_25_PROSENT_MED_TIDSBEGRENSET_ARBEIDSFORHOLD_I_FOREGÅENDE_PERIODE = new RuleReasonRefImpl(ID, BESKRIVELSE);

    FastsettesVedSkjønnEtterTidsbegrensetArbeidsforholdAT() {
        super(AVVIK_MER_ENN_25_PROSENT_MED_TIDSBEGRENSET_ARBEIDSFORHOLD_I_FOREGÅENDE_PERIODE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT);
        BigDecimal avvikProsent = sg.getAvvikProsent();
        return nei(new RuleReasonRefImpl(ID, String.valueOf(avvikProsent)));
    }
}
