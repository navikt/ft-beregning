package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class Refusjonsperiode {

    @JsonProperty(value = "periode", required = true)
    @Valid
    @NotNull
    private Periode periode;

    @JsonProperty(value = "beløp", required = true)
    @Valid
    @NotNull
    private Beløp beløp;


    public Refusjonsperiode() {
    }

    public Refusjonsperiode(Periode periode, Beløp beløp) {
        this.periode = periode;
        this.beløp = beløp;
    }

    public Periode getPeriode() {
        return periode;
    }

    public Beløp getBeløp() {
        return beløp;
    }

    @Override
    public String toString() {
        return "Refusjonsperiode{" +
                "periode=" + periode +
                ", beløp=" + beløp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Refusjonsperiode that = (Refusjonsperiode) o;
        return Objects.equals(periode, that.periode) && Objects.equals(beløp, that.beløp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, beløp);
    }
}
