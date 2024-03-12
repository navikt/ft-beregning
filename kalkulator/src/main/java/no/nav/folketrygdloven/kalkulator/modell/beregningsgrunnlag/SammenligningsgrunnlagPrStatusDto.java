package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.SjekkVedKopiering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;


public class SammenligningsgrunnlagPrStatusDto {

    @SjekkVedKopiering
    private Intervall sammenligningsperiode;
    @SjekkVedKopiering
    private SammenligningsgrunnlagType sammenligningsgrunnlagType;
    @SjekkVedKopiering
    private Beløp rapportertPrÅr;
    @SjekkVedKopiering
    private BigDecimal avvikPromilleNy = BigDecimal.ZERO;

    public SammenligningsgrunnlagPrStatusDto() {
    }

    public SammenligningsgrunnlagPrStatusDto(SammenligningsgrunnlagPrStatusDto fraKopi) {
        this.sammenligningsperiode = fraKopi.sammenligningsperiode;
        this.sammenligningsgrunnlagType = fraKopi.sammenligningsgrunnlagType;
        this.rapportertPrÅr = fraKopi.rapportertPrÅr;
        this.avvikPromilleNy = fraKopi.getAvvikPromilleNy();
    }

    public LocalDate getSammenligningsperiodeFom() {
        return sammenligningsperiode.getFomDato();
    }

    public LocalDate getSammenligningsperiodeTom() {
        return sammenligningsperiode.getTomDato();
    }

    public Beløp getRapportertPrÅr() {
        return rapportertPrÅr;
    }

    public BigDecimal getAvvikPromilleNy() {
        return avvikPromilleNy;
    }

    public SammenligningsgrunnlagType getSammenligningsgrunnlagType() {
        return sammenligningsgrunnlagType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof SammenligningsgrunnlagPrStatusDto)) {
            return false;
        }
        SammenligningsgrunnlagPrStatusDto other = (SammenligningsgrunnlagPrStatusDto) obj;
        return Objects.equals(this.getSammenligningsgrunnlagType(), other.getSammenligningsgrunnlagType())
                && Objects.equals(this.getSammenligningsperiodeFom(), other.getSammenligningsperiodeFom())
                && Objects.equals(this.getSammenligningsperiodeTom(), other.getSammenligningsperiodeTom())
                && Objects.equals(this.getRapportertPrÅr(), other.getRapportertPrÅr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sammenligningsgrunnlagType, sammenligningsperiode, rapportertPrÅr);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "sammenligningsgrunnlagType=" + sammenligningsgrunnlagType + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "sammenligningsperiodeFom=" + sammenligningsperiode.getFomDato() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "sammenligningsperiodeTom=" + sammenligningsperiode.getTomDato() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "rapportertPrÅr=" + rapportertPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlagMal;

        public static Builder kopier(SammenligningsgrunnlagPrStatusDto s) {
            return new Builder(s);
        }

        public Builder(SammenligningsgrunnlagPrStatusDto s) {
            sammenligningsgrunnlagMal = new SammenligningsgrunnlagPrStatusDto(s);
        }

        public Builder() {
            sammenligningsgrunnlagMal = new SammenligningsgrunnlagPrStatusDto();
        }

        public Builder medSammenligningsgrunnlagType(SammenligningsgrunnlagType sammenligningsgrunnlagType) {
            sammenligningsgrunnlagMal.sammenligningsgrunnlagType = sammenligningsgrunnlagType;
            return this;
        }

        public Builder medSammenligningsperiode(LocalDate fom, LocalDate tom) {
            sammenligningsgrunnlagMal.sammenligningsperiode = Intervall.fraOgMedTilOgMed(fom, tom);
            return this;
        }

        public Builder medRapportertPrÅr(Beløp rapportertPrÅr) {
            sammenligningsgrunnlagMal.rapportertPrÅr = rapportertPrÅr;
            return this;
        }

        public Builder medAvvikPromilleNy(BigDecimal avvikPromilleUtenAvrunding) {
            if(avvikPromilleUtenAvrunding != null) {
                sammenligningsgrunnlagMal.avvikPromilleNy = avvikPromilleUtenAvrunding;
            }
            return this;
        }

        public SammenligningsgrunnlagPrStatusDto build() {
            verifyStateForBuild();
            return sammenligningsgrunnlagMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(sammenligningsgrunnlagMal.sammenligningsgrunnlagType, "sammenligningsgrunnlagType");
            Objects.requireNonNull(sammenligningsgrunnlagMal.sammenligningsperiode, "sammenligningsperiodePeriode");
            Objects.requireNonNull(sammenligningsgrunnlagMal.sammenligningsperiode.getFomDato(), "sammenligningsperiodeFom");
            Objects.requireNonNull(sammenligningsgrunnlagMal.sammenligningsperiode.getTomDato(), "sammenligningsperiodeTom");
            Objects.requireNonNull(sammenligningsgrunnlagMal.rapportertPrÅr, "rapportertPrÅr");
        }

    }

}
