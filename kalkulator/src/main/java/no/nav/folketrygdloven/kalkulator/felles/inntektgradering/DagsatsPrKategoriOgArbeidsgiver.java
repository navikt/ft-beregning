package no.nav.folketrygdloven.kalkulator.felles.inntektgradering;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public record DagsatsPrKategoriOgArbeidsgiver(Inntektskategori inntektskategori,
                                              Arbeidsgiver arbeidsgiver,
                                              Beløp dagsats) implements Comparable<DagsatsPrKategoriOgArbeidsgiver> {


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DagsatsPrKategoriOgArbeidsgiver that = (DagsatsPrKategoriOgArbeidsgiver) o;
        return inntektskategori == that.inntektskategori && Objects.equals(arbeidsgiver, that.arbeidsgiver) && Objects.equals(dagsats, that.dagsats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inntektskategori, arbeidsgiver, dagsats);
    }

    @Override
    public int compareTo(DagsatsPrKategoriOgArbeidsgiver o) {
        if (this.inntektskategori == null) {
            return o.inntektskategori == null ? 0 : -1;
        } else if (o.inntektskategori == null) {
            return 1;
        }
        var compareInntektskategori = this.inntektskategori.compareTo(o.inntektskategori);
        if (compareInntektskategori == 0) {
            if (this.arbeidsgiver == null) {
                return o.arbeidsgiver == null ? 0 : -1;
            } else if (o.arbeidsgiver == null) {
                return 1;
            }
            return this.arbeidsgiver.getIdentifikator().compareTo(o.arbeidsgiver.getIdentifikator());
        }
        return compareInntektskategori;
    }
}
