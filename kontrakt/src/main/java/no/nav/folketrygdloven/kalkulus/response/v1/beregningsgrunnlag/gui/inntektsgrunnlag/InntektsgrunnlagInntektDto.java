package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektAktivitetType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class InntektsgrunnlagInntektDto {

    @Valid
    @NotNull
    @JsonProperty(value = "inntektAktivitetType")
    private InntektAktivitetType inntektAktivitetType;

    @Valid
    @JsonProperty("beløp")
    private Beløp beløp;

    public InntektsgrunnlagInntektDto() {
    }

    public InntektsgrunnlagInntektDto(@Valid @NotNull InntektAktivitetType inntektAktivitetType,
                                      @Valid Beløp beløp) {
        this.inntektAktivitetType = inntektAktivitetType;
        this.beløp = beløp;
    }

    public Beløp getBeløp() {
        return beløp;
    }

    public InntektAktivitetType getInntektAktivitetType() {
        return inntektAktivitetType;
    }
}
