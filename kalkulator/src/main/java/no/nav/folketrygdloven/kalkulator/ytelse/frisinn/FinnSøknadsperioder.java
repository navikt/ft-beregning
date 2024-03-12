package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

/**
 * Finner søknadsperioder for FRISINN
 */
public class FinnSøknadsperioder {

    public static final YearMonth MARS = YearMonth.of(2020, 3);
    public static final YearMonth APRIL = YearMonth.of(2020, 4);

    private FinnSøknadsperioder() {
        // skjul
    }

    /**
     * Finner søknadsperioder for FRISINN
     * @param frisinnGrunnlag FrisinnGrunnlag
     * @return Liste med søknadsperioder
     */
    public static List<Intervall> finnSøknadsperioder(FrisinnGrunnlag frisinnGrunnlag) {
        Map<YearMonth, Intervall> månedTilPeriodeMap = frisinnGrunnlag.getFrisinnPerioder().stream()
                .collect(Collectors.toMap(p -> YearMonth.from(p.getPeriode().getTomDato()), FrisinnPeriode::getPeriode, finnStørsteIntervall()));

        // Må spesialbehandle dei periodene som slutter i mars fordi dei tilhører søknadsperiode for april
        if (månedTilPeriodeMap.containsKey(MARS)) {
            Intervall marsPeriode = månedTilPeriodeMap.get(MARS);
            månedTilPeriodeMap.remove(MARS);
            Intervall aprilPeriode = månedTilPeriodeMap.get(APRIL);
            månedTilPeriodeMap.put(APRIL, Intervall.fraOgMedTilOgMed(marsPeriode.getFomDato(), aprilPeriode.getTomDato()));
        }

        return månedTilPeriodeMap.values().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    }

    private static BinaryOperator<Intervall> finnStørsteIntervall() {
        return (p1, p2) -> p1.getFomDato().isBefore(p2.getFomDato()) ? Intervall.fraOgMedTilOgMed(p1.getFomDato(), p2.getTomDato()) :
                Intervall.fraOgMedTilOgMed(p2.getFomDato(), p1.getTomDato());
    }


    /**
     * Finner siste søknadsperiode for frisinn
     *
     * @param frisinnGrunnlag FrisinnGrunnlag
     * @return Siste søknadsperiode
     */
    public static Intervall finnSisteSøknadsperiode(FrisinnGrunnlag frisinnGrunnlag) {
        List<Intervall> finnSøknadsperioder = finnSøknadsperioder(frisinnGrunnlag);
        return finnSøknadsperioder.get(finnSøknadsperioder.size() - 1);
    }



}
