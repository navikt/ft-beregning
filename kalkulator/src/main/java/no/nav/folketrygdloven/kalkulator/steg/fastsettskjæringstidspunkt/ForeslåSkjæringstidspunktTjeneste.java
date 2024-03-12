package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningAktiviteterFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningAktiviteterFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningAktiviteterFraVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.GrunnbeløpMapper;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt.SkjæringstidspunktFastsetter;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Vilkårsavslagsårsak;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

public class ForeslåSkjæringstidspunktTjeneste {

    public BeregningsgrunnlagRegelResultat foreslåSkjæringstidspunkt(FastsettBeregningsaktiviteterInput input) {
        var beregningAktivitetAggregat = fastsettAktiviteter(input);
        if (beregningAktivitetAggregat.getBeregningAktiviteter().isEmpty() && !input.getOpptjeningAktiviteter().erMidlertidigInaktiv()) {
            // Avslår vilkår
            var beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(beregningAktivitetAggregat);
            beregningsgrunnlagRegelResultat.setVilkårsresultat(List.of(new BeregningVilkårResultat(false, Vilkårsavslagsårsak.FOR_LAVT_BG, Intervall.fraOgMed(input.getSkjæringstidspunktOpptjening()))));
            return beregningsgrunnlagRegelResultat;
        }
        var beregningsgrunnlagRegelResultat = SkjæringstidspunktFastsetter.utledFastsettSkjæringstidspunktTjeneste(input.getFagsakYtelseType())
                .fastsettSkjæringstidspunktOgStatuser(input, beregningAktivitetAggregat, GrunnbeløpMapper.mapGrunnbeløpInput(input.getGrunnbeløpInput()));
        beregningsgrunnlagRegelResultat.setRegisterAktiviteter(beregningAktivitetAggregat);
        return beregningsgrunnlagRegelResultat;
    }

    static BeregningAktivitetAggregatDto fastsettAktiviteter(FastsettBeregningsaktiviteterInput input) {
        // Oversetter Opptjening -> regelmodell, hvor også skjæringstidspunkt for Opptjening er lagret
        AktivitetStatusModell regelmodell = finnMapper(input.getFagsakYtelseType()).mapForSkjæringstidspunkt(input);
        return MapBeregningAktiviteterFraRegelTilVL.map(regelmodell);
    }

    private static MapBeregningAktiviteterFraVLTilRegel finnMapper(FagsakYtelseType ytelseType) {
        return switch (ytelseType) {
            case PLEIEPENGER_SYKT_BARN, PLEIEPENGER_NÆRSTÅENDE, OMSORGSPENGER, OPPLÆRINGSPENGER, FORELDREPENGER, SVANGERSKAPSPENGER ->
                    new MapBeregningAktiviteterFraVLTilRegelFelles();
            default -> throw new IllegalArgumentException("Finner ikke MapInntektsgrunnlagVLTilRegel implementasjon for ytelse " + ytelseType);
        };
    }

}
