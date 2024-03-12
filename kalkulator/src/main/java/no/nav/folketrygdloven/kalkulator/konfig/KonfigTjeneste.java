package no.nav.folketrygdloven.kalkulator.konfig;

import java.math.BigDecimal;
import java.time.Period;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public class KonfigTjeneste {

    private static final Map<FagsakYtelseType, BigDecimal> MINSTE_G_MILITÆR_SIVIL = Map.of(
            FagsakYtelseType.FORELDREPENGER, BigDecimal.valueOf(3),
            FagsakYtelseType.FRISINN, BigDecimal.ZERO
    );

    private static final Konfigverdier DEFAULTS = new Konfigverdier();

    private KonfigTjeneste() {
        // Skjuler default
    }


    public static Konfigverdier forYtelse(FagsakYtelseType ytelse) {
        verfisierYtelsetype(ytelse);
        return Optional.ofNullable(MINSTE_G_MILITÆR_SIVIL.get(ytelse))
                .map(Konfigverdier::new).orElseGet(Konfigverdier::new);
    }

    public static Konfigverdier forUtbetalingsgradYtelse() {
        return new Konfigverdier();
    }

    private static void verfisierYtelsetype(FagsakYtelseType ytelse) {
        if (ytelse == null || FagsakYtelseType.UDEFINERT.equals(ytelse)) {
            throw new IllegalStateException("Ytelsetype " + ytelse + " har ingen definerte konfigverdier");
        }
    }

    public static BigDecimal getAvviksgrenseProsent() {
        return DEFAULTS.getAvviksgrenseProsent();
    }

    public static BigDecimal getAntallGØvreGrenseverdi() {
        return DEFAULTS.getAntallGØvreGrenseverdi();
    }

    public static Period getMeldekortPeriode() {
        return DEFAULTS.getMeldekortPeriode();
    }

    public static BigDecimal getYtelsesdagerIÅr() {
        return DEFAULTS.getYtelsesdagerIÅr();
    }

    public static BigDecimal getMånederIÅr() {
        return DEFAULTS.getMånederIÅr();
    }

    public static int getMånederIÅrInt() {
        return DEFAULTS.getMånederIÅrInt();
    }

    public static int getFristMånederEtterRefusjon() {
        return DEFAULTS.getFristMånederEtterRefusjon();
    }
}
