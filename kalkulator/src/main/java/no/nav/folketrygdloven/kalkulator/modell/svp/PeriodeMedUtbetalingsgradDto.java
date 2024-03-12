package no.nav.folketrygdloven.kalkulator.modell.svp;

import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class PeriodeMedUtbetalingsgradDto implements Comparable<PeriodeMedUtbetalingsgradDto> {
    private Intervall periode;
    private Utbetalingsgrad utbetalingsgrad;
    private Aktivitetsgrad aktivitetsgrad;

    public PeriodeMedUtbetalingsgradDto(Intervall periode, Utbetalingsgrad utbetalingsgrad) {
        this.periode = periode;
        this.utbetalingsgrad = utbetalingsgrad;
    }

    public PeriodeMedUtbetalingsgradDto(Intervall periode, Utbetalingsgrad utbetalingsgrad, Aktivitetsgrad aktivitetsgrad) {
        this.periode = periode;
        this.utbetalingsgrad = utbetalingsgrad;
        this.aktivitetsgrad = aktivitetsgrad;
    }

    public Intervall getPeriode() {
        return periode;
    }

    public Utbetalingsgrad getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public Optional<Aktivitetsgrad> getAktivitetsgrad() {
        return Optional.ofNullable(aktivitetsgrad);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeriodeMedUtbetalingsgradDto that = (PeriodeMedUtbetalingsgradDto) o;
        return Objects.equals(periode, that.periode) &&
                Objects.equals(utbetalingsgrad, that.utbetalingsgrad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, utbetalingsgrad);
    }


    @Override
    public int compareTo(PeriodeMedUtbetalingsgradDto periode) {
        return this.getPeriode().compareTo(periode.getPeriode());
    }
}

