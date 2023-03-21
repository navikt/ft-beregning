package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

public class PeriodiserBeregningsgrunnlag {

	private PeriodiserBeregningsgrunnlag() {}

	public static List<SplittetPeriode> periodiserBeregningsgrunnlag(FinnNyeAndelerTjeneste nyeAndelerTjeneste, IdentifisertePeriodeÅrsaker identifisertePeriodeÅrsaker, LocalDate skjæringstidspunkt) {
		// lag alle periodene, med riktige andeler
		Map<LocalDate, Set<PeriodeSplittData>> periodeMap = identifisertePeriodeÅrsaker.getPeriodeMap();
		List<Map.Entry<LocalDate, Set<PeriodeSplittData>>> entries = new ArrayList<>(periodeMap.entrySet());
		ListIterator<Map.Entry<LocalDate, Set<PeriodeSplittData>>> listIterator = entries.listIterator();
		List<SplittetPeriode> list = new ArrayList<>();
		while (listIterator.hasNext()) {
			Map.Entry<LocalDate, Set<PeriodeSplittData>> entry = listIterator.next();
			LocalDate periodeFom = entry.getKey();
			LocalDate periodeTom = utledPeriodeTom(entries, listIterator);
			Set<PeriodeSplittData> periodeSplittData = entry.getValue();
			List<SplittetAndel> nyeAndeler = nyeAndelerTjeneste.finnNyeAndeler(periodeFom, periodeTom);
			Periode periode = new Periode(periodeFom, periodeTom);
			SplittetPeriode splittetPeriode = SplittetPeriode.builder()
					.medPeriode(periode)
					.medPeriodeÅrsaker(getPeriodeÅrsaker(periodeSplittData, skjæringstidspunkt, periodeFom))
					.medNyeAndeler(nyeAndeler)
					.build();
			list.add(splittetPeriode);
		}
		return list;
	}

	private static LocalDate utledPeriodeTom(List<Map.Entry<LocalDate, Set<PeriodeSplittData>>> entries, ListIterator<Map.Entry<LocalDate, Set<PeriodeSplittData>>> listIterator) {
		return listIterator.hasNext() ?
				entries.get(listIterator.nextIndex()).getKey().minusDays(1) :
				null;
	}

	private static List<PeriodeÅrsak> getPeriodeÅrsaker(Set<PeriodeSplittData> periodeSplittData, LocalDate skjæringstidspunkt, LocalDate periodeFom) {
		return periodeSplittData.stream()
				.map(PeriodeSplittData::getPeriodeÅrsak)
				.filter(periodeÅrsak -> !PeriodeÅrsak.UDEFINERT.equals(periodeÅrsak))
				.filter(periodeÅrsak -> !(Set.of(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR).contains(periodeÅrsak)
						&& skjæringstidspunkt.equals(periodeFom)))
				.toList();
	}


	@FunctionalInterface
	public interface FinnNyeAndelerTjeneste {
		List<SplittetAndel> finnNyeAndeler(LocalDate periodeFom, LocalDate periodeTom);
	}

}
