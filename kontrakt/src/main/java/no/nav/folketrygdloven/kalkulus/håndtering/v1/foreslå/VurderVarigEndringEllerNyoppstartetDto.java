package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VurderVarigEndringEllerNyoppstartetDto {

    @JsonProperty("erVarigEndretNaering")
    @Valid
    @NotNull
    private boolean erVarigEndretNaering;

    @JsonProperty("bruttoBeregningsgrunnlag")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer bruttoBeregningsgrunnlag;

    public VurderVarigEndringEllerNyoppstartetDto() {
        // For Json deserialisering
    }

    public VurderVarigEndringEllerNyoppstartetDto(@Valid @NotNull boolean erVarigEndretNaering, @Valid @Min(0) @Max(178956970) Integer bruttoBeregningsgrunnlag) {
        this.erVarigEndretNaering = erVarigEndretNaering;
        this.bruttoBeregningsgrunnlag = bruttoBeregningsgrunnlag;
    }

    public boolean getErVarigEndretNaering() {
        return erVarigEndretNaering;
    }

    public Integer getBruttoBeregningsgrunnlag() {
        return bruttoBeregningsgrunnlag;
    }
}
