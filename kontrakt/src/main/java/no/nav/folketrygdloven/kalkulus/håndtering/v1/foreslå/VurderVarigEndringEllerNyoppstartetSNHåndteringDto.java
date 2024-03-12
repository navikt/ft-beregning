package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VurderVarigEndringEllerNyoppstartetSNHåndteringDto extends HåndterBeregningDto implements FastsettBruttoBeregningsgrunnlag {

    @JsonProperty("vurderVarigEndringEllerNyoppstartetSNDto")
    @Valid
    private VurderVarigEndringEllerNyoppstartetDto vurderVarigEndringEllerNyoppstartetSNDto;

    // TODO: begynn å sende vurderVarigEndringEllerNyoppstartetDto i frontend
    @JsonProperty("vurderVarigEndringEllerNyoppstartetDto")
    @Valid
    private VurderVarigEndringEllerNyoppstartetDto vurderVarigEndringEllerNyoppstartetDto;

    public VurderVarigEndringEllerNyoppstartetSNHåndteringDto() {
        super(AvklaringsbehovDefinisjon.VURDER_VARIG_ENDRT_NYOPPSTR_NAERNG_SN);
    }

    public VurderVarigEndringEllerNyoppstartetSNHåndteringDto(@Valid VurderVarigEndringEllerNyoppstartetDto vurderVarigEndringEllerNyoppstartetDto) {
        super(AvklaringsbehovDefinisjon.VURDER_VARIG_ENDRT_NYOPPSTR_NAERNG_SN);
        this.vurderVarigEndringEllerNyoppstartetSNDto = vurderVarigEndringEllerNyoppstartetDto;
        this.vurderVarigEndringEllerNyoppstartetDto = vurderVarigEndringEllerNyoppstartetDto;
    }

    public VurderVarigEndringEllerNyoppstartetDto getVurderVarigEndringEllerNyoppstartetSNDto() {
        return vurderVarigEndringEllerNyoppstartetSNDto;
    }

    @Override
    public Integer getBruttoBeregningsgrunnlag() {
        if (vurderVarigEndringEllerNyoppstartetDto != null) {
            return vurderVarigEndringEllerNyoppstartetDto.getBruttoBeregningsgrunnlag();
        }
        return vurderVarigEndringEllerNyoppstartetSNDto.getBruttoBeregningsgrunnlag();
    }

    @AssertTrue
    public boolean isSkalHaVurderVarigEndringEllerNyoppstartetSatt() {
        return vurderVarigEndringEllerNyoppstartetSNDto != null || vurderVarigEndringEllerNyoppstartetDto != null;
    }

}
