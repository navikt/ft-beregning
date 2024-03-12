package no.nav.folketrygdloven.kalkulator.konfig;

import java.math.BigDecimal;
import java.time.Period;
import java.util.Objects;

public record Konfigverdier(BigDecimal antallGMilitærHarKravPå) {

    public Konfigverdier() {
        this(STANDARD_MINSTE_G_MILITÆR_SIVIL);
    }

    public Konfigverdier {
        Objects.requireNonNull(antallGMilitærHarKravPå);
    }

    // Standardverdier
    private static final int FRIST_MÅNEDER_ETTER_REFUSJON = 3;
    private static final BigDecimal AVVIKSGRENSE_PROSENT = BigDecimal.valueOf(25);
    private static final BigDecimal ANTALL_G_ØVRE_GRENSEVERDI = BigDecimal.valueOf(6);
    private static final Period MELDEKORT_PERIODE = Period.parse("P30D");
    private static final BigDecimal YTELSESDAGER_I_ÅR = BigDecimal.valueOf(260);
    private static final int MÅNEDER_I_ÅR_INT = 12;
    private static final BigDecimal MÅNEDER_I_ÅR = BigDecimal.valueOf(MÅNEDER_I_ÅR_INT);
    private static final BigDecimal STANDARD_MINSTE_G_MILITÆR_SIVIL = BigDecimal.valueOf(2);

    public BigDecimal getAvviksgrenseProsent() {
        return Konfigverdier.AVVIKSGRENSE_PROSENT;
    }

    public BigDecimal getAntallGØvreGrenseverdi() {
        return Konfigverdier.ANTALL_G_ØVRE_GRENSEVERDI;
    }

    public Period getMeldekortPeriode() {
        return Konfigverdier.MELDEKORT_PERIODE;
    }

    public BigDecimal getYtelsesdagerIÅr() {
        return Konfigverdier.YTELSESDAGER_I_ÅR;
    }

    public BigDecimal getMånederIÅr() {
        return Konfigverdier.MÅNEDER_I_ÅR;
    }

    public int getMånederIÅrInt() {
        return Konfigverdier.MÅNEDER_I_ÅR_INT;
    }

    public int getFristMånederEtterRefusjon() {
        return Konfigverdier.FRIST_MÅNEDER_ETTER_REFUSJON;
    }

}
