package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

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
public class VurderVarigEndretArbeidssituasjonHåndteringDto extends HåndterBeregningDto implements FastsettBruttoBeregningsgrunnlag {

    @JsonProperty("vurderVarigEndringEllerNyoppstartetDto")
    @Valid
    @NotNull
    private VurderVarigEndringEllerNyoppstartetDto vurderVarigEndringEllerNyoppstartetDto;

    public VurderVarigEndretArbeidssituasjonHåndteringDto() {
        super(AvklaringsbehovDefinisjon.VURDER_VARIG_ENDRT_ARB_SITSJN_MDL_INAKTV);
    }

    public VurderVarigEndretArbeidssituasjonHåndteringDto(@Valid @NotNull VurderVarigEndringEllerNyoppstartetDto vurderVarigEndringEllerNyoppstartetDto) {
        super(AvklaringsbehovDefinisjon.VURDER_VARIG_ENDRT_ARB_SITSJN_MDL_INAKTV);
        this.vurderVarigEndringEllerNyoppstartetDto = vurderVarigEndringEllerNyoppstartetDto;
    }

    public VurderVarigEndringEllerNyoppstartetDto getVurderVarigEndringEllerNyoppstartetDto() {
        return vurderVarigEndringEllerNyoppstartetDto;
    }

    @Override
    public Integer getBruttoBeregningsgrunnlag() {
        return vurderVarigEndringEllerNyoppstartetDto.getBruttoBeregningsgrunnlag();
    }
}
