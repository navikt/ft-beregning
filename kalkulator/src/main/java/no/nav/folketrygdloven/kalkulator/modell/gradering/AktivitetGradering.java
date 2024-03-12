package no.nav.folketrygdloven.kalkulator.modell.gradering;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AktivitetGradering {

    public static final AktivitetGradering INGEN_GRADERING = new AktivitetGradering();

    private Set<AndelGradering> andelGradering = new LinkedHashSet<>();

    private AktivitetGradering() {
        // tom gradering
    }

    public AktivitetGradering(Collection<AndelGradering> graderinger) {
       this.andelGradering.addAll(graderinger);
    }

    public AktivitetGradering(AndelGradering... andelGradering) {
        this(List.of(andelGradering));
    }

    public Set<AndelGradering> getAndelGradering() {
        return Set.copyOf(andelGradering);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"<" + andelGradering +">";
    }

}
