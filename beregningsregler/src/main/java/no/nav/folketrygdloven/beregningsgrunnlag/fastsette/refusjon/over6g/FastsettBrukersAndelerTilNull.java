package no.nav.folketrygdloven.beregningsgrunnlag.fastsette.refusjon.over6g;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettBrukersAndelerTilNull.ID)
class FastsettBrukersAndelerTilNull extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_29.13.4";
    static final String BESKRIVELSE = "For alle beregningsgrunnlagsandeler, sett andel til bruker = 0";

    FastsettBrukersAndelerTilNull() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
        var avkortetTilNull = BigDecimal.ZERO;
        grunnlag.getBeregningsgrunnlagPrStatus().stream()
            .flatMap(bgs -> bgs.getArbeidsforhold().stream())
            .forEach(af -> {
                BeregningsgrunnlagPrArbeidsforhold.builder(af).medAvkortetBrukersAndelPrÅr(avkortetTilNull).build();
                resultater.put("brukersAndel." + af.getArbeidsgiverId(), avkortetTilNull);
            });
        grunnlag.getBeregningsgrunnlagPrStatus().stream()
            .filter(bgps -> !bgps.erArbeidstakerEllerFrilanser())
            .forEach(bgps -> {
                BeregningsgrunnlagPrStatus.builder(bgps).medAvkortetPrÅr(avkortetTilNull).build();
                resultater.put("avkortetPrÅr.status." + bgps.getAktivitetStatus().name(), avkortetTilNull);
            });


        var resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;

    }
}
