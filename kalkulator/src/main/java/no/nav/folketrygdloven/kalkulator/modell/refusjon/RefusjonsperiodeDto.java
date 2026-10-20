package no.nav.folketrygdloven.kalkulator.modell.refusjon;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;

public record RefusjonsperiodeDto(Intervall periode, Beløp refusjonPrMnd, RefusjonUtfall utfall, RefusjonUtfallÅrsak utfallÅrsak,
        String utfallBegrunnelse) {

    @Override
    public String toString() {
        return "RefusjonsperiodeDto[periode=" + periode + ", utfall=" + utfall + ", utfallÅrsak=" + utfallÅrsak + "]";
    }
}
