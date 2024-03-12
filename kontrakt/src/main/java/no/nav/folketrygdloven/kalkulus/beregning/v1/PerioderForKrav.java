package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.time.LocalDate;
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

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class PerioderForKrav {

    @JsonProperty(value = "innsendingsdato")
    @Valid
    @NotNull
    private LocalDate innsendingsdato;

    @JsonProperty(value = "refusjonsperioder")
    @Valid
    @Size(min = 1)
    @NotNull
    private List<Refusjonsperiode> refusjonsperioder;

    public PerioderForKrav(@Valid @NotNull LocalDate innsendingsdato,
                           @Valid @NotNull List<Refusjonsperiode> perioder)  {

        this.innsendingsdato = innsendingsdato;
        this.refusjonsperioder = perioder;
    }

    protected PerioderForKrav() {
        // jackson
    }


    public LocalDate getInnsendingsdato() {
        return innsendingsdato;
    }

    public List<Refusjonsperiode> getRefusjonsperioder() {
        return refusjonsperioder;
    }

    @Override
    public String toString() {
        return "PerioderForKrav{" +
                "innsendingsdato=" + innsendingsdato +
                ", kravperioder=" + refusjonsperioder +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerioderForKrav that = (PerioderForKrav) o;
        return Objects.equals(innsendingsdato, that.innsendingsdato) && Objects.equals(refusjonsperioder, that.refusjonsperioder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(innsendingsdato, refusjonsperioder);
    }
}
