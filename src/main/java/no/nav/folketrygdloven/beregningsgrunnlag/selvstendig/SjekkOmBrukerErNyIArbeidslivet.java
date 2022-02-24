package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBrukerErNyIArbeidslivet.ID)
public class SjekkOmBrukerErNyIArbeidslivet extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.18";

    public static final RuleReasonRef FASTSETT_BG_FOR_SN_NY_I_ARBEIDSLIVET = new BeregningUtfallMerknad(BeregningUtfallÅrsak.FASTSETT_SELVSTENDIG_NY_ARBEIDSLIVET);

    public SjekkOmBrukerErNyIArbeidslivet() {
        super(ID, BeregningUtfallÅrsak.FASTSETT_SELVSTENDIG_NY_ARBEIDSLIVET.getNavn());
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return Boolean.TRUE.equals(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getNyIArbeidslivet())
            ? ja()
            : nei();
    }
}
