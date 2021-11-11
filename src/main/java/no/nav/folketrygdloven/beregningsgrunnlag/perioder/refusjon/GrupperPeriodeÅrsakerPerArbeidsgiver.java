package no.nav.folketrygdloven.beregningsgrunnlag.perioder.refusjon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

public class GrupperPeriodeÅrsakerPerArbeidsgiver {
    private GrupperPeriodeÅrsakerPerArbeidsgiver() {
        // skjul public constructor
    }

    public static Map<ArbeidsforholdOgInntektsmelding, List<Refusjonskrav>> grupper(Map<LocalDate, Set<PeriodeSplittData>> periodeMap) {
        Map<ArbeidsforholdOgInntektsmelding, List<PeriodeSplittData>> map = periodeMap.values().stream()
            .flatMap(Collection::stream)
            .filter(splitt -> splitt.getInntektsmelding() != null)
            .collect(Collectors.groupingBy(PeriodeSplittData::getInntektsmelding));

        Map<ArbeidsforholdOgInntektsmelding, List<Refusjonskrav>> resultatMap = new HashMap<>();
        map.forEach((im, periodeSplittList) -> {
            ListIterator<PeriodeSplittData> arbeidsgiverListIterator = periodeSplittList.listIterator();
            while (arbeidsgiverListIterator.hasNext()) {
                PeriodeSplittData splittData = arbeidsgiverListIterator.next();
                BigDecimal refusjonskravPrÅr = splittData.getRefusjonskravPrMåned();
                if (refusjonskravPrÅr == null) {
                    continue;
                }
                LocalDate tom = arbeidsgiverListIterator.hasNext() ?
                    periodeSplittList.get(arbeidsgiverListIterator.nextIndex()).getFom().minusDays(1) :
                    DateUtil.TIDENES_ENDE;
                Refusjonskrav refusjonskrav = new Refusjonskrav(refusjonskravPrÅr, splittData.getFom(), tom, splittData.getUtfall());
                if (resultatMap.containsKey(im)) {
                    resultatMap.get(im).add(refusjonskrav);
                } else {
                    List<Refusjonskrav> refusjonskravListe = new ArrayList<>();
                    refusjonskravListe.add(refusjonskrav);
                    resultatMap.put(im, refusjonskravListe);
                }
            }
        });
        return resultatMap;
    }
}
