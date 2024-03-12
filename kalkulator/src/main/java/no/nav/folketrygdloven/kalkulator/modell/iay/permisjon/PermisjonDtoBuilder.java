package no.nav.folketrygdloven.kalkulator.modell.iay.permisjon;

import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;

public class PermisjonDtoBuilder {
    private final PermisjonDto permisjon;

    PermisjonDtoBuilder(PermisjonDto permisjonDto) {
        this.permisjon = permisjonDto; // NOSONAR
    }

    public static PermisjonDtoBuilder ny() {
        return new PermisjonDtoBuilder(new PermisjonDto());
    }


    public PermisjonDtoBuilder medPeriode(Intervall periode) {
        this.permisjon.setPeriode(periode);
        return this;
    }

    public PermisjonDtoBuilder medProsentsats(Stillingsprosent prosentsats) {
        this.permisjon.setProsentsats(prosentsats);
        return this;
    }

    public PermisjonDtoBuilder medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType permisjonsbeskrivelseType) {
        this.permisjon.setPermisjonsbeskrivelseType(permisjonsbeskrivelseType);
        return this;
    }

    public PermisjonDto build() {
        return permisjon;
    }
}
