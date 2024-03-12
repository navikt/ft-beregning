package no.nav.folketrygdloven.kalkulator.steg.fullføre;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.fastsett.MapFullføreBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.steg.BeregningsgrunnlagVerifiserer;


public abstract class FullføreBeregningsgrunnlag {

    public BeregningsgrunnlagRegelResultat fullføreBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();

        // Oversetter foreslått Beregningsgrunnlag -> regelmodell
        var beregningsgrunnlagRegel = new MapFullføreBeregningsgrunnlagFraVLTilRegel().map(input, grunnlag.getBeregningsgrunnlagHvisFinnes().orElse(null));

        // Evaluerer hver BeregningsgrunnlagPeriode fra foreslått Beregningsgrunnlag
        List<RegelResultat> regelResultater = evaluerRegelmodell(beregningsgrunnlagRegel, input);

        // Oversett endelig resultat av regelmodell til fastsatt Beregningsgrunnlag  (+ spore input -> evaluation)
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().orElse(null);
        BeregningsgrunnlagDto fastsattBeregningsgrunnlag = FullføreBeregningsgrunnlagUtils.mapBeregningsgrunnlagFraRegelTilVL(beregningsgrunnlagRegel, beregningsgrunnlag);

        List<RegelSporingPeriode> regelsporinger = FullføreBeregningsgrunnlagUtils.mapRegelSporinger(regelResultater, fastsattBeregningsgrunnlag, input.getForlengelseperioder());
        BeregningsgrunnlagVerifiserer.verifiserFastsattBeregningsgrunnlag(fastsattBeregningsgrunnlag, input.getYtelsespesifiktGrunnlag(), input.getForlengelseperioder());
        return new BeregningsgrunnlagRegelResultat(fastsattBeregningsgrunnlag, new RegelSporingAggregat(regelsporinger));
    }

    protected abstract List<RegelResultat> evaluerRegelmodell(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag beregningsgrunnlagRegel, BeregningsgrunnlagInput input);

}
