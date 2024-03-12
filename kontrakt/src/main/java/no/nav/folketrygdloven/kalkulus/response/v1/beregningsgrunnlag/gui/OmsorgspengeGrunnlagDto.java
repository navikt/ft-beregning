package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OmsorgspengeGrunnlagDto extends YtelsespesifiktGrunnlagDto {
    @JsonProperty("skalAvviksvurdere")
    @Valid
    private boolean skalAvviksvurdere = true;

    public OmsorgspengeGrunnlagDto() {
        super();
    }

    public boolean getSkalAvviksvurdere() {
        return skalAvviksvurdere;
    }

    public void setSkalAvviksvurdere(boolean skalAvviksvurdere) {
        this.skalAvviksvurdere = skalAvviksvurdere;
    }
}

