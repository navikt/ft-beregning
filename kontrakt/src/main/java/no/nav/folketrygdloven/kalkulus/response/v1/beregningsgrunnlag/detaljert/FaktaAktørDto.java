package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FaktaAktørDto {

    @JsonProperty(value = "erNyIArbeidslivetSN")
    @Valid
    private Boolean erNyIArbeidslivetSN;

    @JsonProperty(value = "erNyoppstartetFL")
    @Valid
    private Boolean erNyoppstartetFL;

    @JsonProperty(value = "harFLMottattYtelse")
    @Valid
    private Boolean harFLMottattYtelse;

    @JsonProperty(value = "skalBeregnesSomMilitær")
    @Valid
    private Boolean skalBeregnesSomMilitær;

    @JsonProperty(value = "skalBesteberegnes")
    @Valid
    private Boolean skalBesteberegnes;

    @JsonProperty(value = "mottarEtterlønnSluttpakke")
    @Valid
    private Boolean mottarEtterlønnSluttpakke;
    
    protected FaktaAktørDto() {
    }

    public FaktaAktørDto(@Valid Boolean erNyIArbeidslivetSN,
                         @Valid Boolean erNyoppstartetFL,
                         @Valid Boolean harFLMottattYtelse,
                         @Valid Boolean skalBeregnesSomMilitær,
                         @Valid Boolean skalBesteberegnes,
                         @Valid Boolean mottarEtterlønnSluttpakke) {
        this.erNyIArbeidslivetSN = erNyIArbeidslivetSN;
        this.erNyoppstartetFL = erNyoppstartetFL;
        this.harFLMottattYtelse = harFLMottattYtelse;
        this.skalBeregnesSomMilitær = skalBeregnesSomMilitær;
        this.skalBesteberegnes = skalBesteberegnes;
        this.mottarEtterlønnSluttpakke = mottarEtterlønnSluttpakke;
    }

    public Boolean getErNyIArbeidslivetSN() {
        return erNyIArbeidslivetSN;
    }

    public Boolean getErNyoppstartetFL() {
        return erNyoppstartetFL;
    }

    public Boolean getHarFLMottattYtelse() {
        return harFLMottattYtelse;
    }

    public Boolean getSkalBeregnesSomMilitær() {
        return skalBeregnesSomMilitær;
    }

    public Boolean getSkalBesteberegnes() {
        return skalBesteberegnes;
    }

    public Boolean getMottarEtterlønnSluttpakke() {
        return mottarEtterlønnSluttpakke;
    }
}
