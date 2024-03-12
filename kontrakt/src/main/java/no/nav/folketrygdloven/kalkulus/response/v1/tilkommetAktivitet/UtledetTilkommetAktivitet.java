package no.nav.folketrygdloven.kalkulus.response.v1.tilkommetAktivitet;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class UtledetTilkommetAktivitet {

    @Valid
    @JsonProperty("aktivitetStatus")
    private AktivitetStatus aktivitetStatus;

    @JsonProperty(value = "arbeidsgiver")
    @Valid
    private Arbeidsgiver arbeidsgiver;

    @JsonProperty(value = "perioder")
    @Valid
    private List<Periode> perioder;

    @JsonCreator
    public UtledetTilkommetAktivitet(
            @Valid
            @JsonProperty("aktivitetStatus")
            AktivitetStatus aktivitetStatus,

            @JsonProperty(value = "arbeidsgiver")
            @Valid
            Arbeidsgiver arbeidsgiver,

            @JsonProperty(value = "perioder")
            @Valid
            List<Periode> perioder
            ) {
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiver = arbeidsgiver;
        this.perioder = perioder;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public List<Periode> getPerioder() {
        return perioder;
    }
}
