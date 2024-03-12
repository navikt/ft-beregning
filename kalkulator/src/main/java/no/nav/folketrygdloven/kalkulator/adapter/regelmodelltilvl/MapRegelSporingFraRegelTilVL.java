package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingGrunnlag;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;

/**
 * Mapper regelsporinger fra output fra regel til resultat som returnerers fra regeltjenestene
 */
public class MapRegelSporingFraRegelTilVL {

    private MapRegelSporingFraRegelTilVL() {
        // Skjul konstruktør
    }

    /**
     * Mapper liste med sporing pr periode til regelresultat. Listen med resultater må vere like lang som listen med perioder.
     *
     * @param regelResultater Liste med resultat pr periode
     * @param perioder perioder som regel er kjørt for
     * @param regelType regeltype
     * @return Mappet regelsporing for perioder
     */
    public static List<RegelSporingPeriode> mapRegelsporingPerioder(List<RegelResultat> regelResultater,
                                                                    List<Intervall> perioder,
                                                                    BeregningsgrunnlagPeriodeRegelType regelType) {
        if (regelResultater.size() != perioder.size()) {
            throw new IllegalArgumentException("Listene må vere like lange.");
        }
        List<RegelSporingPeriode> regelsporingPerioder = new ArrayList<>();
        var resultatIterator = regelResultater.iterator();
        for (var periode : perioder) {
            RegelResultat resultat = resultatIterator.next();
            var hovedRegelResultat = resultat.sporing();
            regelsporingPerioder.add(new RegelSporingPeriode(hovedRegelResultat.sporing(), hovedRegelResultat.input(), periode, regelType, hovedRegelResultat.versjon()));
        }
        return regelsporingPerioder;
    }

    /**
     * Mapper ett enkelt regelresultat som ikke ligger på grunnlagsnivå (ikke periodenivå)
     *
     * @param regelResultat Liste med resultat pr periode
     * @return Aggregat som innholder regelsporing for perioder
     */
    public static RegelSporingGrunnlag mapRegelSporingGrunnlag(RegelResultat regelResultat, BeregningsgrunnlagRegelType regelType) {
        var regelSporing = regelResultat.sporing();
        return new RegelSporingGrunnlag(regelSporing.sporing(), regelSporing.input(), regelType, regelSporing.versjon());
    }



}
