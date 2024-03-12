package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum BeregningsgrunnlagTilstand implements Kodeverdi, DatabaseKode, KontraktKode {

    OPPRETTET,
    FASTSATT_BEREGNINGSAKTIVITETER,
    OPPDATERT_MED_ANDELER,
    KOFAKBER_UT,
    BESTEBEREGNET,
    FORESLÅTT,
    FORESLÅTT_UT,
    FORESLÅTT_DEL_2,
    FORESLÅTT_DEL_2_UT,
    VURDERT_VILKÅR,
    VURDERT_TILKOMMET_INNTEKT,
    VURDERT_TILKOMMET_INNTEKT_UT,
    VURDERT_REFUSJON,
    VURDERT_REFUSJON_UT,
    OPPDATERT_MED_REFUSJON_OG_GRADERING,
    FASTSATT_INN,
    FASTSATT,
    UDEFINERT,
    ;

    /**
     * Rekkefølge tilstandene opptrer i løsningen.
     * <p>
     * IKKE ENDRE REKKEFØLGE AV TILSTANDER UTEN Å ENDRE REKKEFØLGE AV LAGRING.
     */
    private static final List<BeregningsgrunnlagTilstand> tilstandRekkefølge = List.of(
            OPPRETTET,
            FASTSATT_BEREGNINGSAKTIVITETER,
            OPPDATERT_MED_ANDELER,
            KOFAKBER_UT,
            FORESLÅTT,
            FORESLÅTT_UT,
            FORESLÅTT_DEL_2,
            FORESLÅTT_DEL_2_UT,
            BESTEBEREGNET,
            VURDERT_VILKÅR,
            VURDERT_TILKOMMET_INNTEKT,
            VURDERT_TILKOMMET_INNTEKT_UT,
            VURDERT_REFUSJON,
            VURDERT_REFUSJON_UT,
            OPPDATERT_MED_REFUSJON_OG_GRADERING,
            FASTSATT_INN,
            FASTSATT
    );


    public static List<BeregningsgrunnlagTilstand> getTilstandRekkefølge() {
        return tilstandRekkefølge;
    }

    public static BeregningsgrunnlagTilstand finnFørste() {
        return tilstandRekkefølge.getFirst();
    }


    public static Optional<BeregningsgrunnlagTilstand> finnForrigeTilstand(BeregningsgrunnlagTilstand tilstand) {
        int tilstandIndex = tilstandRekkefølge.indexOf(tilstand);
        if (tilstandIndex == 0) {
            return Optional.empty();
        }
        BeregningsgrunnlagTilstand forrigeTilstand = tilstandRekkefølge.get(tilstandIndex - 1);
        return Optional.of(forrigeTilstand);
    }

    public static Optional<BeregningsgrunnlagTilstand> finnNesteTilstand(BeregningsgrunnlagTilstand tilstand) {
        int tilstandIndex = tilstandRekkefølge.indexOf(tilstand);
        if (tilstandIndex == tilstandRekkefølge.size() - 1) {
            return Optional.empty();
        }
        BeregningsgrunnlagTilstand forrigeTilstand = tilstandRekkefølge.get(tilstandIndex + 1);
        return Optional.of(forrigeTilstand);
    }


    public boolean erFør(BeregningsgrunnlagTilstand that) {
        int thisIndex = tilstandRekkefølge.indexOf(this);
        int thatIndex = tilstandRekkefølge.indexOf(that);
        return thisIndex < thatIndex;
    }

    public boolean erEtter(BeregningsgrunnlagTilstand that) {
        int thisIndex = tilstandRekkefølge.indexOf(this);
        int thatIndex = tilstandRekkefølge.indexOf(that);
        return thisIndex > thatIndex;
    }

    @JsonCreator
    public static BeregningsgrunnlagTilstand fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return BeregningsgrunnlagTilstand.valueOf(kode);
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }


}
