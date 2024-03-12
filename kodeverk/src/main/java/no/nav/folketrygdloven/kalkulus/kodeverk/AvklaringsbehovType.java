package no.nav.folketrygdloven.kalkulus.kodeverk;

/**
 * Mulige statuser for avklaringsbehov.
 * OPPRETTET - Avklaringsbehovet er opprettet og ligger uløst på koblingen
 * UTFØRT - Avklaringsbehovet er opprettet og løst av saksbehandler
 * AVBRUTT - Avklaringsbehovet var før opprettet men er blitt avbrutt
 */
public enum AvklaringsbehovType implements Kodeverdi {

    AUTOPUNKT("Autopunkt"),
    MANUELL("Manuell"),
    OVERSTYRING("Overstyring")
    ;

    private final String navn;

    AvklaringsbehovType(String navn) {
        this.navn = navn;
    }

    @Override
    public String getKode() {
        return name();
    }

    public String getNavn() {
        return navn;
    }

}
