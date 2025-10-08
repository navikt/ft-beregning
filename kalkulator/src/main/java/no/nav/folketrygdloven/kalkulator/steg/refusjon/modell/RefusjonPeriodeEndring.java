package no.nav.folketrygdloven.kalkulator.steg.refusjon.modell;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

public record RefusjonPeriodeEndring(List<RefusjonAndel> forrigeAndeler, List<RefusjonAndel> andeler) {
    public RefusjonPeriodeEndring {
        Objects.requireNonNull(forrigeAndeler, "forrigeAndeler");
        Objects.requireNonNull(andeler, "andeler");
    }

    public Map<RefusjonAndelNøkkel, List<RefusjonAndel>> getRevurderingAndelerMap() {
        return andeler.stream().collect(Collectors.groupingBy(RefusjonAndel::getNøkkel));
    }

    public Map<RefusjonAndelNøkkel, List<RefusjonAndel>> getForrigeAndelerMap() {
        return forrigeAndeler.stream().collect(Collectors.groupingBy(RefusjonAndel::getNøkkel));
    }

    public Beløp getBruttoForForrigeAndeler() {
        return getBrutto(forrigeAndeler);
    }

    public Beløp getBruttoForAndeler() {
        return getBrutto(andeler);
    }

    public Beløp getRefusjonForForrigeAndeler() {
        return getRefusjon(forrigeAndeler);
    }

    public Beløp getRefusjonForAndeler() {
        return getRefusjon(andeler);
    }

    private Beløp getRefusjon(List<RefusjonAndel> andeler) {
        return andeler.stream().map(RefusjonAndel::getRefusjon).filter(Objects::nonNull).reduce(Beløp::adder).orElse(Beløp.ZERO);
    }

    private Beløp getBrutto(List<RefusjonAndel> andeler) {
        return andeler.stream().map(RefusjonAndel::getBrutto).filter(Objects::nonNull).reduce(Beløp::adder).orElse(Beløp.ZERO);
    }

}
