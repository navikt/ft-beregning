package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;

public class BesteberegningRegelResultat extends BeregningsgrunnlagRegelResultat {

    private final BesteberegningVurderingGrunnlag besteberegningVurderingGrunnlag;

    public BesteberegningRegelResultat(BeregningsgrunnlagDto beregningsgrunnlag,
                                       RegelSporingAggregat regelsporinger,
                                       BesteberegningVurderingGrunnlag besteberegningVurderingGrunnlag) {
        super(beregningsgrunnlag, regelsporinger);
        this.besteberegningVurderingGrunnlag = besteberegningVurderingGrunnlag;
    }

    public BesteberegningVurderingGrunnlag getBesteberegningVurderingGrunnlag() {
        return besteberegningVurderingGrunnlag;
    }
}
