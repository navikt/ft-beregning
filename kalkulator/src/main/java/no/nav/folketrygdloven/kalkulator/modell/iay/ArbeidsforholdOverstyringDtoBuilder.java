package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType;


public class ArbeidsforholdOverstyringDtoBuilder {

    private final ArbeidsforholdOverstyringDto kladd;
    private final boolean oppdatering;

    private ArbeidsforholdOverstyringDtoBuilder(ArbeidsforholdOverstyringDto kladd, boolean oppdatering) {
        this.kladd = kladd;
        this.oppdatering = oppdatering;
    }

    static ArbeidsforholdOverstyringDtoBuilder ny() {
        return new ArbeidsforholdOverstyringDtoBuilder(new ArbeidsforholdOverstyringDto(), false);
    }

    static ArbeidsforholdOverstyringDtoBuilder oppdatere(ArbeidsforholdOverstyringDto oppdatere) {
        return new ArbeidsforholdOverstyringDtoBuilder(new ArbeidsforholdOverstyringDto(oppdatere), true);
    }

    public static ArbeidsforholdOverstyringDtoBuilder oppdatere(Optional<ArbeidsforholdOverstyringDto> oppdatere) {
        return oppdatere.map(ArbeidsforholdOverstyringDtoBuilder::oppdatere).orElseGet(ArbeidsforholdOverstyringDtoBuilder::ny);
    }

    public ArbeidsforholdOverstyringDtoBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        kladd.setArbeidsgiver(arbeidsgiver);
        return this;
    }

    public ArbeidsforholdOverstyringDtoBuilder medArbeidsforholdRef(InternArbeidsforholdRefDto ref) {
        kladd.setArbeidsforholdRef(ref);
        return this;
    }

    public ArbeidsforholdOverstyringDtoBuilder medNyArbeidsforholdRef(InternArbeidsforholdRefDto ref) {
        kladd.setNyArbeidsforholdRef(ref);
        return this;
    }

    public ArbeidsforholdOverstyringDtoBuilder medHandling(ArbeidsforholdHandlingType type) {
        kladd.setHandling(type);
        return this;
    }

    public ArbeidsforholdOverstyringDtoBuilder medAngittStillingsprosent(Stillingsprosent stillingsprosent) {
        kladd.setStillingsprosent(stillingsprosent);
        return this;
    }

    public ArbeidsforholdOverstyringDtoBuilder leggTilOverstyrtPeriode(LocalDate fom, LocalDate tom) {
        kladd.leggTilOverstyrtPeriode(fom, tom);
        return this;
    }

    public ArbeidsforholdOverstyringDto build() {
        return kladd;
    }

    boolean isOppdatering() {
        return oppdatering;
    }

}
