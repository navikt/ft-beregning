package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

public class VurderMilitærDto {

    private Boolean harMilitaer;

    public VurderMilitærDto(Boolean harMilitaer) {
        this.harMilitaer = harMilitaer;
    }


    public Boolean getHarMilitaer() {
        return harMilitaer;
    }

    public void setHarMilitaer(Boolean harMilitaer) {
        this.harMilitaer = harMilitaer;
    }
}
