package no.nav.folketrygdloven.kalkulus.beregning.v1.besteberegning;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class Ytelsegrunnlag {

    @JsonProperty(value = "ytelse")
    @Valid
    @NotNull
    private YtelseType ytelse;

    @JsonProperty(value = "perioder")
    @Valid
    @NotNull
    private List<Ytelseperiode> perioder;

    public Ytelsegrunnlag(@Valid @NotNull YtelseType ytelse,
                          @Valid @NotNull List<Ytelseperiode> perioder) {
        this.ytelse = ytelse;
        this.perioder = perioder;
    }

    public YtelseType getYtelse() {
        return ytelse;
    }

    public List<Ytelseperiode> getPerioder() {
        return perioder;
    }
}
