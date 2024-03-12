package no.nav.folketrygdloven.kalkulator.modell.behandling;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Inneholder relevante tidspunkter for en behandling
 */
public class Skjæringstidspunkt {
    private LocalDate skjæringstidspunktOpptjening;
    private LocalDate skjæringstidspunktBeregning;
    private LocalDate førsteUttaksdato;

    private Skjæringstidspunkt() {
        // hide constructor
    }

    private Skjæringstidspunkt(Skjæringstidspunkt other) {
        this.skjæringstidspunktOpptjening = other.skjæringstidspunktOpptjening;
        this.skjæringstidspunktBeregning = other.skjæringstidspunktBeregning;
        this.førsteUttaksdato = other.førsteUttaksdato;
    }


    /** Skjæringstidspunkt for opptjening er definert som dagen etter slutt av opptjeningsperiode. */
    public LocalDate getSkjæringstidspunktOpptjening() {
        return skjæringstidspunktOpptjening;
    }

    /** Skjæringstidspunkt for beregning er definert som dagen etter siste dag med godkjente aktiviteter. */
    public LocalDate getSkjæringstidspunktBeregning() {
        return skjæringstidspunktBeregning;
    }

    /** Første uttaksdato er første dag bruker får utbetalt ytelse. Bestemmer gbeløp. Er lik de andre dagene for alle ytelser utenom FP,  */
    public LocalDate getFørsteUttaksdato() {
        return førsteUttaksdato;
    }

    @Override
    public int hashCode() {
        return Objects.hash(skjæringstidspunktBeregning, skjæringstidspunktOpptjening, førsteUttaksdato);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==this) {
            return true;
        } else if (obj==null || !(obj.getClass().equals(this.getClass()))) {
            return false;
        }
        Skjæringstidspunkt other = (Skjæringstidspunkt) obj;
        return Objects.equals(this.skjæringstidspunktBeregning, other.skjæringstidspunktBeregning)
                && Objects.equals(this.førsteUttaksdato, other.førsteUttaksdato)
                && Objects.equals(this.skjæringstidspunktOpptjening, other.skjæringstidspunktOpptjening);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + skjæringstidspunktBeregning + ">";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Skjæringstidspunkt other) {
        return new Builder(other);
    }

    public static class Builder {
        private final Skjæringstidspunkt kladd;

        private Builder() {
            this.kladd = new Skjæringstidspunkt();
        }

        private Builder(Skjæringstidspunkt other) {
            this.kladd = new Skjæringstidspunkt(other);
        }


        public Builder medSkjæringstidspunktOpptjening(LocalDate dato) {
            kladd.skjæringstidspunktOpptjening = dato;
            return this;
        }

        public Builder medSkjæringstidspunktBeregning(LocalDate dato) {
            kladd.skjæringstidspunktBeregning = dato;
            return this;
        }

        public Builder medFørsteUttaksdato(LocalDate dato) {
            kladd.førsteUttaksdato = dato;
            return this;
        }

        public Skjæringstidspunkt build() {
            return kladd;
        }
    }
}
