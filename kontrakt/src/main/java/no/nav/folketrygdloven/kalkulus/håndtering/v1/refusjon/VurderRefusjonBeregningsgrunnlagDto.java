package no.nav.folketrygdloven.kalkulus.håndtering.v1.refusjon;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VurderRefusjonBeregningsgrunnlagDto extends HåndterBeregningDto {

    @JsonProperty("fastsatteAndeler")
    @Valid
    @Size(min = 1)
    private List<VurderRefusjonAndelBeregningsgrunnlagDto> fastsatteAndeler;

    public VurderRefusjonBeregningsgrunnlagDto() {
        // For Json deserialisering
    }

    public VurderRefusjonBeregningsgrunnlagDto(@Valid @Size(min = 1) List<VurderRefusjonAndelBeregningsgrunnlagDto> fastsatteAndeler) {
        super(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV);
        this.fastsatteAndeler = fastsatteAndeler;
    }

    public List<VurderRefusjonAndelBeregningsgrunnlagDto> getFastsatteAndeler() {
        return fastsatteAndeler;
    }
}
