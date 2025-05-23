package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.time.LocalDate;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetNøkkel;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class BeregningsaktivitetLagreDto {

    private OpptjeningAktivitetType opptjeningAktivitetType;

    private LocalDate fom;

    private LocalDate tom;

    private String oppdragsgiverOrg;

    private String arbeidsgiverIdentifikator;

    private String arbeidsforholdRef;

    private boolean skalBrukes;

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public String getOppdragsgiverOrg() {
        return oppdragsgiverOrg;
    }

    public String getArbeidsgiverIdentifikator() {
        return arbeidsgiverIdentifikator;
    }

    public String getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public boolean getSkalBrukes() {
        return skalBrukes;
    }

    public BeregningAktivitetNøkkel getNøkkel() {
	    var builder = BeregningAktivitetNøkkel.builder()
            .medOpptjeningAktivitetType(opptjeningAktivitetType)
            .medFom(fom)
            .medArbeidsforholdRef(arbeidsforholdRef);
        if (oppdragsgiverOrg != null) {
            builder.medArbeidsgiverIdentifikator(oppdragsgiverOrg);
        } else {
            builder.medArbeidsgiverIdentifikator(arbeidsgiverIdentifikator);
        }
        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BeregningsaktivitetLagreDto kladd;

        private Builder() {
            kladd = new BeregningsaktivitetLagreDto();
        }

        public Builder medOpptjeningAktivitetType(OpptjeningAktivitetType opptjeningAktivitetType) {
            kladd.opptjeningAktivitetType = opptjeningAktivitetType;
            return this;
        }

        public Builder medFom(LocalDate fom) {
            kladd.fom = fom;
            return this;
        }

        public Builder medTom(LocalDate tom) {
            kladd.tom = tom;
            return this;
        }

        public Builder medOppdragsgiverOrg(String oppdragsgiverOrg) {
            kladd.oppdragsgiverOrg = oppdragsgiverOrg;
            return this;
        }

        public Builder medArbeidsgiverIdentifikator(String arbeidsgiverIdentifikator) {
            kladd.arbeidsgiverIdentifikator = arbeidsgiverIdentifikator;
            return this;
        }

        public Builder medArbeidsforholdRef(String arbeidsforholdRef) {
            kladd.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medSkalBrukes(boolean skalBrukes) {
            kladd.skalBrukes = skalBrukes;
            return this;
        }

        public BeregningsaktivitetLagreDto build() {
            return kladd;
        }
    }
}
