package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag;
import static no.nav.folketrygdloven.kalkulator.steg.besteberegning.MapBesteberegningFraRegelTilVL.mapTilBeregningsgrunnlag;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.besteberegning.modell.BesteberegningRegelmodell;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegningOutput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

public class ForeslåBesteberegning {
    /** Foreslår besteberegning
     *
     * @param input Input til foreslå besteberegning
     * @return Beregningsresultat med nytt besteberegnet grunnlag
     */
    public BesteberegningRegelResultat foreslåBesteberegning(ForeslåBesteberegningInput input) {
        BesteberegningRegelmodell regelmodell = MapTilBesteberegningRegelmodell.map(input);
        RegelResultat regelResultat = KalkulusRegler.foreslåBesteberegning(regelmodell);
        BesteberegningOutput output = regelmodell.getOutput();
        var besteberegnetGrunnlag = mapTilBeregningsgrunnlag(input.getBeregningsgrunnlagGrunnlag(), output);
        var seksBesteMåneder = MapBesteberegningFraRegelTilVL.mapSeksBesteMåneder(output);

        // Bryr oss kun om avvik om beregning etter tredje ledd (seks beste måneder) blir brukt
        var avvikFraFørsteLedd = regelmodell.getOutput().getSkalBeregnesEtterSeksBesteMåneder()
                ? finnAvvik(regelmodell, besteberegnetGrunnlag)
                : null;

        var besteberegningVurderingsgrunnlag = new BesteberegningVurderingGrunnlag(seksBesteMåneder, avvikFraFørsteLedd);
        return new BesteberegningRegelResultat(besteberegnetGrunnlag,
                new RegelSporingAggregat(mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.BESTEBEREGNING)),
                besteberegningVurderingsgrunnlag);
    }

    private Beløp finnAvvik(BesteberegningRegelmodell regelmodell, BeregningsgrunnlagDto besteberegnetGrunnlag) {
        var beregningEtter1ledd = Beløp.fra(regelmodell.getInput().getBeregnetGrunnlag());
        var beregningEtter3ledd = besteberegnetGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBruttoPrÅr();
        var avvik = beregningEtter3ledd.subtraher(beregningEtter1ledd);
        if (avvik.compareTo(Beløp.ZERO) < 0) {
            throw new IllegalStateException("Avvik kan ikke være mindre enn 0 kr når sak skal besteberegnes");
        }
        return avvik;
    }

}
