package no.nav.folketrygdloven.kalkulus.migrering;

import java.time.LocalDateTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


public class BaseMigreringDto  {
    @Valid
    @NotNull
    private String opprettetAv;
    @Valid
    @NotNull
    private LocalDateTime opprettetTidspunkt;
    private String endretAv;
    private LocalDateTime endretTidspunkt;

    public void setOpprettetAv(String opprettetAv) {
        this.opprettetAv = opprettetAv;
    }

    public void setOpprettetTidspunkt(LocalDateTime opprettetTidspunkt) {
        this.opprettetTidspunkt = opprettetTidspunkt;
    }

    public void setEndretAv(String endretAv) {
        this.endretAv = endretAv;
    }

    public void setEndretTidspunkt(LocalDateTime endretTidspunkt) {
        this.endretTidspunkt = endretTidspunkt;
    }

    public String getOpprettetAv() {
        return opprettetAv;
    }

    public LocalDateTime getOpprettetTidspunkt() {
        return opprettetTidspunkt;
    }

    public String getEndretAv() {
        return endretAv;
    }

    public LocalDateTime getEndretTidspunkt() {
        return endretTidspunkt;
    }
}
