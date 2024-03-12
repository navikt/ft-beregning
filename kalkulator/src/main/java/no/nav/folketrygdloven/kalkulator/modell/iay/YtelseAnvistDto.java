package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class YtelseAnvistDto {

    private Intervall anvistPeriode;
    private Beløp beløp;
    private Beløp dagsats;
    private Stillingsprosent utbetalingsgradProsent;
    private List<AnvistAndel> anvisteAndeler;

    public YtelseAnvistDto() {
        // hibernate
    }

    public YtelseAnvistDto(YtelseAnvistDto ytelseAnvist) {
        this.anvistPeriode = Intervall.fraOgMedTilOgMed(ytelseAnvist.getAnvistFOM(), ytelseAnvist.getAnvistTOM());
        this.beløp = ytelseAnvist.getBeløp().orElse(null);
        this.dagsats = ytelseAnvist.getDagsats().orElse(null);
        this.utbetalingsgradProsent = ytelseAnvist.getUtbetalingsgradProsent().orElse(null);
        this.utbetalingsgradProsent = ytelseAnvist.getUtbetalingsgradProsent().orElse(null);

    }

    public Intervall getAnvistPeriode() {
        return anvistPeriode;
    }

    public LocalDate getAnvistFOM() {
        return anvistPeriode.getFomDato();
    }

    public LocalDate getAnvistTOM() {
        return anvistPeriode.getTomDato();
    }

    public Optional<Stillingsprosent> getUtbetalingsgradProsent() {
        return Optional.ofNullable(utbetalingsgradProsent);
    }

    void setUtbetalingsgradProsent(Stillingsprosent utbetalingsgradProsent) {
        this.utbetalingsgradProsent = utbetalingsgradProsent;
    }

    public Optional<Beløp> getBeløp() {
        return Optional.ofNullable(beløp);
    }

    void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    public Optional<Beløp> getDagsats() {
        return Optional.ofNullable(dagsats);
    }

    void setDagsats(Beløp dagsats) {
        this.dagsats = dagsats;
    }

    void setAnvistPeriode(Intervall periode) {
        this.anvistPeriode = periode;
    }

    public List<AnvistAndel> getAnvisteAndeler() {
        return anvisteAndeler == null ? Collections.emptyList() : anvisteAndeler;
    }

    public void setAnvisteAndeler(List<AnvistAndel> anvisteAndeler) {
        this.anvisteAndeler = anvisteAndeler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof YtelseAnvistDto)) return false;
        YtelseAnvistDto that = (YtelseAnvistDto) o;
        return Objects.equals(anvistPeriode, that.anvistPeriode) &&
                Objects.equals(beløp, that.beløp) &&
                Objects.equals(dagsats, that.dagsats) &&
                Objects.equals(utbetalingsgradProsent, that.utbetalingsgradProsent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anvistPeriode, beløp, dagsats, utbetalingsgradProsent);
    }

    @Override
    public String toString() {
        return "YtelseAnvistEntitet{" +
                "periode=" + anvistPeriode +
                ", beløp=" + beløp +
                ", dagsats=" + dagsats +
                ", utbetalingsgradProsent=" + utbetalingsgradProsent +
                '}';
    }
}
