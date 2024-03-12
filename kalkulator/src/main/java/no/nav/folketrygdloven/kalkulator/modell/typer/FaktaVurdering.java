package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.io.Serializable;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;

public class FaktaVurdering implements Serializable {


    private Boolean vurdering;
    private FaktaVurderingKilde kilde;

    public FaktaVurdering(Boolean vurdering, FaktaVurderingKilde kilde) {
        this.vurdering = vurdering;
        this.kilde = kilde;
    }


    public Boolean getVurdering() {
        return vurdering;
    }

    public FaktaVurderingKilde getKilde() {
        return kilde;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FaktaVurdering that = (FaktaVurdering) o;
        return vurdering.equals(that.vurdering) && kilde == that.kilde;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vurdering, kilde);
    }

    @Override
    public String toString() {
        return "FaktaVurdering{" +
                "vurdering=" + vurdering +
                ", kilde=" + kilde +
                '}';
    }
}
