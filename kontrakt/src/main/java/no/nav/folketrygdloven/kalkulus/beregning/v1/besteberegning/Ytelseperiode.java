package no.nav.folketrygdloven.kalkulus.beregning.v1.besteberegning;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class Ytelseperiode {

    @JsonProperty(value = "periode")
    @Valid
    @NotNull
    private Periode periode;

    @JsonProperty(value = "andeler")
    @Valid
    @NotNull
    private List<Ytelseandel> andeler = new ArrayList<>();

    public Ytelseperiode(@Valid @NotNull Periode periode,
                         @Valid @NotNull List<Ytelseandel> andeler) {
        this.periode = periode;
        this.andeler = andeler;
    }

    public Periode getPeriode() {
        return periode;
    }

    public List<Ytelseandel> getAndeler() {
        return andeler;
    }
}
