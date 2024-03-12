package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.DiffIgnore;
import no.nav.folketrygdloven.kalkulator.modell.diff.SjekkVedKopiering;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;


public class BeregningsgrunnlagAktivitetStatusDto {

    @DiffIgnore
    private BeregningsgrunnlagDto beregningsgrunnlag;
    @SjekkVedKopiering
    private AktivitetStatus aktivitetStatus;
    private Hjemmel hjemmel;

    public BeregningsgrunnlagAktivitetStatusDto() {
    }

    public BeregningsgrunnlagAktivitetStatusDto(BeregningsgrunnlagAktivitetStatusDto o) {
        this.aktivitetStatus = o.aktivitetStatus;
        this.hjemmel = o.hjemmel;
    }


    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Hjemmel getHjemmel() {
        return hjemmel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagAktivitetStatusDto)) {
            return false;
        }
        BeregningsgrunnlagAktivitetStatusDto other = (BeregningsgrunnlagAktivitetStatusDto) obj;
        return Objects.equals(this.getAktivitetStatus(), other.getAktivitetStatus())
                && Objects.equals(this.getBeregningsgrunnlag(), other.getBeregningsgrunnlag());
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningsgrunnlag, aktivitetStatus);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "aktivitetStatus=" + aktivitetStatus + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "hjemmel=" + hjemmel + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    public void setBeregningsgrunnlagDto(BeregningsgrunnlagDto beregningsgrunnlagDto) {
        this.beregningsgrunnlag = beregningsgrunnlagDto;
    }

    public static class Builder {
        private BeregningsgrunnlagAktivitetStatusDto beregningsgrunnlagAktivitetStatusMal;

        public Builder() {
            beregningsgrunnlagAktivitetStatusMal = new BeregningsgrunnlagAktivitetStatusDto();
            beregningsgrunnlagAktivitetStatusMal.hjemmel = Hjemmel.UDEFINERT;
        }

        public Builder(BeregningsgrunnlagAktivitetStatusDto o) {
            beregningsgrunnlagAktivitetStatusMal = new BeregningsgrunnlagAktivitetStatusDto(o);
        }

        public static Builder kopier(BeregningsgrunnlagAktivitetStatusDto o) {
            return new Builder(o);
        }

        public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
            beregningsgrunnlagAktivitetStatusMal.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medHjemmel(Hjemmel hjemmel) {
            beregningsgrunnlagAktivitetStatusMal.hjemmel = hjemmel;
            return this;
        }

        public BeregningsgrunnlagAktivitetStatusDto build(BeregningsgrunnlagDto beregningsgrunnlag) {
            beregningsgrunnlagAktivitetStatusMal.beregningsgrunnlag = beregningsgrunnlag;
            verifyStateForBuild();
            beregningsgrunnlag.leggTilBeregningsgrunnlagAktivitetStatus(beregningsgrunnlagAktivitetStatusMal);
            return beregningsgrunnlagAktivitetStatusMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(beregningsgrunnlagAktivitetStatusMal.beregningsgrunnlag, "beregningsgrunnlag");
            Objects.requireNonNull(beregningsgrunnlagAktivitetStatusMal.aktivitetStatus, "aktivitetStatus");
            Objects.requireNonNull(beregningsgrunnlagAktivitetStatusMal.getHjemmel(), "hjemmel");
        }
    }
}
