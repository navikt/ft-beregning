package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FastsettBeregningsgrunnlagATFLHåndteringDto extends HåndterBeregningDto {

    @JsonProperty(value = "inntektPrAndelList")
    @Size(max = 100)
    private List<@Valid InntektPrAndelDto> inntektPrAndelList;

    @JsonProperty(value = "inntektFrilanser")
    @Valid
    @Min(0)
    @Max(100 * 1000 * 1000)
    private Integer inntektFrilanser;

    protected FastsettBeregningsgrunnlagATFLHåndteringDto() {
        super(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);
    }

	public FastsettBeregningsgrunnlagATFLHåndteringDto(@Size(max = 100) List<@Valid InntektPrAndelDto> inntektPrAndelList,
                                                       @Valid @Min(0) @Max(100 * 1000 * 1000) Integer inntektFrilanser) {
        super(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);
        this.inntektPrAndelList = inntektPrAndelList;
        this.inntektFrilanser = inntektFrilanser;
    }

    public Integer getInntektFrilanser() {
        return inntektFrilanser;
    }

    public List<InntektPrAndelDto> getInntektPrAndelList() {
        return inntektPrAndelList;
    }
}
