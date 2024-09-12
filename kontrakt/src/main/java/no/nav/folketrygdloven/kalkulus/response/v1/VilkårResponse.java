package no.nav.folketrygdloven.kalkulus.response.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.kodeverk.Vilkårsavslagsårsak;

/**
 * Beskriver resultatet av vilkårsvurderingen samt sporing av både regelkjøring og input til denne vurderingen
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VilkårResponse {

    @JsonProperty(value = "erVilkarOppfylt")
    @Valid
    @NotNull
    private Boolean erVilkarOppfylt;

    @JsonProperty(value = "regelEvalueringSporing")
    @Valid
    @NotNull
    private String regelEvalueringSporing;

	@JsonProperty(value = "regelInputSporing")
    @Valid
    @NotNull
	private String regelInputSporing;

    @JsonProperty(value = "regelVersjon")
    @Valid
    @NotNull
    private String regelVersjon;

	@JsonProperty("avslagsÅrsak")
	@Valid
	private Vilkårsavslagsårsak avslagsÅrsak;

	public VilkårResponse() {
        // default ctor
    }

	public VilkårResponse(Boolean erVilkarOppfylt, String regelEvalueringSporing,
	                      String regelInputSporing, String regelVersjon,
	                      Vilkårsavslagsårsak avslagsÅrsak) {
		this.erVilkarOppfylt = erVilkarOppfylt;
		this.regelEvalueringSporing = regelEvalueringSporing;
		this.regelInputSporing = regelInputSporing;
		this.regelVersjon = regelVersjon;
		this.avslagsÅrsak = avslagsÅrsak;
	}

	public Boolean getErVilkarOppfylt() {
		return erVilkarOppfylt;
	}

	public String getRegelEvalueringSporing() {
		return regelEvalueringSporing;
	}

	public String getRegelInputSporing() {
		return regelInputSporing;
	}

	public String getRegelVersjon() {
		return regelVersjon;
	}

	public Vilkårsavslagsårsak getAvslagsÅrsak() {
		return avslagsÅrsak;
	}

	@AssertTrue(message = "Krever avslagsårsak når vilkåret ikke er oppfylt")
    public boolean isSjekkOmHarAvslagsårsak() {
        if (erVilkarOppfylt != null && !erVilkarOppfylt) {
            return avslagsÅrsak != null;
        }
        return true;
    }
}
