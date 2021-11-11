package no.nav.folketrygdloven.beregningsgrunnlag.perioder.refusjon;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_BEGYNNELSE;
import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Utfall;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class IdentifiserPerioderForRefusjon {
    private IdentifiserPerioderForRefusjon() {
        // skjul public constructor
    }

    public static Set<PeriodeSplittData> identifiserPerioderForRefusjon(ArbeidsforholdOgInntektsmelding inntektsmelding,
                                                                        LocalDateTimeline<Utfall> fristvurdertTidslinje,
                                                                        Map<String, Object> resultater) {
        if (inntektsmelding.getRefusjoner() == null || inntektsmelding.getRefusjoner().isEmpty()) {
            return Collections.emptySet();
        }

	    List<LocalDateSegment<BigDecimal>> kravSegmenter = inntektsmelding.getRefusjoner().stream()
			    .map(r -> new LocalDateSegment<>(r.getPeriode().getFom(), r.getPeriode().getTom(), r.getMånedsbeløp()))
			    .collect(Collectors.toList());

	    LocalDateTimeline<BigDecimal> kravTidslinje = new LocalDateTimeline<>(kravSegmenter);

	    LocalDateTimeline<KravOgVilkårvurdering> kravOgVurderingTidslinje = fristvurdertTidslinje.intersection(kravTidslinje, IdentifiserPerioderForRefusjon::kombiner);

	    LocalDate førsteDato = kravOgVurderingTidslinje.stream()
			    .map(LocalDateSegment::getFom)
			    .min(Comparator.naturalOrder()).orElse(inntektsmelding.getStartdatoPermisjon());
	    Set<PeriodeSplittData> resultatSet = kravOgVurderingTidslinje.stream()
			    .filter(s -> !(s.getFom().equals(førsteDato) && s.getValue().krav().compareTo(BigDecimal.ZERO) == 0))
			    .map(segment -> PeriodeSplittData.builder()
					    .medPeriodeÅrsak(utledPeriodeÅrsak(segment.getValue()))
					    .medInntektsmelding(inntektsmelding)
					    .medFom(segment.getFom())
					    .medRefusjonskravPrMåned(segment.getValue().krav())
					    .medRefusjonsfristVilkårUtfall(segment.getValue().vilkårvurdering())
					    .build())
			    .collect(Collectors.toSet());


	    resultatSet.forEach(splittData -> {
	    	resultater.put("søktRefusjonFom", splittData.getFom());
		    resultater.put("refusjonskrav", splittData.getRefusjonskravPrMåned());
		    resultater.put("vurdertFristUtfall", splittData.getUtfall());
	    });

	    return resultatSet;

    }

	private static LocalDateSegment<KravOgVilkårvurdering> kombiner(LocalDateInterval interval, LocalDateSegment<Utfall> lhs, LocalDateSegment<BigDecimal> rhs) {
		if (lhs == null) {
			return new LocalDateSegment<>(interval, new KravOgVilkårvurdering(rhs.getValue(), Utfall.UNDERKJENT));
		} else {
			return new LocalDateSegment<>(interval, new KravOgVilkårvurdering(rhs.getValue(), lhs.getValue()));
		}
	}

	private static PeriodeÅrsak utledPeriodeÅrsak(KravOgVilkårvurdering kravOgVilkårvurdering) {
    	if (kravOgVilkårvurdering.vilkårvurdering() == Utfall.UNDERKJENT) {
    		return PeriodeÅrsak.REFUSJON_AVSLÅTT;
	    }
        if (kravOgVilkårvurdering.krav().compareTo(BigDecimal.ZERO) == 0) {
            return PeriodeÅrsak.REFUSJON_OPPHØRER;
        }
        return PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV;
    }

    private record KravOgVilkårvurdering(BigDecimal krav, Utfall vilkårvurdering) { }

}
