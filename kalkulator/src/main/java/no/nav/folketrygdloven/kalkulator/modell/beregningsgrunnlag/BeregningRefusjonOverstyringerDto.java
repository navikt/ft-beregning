package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class BeregningRefusjonOverstyringerDto {

    private List<BeregningRefusjonOverstyringDto> overstyringer = new ArrayList<>();

    protected BeregningRefusjonOverstyringerDto() {
        // Hibernate
    }

    public List<BeregningRefusjonOverstyringDto> getRefusjonOverstyringer() {
        return Collections.unmodifiableList(overstyringer);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BeregningRefusjonOverstyringerDto kladd;

        private Builder() {
            kladd = new BeregningRefusjonOverstyringerDto();
        }

        public Builder leggTilOverstyring(BeregningRefusjonOverstyringDto beregningRefusjonOverstyring) {
            BeregningRefusjonOverstyringDto entitet = beregningRefusjonOverstyring;
            entitet.setRefusjonOverstyringerEntitet(kladd);
            kladd.overstyringer.add(entitet);
            return this;
        }

        public BeregningRefusjonOverstyringerDto build() {
            return kladd;
        }
    }
}
