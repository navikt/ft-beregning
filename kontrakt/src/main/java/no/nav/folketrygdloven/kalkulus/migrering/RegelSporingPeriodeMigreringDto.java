package no.nav.folketrygdloven.kalkulus.migrering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;

public class RegelSporingPeriodeMigreringDto extends BaseMigreringDto {
    @Valid
    @NotNull
    private String regelEvaluering;
    @Valid
    @NotNull
    private String regelInput;
    @Valid
    @NotNull
    private Periode periode;
    @Valid
    @NotNull
    private BeregningsgrunnlagPeriodeRegelType regelType;
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String regelVersjon;

    public RegelSporingPeriodeMigreringDto() {
    }

    public RegelSporingPeriodeMigreringDto(String regelEvaluering,
                                           String regelInput,
                                           Periode periode,
                                           BeregningsgrunnlagPeriodeRegelType regelType,
                                           String regelVersjon) {
        this.regelEvaluering = regelEvaluering;
        this.regelInput = regelInput;
        this.periode = periode;
        this.regelType = regelType;
        this.regelVersjon = regelVersjon;
    }

    public String getRegelEvaluering() {
        return regelEvaluering;
    }

    public String getRegelInput() {
        return regelInput;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BeregningsgrunnlagPeriodeRegelType getRegelType() {
        return regelType;
    }

    public String getRegelVersjon() {
        return regelVersjon;
    }
}
