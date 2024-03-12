package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FaktaOmBeregningHåndteringDto extends HåndterBeregningDto {

    @JsonProperty("fakta")
    @Valid
    @NotNull
    private FaktaBeregningLagreDto fakta;

    public FaktaOmBeregningHåndteringDto() {
        super(AvklaringsbehovDefinisjon.VURDER_FAKTA_ATFL_SN);
    }

    public FaktaOmBeregningHåndteringDto(@Valid @NotNull FaktaBeregningLagreDto fakta) {
        super(AvklaringsbehovDefinisjon.VURDER_FAKTA_ATFL_SN);
        this.fakta = fakta;
    }


    public FaktaBeregningLagreDto getFakta() {
        return fakta;
    }


}
