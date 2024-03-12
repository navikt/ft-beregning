package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.naturalytelse;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

class MapNaturalytelser {
    private MapNaturalytelser() {
        // skjul public constructor
    }

    static List<NaturalYtelse> mapNaturalytelser(InntektsmeldingDto im) {
        return im.getNaturalYtelser().stream()
            .map(ny -> new NaturalYtelse(Beløp.safeVerdi(ny.getBeloepPerMnd()), ny.getPeriode().getFomDato(), ny.getPeriode().getTomDato()))
            .collect(Collectors.toList());
    }
}
