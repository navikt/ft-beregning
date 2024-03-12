package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.SjekkVedKopiering;

public class BeregningAktivitetAggregatDto {


    @SjekkVedKopiering
    private List<BeregningAktivitetDto> aktiviteter = new ArrayList<>();
    @SjekkVedKopiering
    private LocalDate skjæringstidspunktOpptjening;

    public BeregningAktivitetAggregatDto() {
        // NOSONAR
    }

    public List<BeregningAktivitetDto> getBeregningAktiviteter() {
        return Collections.unmodifiableList(aktiviteter);
    }

    public LocalDate getSkjæringstidspunktOpptjening() {
        return skjæringstidspunktOpptjening;
    }

    private void leggTilAktivitet(BeregningAktivitetDto beregningAktivitet) {
        beregningAktivitet.setBeregningAktiviteter(this);
        aktiviteter.add(beregningAktivitet);
    }


    public List<BeregningAktivitetDto> getAktiviteterPåDato(LocalDate dato) {
        return aktiviteter.stream()
                .filter(ba -> ba.getPeriode().inkluderer(dato)).toList();
    }

    @Override
    public String toString() {
        return "BeregningAktivitetAggregatDto{" +
                "aktiviteter=" + aktiviteter +
                ", skjæringstidspunktOpptjening=" + skjæringstidspunktOpptjening +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BeregningAktivitetAggregatDto kladd;

        private Builder() {
            kladd = new BeregningAktivitetAggregatDto();
        }

        public Builder medSkjæringstidspunktOpptjening(LocalDate skjæringstidspunktOpptjening) {
            kladd.skjæringstidspunktOpptjening = skjæringstidspunktOpptjening;
            return this;
        }

        public Builder leggTilAktivitet(BeregningAktivitetDto beregningAktivitet) { // NOSONAR
            kladd.leggTilAktivitet(beregningAktivitet);
            return this;
        }

        public BeregningAktivitetAggregatDto build() {
            verifyStateForBuild();
            return kladd;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(kladd.skjæringstidspunktOpptjening, "skjæringstidspunktOpptjening");
        }
    }
}
