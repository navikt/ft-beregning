package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagGrunnlagDto {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;

    @JsonProperty(value = "beregningsgrunnlag")
    @Valid
    private BeregningsgrunnlagDto beregningsgrunnlag;

    @JsonProperty(value = "erForlengelse")
    @Valid
    @NotNull
    private Boolean erForlengelse;


    public BeregningsgrunnlagGrunnlagDto() {
    }

    public BeregningsgrunnlagGrunnlagDto(UUID eksternReferanse,
                                         BeregningsgrunnlagDto beregningsgrunnlag,
                                         Boolean erForlengelse) {
        this.eksternReferanse = eksternReferanse;
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.erForlengelse = erForlengelse;
    }

    public UUID getEksternReferanse() {
        return eksternReferanse;
    }

    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public Boolean getErForlengelse() {
        return erForlengelse;
    }
}
