package no.nav.folketrygdloven.kalkulator.modell.besteberegning;

import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

public record Ytelsegrunnlag(YtelseType ytelse, List<Ytelseperiode> perioder) {
    public Ytelsegrunnlag {
        Objects.requireNonNull(ytelse, "ytelse");
        Objects.requireNonNull(perioder, "ytelseperioder");
    }
}
