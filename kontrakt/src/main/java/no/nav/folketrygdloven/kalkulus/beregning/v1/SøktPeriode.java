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
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class SøktPeriode {

    @JsonProperty(value = "periode", required = true)
    @NotNull
    @Valid
    private Periode periode;

    @JsonProperty(value = "harBrukerSøkt", required = true)
    @NotNull
    private boolean harBrukerSøkt;

    public SøktPeriode() {
    }


    public SøktPeriode(Periode periode, boolean harBrukerSøkt) {
        this.periode = periode;
        this.harBrukerSøkt = harBrukerSøkt;
    }

    public Periode getPeriode() {
        return periode;
    }


    public boolean getHarBrukerSøkt() {
        return harBrukerSøkt;
    }

    @Override
    public String toString() {
        return "SøktPeriode{" +
                "periode=" + periode +
                ", harBrukerSøkt=" + harBrukerSøkt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SøktPeriode that = (SøktPeriode) o;
        return harBrukerSøkt == that.harBrukerSøkt && periode.equals(that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, harBrukerSøkt);
    }
}

