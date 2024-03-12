package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;

public class InntektDto {

    private Arbeidsgiver arbeidsgiver;
    private InntektskildeType inntektsKilde;
    private List<InntektspostDto> inntektspost = new ArrayList<>();

    InntektDto() {
        // hibernate
    }

    /**
     * Copy ctor
     */
    InntektDto(InntektDto inntektMal) {
        this.inntektsKilde = inntektMal.getInntektsKilde();
        this.arbeidsgiver = inntektMal.getArbeidsgiver();
        this.inntektspost = inntektMal.getAlleInntektsposter().stream().map(ip -> {
            InntektspostDto inntektspost = new InntektspostDto(ip);
            inntektspost.setInntekt(this);
            return inntektspost;
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj instanceof InntektDto)) {
            return false;
        }
        InntektDto other = (InntektDto) obj;
        return Objects.equals(this.getInntektsKilde(), other.getInntektsKilde())
            && Objects.equals(this.getArbeidsgiver(), other.getArbeidsgiver());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInntektsKilde(), getArbeidsgiver());
    }

    /**
     * System (+ filter) som inntektene er hentet inn fra / med
     *
     * @return {@link InntektskildeType}
     */
    public InntektskildeType getInntektsKilde() {
        return inntektsKilde;
    }

    void setInntektsKilde(InntektskildeType inntektsKilde) {
        this.inntektsKilde = inntektsKilde;
    }


    /**
     * Utbetaler
     *
     * @return {@link Arbeidsgiver}
     */
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    /**
     * Alle utbetalinger utført av utbetaler (ufiltrert).
     */
    public Collection<InntektspostDto> getAlleInntektsposter() {
        return Collections.unmodifiableList(inntektspost);
    }

    void leggTilInntektspost(InntektspostDto inntektspost) {
        inntektspost.setInntekt(this);
        this.inntektspost.add(inntektspost);
    }

    public InntektspostDtoBuilder getInntektspostBuilder() {
        return InntektspostDtoBuilder.ny();
    }

    public boolean hasValues() {
        return arbeidsgiver != null || inntektsKilde != null || inntektspost != null;
    }

}
