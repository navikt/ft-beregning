package no.nav.folketrygdloven.kalkulator.modell.refusjon;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;

public record Refusjonsperiode(Intervall periode, Beløp refusjonsbeløpPrMnd, RefusjonUtfall utfall, RefusjonUtfallÅrsak utfallÅrsak) { }
