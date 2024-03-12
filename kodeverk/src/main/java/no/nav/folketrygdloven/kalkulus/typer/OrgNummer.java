package no.nav.folketrygdloven.kalkulus.typer;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Id som genereres fra NAV Aktør Register. Denne iden benyttes til interne forhold i Nav og vil ikke endres f.eks. dersom bruker går fra
 * DNR til FNR i Folkeregisteret. Tilsvarende vil den kunne referere personer som har ident fra et utenlandsk system.
 *
 * Støtter også kunstige orgnummer (internt definert konstant i fp - orgnummer=342352362)
 */
public class OrgNummer implements Serializable, Comparable<OrgNummer> {

    /**
     * Orgnr for KUNSTIG organisasjoner. Går sammen med OrganisasjonType#KUNSTIG.
     * (p.t. kun en kunstig organisasjon som holder på arbeidsforhold lagt til av saksbehandler.)
     */
    public static final String KUNSTIG_ORG = "342352362"; // magic constant

    @JsonValue
    private String orgNummer; // NOSONAR

    public OrgNummer(String orgNummer) {
        Objects.requireNonNull(orgNummer, "orgNummer");
        if (!erGyldigOrgnr(orgNummer)) {
            // skal ikke skje, funksjonelle feilmeldinger håndteres ikke her.
            throw new IllegalArgumentException("Ikke gyldig orgnummer: " + orgNummer);
        }
        this.orgNummer = orgNummer;
    }

    protected OrgNummer() {
        // for jpa
    }

    public static boolean erKunstig(String orgNr) {
        return KUNSTIG_ORG.equals(orgNr);
    }

    @Override
    public int compareTo(OrgNummer o) {
        // TODO: Burde ikke finnes
        return orgNummer.compareTo(o.orgNummer);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OrgNummer)) {
            return false;
        }
        OrgNummer other = (OrgNummer) obj;
        return Objects.equals(orgNummer, other.orgNummer);
    }

    public String getId() {
        return orgNummer;
    }

    public String getOrgNummer() {
        return orgNummer;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(orgNummer);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<********>";
    }

    /** @return false hvis ikke gyldig orgnr. */
    public static boolean erGyldigOrgnr(String ident) {
        return erKunstig(ident) || OrganisasjonsNummerValidator.erGyldig(ident);
    }
}
