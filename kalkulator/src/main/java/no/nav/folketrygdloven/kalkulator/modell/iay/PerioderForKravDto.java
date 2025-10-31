package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public class PerioderForKravDto {

    private final LocalDate innsendingsdato;

    private final List<RefusjonsperiodeDto> perioder;

    public PerioderForKravDto(LocalDate innsendingsdato, List<RefusjonsperiodeDto> perioder) {
        this.innsendingsdato = innsendingsdato;
        this.perioder = perioder;
    }

    public LocalDate getInnsendingsdato() {
        return innsendingsdato;
    }

    public List<RefusjonsperiodeDto> getPerioder() {
        return perioder;
    }

    public Optional<PerioderForKravDto> finnKravMedOverlappMedSisteSøkte(LocalDateTimeline<Object> søktePerioderTimeline) {
        var periodeTimeline = finnKravperioderTidslinje();
        var overlapp = søktePerioderTimeline.intersection(periodeTimeline, StandardCombinators::rightOnly);
        if (overlapp.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(lagMedOverlapp(overlapp));

    }

    private LocalDateTimeline<Beløp> finnKravperioderTidslinje() {
        var periodeSegmenter = perioder.stream()
                .map(p -> new LocalDateSegment<>(p.periode().getFomDato(), p.periode().getTomDato(), p.beløp()))
                .toList();
        return new LocalDateTimeline<>(periodeSegmenter);
    }

    private PerioderForKravDto lagMedOverlapp(LocalDateTimeline<Beløp> overlapp) {
        return new PerioderForKravDto(innsendingsdato, finnPerioderFraTidslinje(overlapp));
    }

    private List<RefusjonsperiodeDto> finnPerioderFraTidslinje(LocalDateTimeline<Beløp> overlapp) {
        return overlapp.stream()
                .map(p -> new RefusjonsperiodeDto(Intervall.fraOgMedTilOgMed(p.getFom(), p.getTom()), p.getValue()))
                .toList();
    }

}
