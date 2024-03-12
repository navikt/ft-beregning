package no.nav.folketrygdloven.kalkulator.modell.avklaringsbehov;

import java.time.LocalDateTime;

import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;

public final class AvklaringsbehovDto {

    private final AvklaringsbehovDefinisjon definisjon;
    private final AvklaringsbehovStatus status;
    private final String begrunnelse;
    private final Boolean erTrukket;
    private final String vurdertAv;
    private final LocalDateTime vurdertTidspunkt;


    public AvklaringsbehovDto(AvklaringsbehovDefinisjon definisjon, AvklaringsbehovStatus status, String begrunnelse, Boolean erTrukket, String vurdertAv, LocalDateTime vurdertTidspunkt) {
        this.definisjon = definisjon;
        this.status = status;
        this.begrunnelse = begrunnelse;
        this.erTrukket = erTrukket;
        this.vurdertAv = vurdertAv;
        this.vurdertTidspunkt = vurdertTidspunkt;
    }

    public AvklaringsbehovDefinisjon getDefinisjon() {
        return definisjon;
    }

    public AvklaringsbehovStatus getStatus() {
        return status;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public Boolean getErTrukket() {
        return erTrukket != null && erTrukket;
    }

    public String getVurdertAv() {
        return vurdertAv;
    }

    public LocalDateTime getVurdertTidspunkt() {
        return vurdertTidspunkt;
    }
}
