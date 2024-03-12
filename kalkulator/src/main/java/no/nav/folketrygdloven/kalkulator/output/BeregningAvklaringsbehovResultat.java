package no.nav.folketrygdloven.kalkulator.output;

import static java.util.Collections.singletonList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;

public class BeregningAvklaringsbehovResultat {

    private AvklaringsbehovDefinisjon beregningAvklaringsbehovDefinisjon;
    private BeregningVenteårsak venteårsak;
    private LocalDateTime ventefrist;


    private BeregningAvklaringsbehovResultat(AvklaringsbehovDefinisjon avklaringsbehovDefinisjon) {
        this.beregningAvklaringsbehovDefinisjon = avklaringsbehovDefinisjon;
    }

    private BeregningAvklaringsbehovResultat(AvklaringsbehovDefinisjon avklaringsbehovDefinisjon, BeregningVenteårsak venteårsak, LocalDateTime ventefrist) {
        this.beregningAvklaringsbehovDefinisjon = avklaringsbehovDefinisjon;
        this.venteårsak = venteårsak;
        this.ventefrist = ventefrist;
    }

    /**
     * Factory-metode direkte basert på {@link AvklaringsbehovDefinisjon}. Ingen callback for consumer.
     */
    public static BeregningAvklaringsbehovResultat opprettFor(AvklaringsbehovDefinisjon avklaringsbehovDefinisjon) {
        return new BeregningAvklaringsbehovResultat(avklaringsbehovDefinisjon);
    }


    /**
     * Factory-metode direkte basert på {@link AvklaringsbehovDefinisjon}, returnerer liste. Ingen callback for consumer.
     */
    public static List<BeregningAvklaringsbehovResultat> opprettListeFor(AvklaringsbehovDefinisjon avklaringsbehovDefinisjon) {
        return singletonList(new BeregningAvklaringsbehovResultat(avklaringsbehovDefinisjon));
    }

    /**
     * Factory-metode som linker {@link AvklaringsbehovDefinisjon} sammen med callback for consumer-operasjon.
     */
    public static BeregningAvklaringsbehovResultat opprettMedFristFor(AvklaringsbehovDefinisjon avklaringsbehovDefinisjon, BeregningVenteårsak venteårsak, LocalDateTime ventefrist) {
        return new BeregningAvklaringsbehovResultat(avklaringsbehovDefinisjon, venteårsak, ventefrist);
    }

    public AvklaringsbehovDefinisjon getBeregningAvklaringsbehovDefinisjon() {
        return beregningAvklaringsbehovDefinisjon;
    }

    public BeregningVenteårsak getVenteårsak() {
        return venteårsak;
    }

    public LocalDateTime getVentefrist() {
        return ventefrist;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + beregningAvklaringsbehovDefinisjon.getKode()
            + ", venteårsak=" + getVenteårsak() + ", ventefrist=" + getVentefrist() + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BeregningAvklaringsbehovResultat))
            return false;

        BeregningAvklaringsbehovResultat that = (BeregningAvklaringsbehovResultat) o;

        return beregningAvklaringsbehovDefinisjon.getKode().equals(that.beregningAvklaringsbehovDefinisjon.getKode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningAvklaringsbehovDefinisjon.getKode());
    }

    public boolean harFrist() {
        return null != getVentefrist();
    }
}
