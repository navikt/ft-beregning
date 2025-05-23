package no.nav.folketrygdloven.kalkulus.opptjening.v1;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.MidlertidigInaktivType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OpptjeningAktiviteterDto {

    @JsonProperty(value = "perioder", required = true)
    @Valid
    @Size()
    private List<OpptjeningPeriodeDto> perioder;

    @JsonProperty(value = "midlertidigInaktivType")
    @Valid
    private MidlertidigInaktivType midlertidigInaktivType;

    protected OpptjeningAktiviteterDto() {
        // default ctor
    }

    public OpptjeningAktiviteterDto(@JsonProperty(value = "perioder",required = true) @Valid @NotEmpty List<OpptjeningPeriodeDto> perioder) {
        this.perioder = perioder;
    }

	@JsonCreator
    public OpptjeningAktiviteterDto(@JsonProperty(value = "perioder", required = true) @Valid @NotEmpty List<OpptjeningPeriodeDto> perioder,
                                    @JsonProperty(value = "midlertidigInaktivType") @Valid MidlertidigInaktivType midlertidigInaktivType) {

        this.perioder = perioder;
        this.midlertidigInaktivType = midlertidigInaktivType;
    }

    public List<OpptjeningPeriodeDto> getPerioder() {
        return perioder;
    }

    public MidlertidigInaktivType getMidlertidigInaktivType() {
        return midlertidigInaktivType;
    }


}
