package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BeregningAktivitetOverstyringerDto {

    private List<BeregningAktivitetOverstyringDto> overstyringer = new ArrayList<>();

    public List<BeregningAktivitetOverstyringDto> getOverstyringer() {
        return Collections.unmodifiableList(overstyringer);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BeregningAktivitetOverstyringerDto kladd;

        private Builder() {
            kladd = new BeregningAktivitetOverstyringerDto();
        }

        public Builder leggTilOverstyring(BeregningAktivitetOverstyringDto beregningAktivitetOverstyring) {
            BeregningAktivitetOverstyringDto entitet = beregningAktivitetOverstyring;
            kladd.overstyringer.add(entitet);
            entitet.setBeregningAktivitetOverstyringer(kladd);
            return this;
        }

        public BeregningAktivitetOverstyringerDto build() {
            return kladd;
        }
    }
}
