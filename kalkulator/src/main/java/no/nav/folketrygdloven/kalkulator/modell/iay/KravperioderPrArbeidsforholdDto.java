package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class KravperioderPrArbeidsforholdDto {

    private final Arbeidsgiver arbeidsgiver;

    private final InternArbeidsforholdRefDto arbeidsforholdRef;

    private final List<PerioderForKravDto> perioder;

    private List<Intervall> sisteSøktePerioder;

    public KravperioderPrArbeidsforholdDto(Arbeidsgiver arbeidsgiver,
                                           InternArbeidsforholdRefDto arbeidsforholdRef, List<PerioderForKravDto> perioder,
                                           List<Intervall> sisteSøktePerioder) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.perioder = perioder;
        this.sisteSøktePerioder = sisteSøktePerioder;
    }


    /**
     * Virksomheten som har sendt inn inntektsmeldingen
     *
     * @return {@link Arbeidsgiver}
     */
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }


    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public List<PerioderForKravDto> getPerioder() {
        return perioder;
    }

    public List<Intervall> getSisteSøktePerioder() {
        return sisteSøktePerioder;
    }

    public List<PerioderForKravDto> finnOverlappMedSisteKrav() {
        List<LocalDateSegment<Boolean>> sisteSøkteSegmenter = sisteSøktePerioder.stream()
                .map(p -> new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), true))
                .collect(Collectors.toList());
        LocalDateTimeline<Boolean> søktePerioderTimeline = new LocalDateTimeline<>(sisteSøkteSegmenter);

        return perioder.stream()
                .map(p -> p.finnKravMedOverlappMedSisteSøkte(søktePerioderTimeline))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
