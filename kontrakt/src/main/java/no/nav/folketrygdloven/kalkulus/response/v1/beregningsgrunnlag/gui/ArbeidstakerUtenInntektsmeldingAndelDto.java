package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

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
public class ArbeidstakerUtenInntektsmeldingAndelDto extends FaktaOmBeregningAndelDto {

    @Valid
    @JsonProperty(value = "mottarYtelse")
    private Boolean mottarYtelse;

    @Valid
    @JsonProperty(value = "inntektPrMnd")
    private Beløp inntektPrMnd;

    public Boolean getMottarYtelse() {
        return mottarYtelse;
    }

    public void setMottarYtelse(boolean mottarYtelse) {
        this.mottarYtelse = mottarYtelse;
    }

    public Beløp getInntektPrMnd() {
        return inntektPrMnd;
    }

    public void setInntektPrMnd(Beløp inntektPrMnd) {
        this.inntektPrMnd = inntektPrMnd;
    }
}
