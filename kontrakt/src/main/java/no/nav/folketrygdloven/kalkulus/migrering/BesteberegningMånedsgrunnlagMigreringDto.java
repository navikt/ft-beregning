package no.nav.folketrygdloven.kalkulus.migrering;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BesteberegningMånedsgrunnlagMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
    @Size(max=100)
    private List<BesteberegningInntektMigreringDto> inntekter;

    @Valid
    @NotNull
    private Periode periode;

    public BesteberegningMånedsgrunnlagMigreringDto() {
    }

    public BesteberegningMånedsgrunnlagMigreringDto(List<BesteberegningInntektMigreringDto> inntekter, Periode periode) {
        this.inntekter = inntekter;
        this.periode = periode;
    }

    public List<BesteberegningInntektMigreringDto> getInntekter() {
        return inntekter;
    }

    public Periode getPeriode() {
        return periode;
    }
}
