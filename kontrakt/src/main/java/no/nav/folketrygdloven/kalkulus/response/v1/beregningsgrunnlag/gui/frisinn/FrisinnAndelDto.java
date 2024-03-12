package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FrisinnAndelDto {

    @Valid
    @JsonProperty("oppgittInntekt")
    private Beløp oppgittInntekt;

    @Valid
    @JsonProperty("statusSøktFor")
    @NotNull
    private AktivitetStatus statusSøktFor;

    public FrisinnAndelDto() {
        // Jackson
    }

    public FrisinnAndelDto(@Valid Beløp oppgittInntekt,
                           @Valid @NotNull AktivitetStatus statusSøktFor) {
        this.oppgittInntekt = oppgittInntekt;
        this.statusSøktFor = statusSøktFor;
    }

    public Beløp getOppgittInntekt() {
        return oppgittInntekt;
    }

    public void setOppgittInntekt(Beløp oppgittInntekt) {
        this.oppgittInntekt = oppgittInntekt;
    }

    public AktivitetStatus getStatusSøktFor() {
        return statusSøktFor;
    }

    public void setStatusSøktFor(AktivitetStatus statusSøktFor) {
        this.statusSøktFor = statusSøktFor;
    }
}
