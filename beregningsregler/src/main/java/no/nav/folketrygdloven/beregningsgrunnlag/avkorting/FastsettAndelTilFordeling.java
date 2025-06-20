package no.nav.folketrygdloven.beregningsgrunnlag.avkorting;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

@RuleDocumentation(FastsettAndelTilFordeling.ID)
class FastsettAndelTilFordeling extends LeafSpecification<BeregningsgrunnlagPeriode> {
    static final String ID = "FP_BR 29.8.6";
    static final String BESKRIVELSE = "Fastsett andel til fordeling";

    FastsettAndelTilFordeling() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var resultat = ja();
        //TODO(OMR-61): Sporing

        var arbeidsforholdene = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforholdIkkeFrilans();

// Er det arbeidsforhold som ikke er fastsatt i tidligere runder?
        var ikkeFastsattAf = arbeidsforholdene.stream()
            .filter(af -> af.getMaksimalRefusjonPrÅr() != null)
            .filter(af -> af.getAvkortetRefusjonPrÅr() == null)
            .toList();
        if (ikkeFastsattAf.isEmpty()) {
            return resultat;
        }
        Map<String, Object> resultater = new HashMap<>();
        resultat.setEvaluationProperties(resultater);
// Beregn refusjonsbeløp som gjenstår å fastsette
        var sumFastsattAvkortetRefusjon = arbeidsforholdene.stream()
            .filter(af -> af.getAvkortetRefusjonPrÅr() != null)
            .map(BeregningsgrunnlagPrArbeidsforhold::getAvkortetRefusjonPrÅr)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
        var grenseverdi = grunnlag.getGrenseverdi();
        resultater.put("grenseverdi", grenseverdi);
        var ikkeFordelt = grenseverdi.subtract(sumFastsattAvkortetRefusjon);

// Forsøk å fastsette andelsmessig brukers andel for de arbeidsforholdene som ikke er fastsatt
        forsøkÅFastsetteBrukersAndeler(ikkeFastsattAf, resultater, sumFastsattAvkortetRefusjon, ikkeFordelt);

// Er noen brukerandeler negative - i så fall sett disse til 0 og fjern beregning for de andre slik at de beregnes i neste runde
        var negativBrukerAndel = ikkeFastsattAf.stream()
            .filter(af -> af.getAvkortetBrukersAndelPrÅr().compareTo(BigDecimal.ZERO) < 0)
            .findAny();

        if (negativBrukerAndel.isPresent()) {
            forberedNesteIterasjon(resultat, ikkeFastsattAf, sumFastsattAvkortetRefusjon, ikkeFordelt);
        }
        return resultat ;
    }

    private void forberedNesteIterasjon(SingleEvaluation resultat, List<BeregningsgrunnlagPrArbeidsforhold> ikkeFastsattAf,
            BigDecimal sumFastsattAvkortetRefusjon, BigDecimal ikkeFordelt) {
        Map<String, Object> korrigerteResultater = new HashMap<>();
        resultat.setEvaluationProperties(korrigerteResultater);
        korrigerteResultater.put("tidligereFastsattRefusjon", sumFastsattAvkortetRefusjon);
        korrigerteResultater.put("gjenstårÅFastsetteRefusjon", ikkeFordelt);
        ikkeFastsattAf.forEach(af -> {
            if (af.getAvkortetBrukersAndelPrÅr().compareTo(BigDecimal.ZERO) < 0) {
                BeregningsgrunnlagPrArbeidsforhold.builder(af)
                    .medAvkortetBrukersAndelPrÅr(BigDecimal.ZERO)
                    .build();
                korrigerteResultater.put("brukersAndel." + af.getArbeidsgiverId(), af.getAvkortetBrukersAndelPrÅr());
                korrigerteResultater.put("avkortetRefusjon." + af.getArbeidsgiverId(), af.getAvkortetRefusjonPrÅr());
            } else {
                BeregningsgrunnlagPrArbeidsforhold.builder(af)
                    .medAvkortetRefusjonPrÅr(null)
                    .medAvkortetBrukersAndelPrÅr(null)
                    .build();
            }
        });
    }

    private void forsøkÅFastsetteBrukersAndeler(List<BeregningsgrunnlagPrArbeidsforhold> ikkeFastsattAf, Map<String, Object> resultater,
            BigDecimal sumFastsattAvkortetRefusjon, BigDecimal ikkeFordelt) {
        var sumBruttoBG = ikkeFastsattAf.stream()
                .map(af -> af.getAktivitetsgradertBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        resultater.put("tidligereFastsattRefusjon", sumFastsattAvkortetRefusjon);
        resultater.put("gjenstårÅFastsetteRefusjon", ikkeFordelt);
        ikkeFastsattAf.forEach(af -> {
            var prosentandel = BigDecimal.valueOf(100)
                .multiply(af.getAktivitetsgradertBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO))
                .divide(sumBruttoBG, 10, RoundingMode.HALF_EVEN);
            resultater.put("gjenstårÅFastsetteRefusjon.prosentandel." + af.getArbeidsgiverId(), prosentandel);
            var andel = ikkeFordelt.multiply(af.getAktivitetsgradertBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO))
                .divide(sumBruttoBG, 10, RoundingMode.HALF_EVEN)
                .subtract(af.getMaksimalRefusjonPrÅr());
            BeregningsgrunnlagPrArbeidsforhold.builder(af)
                .medAvkortetRefusjonPrÅr(af.getMaksimalRefusjonPrÅr())
                .medAvkortetBrukersAndelPrÅr(andel)
                .build();
            resultater.put("brukersAndel." + af.getArbeidsgiverId(), af.getAvkortetBrukersAndelPrÅr());
            resultater.put("avkortetRefusjon." + af.getArbeidsgiverId(), af.getAvkortetRefusjonPrÅr());
        });
    }
}
