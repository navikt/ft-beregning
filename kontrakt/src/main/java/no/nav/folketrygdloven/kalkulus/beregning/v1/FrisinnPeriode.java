package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
public class FrisinnPeriode {

    /**
     * Datoene denne perioden gjelder for
     */
    @JsonProperty(value = "periode", required = true)
    @NotNull
    @Valid
    private Periode periode;

    /**
     * Er det søkt ytelse for frilansaktivitet
     */
    @JsonProperty("søkerYtelseForFrilans")
    @Valid
    @NotNull
    private Boolean søkerYtelseForFrilans;


    /**
     * Er det søkt ytelse for næringsinntekt
     */
    @JsonProperty("søkerYtelseForNæring")
    @Valid
    @NotNull
    private Boolean søkerYtelseForNæring;

    protected FrisinnPeriode() {
    }

    public FrisinnPeriode(@Valid @NotNull Boolean søkerYtelseForFrilans, @Valid @NotNull Boolean søkerYtelseForNæring, @Valid @NotNull Periode periode) {
        if (!søkerYtelseForFrilans && !søkerYtelseForNæring) {
            throw new IllegalStateException("Søker ytelse for hverken næring eller frilans, ugyldig tilstand for ytelse FRISINN");
        }
        this.søkerYtelseForFrilans = søkerYtelseForFrilans;
        this.søkerYtelseForNæring = søkerYtelseForNæring;
        this.periode = periode;
    }

    public Periode getPeriode() {
        return periode;
    }

    public Boolean getSøkerYtelseForFrilans() {
        return søkerYtelseForFrilans;
    }

    public Boolean getSøkerYtelseForNæring() {
        return søkerYtelseForNæring;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FrisinnPeriode that = (FrisinnPeriode) o;
        return Objects.equals(periode, that.periode) &&
                Objects.equals(søkerYtelseForFrilans, that.søkerYtelseForFrilans) &&
                Objects.equals(søkerYtelseForNæring, that.søkerYtelseForNæring);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, søkerYtelseForFrilans, søkerYtelseForNæring);
    }

    @Override
    public String toString() {
        return "FrisinnPeriode{" +
                "periode=" + periode +
                ", søkerYtelseForFrilans=" + søkerYtelseForFrilans +
                ", søkerYtelseForNæring=" + søkerYtelseForNæring +
                '}';
    }
}
