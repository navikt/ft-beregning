package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.SjekkVedKopiering;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

/**
 * FastsattInntektskategori representerer inntektskategorien for en andel
 */
public class FastsattInntektskategori implements Comparable<FastsattInntektskategori> {


    @SjekkVedKopiering
    private Inntektskategori inntektskategori = Inntektskategori.UDEFINERT;
    @SjekkVedKopiering
    private Inntektskategori inntektskategoriAutomatiskFordeling;
    @SjekkVedKopiering
    private Inntektskategori inntektskategoriManuellFordeling;



    public FastsattInntektskategori() {
    }


    public FastsattInntektskategori(Inntektskategori inntektskategori, Inntektskategori inntektskategoriAutomatiskFordeling, Inntektskategori inntektskategoriManuellFordeling) {
        this.inntektskategori = inntektskategori;
        this.inntektskategoriAutomatiskFordeling = inntektskategoriAutomatiskFordeling;
        this.inntektskategoriManuellFordeling = inntektskategoriManuellFordeling;
    }

    public FastsattInntektskategori(FastsattInntektskategori fastsattInntektskategori) {
        this.inntektskategori = fastsattInntektskategori.getInntektskategori();
        this.inntektskategoriAutomatiskFordeling = fastsattInntektskategori.getInntektskategoriAutomatiskFordeling();
        this.inntektskategoriManuellFordeling = fastsattInntektskategori.getInntektskategoriManuellFordeling();
    }


    public Inntektskategori getInntektskategori() {
        return inntektskategori == null ? Inntektskategori.UDEFINERT : inntektskategori;
    }

    public Inntektskategori getInntektskategoriAutomatiskFordeling() {
        return inntektskategoriAutomatiskFordeling;
    }

    public Inntektskategori getInntektskategoriManuellFordeling() {
        return inntektskategoriManuellFordeling;
    }

    // Rekkefølge er viktig
    public Inntektskategori getGjeldendeInntektskategori() {
        if (inntektskategoriManuellFordeling != null) {
            return inntektskategoriManuellFordeling;
        } else if (inntektskategoriAutomatiskFordeling != null) {
            return inntektskategoriAutomatiskFordeling;
        }
        return inntektskategori;
    }

    public void setInntektskategori(Inntektskategori inntektskategori) {
        this.inntektskategori = inntektskategori;
    }

    public void setInntektskategoriAutomatiskFordeling(Inntektskategori inntektskategoriAutomatiskFordeling) {
        this.inntektskategoriAutomatiskFordeling = inntektskategoriAutomatiskFordeling;
    }

    public void setInntektskategoriManuellFordeling(Inntektskategori inntektskategoriManuellFordeling) {
        this.inntektskategoriManuellFordeling = inntektskategoriManuellFordeling;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FastsattInntektskategori that = (FastsattInntektskategori) o;
        return inntektskategori == that.inntektskategori &&
                inntektskategoriAutomatiskFordeling == that.inntektskategoriAutomatiskFordeling &&
                inntektskategoriManuellFordeling == that.inntektskategoriManuellFordeling;
    }

    @Override
    public int hashCode() {
        return Objects.hash(inntektskategori, inntektskategoriAutomatiskFordeling, inntektskategoriManuellFordeling);
    }

    @Override
    public String toString() {
        return "FastsattInntektskategori{" +
                "inntektskategori=" + inntektskategori +
                ", inntektskategoriFordeling=" + inntektskategoriAutomatiskFordeling +
                ", inntektskategoriManuellFordeling=" + inntektskategoriManuellFordeling +
                '}';
    }

    @Override
    public int compareTo(FastsattInntektskategori o) {
        return getGjeldendeInntektskategori().compareTo(o.getGjeldendeInntektskategori());
    }
}
