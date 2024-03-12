package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsattePerioderTidsbegrensetDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FastsettBeregningsgrunnlagATFLHåndteringDto extends HåndterBeregningDto {

    @JsonProperty(value = "inntektPrAndelList")
    @Valid
    @Size(max = 100)
    private List<InntektPrAndelDto> inntektPrAndelList;

    @JsonProperty(value = "inntektFrilanser")
    @Valid
    @Min(0)
    @Max(100 * 1000 * 1000)
    private Integer inntektFrilanser;

    @JsonProperty(value = "fastsatteTidsbegrensedePerioder")
    @Valid
    @Size(max = 100)
    private List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder;


    protected FastsettBeregningsgrunnlagATFLHåndteringDto() {
        super(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);
    }
    public FastsettBeregningsgrunnlagATFLHåndteringDto(@Valid @Size(max = 100) List<InntektPrAndelDto> inntektPrAndelList,
                                                       @JsonProperty(value = "inntektFrilanser") @Valid @Min(0) @Max(100 * 1000 * 1000) Integer inntektFrilanser,
                                                       @JsonProperty(value = "fastsatteTidsbegrensedePerioder") @Valid @Size(max = 100) List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder) {
        super(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);
        this.inntektPrAndelList = inntektPrAndelList;
        this.inntektFrilanser = inntektFrilanser;
        this.fastsatteTidsbegrensedePerioder = fastsatteTidsbegrensedePerioder;
    }

    public Integer getInntektFrilanser() {
        return inntektFrilanser;
    }

    public List<InntektPrAndelDto> getInntektPrAndelList() {
        return inntektPrAndelList;
    }

    public List<FastsattePerioderTidsbegrensetDto> getFastsatteTidsbegrensedePerioder() {
        return fastsatteTidsbegrensedePerioder;
    }

    public boolean tidsbegrensetInntektErFastsatt() {
        return fastsatteTidsbegrensedePerioder != null && !fastsatteTidsbegrensedePerioder.isEmpty();
    }

}
