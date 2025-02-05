package no.nav.folketrygdloven.kalkulus.migrering;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

public class BesteberegningMånedsgrunnlagMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
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
