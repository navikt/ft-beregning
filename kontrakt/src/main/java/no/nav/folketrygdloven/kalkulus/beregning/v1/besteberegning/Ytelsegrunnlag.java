package no.nav.folketrygdloven.kalkulus.beregning.v1.besteberegning;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class Ytelsegrunnlag {

    @JsonProperty(value = "ytelse")
    @Valid
    @NotNull
    private YtelseType ytelse;

    @JsonProperty(value = "perioder")
    @NotNull
    private List<@Valid Ytelseperiode> perioder;

	protected Ytelsegrunnlag() {
		// default ctor
	}

	public Ytelsegrunnlag(@Valid @NotNull YtelseType ytelse,
	                      @NotNull List<@Valid Ytelseperiode> perioder) {
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
