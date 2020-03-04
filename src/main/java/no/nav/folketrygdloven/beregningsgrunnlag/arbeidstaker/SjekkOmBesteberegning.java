package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBesteberegning.ID)
class SjekkOmBesteberegning extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR X.X";
    static final String BESKRIVELSE = "Gjelder besteberegning?";

    SjekkOmBesteberegning() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        if (grunnlag.erBesteberegnet()) {
            setHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.F_14_7);
            setHjemmel(grunnlag, AktivitetStatus.ATFL_SN, BeregningsgrunnlagHjemmel.F_14_7);
            return ja();
        }
        return nei();
    }

    private void setHjemmel(BeregningsgrunnlagPeriode grunnlag, AktivitetStatus status, BeregningsgrunnlagHjemmel hjemmel) {
        grunnlag.getBeregningsgrunnlag().getAktivitetStatuser().stream()
            .filter(as -> status.equals(as.getAktivitetStatus()))
            .forEach(as -> as.setHjemmel(hjemmel));
    }
}
