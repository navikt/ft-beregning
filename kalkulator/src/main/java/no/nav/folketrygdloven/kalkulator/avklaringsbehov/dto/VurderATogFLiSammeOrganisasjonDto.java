package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.util.List;

public class VurderATogFLiSammeOrganisasjonDto {

    private List<VurderATogFLiSammeOrganisasjonAndelDto> vurderATogFLiSammeOrganisasjonAndelListe;

    public VurderATogFLiSammeOrganisasjonDto(List<VurderATogFLiSammeOrganisasjonAndelDto> vurderATogFLiSammeOrganisasjonAndelListe) {
        this.vurderATogFLiSammeOrganisasjonAndelListe = vurderATogFLiSammeOrganisasjonAndelListe;
    }

    public List<VurderATogFLiSammeOrganisasjonAndelDto> getVurderATogFLiSammeOrganisasjonAndelListe() {
        return vurderATogFLiSammeOrganisasjonAndelListe;
    }

    public void setVurderATogFLiSammeOrganisasjonAndelListe(List<VurderATogFLiSammeOrganisasjonAndelDto> vurderATogFLiSammeOrganisasjonAndelListe) {
        this.vurderATogFLiSammeOrganisasjonAndelListe = vurderATogFLiSammeOrganisasjonAndelListe;
    }
}
