package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.List;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class KravperioderPrArbeidsforhold {

    @JsonProperty(value = "arbeidsgiver")
    @Valid
    @NotNull
    private Aktør arbeidsgiver;

    @JsonProperty("internreferanse")
    @Valid
    private InternArbeidsforholdRefDto internreferanse;

    @JsonProperty(value = "alleSøktePerioder")
    @Valid
    @Size(min = 1)
    @NotNull
    private List<PerioderForKrav> alleSøktePerioder;

    @JsonProperty(value = "sisteSøktePerioder")
    @Valid
    @NotNull
    private PerioderForKrav sisteSøktePerioder;

    public KravperioderPrArbeidsforhold(Aktør arbeidsgiver,
                                        InternArbeidsforholdRefDto internreferanse, List<PerioderForKrav> krav,
                                        PerioderForKrav sisteSøktePerioder)  {

        this.arbeidsgiver = arbeidsgiver;
        this.internreferanse = internreferanse;
        this.alleSøktePerioder = krav;
        this.sisteSøktePerioder = sisteSøktePerioder;
    }

    protected KravperioderPrArbeidsforhold() {
        // jackson
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getInternreferanse() {
        return internreferanse;
    }

    public List<PerioderForKrav> getAlleSøktePerioder() {
        return alleSøktePerioder;
    }


    public PerioderForKrav getSisteSøktePerioder() {
        return sisteSøktePerioder;
    }

    @Override
    public String toString() {
        return "KravperioderPrArbeidsgiver{" +
                "arbeidsgiver=" + arbeidsgiver +
                ", alleSøktePerioder=" + alleSøktePerioder +
                ", sisteSøktePerioder=" + sisteSøktePerioder +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KravperioderPrArbeidsforhold that = (KravperioderPrArbeidsforhold) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
                Objects.equals(internreferanse, that.internreferanse) &&
                Objects.equals(alleSøktePerioder, that.alleSøktePerioder) &&
                Objects.equals(sisteSøktePerioder, that.sisteSøktePerioder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, internreferanse, alleSøktePerioder, sisteSøktePerioder);
    }
}
