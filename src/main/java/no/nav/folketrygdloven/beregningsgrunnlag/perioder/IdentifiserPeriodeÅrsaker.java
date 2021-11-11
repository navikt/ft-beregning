package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeSplittProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(IdentifiserPeriodeÅrsaker.ID)
public class IdentifiserPeriodeÅrsaker extends LeafSpecification<PeriodeSplittProsesstruktur> {

    static final String ID = FastsettPeriodeRegel.ID + ".1";
    static final String BESKRIVELSE = "Identifiserer dato og årsak for splitting";

    public IdentifiserPeriodeÅrsaker() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(PeriodeSplittProsesstruktur prosseseringStruktur) {
        Map<String, Object> resultater = new HashMap<>();
        IdentifisertePeriodeÅrsaker årsaker = identifiser(prosseseringStruktur.getInput(), resultater);
        prosseseringStruktur.setIdentifisertePeriodeÅrsaker(årsaker);
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;
    }

    static IdentifisertePeriodeÅrsaker identifiser(PeriodeModell input, Map<String, Object> resultater) {
        LocalDate skjæringstidspunkt = input.getSkjæringstidspunkt();
        IdentifisertePeriodeÅrsaker map = new IdentifisertePeriodeÅrsaker();
        leggTilPeriodesplitterForEksisterendePerioder(input, map);
        resultater.put("eksisterendePerioder", map.getPeriodeMap());

        // Naturalytelse TODO: Flytt til egen regel og egen modell
        input.getArbeidsforholdOgInntektsmeldinger().forEach(inntektsmelding -> {
            resultater.put("arbeidsforhold", inntektsmelding.getArbeidsforhold());
            Set<PeriodeSplittData> naturalYtelsePerioder = IdentifiserPerioderForNaturalytelse.identifiserPerioderForNaturalytelse(inntektsmelding, skjæringstidspunkt);
            naturalYtelsePerioder.forEach(map::leggTilPeriodeÅrsak);
            resultater.put("naturalYtelsePerioder", naturalYtelsePerioder);
        });

        // Gradering FP
        input.getAndelGraderinger().forEach(andelGradering -> {
            resultater.put("graderingForAktivitetstatus", andelGradering.getAktivitetStatus());
            Set<PeriodeSplittData> graderingPerioder = IdentifiserPerioderForGradering.identifiser(input, andelGradering);
            graderingPerioder.forEach(map::leggTilPeriodeÅrsak);
            resultater.put("graderingPerioder", graderingPerioder);
        });

        // Utbetalingsgrad
        input.getEndringerISøktYtelse().forEach(endringISøktYtelse -> {
            resultater.put("aktivitet", endringISøktYtelse.getArbeidsforhold());
            Set<PeriodeSplittData> endringerISøktYtelse = IdentifiserPerioderForEndringISøktYtelseSvangerskapspenger.identifiser(endringISøktYtelse);
            endringerISøktYtelse.forEach(map::leggTilPeriodeÅrsak);
            resultater.put("endringerISøktYtelse", endringerISøktYtelse);
        });


        // må alltid ha en første periode, også når ingen gradering/refusjon/naturalytelse fra start
        if (!map.getPeriodeMap().containsKey(input.getSkjæringstidspunkt())) {
            PeriodeSplittData førstePeriode = PeriodeSplittData.builder()
                .medFom(input.getSkjæringstidspunkt())
                .medPeriodeÅrsak(PeriodeÅrsak.UDEFINERT)
                .build();
            map.leggTilPeriodeÅrsak(førstePeriode);
        }
        return map;
    }

    private static void leggTilPeriodesplitterForEksisterendePerioder(PeriodeModell input, IdentifisertePeriodeÅrsaker map) {
        input.getEksisterendePerioder().forEach(eksisterendePeriode -> {
            if (!eksisterendePeriode.getPeriodeÅrsaker().isEmpty()) {
                eksisterendePeriode.getPeriodeÅrsaker().forEach(periodeÅrsak -> {
                    PeriodeSplittData periodeSplittData = PeriodeSplittData.builder()
                        .medFom(eksisterendePeriode.getPeriode().getFom())
                        .medPeriodeÅrsak(periodeÅrsak).build();
                    map.leggTilPeriodeÅrsak(periodeSplittData);
                });
            }
        });
    }
}
