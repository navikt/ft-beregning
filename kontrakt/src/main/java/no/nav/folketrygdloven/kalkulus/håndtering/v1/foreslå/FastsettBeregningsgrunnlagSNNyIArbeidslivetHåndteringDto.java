package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
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
public class FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto extends HåndterBeregningDto {

    @JsonProperty("fastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto")
    @Valid
    @NotNull
    private FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto fastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto;

    @JsonCreator
    public FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto(@JsonProperty("fastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto") @Valid @NotNull FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto fastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto) {
        super(AvklaringsbehovDefinisjon.FASTSETT_BG_SN_NY_I_ARB_LIVT);
        this.fastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto = fastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto;
    }

    public FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto getFastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto() {
        return fastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto;
    }
}
