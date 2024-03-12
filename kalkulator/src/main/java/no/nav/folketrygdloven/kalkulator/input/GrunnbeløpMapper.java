package no.nav.folketrygdloven.kalkulator.input;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;

public final class GrunnbeløpMapper {
    private GrunnbeløpMapper() {
        // Hindrer instansiering av klasse
    }

    public static List<Grunnbeløp> mapGrunnbeløpInput(List<GrunnbeløpInput> grunnbeløpInput) {
        return grunnbeløpInput.stream().map(g -> new Grunnbeløp(g.fom(), g.tom(), g.gVerdi(), g.gSnitt())).collect(Collectors.toList());
    }
}
