package no.nav.folketrygdloven.kalkulus.migrering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;

public class BeregningsgrunnlagAktivitetStatusMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
    private AktivitetStatus aktivitetStatus;

    @Valid
    @NotNull
    private Hjemmel hjemmel;

    public BeregningsgrunnlagAktivitetStatusMigreringDto() {
    }

    public BeregningsgrunnlagAktivitetStatusMigreringDto(AktivitetStatus aktivitetStatus, Hjemmel hjemmel) {
        this.aktivitetStatus = aktivitetStatus;
        this.hjemmel = hjemmel;
    }

    public @Valid @NotNull AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public @Valid @NotNull Hjemmel getHjemmel() {
        return hjemmel;
    }
}
