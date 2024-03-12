package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.io.Serializable;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.SjekkVedKopiering;

/**
 * Årsgrunnlag representerer inntektsgrunnlaget for en andel
 */
public class Årsgrunnlag implements Serializable, Comparable<Årsgrunnlag> {

    @SjekkVedKopiering
    private Beløp beregnetPrÅr;
    @SjekkVedKopiering
    private Beløp fordeltPrÅr;
    @SjekkVedKopiering
    private Beløp manueltFordeltPrÅr;
    @SjekkVedKopiering
    private Beløp overstyrtPrÅr;
    @SjekkVedKopiering
    private Beløp besteberegningPrÅr;
    @SjekkVedKopiering
    private Beløp bruttoPrÅr;

    public Årsgrunnlag() {
    }

    public Årsgrunnlag(Årsgrunnlag årsgrunnlag) {
        this.bruttoPrÅr = årsgrunnlag.bruttoPrÅr;
        this.fordeltPrÅr = årsgrunnlag.fordeltPrÅr;
        this.manueltFordeltPrÅr = årsgrunnlag.manueltFordeltPrÅr;
        this.overstyrtPrÅr = årsgrunnlag.overstyrtPrÅr;
        this.besteberegningPrÅr = årsgrunnlag.besteberegningPrÅr;
        this.beregnetPrÅr = årsgrunnlag.beregnetPrÅr;
    }

    public Beløp getBeregnetPrÅr() {
        return beregnetPrÅr;
    }

    public Beløp getFordeltPrÅr() {
        return fordeltPrÅr;
    }

    public Beløp getManueltFordeltPrÅr() {
        return manueltFordeltPrÅr;
    }

    public Beløp getOverstyrtPrÅr() {
        return overstyrtPrÅr;
    }

    public Beløp getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public Beløp getBruttoUtenManueltFordelt() {
        if (fordeltPrÅr != null) {
            return fordeltPrÅr;
        }
        if (besteberegningPrÅr != null) {
            return besteberegningPrÅr;
        }
        if (overstyrtPrÅr != null) {
            return overstyrtPrÅr;
        }
        return beregnetPrÅr;
    }

    public Beløp getBruttoUtenFordelt() {
        if (besteberegningPrÅr != null) {
            return besteberegningPrÅr;
        }
        if (overstyrtPrÅr != null) {
            return overstyrtPrÅr;
        }
        return beregnetPrÅr;
    }

    public Beløp getBesteberegningPrÅr() {
        return besteberegningPrÅr;
    }

    public void setBeregnetPrÅr(Beløp beregnetPrÅr) {
        if (beregnetPrÅr != null && fordeltPrÅr == null && overstyrtPrÅr == null && besteberegningPrÅr == null && manueltFordeltPrÅr == null) {
            bruttoPrÅr = beregnetPrÅr;
        }
        this.beregnetPrÅr = beregnetPrÅr;
    }

    public void setFordeltPrÅr(Beløp fordeltPrÅr) {
        this.fordeltPrÅr = fordeltPrÅr;
        if (fordeltPrÅr != null && manueltFordeltPrÅr == null) {
            this.bruttoPrÅr = fordeltPrÅr;
        }
    }

    public void setManueltFordeltPrÅr(Beløp manueltFordeltPrÅr) {
        this.manueltFordeltPrÅr = manueltFordeltPrÅr;
        if (manueltFordeltPrÅr != null) {
            this.bruttoPrÅr = manueltFordeltPrÅr;
        }
    }

    public void setOverstyrtPrÅr(Beløp overstyrtPrÅr) {
        this.overstyrtPrÅr = overstyrtPrÅr;
        if (overstyrtPrÅr != null && fordeltPrÅr == null && manueltFordeltPrÅr == null && besteberegningPrÅr == null) {
            bruttoPrÅr = overstyrtPrÅr;
        }
    }

    public void setBesteberegningPrÅr(Beløp besteberegningPrÅr) {
        this.besteberegningPrÅr = besteberegningPrÅr;
        if (besteberegningPrÅr != null && fordeltPrÅr == null && manueltFordeltPrÅr == null) {
            bruttoPrÅr = besteberegningPrÅr;
        }
    }

    public boolean erSatt() {
        return beregnetPrÅr != null || overstyrtPrÅr != null || fordeltPrÅr != null || bruttoPrÅr != null || manueltFordeltPrÅr != null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Årsgrunnlag that = (Årsgrunnlag) o;
        return Objects.equals(beregnetPrÅr, that.beregnetPrÅr) && Objects.equals(fordeltPrÅr, that.fordeltPrÅr) && Objects.equals(manueltFordeltPrÅr, that.manueltFordeltPrÅr) && Objects.equals(overstyrtPrÅr, that.overstyrtPrÅr) && Objects.equals(bruttoPrÅr, that.bruttoPrÅr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregnetPrÅr, fordeltPrÅr, manueltFordeltPrÅr, overstyrtPrÅr, bruttoPrÅr);
    }

    @Override
    public String toString() {
        return "Årsgrunnlag{" +
                "beregnetPrÅr=" + beregnetPrÅr +
                ", fordeltPrÅr=" + fordeltPrÅr +
                ", manueltFordeltPrÅr=" + manueltFordeltPrÅr +
                ", overstyrtPrÅr=" + overstyrtPrÅr +
                ", bruttoPrÅr=" + bruttoPrÅr +
                '}';
    }

    @Override
    public int compareTo(Årsgrunnlag o) {
        if (!erSatt() || !o.erSatt()) {
            if (erSatt() == o.erSatt()) {
                return 0;
            }
            return erSatt() ? 1 : -1;
        }
        return getBruttoPrÅr().compareTo(o.getBruttoPrÅr());
    }
}
