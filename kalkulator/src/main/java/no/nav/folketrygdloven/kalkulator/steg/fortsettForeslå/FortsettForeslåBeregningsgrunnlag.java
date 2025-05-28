package no.nav.folketrygdloven.kalkulator.steg.fortsettForeslå;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.FortsettForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.steg.BeregningsgrunnlagVerifiserer;
import no.nav.folketrygdloven.kalkulator.steg.foreslå.AvklaringsbehovUtlederForeslåBeregning;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

public class FortsettForeslåBeregningsgrunnlag {
    private final MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel();
    private final MapBeregningsgrunnlagFraRegelTilVL mapBeregningsgrunnlagFraRegelTilVL = new MapBeregningsgrunnlagFraRegelTilVL();

    public BeregningsgrunnlagRegelResultat fortsettForeslåBeregningsgrunnlag(FortsettForeslåBeregningsgrunnlagInput input) {
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();
        var beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes()
                .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her"));

        // Oversetter initielt Beregningsgrunnlag -> regelmodell
        var regelmodellBeregningsgrunnlag = mapBeregningsgrunnlagFraVLTilRegel.map(input, grunnlag);
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (var periode : regelmodellBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            regelResultater.add(KalkulusRegler.fortsettForeslåBeregningsgrunnlag(periode));
        }

        // Oversett endelig resultat av regelmodell til foreslått Beregningsgrunnlag  (+ spore input -> evaluation)
        var foreslåttBeregningsgrunnlag = mapBeregningsgrunnlagFraRegelTilVL.mapForeslåBeregningsgrunnlag(regelmodellBeregningsgrunnlag, beregningsgrunnlag);

        verifiserBeregningsgrunnlag(foreslåttBeregningsgrunnlag);

        var avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(input, regelResultater);
        var regelsporinger = MapRegelSporingFraRegelTilVL.mapRegelsporingPerioder(
                regelResultater,
                foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode).collect(Collectors.toList()),
                BeregningsgrunnlagPeriodeRegelType.FORESLÅ_2);
        return new BeregningsgrunnlagRegelResultat(foreslåttBeregningsgrunnlag, avklaringsbehov,
                new RegelSporingAggregat(regelsporinger));
    }

    private void verifiserBeregningsgrunnlag(BeregningsgrunnlagDto foreslåttBeregningsgrunnlag) {
        BeregningsgrunnlagVerifiserer.verifiserFortsettForeslåttBeregningsgrunnlag(foreslåttBeregningsgrunnlag);
    }

}
