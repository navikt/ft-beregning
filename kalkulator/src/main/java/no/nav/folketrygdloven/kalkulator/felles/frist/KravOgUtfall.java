package no.nav.folketrygdloven.kalkulator.felles.frist;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

public record KravOgUtfall(Beløp refusjonskrav, Utfall utfall) {}
