package no.nav.folketrygdloven.kalkulator.output;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Vilkårsavslagsårsak;

public class BeregningVilkårResultat {

    private final boolean erVilkårOppfylt;
    private Vilkårsavslagsårsak vilkårsavslagsårsak;
    private Intervall periode;

    public BeregningVilkårResultat(boolean erVilkårOppfylt, Vilkårsavslagsårsak vilkårsavslagsårsak, Intervall periode) {
        this.erVilkårOppfylt = erVilkårOppfylt;
        this.vilkårsavslagsårsak = vilkårsavslagsårsak;
        this.periode = periode;
    }

    public BeregningVilkårResultat(boolean erVilkårOppfylt, Intervall periode) {
        this.erVilkårOppfylt = erVilkårOppfylt;
        this.periode = periode;
    }

    public boolean getErVilkårOppfylt() {
        return erVilkårOppfylt;
    }

    public Vilkårsavslagsårsak getVilkårsavslagsårsak() {
        return vilkårsavslagsårsak;
    }

    public Intervall getPeriode() {
        return periode;
    }
}
