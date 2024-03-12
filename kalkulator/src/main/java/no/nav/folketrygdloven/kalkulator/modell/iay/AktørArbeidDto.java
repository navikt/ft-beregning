package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;

public class AktørArbeidDto {

    private Set<YrkesaktivitetDto> yrkesaktiviter = new LinkedHashSet<>();

    AktørArbeidDto() {
        // hibernate
    }

    /**
     * Deep copy ctor
     */
    AktørArbeidDto(AktørArbeidDto aktørArbeid) {
        this.yrkesaktiviter = aktørArbeid.yrkesaktiviter.stream().map(YrkesaktivitetDto::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Ufiltrert liste av yrkesaktiviteter.
     */
    public Collection<YrkesaktivitetDto> hentAlleYrkesaktiviteter() {
        return Set.copyOf(yrkesaktiviter);
    }

    boolean hasValues() {
        return yrkesaktiviter != null;
    }

    YrkesaktivitetDtoBuilder getYrkesaktivitetBuilderForNøkkel(OpptjeningsnøkkelDto identifikator, ArbeidType arbeidType) {
        Optional<YrkesaktivitetDto> yrkesaktivitet = yrkesaktiviter.stream()
                .filter(ya -> ya.getArbeidType().equals(arbeidType) && new OpptjeningsnøkkelDto(ya).equals(identifikator))
                .findFirst();
        final YrkesaktivitetDtoBuilder oppdatere = YrkesaktivitetDtoBuilder.oppdatere(yrkesaktivitet);
        oppdatere.medArbeidType(arbeidType);
        return oppdatere;
    }
    YrkesaktivitetDtoBuilder getYrkesaktivitetBuilderForType(ArbeidType type) {
        Optional<YrkesaktivitetDto> yrkesaktivitet = yrkesaktiviter.stream()
                .filter(ya -> ya.getArbeidType().equals(type))
                .findFirst();
        final YrkesaktivitetDtoBuilder oppdatere = YrkesaktivitetDtoBuilder.oppdatere(yrkesaktivitet);
        oppdatere.medArbeidType(type);
        return oppdatere;
    }

    void leggTilYrkesaktivitet(YrkesaktivitetDto yrkesaktivitet) {
        this.yrkesaktiviter.add(yrkesaktivitet);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AktørArbeidDto)) {
            return false;
        }
        AktørArbeidDto other = (AktørArbeidDto) obj;
        return Objects.equals(this.hentAlleYrkesaktiviteter(), other.hentAlleYrkesaktiviteter());
    }

    @Override
    public int hashCode() {
        return Objects.hash(yrkesaktiviter);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
                ", yrkesaktiviteter=" + yrkesaktiviter +
                '>';
    }

}
