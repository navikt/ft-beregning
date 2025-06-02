package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class IdentifiserteNaturalytelsePeriodeÅrsaker {

    private SortedMap<LocalDate, Set<PeriodeSplittDataNaturalytelse>> periodeMap = new TreeMap<>();

    public IdentifiserteNaturalytelsePeriodeÅrsaker() {
        //tom constructor
    }

    public Map<LocalDate, Set<PeriodeSplittDataNaturalytelse>> getPeriodeMap() {
        return Collections.unmodifiableMap(periodeMap);
    }

    public void leggTilPeriodeÅrsak(PeriodeSplittDataNaturalytelse splittData) {
        var dato = splittData.getFom();
        if (periodeMap.containsKey(dato)) {
            var data = periodeMap.get(dato);
            data.add(splittData);
        } else {
            Set<PeriodeSplittDataNaturalytelse> set = new HashSet<>();
            set.add(splittData);
            periodeMap.put(dato, set);
        }
    }
}
