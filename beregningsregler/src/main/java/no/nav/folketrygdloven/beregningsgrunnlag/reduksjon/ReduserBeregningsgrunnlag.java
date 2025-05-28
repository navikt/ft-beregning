package no.nav.folketrygdloven.beregningsgrunnlag.reduksjon;

import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ReduserBeregningsgrunnlag.ID)
public class ReduserBeregningsgrunnlag extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR 6.2";
    public static final String BESKRIVELSE = "Reduser beregningsgrunnlag iht dekningsgrad";

    public ReduserBeregningsgrunnlag() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var dekningsgrad = grunnlag.getDekningsgrad().getVerdi();
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("dekningsgrad", grunnlag.getDekningsgrad());

        grunnlag.getBeregningsgrunnlagPrStatus().forEach(bps -> {
            if (bps.erArbeidstakerEllerFrilanser()) {
                bps.getArbeidsforhold().forEach(af -> {
                    var redusertAF = dekningsgrad.multiply(af.getAvkortetPrÅr());
                    BeregningsgrunnlagPrArbeidsforhold.builder(af)
                        .medRedusertPrÅr(dekningsgrad.multiply(af.getAvkortetPrÅr()))
                        .medRedusertRefusjonPrÅr(dekningsgrad.multiply(af.getAvkortetRefusjonPrÅr()), grunnlag.getYtelsedagerPrÅr())
                        .medRedusertBrukersAndelPrÅr(dekningsgrad.multiply(af.getAvkortetBrukersAndelPrÅr()), grunnlag.getYtelsedagerPrÅr())
                        .build();
                    resultater.put("redusertPrÅr.ATFL." + af.getArbeidsgiverId(), redusertAF);
                });
            } else {
                var redusertPS = dekningsgrad.multiply(bps.getAvkortetPrÅr());
                BeregningsgrunnlagPrStatus.builder(bps).medRedusertPrÅr(redusertPS).build();
                resultater.put("redusertPrÅr." + bps.getAktivitetStatus().name(), redusertPS);
            }
        });
        var resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;

    }
}
