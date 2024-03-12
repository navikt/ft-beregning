package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fp;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class BesteberegningMånedGrunnlagDto {

    @Valid
    @JsonProperty("inntekter")
    @Size()
    private final List<BesteberegningInntektDto> inntekter;

    @Valid
    @JsonProperty("fom")
    @NotNull
    private final LocalDate fom;

    @Valid
    @JsonProperty("tom")
    @NotNull
    private final LocalDate tom;

    public BesteberegningMånedGrunnlagDto(List<BesteberegningInntektDto> inntekter, LocalDate fom, LocalDate tom) {
        this.inntekter = inntekter;
        this.fom = fom;
        this.tom = tom;
    }

    public List<BesteberegningInntektDto> getInntekter() {
        return inntekter;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }
}
