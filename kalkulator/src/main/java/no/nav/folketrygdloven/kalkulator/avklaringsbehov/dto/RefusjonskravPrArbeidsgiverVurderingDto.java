package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

public class RefusjonskravPrArbeidsgiverVurderingDto {

    private String arbeidsgiverId;

    private boolean skalUtvideGyldighet;

    public RefusjonskravPrArbeidsgiverVurderingDto(String arbeidsgiverId, boolean skalUtvideGyldighet) {
        this.arbeidsgiverId = arbeidsgiverId;
        this.skalUtvideGyldighet = skalUtvideGyldighet;
    }

    public String getArbeidsgiverId() {
        return arbeidsgiverId;
    }

    public void setArbeidsgiverId(String arbeidsgiverId) {
        this.arbeidsgiverId = arbeidsgiverId;
    }

    public boolean isSkalUtvideGyldighet() {
        return skalUtvideGyldighet;
    }

    public void setSkalUtvideGyldighet(boolean skalUtvideGyldighet) {
        this.skalUtvideGyldighet = skalUtvideGyldighet;
    }
}
