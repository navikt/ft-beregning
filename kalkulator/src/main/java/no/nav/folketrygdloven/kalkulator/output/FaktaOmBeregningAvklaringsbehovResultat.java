package no.nav.folketrygdloven.kalkulator.output;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


public class FaktaOmBeregningAvklaringsbehovResultat {

    public final static FaktaOmBeregningAvklaringsbehovResultat INGEN_AKSJONSPUNKTER = new FaktaOmBeregningAvklaringsbehovResultat();

    private List<BeregningAvklaringsbehovResultat> beregningAvklaringsbehovResultatList = new ArrayList<>();

    private List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = new ArrayList<>();

    private FaktaOmBeregningAvklaringsbehovResultat() { }

    public FaktaOmBeregningAvklaringsbehovResultat(List<BeregningAvklaringsbehovResultat> beregningAvklaringsbehovResultatList, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
        this.beregningAvklaringsbehovResultatList = beregningAvklaringsbehovResultatList;
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
    }


    public List<BeregningAvklaringsbehovResultat> getBeregningAvklaringsbehovResultatList() {
        return beregningAvklaringsbehovResultatList;
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller;
    }

}
