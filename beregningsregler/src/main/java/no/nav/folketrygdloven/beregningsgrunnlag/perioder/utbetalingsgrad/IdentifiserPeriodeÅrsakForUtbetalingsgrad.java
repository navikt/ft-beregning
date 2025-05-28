package no.nav.folketrygdloven.beregningsgrunnlag.perioder.utbetalingsgrad;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.PeriodeModellUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.PeriodiseringUtbetalingsgradProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(IdentifiserPeriodeÅrsakForUtbetalingsgrad.ID)
public class IdentifiserPeriodeÅrsakForUtbetalingsgrad extends LeafSpecification<PeriodiseringUtbetalingsgradProsesstruktur> {

    static final String ID = FastsettPerioderForUtbetalingsgradRegel.ID + ".1";
    static final String BESKRIVELSE = "Identifiserer dato og årsak for splitting";

    public IdentifiserPeriodeÅrsakForUtbetalingsgrad() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(PeriodiseringUtbetalingsgradProsesstruktur prosseseringStruktur) {
        Map<String, Object> resultater = new HashMap<>();
        var årsaker = identifiser(prosseseringStruktur.getInput(), resultater);
        prosseseringStruktur.setIdentifisertePeriodeÅrsaker(årsaker);
        var resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;
    }

    static IdentifisertePeriodeÅrsaker identifiser(PeriodeModellUtbetalingsgrad input, Map<String, Object> resultater) {
        var map = new IdentifisertePeriodeÅrsaker();
        leggTilPeriodesplitterForEksisterendePerioder(input, map);
        resultater.put("eksisterendePerioder", map.getPeriodeMap());

        // Utbetalingsgrad
        input.getEndringerISøktYtelse().forEach(endringISøktYtelse -> {
            resultater.put("aktivitet", endringISøktYtelse.getArbeidsforhold());
            var endringerISøktYtelse = IdentifiserPerioderForEndringISøktYtelse.identifiser(endringISøktYtelse);
            endringerISøktYtelse.forEach(map::leggTilPeriodeÅrsak);
            resultater.put("endringerISøktYtelse", endringerISøktYtelse);
        });

        // må alltid ha en første periode, også når ingen gradering/refusjon/naturalytelse fra start
        if (!map.getPeriodeMap().containsKey(input.getSkjæringstidspunkt())) {
            var førstePeriode = PeriodeSplittData.builder()
                .medFom(input.getSkjæringstidspunkt())
                .medPeriodeÅrsak(PeriodeÅrsak.UDEFINERT)
                .build();
            map.leggTilPeriodeÅrsak(førstePeriode);
        }
        return map;
    }

    private static void leggTilPeriodesplitterForEksisterendePerioder(PeriodeModellUtbetalingsgrad input, IdentifisertePeriodeÅrsaker map) {
        input.getEksisterendePerioder().forEach(eksisterendePeriode -> {
            if (!eksisterendePeriode.getPeriodeÅrsaker().isEmpty()) {
                eksisterendePeriode.getPeriodeÅrsaker().forEach(periodeÅrsak -> {
                    var periodeSplittData = PeriodeSplittData.builder()
                        .medFom(eksisterendePeriode.getPeriode().getFom())
                        .medPeriodeÅrsak(periodeÅrsak).build();
                    map.leggTilPeriodeÅrsak(periodeSplittData);
                });
            }
        });
    }
}
