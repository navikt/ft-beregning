package no.nav.folketrygdloven.kalkulus.response.v1;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AvklaringsbehovMedTilstandDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.KalkulusResultatKode;
import no.nav.folketrygdloven.kalkulus.kodeverk.Vilkårsavslagsårsak;

/**
 * Beskriver hvilke avklaringsbehov som må løses av K9 eller FPSAK for at beregningen kan fortsette
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class TilstandResponse implements KalkulusRespons {

    @JsonProperty(value = "eksternReferanse")
    @Valid
    private UUID eksternReferanse;

    @JsonProperty(value = "resultatkode")
    @Valid
    private KalkulusResultatKode resultatkode;

    @JsonProperty(value = "avklaringsbehovMedTilstandDto")
    @Valid
    private List<AvklaringsbehovMedTilstandDto> avklaringsbehovMedTilstandDto;

    @JsonProperty(value = "vilkarOppfylt")
    @Valid
    private Boolean vilkarOppfylt;

    @JsonProperty("vilkårsavslagsårsak")
    @Valid
    private Vilkårsavslagsårsak vilkårsavslagsårsak;

    public TilstandResponse() {
        // default ctor
    }

    public TilstandResponse(@JsonProperty(value = "eksternReferanse") @Valid UUID eksternReferanse,
                            @JsonProperty(value = "resultatkode") @Valid KalkulusResultatKode resultatkode,
                            @JsonProperty(value = "avklaringsbehovMedTilstandDto") @Valid List<AvklaringsbehovMedTilstandDto> avklaringsbehovMedTilstandDto) {
        this.eksternReferanse = eksternReferanse;
        this.resultatkode = resultatkode;
        this.avklaringsbehovMedTilstandDto = avklaringsbehovMedTilstandDto;
    }

    public TilstandResponse(UUID eksternReferanse, KalkulusResultatKode resultatkode) {
        this.eksternReferanse = eksternReferanse;
        this.resultatkode = resultatkode;
    }

    public TilstandResponse(@Valid UUID eksternReferanse,
                            @Valid List<AvklaringsbehovMedTilstandDto> avklaringsbehovMedTilstandDto,
                            KalkulusResultatKode resultatkode, @Valid Boolean vilkarOppfylt,
                            @Valid Vilkårsavslagsårsak vilkårsavslagsårsak) {
        this.eksternReferanse = eksternReferanse;
        this.avklaringsbehovMedTilstandDto = avklaringsbehovMedTilstandDto;
        this.vilkarOppfylt = vilkarOppfylt;
        this.vilkårsavslagsårsak = vilkårsavslagsårsak;
        this.resultatkode = resultatkode;
    }

    public TilstandResponse medVilkårResultat(boolean resultat) {
        vilkarOppfylt = resultat;
        return this;
    }

    public TilstandResponse medVilkårsavslagsårsak(Vilkårsavslagsårsak vilkårsavslagsårsak) {
        this.vilkårsavslagsårsak = vilkårsavslagsårsak;
        return this;
    }

    @AssertTrue(message = "Krever vilkårsavslagsårsak når vilkåret ikke er oppfylt")
    public boolean isSjekkOmHarAvslagsårsak() {
        if (vilkarOppfylt != null && !vilkarOppfylt) {
            return vilkårsavslagsårsak != null;
        }
        return true;
    }

    public List<AvklaringsbehovMedTilstandDto> getAvklaringsbehovMedTilstandDto() {
        return avklaringsbehovMedTilstandDto;
    }

    public Boolean getVilkarOppfylt() {
        return vilkarOppfylt;
    }

    public Vilkårsavslagsårsak getVilkårsavslagsårsak() {
        return vilkårsavslagsårsak;
    }

    @Override
    public UUID getEksternReferanse() {
        return eksternReferanse;
    }


    public KalkulusResultatKode getResultatkode() {
        return resultatkode;
    }
}
