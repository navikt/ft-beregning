package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

class RefusjonOgFastsattBeløp {

    private Beløp totalRefusjonPrÅr;
    private Beløp totalFastsattBeløpPrÅr = Beløp.ZERO;

    RefusjonOgFastsattBeløp(Beløp totalRefusjonPrÅr, Beløp totalFastsattBeløpPrÅr) {
        this.totalRefusjonPrÅr = totalRefusjonPrÅr;
        this.totalFastsattBeløpPrÅr = totalFastsattBeløpPrÅr;
    }

    RefusjonOgFastsattBeløp(Beløp totalRefusjonPrÅr) {
        this.totalRefusjonPrÅr = totalRefusjonPrÅr;
    }

    Beløp getTotalRefusjonPrÅr() {
        return totalRefusjonPrÅr;
    }

    Beløp getTotalFastsattBeløpPrÅr() {
        return totalFastsattBeløpPrÅr;
    }

    RefusjonOgFastsattBeløp leggTilRefusjon(Beløp refusjon) {
        var nyTotalRefusjon = this.totalRefusjonPrÅr.adder(refusjon);
        return new RefusjonOgFastsattBeløp(nyTotalRefusjon, this.totalFastsattBeløpPrÅr);
    }

    RefusjonOgFastsattBeløp leggTilFastsattBeløp(Beløp fastsattBeløp) {
        var nyttTotalFastsattBeløp = this.totalFastsattBeløpPrÅr.adder(fastsattBeløp);
        return new RefusjonOgFastsattBeløp(this.totalRefusjonPrÅr, nyttTotalFastsattBeløp);
    }
}
