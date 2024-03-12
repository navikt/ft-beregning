package no.nav.folketrygdloven.kalkulator.modell.iay;


import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;

public class AktørInntektDto {


    private Set<InntektDto> inntekt = new LinkedHashSet<>();

    AktørInntektDto() {
        //hibernate
    }

    /**
     * Deep copy ctor
     */
    AktørInntektDto(AktørInntektDto aktørInntekt) {

        this.inntekt = aktørInntekt.inntekt.stream().map(i -> {
            var inntekt = new InntektDto(i);
            return inntekt;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Get alle inntekter */
    public Collection<InntektDto> getInntekt() {
        return List.copyOf(inntekt);
    }

    public boolean hasValues() {
        return inntekt != null;
    }

    InntektDtoBuilder getInntektBuilder(InntektskildeType inntektsKilde, OpptjeningsnøkkelDto nøkkel) {
        Optional<InntektDto> inntektOptional = getInntekt()
            .stream()
            .filter(i -> inntektsKilde.equals(i.getInntektsKilde()))
            .filter(i -> i.getArbeidsgiver() != null && new OpptjeningsnøkkelDto(i.getArbeidsgiver()).matcher(nøkkel)
                || inntektsKilde.equals(InntektskildeType.SIGRUN)).findFirst();
        InntektDtoBuilder oppdatere = InntektDtoBuilder.oppdatere(inntektOptional);
        if (!oppdatere.getErOppdatering()) {
            oppdatere.medInntektsKilde(inntektsKilde);
        }
        return oppdatere;
    }

    void leggTilInntekt(InntektDto inntekt) {
        this.inntekt.add(inntekt);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AktørInntektDto)) {
            return false;
        }
        AktørInntektDto other = (AktørInntektDto) obj;
        return Objects.equals(this.getInntekt(), other.getInntekt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(inntekt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            ", inntekt=" + inntekt +
            '>';
    }

}
