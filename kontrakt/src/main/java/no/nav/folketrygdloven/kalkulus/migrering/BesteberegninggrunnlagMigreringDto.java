package no.nav.folketrygdloven.kalkulus.migrering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;

import java.util.HashSet;
import java.util.Set;


public class BesteberegninggrunnlagMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
    private Set<BesteberegningMånedsgrunnlagMigreringDto> seksBesteMåneder = new HashSet<>();

    @Valid
    private Beløp avvik;

    public BesteberegninggrunnlagMigreringDto() {
    }

    public BesteberegninggrunnlagMigreringDto(Set<BesteberegningMånedsgrunnlagMigreringDto> seksBesteMåneder, Beløp avvik) {
        this.seksBesteMåneder = seksBesteMåneder;
        this.avvik = avvik;
    }

    public @Valid @NotNull Set<BesteberegningMånedsgrunnlagMigreringDto> getSeksBesteMåneder() {
        return seksBesteMåneder;
    }

    public @Valid Beløp getAvvik() {
        return avvik;
    }
}
