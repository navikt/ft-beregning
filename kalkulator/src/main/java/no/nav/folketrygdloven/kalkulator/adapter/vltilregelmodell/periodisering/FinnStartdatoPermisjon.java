package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;

public class FinnStartdatoPermisjon {
    private FinnStartdatoPermisjon() {
        // skjul public constructor
    }

    /**
     * @param inntektsmelding inntektsmelding
     * @param skjæringstidspunktBeregning første ønskede dag med uttak av foreldrepenger
     * @param startdato       første dag i aktiviteten. Kan være før første uttaksdag,
     *                        eller etter første uttaksdag dersom bruker starter i arbeidsforholdet
     *                        eller er i permisjon (f.eks. PERMITTERT) ved første uttaksdag.
     */
    public static LocalDate finnStartdatoPermisjon(Optional<InntektsmeldingDto> inntektsmelding,
                                                   LocalDate skjæringstidspunktBeregning,
                                                   LocalDate startdato) {
        return startdato.isBefore(skjæringstidspunktBeregning) ? skjæringstidspunktBeregning : utledStartdato(inntektsmelding, startdato);
    }


    private static LocalDate utledStartdato(Optional<InntektsmeldingDto> inntektsmelding, LocalDate startdato) {
        Optional<LocalDate> startDatoFraIM = inntektsmelding.flatMap(InntektsmeldingDto::getStartDatoPermisjon);
        return startDatoFraIM.filter(dato -> dato.isAfter(startdato)).orElse(startdato);
    }
}
