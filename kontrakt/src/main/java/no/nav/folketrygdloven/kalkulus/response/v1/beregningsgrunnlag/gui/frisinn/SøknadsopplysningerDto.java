package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class SøknadsopplysningerDto {

    @Valid
    @JsonProperty("oppgittÅrsinntekt")
    private Beløp oppgittÅrsinntekt;

    @Valid
    @JsonProperty("oppgittInntekt")
    private Beløp oppgittInntekt;

    @Valid
    @JsonProperty("erNyoppstartet")
    private boolean erNyoppstartet;

    public SøknadsopplysningerDto() {
        // Jackson
    }

    public Beløp getOppgittÅrsinntekt() {
        return oppgittÅrsinntekt;
    }

    public void setOppgittÅrsinntekt(Beløp oppgittÅrsinntekt) {
        this.oppgittÅrsinntekt = oppgittÅrsinntekt;
    }

    public Beløp getOppgittInntekt() {
        return oppgittInntekt;
    }

    public void setOppgittInntekt(Beløp oppgittInntekt) {
        this.oppgittInntekt = oppgittInntekt;
    }

    public boolean isErNyoppstartet() {
        return erNyoppstartet;
    }

    public void setErNyoppstartet(boolean erNyoppstartet) {
        this.erNyoppstartet = erNyoppstartet;
    }
}
