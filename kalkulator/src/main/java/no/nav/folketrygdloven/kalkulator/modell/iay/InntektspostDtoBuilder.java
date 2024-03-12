package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.LønnsinntektBeskrivelse;
import no.nav.folketrygdloven.kalkulus.kodeverk.SkatteOgAvgiftsregelType;

public class InntektspostDtoBuilder {
    private InntektspostDto inntektspost;

    InntektspostDtoBuilder(InntektspostDto inntektspost) {
        this.inntektspost = inntektspost;
    }

    public static InntektspostDtoBuilder ny() {
        return new InntektspostDtoBuilder(new InntektspostDto());
    }

    public InntektspostDtoBuilder medInntektspostType(InntektspostType inntektspostType) {
        this.inntektspost.setInntektspostType(inntektspostType);
        return this;
    }

    public InntektspostDtoBuilder medSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType skatteOgAvgiftsregelType) {
        this.inntektspost.setSkatteOgAvgiftsregelType(skatteOgAvgiftsregelType);
        return this;
    }


    public InntektspostDtoBuilder medLønnsinntektBeskrivelse(LønnsinntektBeskrivelse lønnsinntektBeskrivelse) {
        this.inntektspost.setLønnsinnntektBeskrivelse(lønnsinntektBeskrivelse);
        return this;
    }

    public InntektspostDtoBuilder medPeriode(LocalDate fraOgMed, LocalDate tilOgMed) {
        this.inntektspost.setPeriode(fraOgMed, tilOgMed);
        return this;
    }

    public InntektspostDtoBuilder medBeløp(Beløp verdi) {
        this.inntektspost.setBeløp(verdi);
        return this;
    }

    public InntektspostDtoBuilder medInntektYtelse(InntektYtelseType inntektYtelseType) {
        this.inntektspost.setInntektYtelseType(inntektYtelseType);
        return this;
    }

    public InntektspostDto build() {
        if (inntektspost.hasValues()) {
            return inntektspost;
        }
        throw new IllegalStateException();
    }
}
