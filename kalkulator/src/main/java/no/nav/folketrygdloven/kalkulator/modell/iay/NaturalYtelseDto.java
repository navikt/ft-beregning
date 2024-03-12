package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.NaturalYtelseType;


public class NaturalYtelseDto {

    private Intervall periode;
    private Beløp beloepPerMnd;

    private NaturalYtelseType type = NaturalYtelseType.UDEFINERT;

    NaturalYtelseDto() {
    }

    public NaturalYtelseDto(LocalDate fom, LocalDate tom, Beløp beloepPerMnd, NaturalYtelseType type) {
        this(Intervall.fraOgMedTilOgMed(fom, tom), beloepPerMnd, type);
        this.type = type;
        this.periode = Intervall.fraOgMedTilOgMed(fom, tom);
    }

    public NaturalYtelseDto(Intervall datoIntervall, Beløp beloepPerMnd, NaturalYtelseType type) {
        this.beloepPerMnd = beloepPerMnd;
        this.type = type;
        this.periode = datoIntervall;
    }

    NaturalYtelseDto(NaturalYtelseDto naturalYtelse) {
        this.periode = naturalYtelse.getPeriode();
        this.beloepPerMnd = naturalYtelse.getBeloepPerMnd();
        this.type = naturalYtelse.getType();
    }

    public Intervall getPeriode() {
        return periode;
    }

    public Beløp getBeloepPerMnd() {
        return beloepPerMnd;
    }

    public NaturalYtelseType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof NaturalYtelseDto))
            return false;
        NaturalYtelseDto that = (NaturalYtelseDto) o;
        return Objects.equals(periode, that.periode) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, type);
    }

    @Override
    public String toString() {
        return "NaturalYtelseEntitet{" +
                "periode=" + periode +
                ", beloepPerMnd=" + beloepPerMnd +
                ", type=" + type +
                '}';
    }
}
