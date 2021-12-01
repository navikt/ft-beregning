package no.nav.folketrygdloven.beregningsgrunnlag.perioder.naturalytelse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.IdentifiserteNaturalytelsePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodeModellNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodeSplittDataNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodiseringNaturalytelseProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(IdentifiserPeriodeÅrsakerNaturalytelse.ID)
public class IdentifiserPeriodeÅrsakerNaturalytelse extends LeafSpecification<PeriodiseringNaturalytelseProsesstruktur> {

    static final String ID = FastsettPerioderNaturalytelseRegel.ID + ".1";
    static final String BESKRIVELSE = "Identifiserer dato og årsak for splitting";

    public IdentifiserPeriodeÅrsakerNaturalytelse() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(PeriodiseringNaturalytelseProsesstruktur prosseseringStruktur) {
        Map<String, Object> resultater = new HashMap<>();
	    IdentifiserteNaturalytelsePeriodeÅrsaker årsaker = identifiser(prosseseringStruktur.getInput(), resultater);
        prosseseringStruktur.setIdentifisertePeriodeÅrsaker(årsaker);
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;
    }

    static IdentifiserteNaturalytelsePeriodeÅrsaker identifiser(PeriodeModellNaturalytelse input, Map<String, Object> resultater) {
        LocalDate skjæringstidspunkt = input.getSkjæringstidspunkt();
        IdentifiserteNaturalytelsePeriodeÅrsaker map = new IdentifiserteNaturalytelsePeriodeÅrsaker();
        leggTilPeriodesplitterForEksisterendePerioder(input, map);
        resultater.put("eksisterendePerioder", map.getPeriodeMap());

        input.getNaturalytelserPrArbeidsforhold().forEach(inntektsmelding -> {
            resultater.put("arbeidsforhold", inntektsmelding.getArbeidsforhold());
            var naturalYtelsePerioder = IdentifiserPerioderForNaturalytelse.identifiserPerioderForNaturalytelse(inntektsmelding, skjæringstidspunkt);
            naturalYtelsePerioder.forEach(map::leggTilPeriodeÅrsak);
            resultater.put("naturalYtelsePerioder", naturalYtelsePerioder);
        });

        // må alltid ha en første periode, også når ingen gradering/refusjon/naturalytelse fra start
        if (!map.getPeriodeMap().containsKey(input.getSkjæringstidspunkt())) {
            var førstePeriode = PeriodeSplittDataNaturalytelse.builder()
                .medFom(input.getSkjæringstidspunkt())
                .medPeriodeÅrsak(PeriodeÅrsak.UDEFINERT)
                .build();
            map.leggTilPeriodeÅrsak(førstePeriode);
        }
        return map;
    }

    private static void leggTilPeriodesplitterForEksisterendePerioder(PeriodeModellNaturalytelse input, IdentifiserteNaturalytelsePeriodeÅrsaker map) {
        input.getEksisterendePerioder().forEach(eksisterendePeriode -> {
            if (!eksisterendePeriode.getPeriodeÅrsaker().isEmpty()) {
                eksisterendePeriode.getPeriodeÅrsaker().forEach(periodeÅrsak -> {
                    var periodeSplittData = PeriodeSplittDataNaturalytelse.builder()
                        .medFom(eksisterendePeriode.getPeriode().getFom())
                        .medPeriodeÅrsak(periodeÅrsak).build();
                    map.leggTilPeriodeÅrsak(periodeSplittData);
                });
            }
        });
    }
}
