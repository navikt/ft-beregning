package no.nav.folketrygdloven.kalkulus.håndtering.v1.overstyring;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsettBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OverstyrBeregningsgrunnlagHåndteringDto extends HåndterBeregningDto {

    @JsonProperty("fakta")
    @Valid
    private FaktaBeregningLagreDto fakta;

    @JsonProperty("overstyrteAndeler")
    @Valid
    @Size(min = 1)
    private List<FastsettBeregningsgrunnlagAndelDto> overstyrteAndeler;

    private OverstyrBeregningsgrunnlagHåndteringDto(boolean avbrutt) {
        super(AvklaringsbehovDefinisjon.OVST_INNTEKT, avbrutt);
    }

    public static OverstyrBeregningsgrunnlagHåndteringDto avbryt() {
        return new OverstyrBeregningsgrunnlagHåndteringDto(true);
    }

    @JsonCreator
    public OverstyrBeregningsgrunnlagHåndteringDto(@JsonProperty("fakta") @Valid FaktaBeregningLagreDto fakta,
                                                   @JsonProperty("overstyrteAndeler") @Valid @NotNull List<FastsettBeregningsgrunnlagAndelDto> overstyrteAndeler) {
        super(AvklaringsbehovDefinisjon.OVST_INNTEKT, false);
        this.fakta = fakta;
        this.overstyrteAndeler = overstyrteAndeler;
    }

    public FaktaBeregningLagreDto getFakta() {
        return fakta;
    }

    public List<FastsettBeregningsgrunnlagAndelDto> getOverstyrteAndeler() {
        return overstyrteAndeler;
    }

}
