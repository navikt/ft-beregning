package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

public class BesteberegningResultat extends BeregningResultatAggregat {

    private BesteberegningVurderingGrunnlag besteberegningVurderingGrunnlag;

    public BesteberegningResultat() {
    }

    public BesteberegningVurderingGrunnlag getBesteberegningVurderingGrunnlag() {
        return besteberegningVurderingGrunnlag;
    }

    protected void setBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        this.beregningsgrunnlagGrunnlag = beregningsgrunnlagGrunnlag;
    }

    public static class Builder {

        private BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder;
        private final BesteberegningResultat kladd  = new BesteberegningResultat();

        public Builder() {
        }

        protected Builder(ForeslåBesteberegningInput input) {
            this.grunnlagBuilder = input.getBeregningsgrunnlagGrunnlag() == null ? BeregningsgrunnlagGrunnlagDtoBuilder.nytt() : BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        }

        public static Builder fra(ForeslåBesteberegningInput input) {
            return new Builder(input);
        }

        public Builder medVurderingsgrunnlag(BesteberegningVurderingGrunnlag vurderingGrunnlag) {
            this.kladd.besteberegningVurderingGrunnlag = vurderingGrunnlag;
            return this;
        }

        public Builder medBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag) {
            grunnlagBuilder.medBeregningsgrunnlag(beregningsgrunnlag);
            return this;
        }

        public Builder medRegelSporingAggregat(RegelSporingAggregat regelsporing) {
            this.kladd.regelSporingAggregat = regelsporing;
            return this;
        }

        public BesteberegningResultat build() {
            if (this.grunnlagBuilder != null) {
                this.kladd.setBeregningsgrunnlagGrunnlag(grunnlagBuilder.build(BeregningsgrunnlagTilstand.BESTEBEREGNET));
                return kladd;
            }
            throw new IllegalStateException("Må sette beregningsgrunnlag beregningresultataggregat!");
        }

    }

}
