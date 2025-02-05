package no.nav.folketrygdloven.kalkulus.migrering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;

public class RegelSporingGrunnlagMigreringDto {
    @Valid
    @NotNull
    private String regelEvaluering;
    @Valid
    @NotNull
    private String regelInput;
    @Valid
    @NotNull
    private BeregningsgrunnlagRegelType regelType;
    @Valid
    private String regelVersjon;

    public RegelSporingGrunnlagMigreringDto() {
    }

    public RegelSporingGrunnlagMigreringDto(String regelEvaluering, String regelInput, BeregningsgrunnlagRegelType regelType, String regelVersjon) {
        this.regelEvaluering = regelEvaluering;
        this.regelInput = regelInput;
        this.regelType = regelType;
        this.regelVersjon = regelVersjon;
    }

    public String getRegelEvaluering() {
        return regelEvaluering;
    }

    public String getRegelInput() {
        return regelInput;
    }

    public BeregningsgrunnlagRegelType getRegelType() {
        return regelType;
    }

    public String getRegelVersjon() {
        return regelVersjon;
    }
}
