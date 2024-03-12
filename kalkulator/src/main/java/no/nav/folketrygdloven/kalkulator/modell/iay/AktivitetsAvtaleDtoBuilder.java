package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class AktivitetsAvtaleDtoBuilder {
    private final AktivitetsAvtaleDto aktivitetsAvtale;
    private boolean oppdatering;

    AktivitetsAvtaleDtoBuilder(AktivitetsAvtaleDto aktivitetsAvtaleEntitet, boolean oppdatering) {
        this.aktivitetsAvtale = aktivitetsAvtaleEntitet; // NOSONAR
        this.oppdatering = oppdatering;
    }

    public static AktivitetsAvtaleDtoBuilder ny() {
        return new AktivitetsAvtaleDtoBuilder(new AktivitetsAvtaleDto(), false);
    }

    static AktivitetsAvtaleDtoBuilder oppdater(Optional<AktivitetsAvtaleDto> aktivitetsAvtale) {
        return new AktivitetsAvtaleDtoBuilder(aktivitetsAvtale.orElse(new AktivitetsAvtaleDto()), aktivitetsAvtale.isPresent());
    }

    public AktivitetsAvtaleDtoBuilder medPeriode(Intervall periode) {
        this.aktivitetsAvtale.setPeriode(periode);
        return this;
    }

    public AktivitetsAvtaleDtoBuilder medErAnsettelsesPeriode(boolean erAnsettelsesPeriode) {
        this.aktivitetsAvtale.setErAnsettelsesPeriode(erAnsettelsesPeriode);
        return this;
    }


    public AktivitetsAvtaleDto build() {
        if (aktivitetsAvtale.hasValues()) {
            return aktivitetsAvtale;
        }
        throw new IllegalStateException();
    }

    public boolean isOppdatering() {
        return oppdatering;
    }

    public AktivitetsAvtaleDtoBuilder medSisteLønnsendringsdato(LocalDate sisteLønnsendringsdato) {
        this.aktivitetsAvtale.sisteLønnsendringsdato(sisteLønnsendringsdato);
        this.aktivitetsAvtale.setErAnsettelsesPeriode(false);
        return this;
    }
}
