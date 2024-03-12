package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class Inntekt {

    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private OpptjeningAktivitetType opptjeningAktivitetType;
    private final Beløp inntekt;

    public Inntekt(OpptjeningAktivitetType opptjeningAktivitetType, Beløp inntekt) {
        this.opptjeningAktivitetType = opptjeningAktivitetType;
        this.inntekt = inntekt;
    }

    public Inntekt(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef, Beløp inntekt) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.inntekt = inntekt;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }

    public Beløp getInntekt() {
        return inntekt;
    }
}
