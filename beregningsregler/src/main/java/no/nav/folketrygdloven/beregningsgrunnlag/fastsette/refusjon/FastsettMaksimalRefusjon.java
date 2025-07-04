package no.nav.folketrygdloven.beregningsgrunnlag.fastsette.refusjon;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettMaksimalRefusjon.ID)
public class FastsettMaksimalRefusjon extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_29_3";
    public static final String BESKRIVELSE = "Fastsett maksimal refusjon for hvert arbeidsforhold";

    public FastsettMaksimalRefusjon() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

        Map<String, Object> resultater = new HashMap<>();
        grunnlag.getBeregningsgrunnlagPrStatus().stream()
            .flatMap(bgs -> bgs.getArbeidsforhold().stream())
            .forEach(af -> {
                var refusjonskravPrArbeidsforholdPrÅr =  af.getAktivitetsgradertRefusjonskravPrÅr().orElse(BigDecimal.ZERO);

                if (af.getMaksimalRefusjonPrÅr() == null) {
                    var bgArbeidsforholdBuilder = BeregningsgrunnlagPrArbeidsforhold.builder(af);
                    var maksimalRefusjon = af.getAktivitetsgradertBruttoPrÅr().min(refusjonskravPrArbeidsforholdPrÅr);
                    bgArbeidsforholdBuilder.medMaksimalRefusjonPrÅr(maksimalRefusjon);
                    bgArbeidsforholdBuilder.build();
                }
                resultater.put("maksimalRefusjonPrÅr." + af.getArbeidsgiverId(), af.getMaksimalRefusjonPrÅr());
                resultater.put("refusjonskravPrÅr." + af.getArbeidsgiverId(), refusjonskravPrArbeidsforholdPrÅr);
            });

        var resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;
    }
}
