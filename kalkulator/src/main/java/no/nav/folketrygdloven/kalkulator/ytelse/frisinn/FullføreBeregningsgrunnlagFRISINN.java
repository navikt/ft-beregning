package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.fastsett.MapFullføreBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.steg.BeregningsgrunnlagVerifiserer;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.FullføreBeregningsgrunnlagUtils;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

public class FullføreBeregningsgrunnlagFRISINN {

    private final VurderBeregningsgrunnlagTjenesteFRISINN vurderBeregningsgrunnlagTjeneste = new VurderBeregningsgrunnlagTjenesteFRISINN();

    public FullføreBeregningsgrunnlagFRISINN() {
        super();
    }

    public BeregningsgrunnlagRegelResultat fullføreBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();

        // Oversetter foreslått Beregningsgrunnlag -> regelmodell
        var beregningsgrunnlagRegel = new MapFullføreBeregningsgrunnlagFraVLTilRegel().map(input, grunnlag.getBeregningsgrunnlagHvisFinnes().orElse(null));

        // Evaluerer hver BeregningsgrunnlagPeriode fra foreslått Beregningsgrunnlag
        var regelResultater = evaluerRegelmodell(beregningsgrunnlagRegel, input);

        // Oversett endelig resultat av regelmodell til fastsatt Beregningsgrunnlag  (+ spore input -> evaluation)
        var beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes().orElse(null);
        var fastsattBeregningsgrunnlag = FullføreBeregningsgrunnlagUtils.mapBeregningsgrunnlagFraRegelTilVL(beregningsgrunnlagRegel, beregningsgrunnlag);

        var regelsporinger = mapRegelSporinger(regelResultater, fastsattBeregningsgrunnlag, input.getForlengelseperioder());
        BeregningsgrunnlagVerifiserer.verifiserFastsattBeregningsgrunnlag(fastsattBeregningsgrunnlag, input.getYtelsespesifiktGrunnlag(), input.getForlengelseperioder());
        return new BeregningsgrunnlagRegelResultat(fastsattBeregningsgrunnlag, new RegelSporingAggregat(regelsporinger));
    }

    private List<RegelSporingPeriode> mapRegelSporinger(List<RegelResultat> regelResultater, BeregningsgrunnlagDto fastsattBeregningsgrunnlag, List<Intervall> forlengelseperioder) {
        var vurdertePerioder = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode)
                .filter(p -> new PerioderTilVurderingTjeneste(forlengelseperioder, fastsattBeregningsgrunnlag).erTilVurdering(p))
                .collect(Collectors.toList());
        return FullføreBeregningsgrunnlagUtils.mapRegelsporingPerioder(regelResultater, vurdertePerioder);
    }


    protected List<RegelResultat> evaluerRegelmodell(Beregningsgrunnlag beregningsgrunnlagRegel, BeregningsgrunnlagInput bgInput) {
        var input = FullføreBeregningsgrunnlagUtils.toJson(beregningsgrunnlagRegel);

        // Vurdere vilkår
        var beregningVilkårResultatListe = vurderVilkår(bgInput);

        FrisinnGrunnlag frisinnGrunnlag = bgInput.getYtelsespesifiktGrunnlag();
        // Finner grenseverdi
        var sporingerFinnGrenseverdi = finnGrenseverdi(beregningsgrunnlagRegel, beregningVilkårResultatListe, frisinnGrunnlag);

        // Fullfører grenseverdi
        var inputNrTo = FullføreBeregningsgrunnlagUtils.toJson(beregningsgrunnlagRegel);
        var regelResultater = kjørRegelFullførberegningsgrunnlag(beregningsgrunnlagRegel);

        FullføreBeregningsgrunnlagUtils.leggTilSporingerForFinnGrenseverdi(input, sporingerFinnGrenseverdi, regelResultater);

        return regelResultater;
    }

    private List<BeregningVilkårResultat> vurderVilkår(BeregningsgrunnlagInput bgInput) {
        var beregningsgrunnlagRegelResultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(bgInput, bgInput.getBeregningsgrunnlagGrunnlag());
        return beregningsgrunnlagRegelResultat.getVilkårsresultat();
    }

    private List<RegelResultat> kjørRegelFullførberegningsgrunnlag(Beregningsgrunnlag beregningsgrunnlagRegel) {
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (var periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            regelResultater.add(KalkulusRegler.fullføreBeregningsgrunnlagFRISINN(periode));
        }
        return regelResultater;
    }

    private List<String> finnGrenseverdi(Beregningsgrunnlag beregningsgrunnlagRegel, List<BeregningVilkårResultat> beregningVilkårResultatListe, FrisinnGrunnlag frisinnGrunnlag) {
        var søknadsperioder = FinnSøknadsperioder.finnSøknadsperioder(frisinnGrunnlag);
        return beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().stream()
                .map(periode -> {
                    var erVilkårOppfylt = erVilkårOppfyltForSøknadsperiode(beregningVilkårResultatListe, søknadsperioder, periode);
                    BeregningsgrunnlagPeriode.oppdater(periode).medErVilkårOppfylt(erVilkårOppfylt);
                    return KalkulusRegler.finnGrenseverdiFRISINN(periode).sporing().sporing();
                }).collect(Collectors.toList());
    }

    private Boolean erVilkårOppfyltForSøknadsperiode(List<BeregningVilkårResultat> beregningVilkårResultatListe, List<Intervall> søknadsperioder, BeregningsgrunnlagPeriode periode) {
        var søknadsperiode = søknadsperioder.stream().filter(p -> p.inkluderer(periode.getPeriodeFom())).findFirst();
        var vilkårResultat = søknadsperiode.map(p -> finnVilkårResultatForPeriode(beregningVilkårResultatListe, p)).orElse(List.of());
        return vilkårResultat.stream().anyMatch(BeregningVilkårResultat::getErVilkårOppfylt);
    }

    private List<BeregningVilkårResultat> finnVilkårResultatForPeriode(List<BeregningVilkårResultat> beregningVilkårResultatListe, Intervall periode) {
        return beregningVilkårResultatListe.stream().filter(vp -> periode.overlapper(vp.getPeriode()))
                .collect(Collectors.toList());
    }

}


