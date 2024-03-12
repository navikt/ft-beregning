package no.nav.folketrygdloven.kalkulus.iay.inntekt.v1;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.NaturalYtelseType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class NaturalYtelseDto {

    @JsonProperty(value = "periode")
    @Valid
    private Periode periode;

    @JsonProperty(value = "beløp")
    @Valid
    private Beløp beløp;

    @JsonProperty(value = "type")
    @Valid
    private NaturalYtelseType type;

    protected NaturalYtelseDto() {
        // default ctor
    }

    public NaturalYtelseDto(Periode periode, Beløp beløp, NaturalYtelseType type) {
        this.periode = periode;
        this.beløp = beløp;
        this.type = type;
    }

    public Periode getPeriode() {
        return periode;
    }

    public Beløp getBeløp() {
        return beløp;
    }

    public NaturalYtelseType getType() {
        return type;
    }
}
