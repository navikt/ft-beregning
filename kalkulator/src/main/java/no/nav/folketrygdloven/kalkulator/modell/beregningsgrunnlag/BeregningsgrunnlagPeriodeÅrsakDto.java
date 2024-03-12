package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;


import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.DiffIgnore;
import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.diff.SjekkVedKopiering;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;


public class BeregningsgrunnlagPeriodeÅrsakDto implements IndexKey {

    @DiffIgnore
    private BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode;
    @SjekkVedKopiering
    private PeriodeÅrsak periodeÅrsak = PeriodeÅrsak.UDEFINERT;

    private BeregningsgrunnlagPeriodeÅrsakDto() {
    }

    public BeregningsgrunnlagPeriodeÅrsakDto(BeregningsgrunnlagPeriodeÅrsakDto kopiereFra) {
        this.periodeÅrsak = kopiereFra.periodeÅrsak;
    }

    public BeregningsgrunnlagPeriodeDto getBeregningsgrunnlagPeriode() {
        return beregningsgrunnlagPeriode;
    }

    public PeriodeÅrsak getPeriodeÅrsak() {
        return periodeÅrsak;
    }


    public void setBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        this.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningsgrunnlagPeriode, periodeÅrsak);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagPeriodeÅrsakDto)) {
            return false;
        }
        BeregningsgrunnlagPeriodeÅrsakDto other = (BeregningsgrunnlagPeriodeÅrsakDto) obj;
        return Objects.equals(this.getBeregningsgrunnlagPeriode(), other.getBeregningsgrunnlagPeriode())
                && Objects.equals(this.getPeriodeÅrsak(), other.getPeriodeÅrsak());
    }

    @Override
    public String getIndexKey() {
        return periodeÅrsak.getIndexKey();
    }

    public static class Builder {
        private BeregningsgrunnlagPeriodeÅrsakDto beregningsgrunnlagPeriodeÅrsakMal;

        public Builder() {
            beregningsgrunnlagPeriodeÅrsakMal = new BeregningsgrunnlagPeriodeÅrsakDto();
        }

        public Builder(BeregningsgrunnlagPeriodeÅrsakDto beregningsgrunnlagPeriodeÅrsakDto) {
            beregningsgrunnlagPeriodeÅrsakMal = new BeregningsgrunnlagPeriodeÅrsakDto(beregningsgrunnlagPeriodeÅrsakDto);
        }

        public static Builder kopier(BeregningsgrunnlagPeriodeÅrsakDto beregningsgrunnlagPeriodeÅrsakDto) {
            return new Builder(beregningsgrunnlagPeriodeÅrsakDto);
        }

        public Builder medPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
            beregningsgrunnlagPeriodeÅrsakMal.periodeÅrsak = periodeÅrsak;
            return this;
        }

        public BeregningsgrunnlagPeriodeÅrsakDto build(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
            beregningsgrunnlagPeriodeÅrsakMal.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
            beregningsgrunnlagPeriode.addBeregningsgrunnlagPeriodeÅrsak(beregningsgrunnlagPeriodeÅrsakMal);
            return beregningsgrunnlagPeriodeÅrsakMal;
        }
    }
}
