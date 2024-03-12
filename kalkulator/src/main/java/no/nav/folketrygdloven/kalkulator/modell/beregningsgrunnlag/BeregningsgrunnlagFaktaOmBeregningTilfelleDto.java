package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.SjekkVedKopiering;
import no.nav.folketrygdloven.kalkulator.modell.diff.DiffIgnore;
import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

public class BeregningsgrunnlagFaktaOmBeregningTilfelleDto implements IndexKey {

    @DiffIgnore
    private BeregningsgrunnlagDto beregningsgrunnlag;
    @SjekkVedKopiering
    private FaktaOmBeregningTilfelle faktaOmBeregningTilfelle = FaktaOmBeregningTilfelle.UDEFINERT;

    public BeregningsgrunnlagFaktaOmBeregningTilfelleDto() {
    }

    public BeregningsgrunnlagFaktaOmBeregningTilfelleDto(FaktaOmBeregningTilfelle kopier) {
        this.faktaOmBeregningTilfelle = kopier;
    }

    public FaktaOmBeregningTilfelle getFaktaOmBeregningTilfelle() {
        return faktaOmBeregningTilfelle;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BeregningsgrunnlagFaktaOmBeregningTilfelleDto)) {
            return false;
        }
        BeregningsgrunnlagFaktaOmBeregningTilfelleDto that = (BeregningsgrunnlagFaktaOmBeregningTilfelleDto) o;
        return Objects.equals(beregningsgrunnlag, that.beregningsgrunnlag) &&
                Objects.equals(faktaOmBeregningTilfelle, that.faktaOmBeregningTilfelle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningsgrunnlag, faktaOmBeregningTilfelle);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getIndexKey() {
        return faktaOmBeregningTilfelle.getIndexKey();
    }

    public static class Builder {
        private BeregningsgrunnlagFaktaOmBeregningTilfelleDto beregningsgrunnlagFaktaOmBeregningTilfelle;

        public Builder() {
            beregningsgrunnlagFaktaOmBeregningTilfelle = new BeregningsgrunnlagFaktaOmBeregningTilfelleDto();
        }

        public Builder(FaktaOmBeregningTilfelle oppdater) {
            beregningsgrunnlagFaktaOmBeregningTilfelle = new BeregningsgrunnlagFaktaOmBeregningTilfelleDto(oppdater);
        }

        public static Builder kopier(FaktaOmBeregningTilfelle beregningsgrunnlagFaktaOmBeregningTilfelle) {
            return new Builder(beregningsgrunnlagFaktaOmBeregningTilfelle);
        }

        BeregningsgrunnlagFaktaOmBeregningTilfelleDto.Builder medFaktaOmBeregningTilfelle(FaktaOmBeregningTilfelle tilfelle) {
            beregningsgrunnlagFaktaOmBeregningTilfelle.faktaOmBeregningTilfelle = tilfelle;
            return this;
        }

        public BeregningsgrunnlagFaktaOmBeregningTilfelleDto build(BeregningsgrunnlagDto beregningsgrunnlag) {
            beregningsgrunnlagFaktaOmBeregningTilfelle.beregningsgrunnlag = beregningsgrunnlag;
            return beregningsgrunnlagFaktaOmBeregningTilfelle;
        }
    }
}
