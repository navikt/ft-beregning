package no.nav.folketrygdloven.kalkulus.beregning.v1;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

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
public class PeriodeMedSøkerInfoDto {

    @JsonProperty(value = "periode", required = true)
    @NotNull
    @Valid
    private Periode periode;

    @JsonProperty(value = "søkerFrilansIPeriode", required = true)
    @Valid
    private Boolean søkerFrilansIPeriode;

    @JsonProperty(value = "søkerNæringIPeriode", required = true)
    @Valid
    private Boolean søkerNæringIPeriode;

    public PeriodeMedSøkerInfoDto() {
    }

    public PeriodeMedSøkerInfoDto(@NotNull @Valid Periode periode, @NotNull @Valid Boolean søkerFrilansIPeriode, @NotNull @Valid Boolean søkerNæringIPeriode) {
        this.periode = periode;
        this.søkerFrilansIPeriode = søkerFrilansIPeriode;
        this.søkerNæringIPeriode = søkerNæringIPeriode;
    }

    public Periode getPeriode() {
        return periode;
    }

    public Boolean getSøkerFrilansIPeriode() {
        return søkerFrilansIPeriode;
    }

    public Boolean getSøkerNæringIPeriode() {
        return søkerNæringIPeriode;
    }

    @Override
    public String toString() {
        return "PeriodeMedSøkerFrisinnDto{" +
                "periode=" + periode +
                ", søkerFrilansIPeriode=" + søkerFrilansIPeriode +
                ", søkerNæringIPeriode=" + søkerNæringIPeriode +
                '}';
    }
}

