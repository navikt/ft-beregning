package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AvklaringsbehovMedTilstandDto {

    @JsonProperty(value = "beregningAvklaringsbehovDefinisjon")
    @Valid
    @NotNull
    private AvklaringsbehovDefinisjon beregningAvklaringsbehovDefinisjon;

    @JsonProperty(value = "beregningAksjonspunktDefinisjon")
    @Valid
    @NotNull
    private AvklaringsbehovDefinisjon beregningAksjonspunktDefinisjon;

    @JsonProperty(value = "venteårsak")
    @Valid
    @NotNull
    private BeregningVenteårsak venteårsak;

    @JsonProperty(value = "ventefrist")
    @Valid
    @NotNull
    private LocalDateTime ventefrist;

    public AvklaringsbehovMedTilstandDto() {
        // default ctor
    }

    public AvklaringsbehovMedTilstandDto(@Valid @NotNull AvklaringsbehovDefinisjon beregningAvklaringsbehovDefinisjon, @Valid @NotNull BeregningVenteårsak venteårsak, @Valid @NotNull LocalDateTime ventefrist) {
        this.beregningAvklaringsbehovDefinisjon = beregningAvklaringsbehovDefinisjon;
        this.beregningAksjonspunktDefinisjon = beregningAvklaringsbehovDefinisjon;
        this.venteårsak = venteårsak;
        this.ventefrist = ventefrist;
    }

    public AvklaringsbehovDefinisjon getBeregningAvklaringsbehovDefinisjon() {
        return beregningAvklaringsbehovDefinisjon;
    }

    public AvklaringsbehovDefinisjon getBeregningAksjonspunktDefinisjon() {
        return beregningAksjonspunktDefinisjon;
    }

    public BeregningVenteårsak getVenteårsak() {
        return venteårsak;
    }

    public LocalDateTime getVentefrist() {
        return ventefrist;
    }

    @Override
    public String toString() {
        return "AvklaringsbehovMedTilstandDto{" +
                "beregningAvklaringsbehovDefinisjon=" + beregningAvklaringsbehovDefinisjon +
                ", venteårsak=" + venteårsak +
                ", ventefrist=" + ventefrist +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvklaringsbehovMedTilstandDto that = (AvklaringsbehovMedTilstandDto) o;
        return Objects.equals(beregningAvklaringsbehovDefinisjon, that.beregningAvklaringsbehovDefinisjon) &&
                Objects.equals(venteårsak, that.venteårsak) &&
                Objects.equals(ventefrist, that.ventefrist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningAvklaringsbehovDefinisjon, venteårsak, ventefrist);
    }
}
