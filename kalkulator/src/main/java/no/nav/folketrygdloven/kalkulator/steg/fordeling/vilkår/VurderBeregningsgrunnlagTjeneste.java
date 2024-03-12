package no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Vilkårsavslagsårsak;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

public class VurderBeregningsgrunnlagTjeneste {

    private static final Set<BeregningUtfallÅrsak> AVSLAGSÅRSAKER = Set.of(
            BeregningUtfallÅrsak.AVSLAG_UNDER_HALV_G,
            BeregningUtfallÅrsak.AVSLAG_UNDER_EN_G,
            BeregningUtfallÅrsak.AVSLAG_UNDER_TREKVART_G,
            BeregningUtfallÅrsak.FRISINN_FRILANS_UTEN_INNTEKT);

    private static final MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel();

    public VurderBeregningsgrunnlagTjeneste() {
    }

    public BeregningsgrunnlagRegelResultat vurderBeregningsgrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDto oppdatertGrunnlag) {
        // Oversetter foreslått Beregningsgrunnlag -> regelmodell
        var beregningsgrunnlagRegel = mapBeregningsgrunnlagFraVLTilRegel.map(input, oppdatertGrunnlag);
        List<RegelResultat> regelResultater = kjørRegel(input, beregningsgrunnlagRegel);
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = Collections.emptyList();
        BeregningsgrunnlagDto beregningsgrunnlag = oppdatertGrunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();
        return mapTilRegelresultat(input, regelResultater, beregningsgrunnlag, avklaringsbehov);
    }

    private BeregningsgrunnlagRegelResultat mapTilRegelresultat(BeregningsgrunnlagInput input, List<RegelResultat> regelResultater,
                                                                  BeregningsgrunnlagDto beregningsgrunnlag,
                                                                  List<BeregningAvklaringsbehovResultat> avklaringsbehov) {
        List<Intervall> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode).collect(Collectors.toList());
        List<RegelSporingPeriode> regelsporinger = MapRegelSporingFraRegelTilVL.mapRegelsporingPerioder(regelResultater, perioder, BeregningsgrunnlagPeriodeRegelType.VILKÅR_VURDERING);
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = new BeregningsgrunnlagRegelResultat(
                beregningsgrunnlag,
                avklaringsbehov,
                new RegelSporingAggregat(regelsporinger));
        beregningsgrunnlagRegelResultat.setVilkårsresultat(mapTilVilkårResultatListe(regelResultater, beregningsgrunnlag, input.getYtelsespesifiktGrunnlag()));
        return beregningsgrunnlagRegelResultat;
    }

    private List<BeregningVilkårResultat> mapTilVilkårResultatListe(List<RegelResultat> regelResultater,
                                                                      BeregningsgrunnlagDto beregningsgrunnlag,
                                                                      YtelsespesifiktGrunnlag ytelsesSpesifiktGrunnlag) {
        List<BeregningVilkårResultat> vilkårsResultatListe = new ArrayList<>();
        Iterator<RegelResultat> regelResultatIterator = regelResultater.iterator();
        for (var periode : beregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            BeregningVilkårResultat vilkårResultat = lagVilkårResultatForPeriode(regelResultatIterator.next(), periode.getPeriode());
            vilkårsResultatListe.add(vilkårResultat);
        }
        return vilkårsResultatListe;
    }

    private BeregningVilkårResultat lagVilkårResultatForPeriode(RegelResultat regelResultat, Intervall periode) {
        Optional<BeregningUtfallÅrsak> utfallÅrsak = regelResultat.merknader().stream().map(RegelMerknad::utfallÅrsak).filter(AVSLAGSÅRSAKER::contains).findFirst();
        boolean erVilkårOppfylt = utfallÅrsak.isEmpty();
        if (erVilkårOppfylt){
            return new BeregningVilkårResultat(true, null, periode);
        } else {
            Vilkårsavslagsårsak avslagsårsak = utfallÅrsak.get() == BeregningUtfallÅrsak.AVSLAG_UNDER_EN_G
                    ? Vilkårsavslagsårsak.FOR_LAVT_BG_8_47
                    : Vilkårsavslagsårsak.FOR_LAVT_BG;
            return new BeregningVilkårResultat(false, avslagsårsak, periode);
        }
    }

    private List<RegelResultat> kjørRegel(BeregningsgrunnlagInput input, Beregningsgrunnlag beregningsgrunnlagRegel) {
        // Evaluerer hver BeregningsgrunnlagPeriode fra foreslått Beregningsgrunnlag
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            regelResultater.add(KalkulusRegler.vurderBeregningsgrunnlag(periode));
        }
        return regelResultater;
    }

}
