package no.nav.folketrygdloven.kalkulus.iay.arbeid.v1;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.IayProsent;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AktivitetsAvtaleDto {

    @JsonProperty("periode")
    @Valid
    @NotNull
    private Periode periode;

    @JsonProperty("sisteLønnsendringsdato")
    @Valid
    private LocalDate sisteLønnsendringsdato;

    @JsonProperty("stillingsprosent")
    @Valid
    private IayProsent stillingsprosent;

    protected AktivitetsAvtaleDto() {
        // default ctor
    }

    public AktivitetsAvtaleDto(@Valid @NotNull Periode periode,
                               @Valid LocalDate sisteLønnsendringsdato,
                               @Valid IayProsent stillingsprosent) {
        this.periode = periode;
        this.sisteLønnsendringsdato = sisteLønnsendringsdato;
        this.stillingsprosent = stillingsprosent;
    }

    public Periode getPeriode() {
        return periode;
    }

    public IayProsent getStillingsprosent() {
        return stillingsprosent;
    }

    public LocalDate getSisteLønnsendringsdato() {
        return sisteLønnsendringsdato;
    }
}
