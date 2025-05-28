package no.nav.folketrygdloven.kalkulus.iay.ytelse.v1;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class YtelseDto {


    @JsonProperty(value = "vedtaksDagsats")
    @Valid
    private Beløp vedtaksDagsats;

    @JsonProperty("ytelseAnvist")
    @Valid
    @Size
    private Set<YtelseAnvistDto> ytelseAnvist;

    @JsonProperty(value = "relatertYtelseType", required = true)
    @Valid
    @NotNull
    private YtelseType relatertYtelseType;

    @JsonProperty(value = "periode", required = true)
    @Valid
    @NotNull
    private Periode periode;

	@JsonProperty(value = "ytelseKilde")
	@Valid
	private YtelseKilde ytelseKilde;


    protected YtelseDto() {
        // default ctor
    }

	public YtelseDto(@Valid Beløp vedtaksDagsats,
	                 @Valid @Size Set<YtelseAnvistDto> ytelseAnvist,
	                 @Valid @NotNull YtelseType relatertYtelseType,
	                 @Valid @NotNull Periode periode,
	                 @Valid @NotNull YtelseKilde ytelseKilde) {
		this.vedtaksDagsats = vedtaksDagsats;
		this.ytelseAnvist = ytelseAnvist;
		this.relatertYtelseType = relatertYtelseType;
		this.periode = periode;
		this.ytelseKilde = ytelseKilde;
	}

    public Set<YtelseAnvistDto> getYtelseAnvist() {
        return ytelseAnvist;
    }

    public YtelseType getRelatertYtelseType() {
        return relatertYtelseType;
    }

    public Periode getPeriode() {
        return periode;
    }

    public Beløp getVedtaksDagsats() {
        return vedtaksDagsats;
    }

	public YtelseKilde getYtelseKilde() {
		return ytelseKilde;
	}
}
