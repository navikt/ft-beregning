package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.math.RoundingMode;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittPeriodeInntekt;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.tid.Virkedager;

public final class EffektivÅrsinntektTjenesteFRISINN {

    private EffektivÅrsinntektTjenesteFRISINN() {
        // SKjuler default
    }

    /**
     * Finner effektiv årsinntekt fra oppgitt inntekt
     *
     * @param oppgittInntekt oppgitt inntektsinformasjon
     * @return effektiv årsinntekt fra inntekt
     */
    public static Beløp finnEffektivÅrsinntektForLøpenedeInntekt(OppgittPeriodeInntekt oppgittInntekt) {
        var dagsats = finnEffektivDagsatsIPeriode(oppgittInntekt);
        return dagsats.multipliser(KonfigTjeneste.getYtelsesdagerIÅr());
    }

    /**
     * Finner opptjent inntekt pr dag i periode
     *
     * @param oppgittInntekt Informasjon om oppgitt inntekt
     * @return dagsats i periode
     */
    private static Beløp finnEffektivDagsatsIPeriode(OppgittPeriodeInntekt oppgittInntekt) {
        Intervall periode = oppgittInntekt.getPeriode();
        var dagerIRapportertPeriode = Virkedager.beregnAntallVirkedagerEllerKunHelg(periode.getFomDato(), periode.getTomDato());
        if (Beløp.safeVerdi(oppgittInntekt.getInntekt()) == null) {
            return Beløp.ZERO;
        }
        return oppgittInntekt.getInntekt().divider(dagerIRapportertPeriode, 10, RoundingMode.HALF_EVEN);
    }
}
