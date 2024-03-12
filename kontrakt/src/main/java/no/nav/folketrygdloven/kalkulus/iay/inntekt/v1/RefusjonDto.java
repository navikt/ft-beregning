package no.nav.folketrygdloven.kalkulus.iay.inntekt.v1;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class RefusjonDto {

    @JsonProperty(value = "refusjonsbeløpMnd")
    @Valid
    private Beløp refusjonsbeløpMnd;

    @JsonProperty(value = "fom")
    @Valid
    private LocalDate fom;

    protected RefusjonDto() {
        // default ctor
    }

    public RefusjonDto(Beløp refusjonsbeløpMnd, LocalDate fom) {
        this.refusjonsbeløpMnd = refusjonsbeløpMnd;
        this.fom = fom;
    }

    public Beløp getRefusjonsbeløpMnd() {
        return refusjonsbeløpMnd;
    }

    public LocalDate getFom() {
        return fom;
    }
}
