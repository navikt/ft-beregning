package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonIgnore;

public enum InntektYtelseType implements Kodeverdi, KontraktKode {

    // Ytelse utbetalt til person som er arbeidstaker/frilanser/ytelsesmottaker
    AAP(Kategori.YTELSE),
    DAGPENGER(Kategori.YTELSE),
    FORELDREPENGER(Kategori.YTELSE),
    SVANGERSKAPSPENGER(Kategori.YTELSE),
    SYKEPENGER(Kategori.YTELSE),
    OMSORGSPENGER(Kategori.YTELSE),
    OPPLÆRINGSPENGER(Kategori.YTELSE),
    PLEIEPENGER(Kategori.YTELSE),
    OVERGANGSSTØNAD_ENSLIG(Kategori.YTELSE),
    VENTELØNN(Kategori.YTELSE),

    // Feriepenger Ytelse utbetalt til person som er arbeidstaker/frilanser/ytelsesmottaker
    // TODO slå sammen til FERIEPENGER_YTELSE - eller ta de med under hver ytelse???
    FERIEPENGER_FORELDREPENGER(Kategori.YTELSE),
    FERIEPENGER_SVANGERSKAPSPENGER(Kategori.YTELSE),
    FERIEPENGER_OMSORGSPENGER(Kategori.YTELSE),
    FERIEPENGER_OPPLÆRINGSPENGER(Kategori.YTELSE),
    FERIEPENGER_PLEIEPENGER(Kategori.YTELSE),
    FERIEPENGER_SYKEPENGER(Kategori.YTELSE),
    FERIETILLEGG_DAGPENGER(Kategori.YTELSE),

    // Annen ytelse utbetalt til person
    KVALIFISERINGSSTØNAD(Kategori.TRYGD),

    // Ytelse utbetalt til person som er næringsdrivende, fisker/lott, dagmamma eller jord/skogbruker
    FORELDREPENGER_NÆRING(Kategori.NÆRING),
    SVANGERSKAPSPENGER_NÆRING(Kategori.NÆRING),
    SYKEPENGER_NÆRING(Kategori.NÆRING),
    OMSORGSPENGER_NÆRING(Kategori.NÆRING),
    OPPLÆRINGSPENGER_NÆRING(Kategori.NÆRING),
    PLEIEPENGER_NÆRING(Kategori.NÆRING),
    DAGPENGER_NÆRING(Kategori.NÆRING),

    // Annen ytelse utbetalt til person som er næringsdrivende
    ANNET(Kategori.NÆRING),
    VEDERLAG(Kategori.NÆRING),
    LOTT_KUN_TRYGDEAVGIFT(Kategori.NÆRING),
    KOMPENSASJON_FOR_TAPT_PERSONINNTEKT(Kategori.NÆRING)
    ;

    @JsonIgnore
    private final Kategori kategori;

    InntektYtelseType(Kategori kategori) {
        this.kategori = kategori;
    }


    @Override
    public String getKode() {
        return name();
    }

    private boolean erOrdinærYtelse() {
        return kategori == Kategori.YTELSE;
    }

    private boolean erNæringsYtelse() {
        return kategori == Kategori.NÆRING;
    }

    public enum Kategori { YTELSE, NÆRING, TRYGD }
}
