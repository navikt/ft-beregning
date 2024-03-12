package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

/**
 * Beskriver hvilke endringer som er gjort på beregningsgrunnlaget ved løst avklaringsbehov
 */
public class Endringer {

    private BeregningsgrunnlagEndring beregningsgrunnlagEndring;
    private BeregningAktiviteterEndring beregningAktiviteterEndring;
    private FaktaOmBeregningVurderinger faktaOmBeregningVurderinger;
    private VarigEndretEllerNyoppstartetNæringEndring varigEndretEllerNyoppstartetNæringEndring;
    private VarigEndretArbeidssituasjonEndring varigEndretArbeidssituasjonEndring;
    private RefusjonoverstyringEndring refusjonoverstyringEndring;

    private Endringer() {
    }

    public Endringer(RefusjonoverstyringEndring refusjonoverstyringEndring) {
        this.refusjonoverstyringEndring = refusjonoverstyringEndring;
    }

    public BeregningAktiviteterEndring getBeregningAktiviteterEndring() {
        return beregningAktiviteterEndring;
    }

    public BeregningsgrunnlagEndring getBeregningsgrunnlagEndring() {
        return beregningsgrunnlagEndring;
    }

    public FaktaOmBeregningVurderinger getFaktaOmBeregningVurderinger() {
        return faktaOmBeregningVurderinger;
    }

    public RefusjonoverstyringEndring getRefusjonoverstyringEndring() {
        return refusjonoverstyringEndring;
    }

    public VarigEndretEllerNyoppstartetNæringEndring getVarigEndretNæringEndring() {
        return varigEndretEllerNyoppstartetNæringEndring;
    }

    public VarigEndretArbeidssituasjonEndring getVarigEndretArbeidssituasjonEndring() {
        return varigEndretArbeidssituasjonEndring;
    }

    public static Builder ny() {
        return new Builder();
    }

    public static class Builder {

        private Endringer kladd;

        private Builder() {
            this.kladd = new Endringer();
        }

        public Builder medBeregningsgrunnlagEndring(BeregningsgrunnlagEndring beregningsgrunnlagEndring) {
            this.kladd.beregningsgrunnlagEndring = beregningsgrunnlagEndring;
            return this;
        }

        public Builder medBeregningAktiviteterEndring(BeregningAktiviteterEndring beregningAktiviteterEndring) {
            this.kladd.beregningAktiviteterEndring = beregningAktiviteterEndring;
            return this;
        }

        public Builder medFaktaOmBeregningVurderinger(FaktaOmBeregningVurderinger faktaOmBeregningVurderinger) {
            this.kladd.faktaOmBeregningVurderinger = faktaOmBeregningVurderinger;
            return this;
        }

        public Builder medVarigEndretNæringEndring(VarigEndretEllerNyoppstartetNæringEndring varigEndretEllerNyoppstartetNæringEndring) {
            this.kladd.varigEndretEllerNyoppstartetNæringEndring = varigEndretEllerNyoppstartetNæringEndring;
            return this;
        }


        public Builder medVarigEndretArbeidssituasjonEndring(VarigEndretArbeidssituasjonEndring varigEndretArbeidssituasjonEndring) {
            this.kladd.varigEndretArbeidssituasjonEndring = varigEndretArbeidssituasjonEndring;
            return this;
        }


        public Endringer build() {
            return kladd;
        }

    }

}
