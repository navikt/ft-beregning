package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public class KravperioderPrArbeidsforholdDto {

    private final Arbeidsgiver arbeidsgiver;

    @Deprecated(forRemoval = true) // Dette feltet skal fjernes da vi skal behandle alle inntektsmeldinger for samme arbeidsgiver likt
    private InternArbeidsforholdRefDto arbeidsforholdRef;

    private final List<PerioderForKravDto> perioder;

    private List<Intervall> sisteSøktePerioder;

    @Deprecated(forRemoval = true) // Fjern konstruktør med arbeidsforholdRef
    public KravperioderPrArbeidsforholdDto(Arbeidsgiver arbeidsgiver,
                                           InternArbeidsforholdRefDto arbeidsforholdRef, List<PerioderForKravDto> perioder,
                                           List<Intervall> sisteSøktePerioder) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.perioder = perioder;
        this.sisteSøktePerioder = sisteSøktePerioder;
    }

    public KravperioderPrArbeidsforholdDto(Arbeidsgiver arbeidsgiver,
                                           List<PerioderForKravDto> perioder,
                                           List<Intervall> sisteSøktePerioder) {
        this.arbeidsgiver = arbeidsgiver;
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
        var sisteSøkteSegmenter = sisteSøktePerioder.stream()
                .map(p -> LocalDateSegment.emptySegment(p.getFomDato(), p.getTomDato()))
                .toList();

        var søktePerioderTimeline = new LocalDateTimeline<>(sisteSøkteSegmenter, StandardCombinators::leftOnly);

        return perioder.stream()
                .map(p -> p.finnKravMedOverlappMedSisteSøkte(søktePerioderTimeline))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
