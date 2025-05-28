package no.nav.folketrygdloven.beregningsgrunnlag.avkorting;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(VurderOmAndelerErFerdigFordeltOgOppdaterFordeltTilBruker.ID)
class VurderOmAndelerErFerdigFordeltOgOppdaterFordeltTilBruker extends LeafSpecification<BeregningsgrunnlagPeriode> {
    static final String ID = "FP_BR 29.8.9";
    static final String BESKRIVELSE = "For hvert arbeidsforhold der beregningsgrunnlagsandelen ikke er fordelt, "
            + "Vurder om andeler er ferdig fordelt og oppdater Fordelt til bruker";

    VurderOmAndelerErFerdigFordeltOgOppdaterFordeltTilBruker() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var resultat = ja();

        var arbeidsforholdene = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();

// Er det arbeidsforhold som ikke er fastsatt i tidligere runder?
        var ikkeFastsattAf = arbeidsforholdene.stream()
            .filter(af -> af.getMaksimalRefusjonPrÅr() != null)
            .filter(af -> af.getAvkortetRefusjonPrÅr() == null)
            .toList();
        if (!ikkeFastsattAf.isEmpty()) {
            return resultat;
        }

        Map<String, Object> resultater = new HashMap<>();
        resultat.setEvaluationProperties(resultater);
     // Alle arbeidsforhold er ferdig beregnet - fastsett avkortet BG
       arbeidsforholdene.forEach(af -> {
           var avkortetPrÅr = af.getAvkortetRefusjonPrÅr().add(af.getAvkortetBrukersAndelPrÅr());
           BeregningsgrunnlagPrArbeidsforhold.builder(af)
               .medAvkortetPrÅr(avkortetPrÅr)
               .build();
           resultater.put("avkortetPrÅr." + af.getArbeidsgiverId(), af.getAvkortetPrÅr());
       });
       return resultat;
    }



}
