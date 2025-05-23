package no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFRISINN;
import no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn.FinnPerioderUtenYtelse;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettSkjæringstidspunktFørPeriodeMedYtelse.ID)
class FastsettSkjæringstidspunktFørPeriodeMedYtelse extends LeafSpecification<AktivitetStatusModellFRISINN> {

    static final String ID = "FP_BR 21.5";
    static final String BESKRIVELSE = "Skjæringstidspunkt for beregning settes til første dag etter siste aktivitetsdag";

    FastsettSkjæringstidspunktFørPeriodeMedYtelse() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModellFRISINN regelmodell) {
        Map<String, Object> resultater = new HashMap<>();
        var perioder = finnBeregningsperioder(regelmodell, resultater);
        resultater.put("beregningsperioder", "Perioder: " + perioder.stream().map(Periode::toString).reduce("", (p1, p2) -> p1 + ", " + p2));
        regelmodell.setBeregningsperioder(perioder);
        var sisteDatoIBeregningsperioden = perioder.stream().map(Periode::getTom)
            .max(Comparator.naturalOrder())
            .orElse(regelmodell.getSkjæringstidspunktForOpptjening().minusDays(1));
        regelmodell.setSkjæringstidspunktForBeregning(sisteDatoIBeregningsperioden.plusDays(1));
        resultater.put("skjæringstidspunktForBeregning", regelmodell.getSkjæringstidspunktForBeregning());
        return beregnet(resultater);
    }

    private List<Periode> finnBeregningsperioder(AktivitetStatusModellFRISINN regelmodell, Map<String, Object> resultater) {
        var inntektsgrunnlag = regelmodell.getInntektsgrunnlag();
        return FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, regelmodell.getSkjæringstidspunktForOpptjening(), resultater);
    }

}
