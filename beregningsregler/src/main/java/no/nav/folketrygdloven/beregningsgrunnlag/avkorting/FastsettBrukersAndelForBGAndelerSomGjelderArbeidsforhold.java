package no.nav.folketrygdloven.beregningsgrunnlag.avkorting;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettBrukersAndelForBGAndelerSomGjelderArbeidsforhold.ID)
class FastsettBrukersAndelForBGAndelerSomGjelderArbeidsforhold extends LeafSpecification<BeregningsgrunnlagPeriode> {
    static final String ID = "FP_BR 29.8.10";
    static final String BESKRIVELSE = "Fastsett brukers andel for alle beregningsgrunnlagsandeler som gjelder arbeidsforhold.";

    FastsettBrukersAndelForBGAndelerSomGjelderArbeidsforhold() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var resultat = ja();
        var atfl = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (atfl != null) {
            Map<String, Object> resultater = new HashMap<>();
            resultat.setEvaluationProperties(resultater);
            atfl.getArbeidsforholdIkkeFrilans().forEach(arbeidsforhold -> {
                var avkortetRefusjonPrÅr = arbeidsforhold.getMaksimalRefusjonPrÅr() == null ? BigDecimal.ZERO : arbeidsforhold.getMaksimalRefusjonPrÅr();
                var avkortetBrukersAndel = arbeidsforhold.getAktivitetsgradertBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO).subtract(avkortetRefusjonPrÅr);
                BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
                    .medAvkortetPrÅr(avkortetRefusjonPrÅr.add(avkortetBrukersAndel))
                    .medAvkortetRefusjonPrÅr(avkortetRefusjonPrÅr)
                    .medAvkortetBrukersAndelPrÅr(avkortetBrukersAndel)
                    .build();
                resultater.put("avkortetBrukersAndelPrÅr" + "." +  arbeidsforhold.getArbeidsgiverId(), avkortetBrukersAndel);
            });
        }
        return resultat;
    }



}
