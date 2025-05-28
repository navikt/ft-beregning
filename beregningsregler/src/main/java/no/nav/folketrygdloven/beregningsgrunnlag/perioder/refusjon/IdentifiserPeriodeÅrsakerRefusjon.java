package no.nav.folketrygdloven.beregningsgrunnlag.perioder.refusjon;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Arbeidsgiver;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodeModellRefusjon;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodiseringRefusjonProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Utfall;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

@RuleDocumentation(IdentifiserPeriodeÅrsakerRefusjon.ID)
public class IdentifiserPeriodeÅrsakerRefusjon extends LeafSpecification<PeriodiseringRefusjonProsesstruktur> {

	public static final String ID = FastsettPerioderRefusjonRegel.ID + ".1";
	public static final String BESKRIVELSE = "Identifiserer dato og årsak for splitting";

    public IdentifiserPeriodeÅrsakerRefusjon() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(PeriodiseringRefusjonProsesstruktur prosseseringStruktur) {
        Map<String, Object> resultater = new HashMap<>();
        var årsaker = identifiser(prosseseringStruktur.getInput(), resultater);
        prosseseringStruktur.setIdentifisertePeriodeÅrsaker(årsaker);
        var resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;
    }

    static IdentifisertePeriodeÅrsaker identifiser(PeriodeModellRefusjon input, Map<String, Object> resultater) {
        var map = new IdentifisertePeriodeÅrsaker();
        leggTilPeriodesplitterForEksisterendePerioder(input, map);
        resultater.put("eksisterendePerioder", map.getPeriodeMap());
        input.getArbeidsforholdOgInntektsmeldinger().forEach(inntektsmelding -> {
            var arbeidsforhold = inntektsmelding.getArbeidsforhold();
	        resultater.put("refusjonForArbeidsforhold", arbeidsforhold);
            var fristvurdertTidslinje = input.getUtfalltidslinjePrArbeidsgiver().entrySet()
			        .stream()
			        .filter(e -> matcherArbeidsgiver(arbeidsforhold, e))
			        .findFirst()
			        .map(Map.Entry::getValue)
			        .orElse(new LocalDateTimeline<>(Collections.emptyList()));

            var refusjonPerioder = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(inntektsmelding, fristvurdertTidslinje, resultater);
            refusjonPerioder.forEach(map::leggTilPeriodeÅrsak);
        });

        var refusjonskravPrArbeidsgiver = GrupperPeriodeÅrsakerPerArbeidsgiver.grupper(map.getPeriodeMap());
        input.getArbeidsforholdOgInntektsmeldinger().forEach(inntektsmelding -> {
            var gyldigeRefusjonskrav = refusjonskravPrArbeidsgiver.getOrDefault(inntektsmelding, List.of());
            resultater.put("gyldigeRefusjonskrav", gyldigeRefusjonskrav);
            inntektsmelding.setGyldigeRefusjonskrav(gyldigeRefusjonskrav);
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

	private static boolean matcherArbeidsgiver(Arbeidsforhold arbeidsforhold, Map.Entry<Arbeidsgiver, LocalDateTimeline<Utfall>> e) {
		return arbeidsforhold.getReferanseType().equals(ReferanseType.ORG_NR) ?
						e.getKey().erOrganisasjon() && e.getKey().getIdentifikator().equals(arbeidsforhold.getArbeidsgiverId()) :
				e.getKey().getIdentifikator().equals(arbeidsforhold.getArbeidsgiverId());
	}

	private static void leggTilPeriodesplitterForEksisterendePerioder(PeriodeModellRefusjon input, IdentifisertePeriodeÅrsaker map) {
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
