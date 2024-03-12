package no.nav.folketrygdloven.kalkulator.steg.fullføre;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.KalkulatorException;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.fastsett.MapFastsattBeregningsgrunnlagFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.mappers.JsonMapper;

public class FullføreBeregningsgrunnlagUtils {

    private static final MapFastsattBeregningsgrunnlagFraRegelTilVL REGEL_TIL_VL = new MapFastsattBeregningsgrunnlagFraRegelTilVL();


    public static BeregningsgrunnlagDto mapBeregningsgrunnlagFraRegelTilVL(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag beregningsgrunnlagRegel,
                                                                           BeregningsgrunnlagDto beregningsgrunnlag) {
        return REGEL_TIL_VL.mapFastsettBeregningsgrunnlag(beregningsgrunnlagRegel, beregningsgrunnlag);
    }

    public static List<RegelSporingPeriode> mapRegelSporinger(List<RegelResultat> regelResultater, BeregningsgrunnlagDto fastsattBeregningsgrunnlag, List<Intervall> forlengelseperioder) {
        var vurdertePerioder = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode)
                .filter(p -> new PerioderTilVurderingTjeneste(forlengelseperioder, fastsattBeregningsgrunnlag).erTilVurdering(p))
                .collect(Collectors.toList());
        return mapRegelsporingPerioder(regelResultater, vurdertePerioder);
    }

    /**
     * Mapper liste med sporing pr periode til regelresultat. Listen med resultater må vere like lang som listen med perioder.
     *
     * @param regelResultater Liste med resultat pr periode
     * @param perioder        perioder som regel er kjørt for
     * @return Mappet regelsporing for perioder
     */
    public static List<RegelSporingPeriode> mapRegelsporingPerioder(List<RegelResultat> regelResultater,
                                                                    List<Intervall> perioder) {
        if (regelResultater.size() != perioder.size()) {
            throw new IllegalArgumentException("Listene må vere like lange.");
        }
        List<RegelSporingPeriode> regelsporingPerioder = new ArrayList<>();
        var resultatIterator = regelResultater.iterator();
        for (var periode : perioder) {
            RegelResultat resultat = resultatIterator.next();
            var hovedRegelResultat = resultat.sporing();
            regelsporingPerioder.add(new RegelSporingPeriode(hovedRegelResultat.sporing(), hovedRegelResultat.input(), periode, BeregningsgrunnlagPeriodeRegelType.FASTSETT, hovedRegelResultat.versjon()));
            Optional.ofNullable(resultat.sporingFinnGrenseverdi())
                    .map(res -> new RegelSporingPeriode(hovedRegelResultat.sporing(), hovedRegelResultat.input(), periode, BeregningsgrunnlagPeriodeRegelType.FINN_GRENSEVERDI, hovedRegelResultat.versjon()))
                    .ifPresent(regelsporingPerioder::add);
        }
        return regelsporingPerioder;
    }

    public static String toJson(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag beregningsgrunnlagRegel) {
        try {
            return JsonMapper.getMapper().writeValueAsString(beregningsgrunnlagRegel);
        } catch (JsonProcessingException e) {
            throw new KalkulatorException("FT-370602", "Kunne ikke serialisere regelinput for beregningsgrunnlag.");
        }
    }

    public static List<RegelResultat> leggTilSporingerForFinnGrenseverdi(String input, List<String> sporingerFinnGrenseverdi, List<RegelResultat> regelResultater) {
        List<RegelResultat> medSporingFinnGrenseverdi = new ArrayList<>();
        if (regelResultater.size() == sporingerFinnGrenseverdi.size()) {
            for (int i = 0; i < regelResultater.size(); i++) {
                RegelResultat res = regelResultater.get(i);
                medSporingFinnGrenseverdi.add(RegelResultat.medRegelsporingFinnGrenseverdi(res, input, sporingerFinnGrenseverdi.get(i)));
            }
            return medSporingFinnGrenseverdi;
        } else {
            throw new IllegalStateException("Utviklerfeil: Antall kjøringer for finn grenseverdi var ulik fastsetting.");
        }
    }
}
