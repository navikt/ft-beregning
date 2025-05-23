package no.nav.folketrygdloven.kalkulus.migrering;

import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;

public class RegelSporingGrunnlagMigreringDto extends BaseMigreringDto {
    @Valid
    private String regelEvaluering;
    @Valid
    @NotNull
    private String regelInput;
    @Valid
    @NotNull
    private BeregningsgrunnlagRegelType regelType;
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String regelVersjon;

    public RegelSporingGrunnlagMigreringDto() {
    }

    public RegelSporingGrunnlagMigreringDto(String regelEvaluering, String regelInput, BeregningsgrunnlagRegelType regelType, String regelVersjon) {
		if (!regelType.equals(BeregningsgrunnlagRegelType.PERIODISERING)) {
			this.regelEvaluering = Objects.requireNonNull(regelEvaluering);
		}
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
