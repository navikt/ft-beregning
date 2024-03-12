package no.nav.folketrygdloven.kalkulator.modell.iay.permisjon;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;

public class PermisjonDto {


    private Intervall periode;

    private Stillingsprosent prosentsats;

    private PermisjonsbeskrivelseType permisjonsbeskrivelseType;

    PermisjonDto(){
        // Skjul default constructor
    }

    public PermisjonDto(Intervall periode,
                        Stillingsprosent prosentsats,
                        PermisjonsbeskrivelseType permisjonsbeskrivelseType) {
        this.periode = periode;
        this.prosentsats = prosentsats;
        this.permisjonsbeskrivelseType = permisjonsbeskrivelseType;
    }

    public PermisjonDto(PermisjonDto p) {
        this.periode = p.periode;
        this.permisjonsbeskrivelseType = p.permisjonsbeskrivelseType;
        this.prosentsats = p.prosentsats;
    }

    public Intervall getPeriode() {
        return periode;
    }

    public void setPeriode(Intervall periode) {
        this.periode = periode;
    }

    public Stillingsprosent getProsentsats() {
        return prosentsats;
    }

    public void setProsentsats(Stillingsprosent prosentsats) {
        this.prosentsats = prosentsats;
    }

    public PermisjonsbeskrivelseType getPermisjonsbeskrivelseType() {
        return permisjonsbeskrivelseType;
    }

    public void setPermisjonsbeskrivelseType(PermisjonsbeskrivelseType permisjonsbeskrivelseType) {
        this.permisjonsbeskrivelseType = permisjonsbeskrivelseType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermisjonDto that = (PermisjonDto) o;
        return Objects.equals(periode, that.periode)
                && Objects.equals(prosentsats, that.prosentsats)
                && Objects.equals(permisjonsbeskrivelseType, that.permisjonsbeskrivelseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, prosentsats, permisjonsbeskrivelseType);
    }

}
