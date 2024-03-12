package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningAktiviteterFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.frisinn.MapBeregningAktiviteterFraVLTilRegelFRISINN;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.GrunnbeløpMapper;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Vilkårsavslagsårsak;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

public class ForeslåSkjæringstidspunktTjenesteFRISINN {

    public BeregningsgrunnlagRegelResultat foreslåSkjæringstidspunkt(FastsettBeregningsaktiviteterInput input) {
        var beregningAktivitetAggregat = fastsettAktiviteter(input);
        if (beregningAktivitetAggregat.getBeregningAktiviteter().isEmpty() && !input.getOpptjeningAktiviteter().erMidlertidigInaktiv()) {
            // Avslår vilkår
            var beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(beregningAktivitetAggregat);
            beregningsgrunnlagRegelResultat.setVilkårsresultat(List.of(new BeregningVilkårResultat(false, Vilkårsavslagsårsak.FOR_LAVT_BG, Intervall.fraOgMed(input.getSkjæringstidspunktOpptjening()))));
            return beregningsgrunnlagRegelResultat;
        }
        var beregningsgrunnlagRegelResultat = FastsettSkjæringstidspunktOgStatuserFRISINN.fastsett(input, beregningAktivitetAggregat, GrunnbeløpMapper.mapGrunnbeløpInput(input.getGrunnbeløpInput()));
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
        return beregningsgrunnlagRegelResultat;
    }

    public BeregningAktivitetAggregatDto fastsettAktiviteter(FastsettBeregningsaktiviteterInput input) {
        // Oversetter Opptjening -> regelmodell, hvor også skjæringstidspunkt for Opptjening er lagret
        AktivitetStatusModell regelmodell = new MapBeregningAktiviteterFraVLTilRegelFRISINN().mapForSkjæringstidspunkt(input);
        return MapBeregningAktiviteterFraRegelTilVL.map(regelmodell);
    }

}
