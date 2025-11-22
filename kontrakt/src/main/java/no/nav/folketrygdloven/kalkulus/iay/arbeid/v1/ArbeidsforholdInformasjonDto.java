package no.nav.folketrygdloven.kalkulus.iay.arbeid.v1;

import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ArbeidsforholdInformasjonDto {

    @JsonProperty(value = "overstyringer")
    @Size(max = 100)
    private List<@Valid ArbeidsforholdOverstyringDto> overstyringer;

	@JsonProperty(value = "referanser")
    @Size(max = 100)
	private Set<@Valid ArbeidsforholdReferanseDto> referanser;

    public ArbeidsforholdInformasjonDto() {
        // default ctor
    }

    public ArbeidsforholdInformasjonDto(@NotEmpty List<@Valid ArbeidsforholdOverstyringDto> overstyringer) {
        this.overstyringer = overstyringer;
    }

	public ArbeidsforholdInformasjonDto(@NotEmpty List<@Valid ArbeidsforholdOverstyringDto> overstyringer,
	                                    Set<@Valid ArbeidsforholdReferanseDto> referanser) {
		this.overstyringer = overstyringer;
        this.referanser = referanser;
    }

    public List<ArbeidsforholdOverstyringDto> getOverstyringer() {
        return overstyringer;
    }

	public Set<ArbeidsforholdReferanseDto> getReferanser() {
		return referanser;
	}
}
