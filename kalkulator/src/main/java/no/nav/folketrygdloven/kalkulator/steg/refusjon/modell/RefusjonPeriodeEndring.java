package no.nav.folketrygdloven.kalkulator.steg.refusjon.modell;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

public class RefusjonPeriodeEndring {
    private List<RefusjonAndel> originaleAndeler;
    private List<RefusjonAndel> revurderingAndeler;

    public RefusjonPeriodeEndring(List<RefusjonAndel> originaleAndeler, List<RefusjonAndel> revurderingAndeler) {
        Objects.requireNonNull(originaleAndeler, "originaleAndeler");
        Objects.requireNonNull(revurderingAndeler, "revurderingAndeler");
        this.originaleAndeler = originaleAndeler;
        this.revurderingAndeler = revurderingAndeler;
    }

    public List<RefusjonAndel> getOriginaleAndeler() {
        return originaleAndeler;
    }

    public List<RefusjonAndel> getRevurderingAndeler() {
        return revurderingAndeler;
    }

    public Map<RefusjonAndelNøkkel, List<RefusjonAndel>> getRevurderingAndelerMap() {
        return revurderingAndeler.stream().collect(Collectors.groupingBy(RefusjonAndel::getNøkkel));
    }

    public Map<RefusjonAndelNøkkel, List<RefusjonAndel>> getOriginaleAndelerMap() {
        return originaleAndeler.stream().collect(Collectors.groupingBy(RefusjonAndel::getNøkkel));
    }

    public Beløp getOriginalBrutto() {
        return getBrutto(originaleAndeler);
    }

    public Beløp getRevurderingBrutto() {
        return getBrutto(revurderingAndeler);
    }

    public Beløp getOriginalRefusjon() {
        return getRefusjon(originaleAndeler);
    }

    public Beløp getRevurderingRefusjon() {
        return getRefusjon(revurderingAndeler);
    }

    private Beløp getRefusjon(List<RefusjonAndel> andeler) {
        return andeler.stream()
                .map(RefusjonAndel::getRefusjon)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

    private Beløp getBrutto(List<RefusjonAndel> andeler) {
        return andeler.stream()
                .map(RefusjonAndel::getBrutto)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefusjonPeriodeEndring that = (RefusjonPeriodeEndring) o;
        return Objects.equals(originaleAndeler, that.originaleAndeler) && Objects.equals(revurderingAndeler, that.revurderingAndeler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originaleAndeler, revurderingAndeler);
    }
}
