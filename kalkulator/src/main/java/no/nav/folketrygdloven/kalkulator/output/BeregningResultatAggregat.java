package no.nav.folketrygdloven.kalkulator.output;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;


public class BeregningResultatAggregat {

    protected Set<BeregningAvklaringsbehovResultat> beregningAvklaringsbehovResultater = new HashSet<>();

    protected BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag;

    protected BeregningVilkårResultat beregningVilkårResultat;

    protected RegelSporingAggregat regelSporingAggregat;

    public Set<BeregningAvklaringsbehovResultat> getBeregningAvklaringsbehovResultater() {
        return beregningAvklaringsbehovResultater;
    }

    public BeregningsgrunnlagGrunnlagDto getBeregningsgrunnlagGrunnlag() {
        return beregningsgrunnlagGrunnlag;
    }

    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow(() -> new IllegalStateException("Forventet å ha beregningsgrunnlag"));
    }

    public BeregningVilkårResultat getBeregningVilkårResultat() {
        return beregningVilkårResultat;
    }


    public Optional<RegelSporingAggregat> getRegelSporingAggregat() {
        return Optional.ofNullable(regelSporingAggregat);
    }


    public void leggTilAvklaringsbehov(BeregningAvklaringsbehovResultat avklaringsbehovResultat) {
        this.beregningAvklaringsbehovResultater.add(avklaringsbehovResultat);
    }

    public static class Builder {

        private BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder;
        private BeregningsgrunnlagTilstand tilstand;
        private final BeregningResultatAggregat kladd  = new BeregningResultatAggregat();

        public Builder() {
        }

        protected Builder(BeregningsgrunnlagInput input) {
            this.grunnlagBuilder = input.getBeregningsgrunnlagGrunnlag() == null ? BeregningsgrunnlagGrunnlagDtoBuilder.nytt() : BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        }

        public static Builder fra(BeregningsgrunnlagInput input) {
            return new Builder(input);
        }

        public Builder medRegisterAktiviteter(BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
            grunnlagBuilder.medRegisterAktiviteter(beregningAktivitetAggregat);
            this.tilstand = BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER;
            return this;
        }

        public Builder medOverstyrteAktiviteter(BeregningAktivitetOverstyringerDto beregningAktivitetOverstyringerDto) {
            grunnlagBuilder.medOverstyring(beregningAktivitetOverstyringerDto);
            this.tilstand = BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER;
            return this;
        }

        public Builder medBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagTilstand tilstand) {
            grunnlagBuilder.medBeregningsgrunnlag(beregningsgrunnlag);
            this.tilstand = tilstand;
            return this;
        }

        public Builder medFaktaAggregat(FaktaAggregatDto faktaAggregatDto, BeregningsgrunnlagTilstand tilstand) {
            if (faktaAggregatDto != null) {
                grunnlagBuilder.medFaktaAggregat(faktaAggregatDto);
                this.tilstand = tilstand;
            }
            return this;
        }

        public Builder medAvklaringsbehov(List<BeregningAvklaringsbehovResultat> beregningAvklaringsbehovResultater) {
            this.kladd.beregningAvklaringsbehovResultater.addAll(beregningAvklaringsbehovResultater);
            return this;
        }

        public Builder medVilkårResultat(BeregningVilkårResultat vilkårResultat) {
            this.kladd.beregningVilkårResultat = vilkårResultat;
            return this;
        }

        public Builder medRegelSporingAggregat(RegelSporingAggregat regelsporing) {
            this.kladd.regelSporingAggregat = regelsporing;
            return this;
        }


        public BeregningResultatAggregat build() {
            if (this.tilstand != null && this.grunnlagBuilder != null) {
                this.kladd.beregningsgrunnlagGrunnlag = grunnlagBuilder.build(tilstand);
                return kladd;
            } else if (this.kladd.beregningAvklaringsbehovResultater != null) {
                return kladd;
            }
            throw new IllegalStateException("Må sette enten beregningsgrunnlag eller beregningavklaringsbehov på beregningresultataggregat!");
        }

    }


}
