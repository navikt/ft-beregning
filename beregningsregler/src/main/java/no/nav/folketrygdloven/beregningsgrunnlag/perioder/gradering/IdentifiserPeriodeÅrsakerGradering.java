package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodiseringGraderingProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(IdentifiserPeriodeÅrsakerGradering.ID)
public class IdentifiserPeriodeÅrsakerGradering extends LeafSpecification<PeriodiseringGraderingProsesstruktur> {

    static final String ID = FastsettPerioderGraderingRegel.ID + ".1";
    static final String BESKRIVELSE = "Identifiserer dato og årsak for splitting";

    public IdentifiserPeriodeÅrsakerGradering() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(PeriodiseringGraderingProsesstruktur prosseseringStruktur) {
        Map<String, Object> resultater = new HashMap<>();
        var årsaker = identifiser(prosseseringStruktur.getInput(), resultater);
        prosseseringStruktur.setIdentifisertePeriodeÅrsaker(årsaker);
        var resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;
    }

    static IdentifisertePeriodeÅrsaker identifiser(PeriodeModellGradering input, Map<String, Object> resultater) {
        var map = new IdentifisertePeriodeÅrsaker();
        leggTilPeriodesplitterForEksisterendePerioder(input, map);
        resultater.put("eksisterendePerioder", map.getPeriodeMap());

        // Gradering FP
        input.getAndelGraderinger().forEach(andelGradering -> {
            resultater.put("graderingForAktivitetstatus", andelGradering.getAktivitetStatus());
            var graderingPerioder = IdentifiserPerioderForGradering.identifiser(input, andelGradering);
            graderingPerioder.forEach(map::leggTilPeriodeÅrsak);
            resultater.put("graderingPerioder", graderingPerioder);
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

    private static void leggTilPeriodesplitterForEksisterendePerioder(PeriodeModellGradering input, IdentifisertePeriodeÅrsaker map) {
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
