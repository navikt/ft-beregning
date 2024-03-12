package no.nav.folketrygdloven.kalkulus.iay.ytelse.v1;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class YtelseDto {


    @JsonProperty(value = "vedtaksDagsats")
    @Valid
    private Beløp vedtaksDagsats;

    @JsonProperty("ytelseAnvist")
    @Valid
    @Size
    private Set<YtelseAnvistDto> ytelseAnvist;

    @JsonProperty(value = "relatertYtelseType", required = true)
    @Valid
    @NotNull
    private YtelseType relatertYtelseType;

    @JsonProperty(value = "periode", required = true)
    @Valid
    @NotNull
    private Periode periode;

    @JsonProperty(value = "ytelseGrunnlag")
    @Valid
    private YtelseGrunnlagDto ytelseGrunnlag;


    protected YtelseDto() {
        // default ctor
    }

    public YtelseDto(@Valid Beløp vedtaksDagsats,
                     @Valid @Size Set<YtelseAnvistDto> ytelseAnvist,
                     @Valid @NotNull YtelseType relatertYtelseType,
                     @Valid @NotNull Periode periode,
                     @Valid YtelseGrunnlagDto ytelseGrunnlag) {
        this.vedtaksDagsats = vedtaksDagsats;
        this.ytelseAnvist = ytelseAnvist;
        this.relatertYtelseType = relatertYtelseType;
        this.periode = periode;
        this.ytelseGrunnlag = ytelseGrunnlag;
    }

    public Set<YtelseAnvistDto> getYtelseAnvist() {
        return ytelseAnvist;
    }

    public YtelseType getRelatertYtelseType() {
        return relatertYtelseType;
    }

    public Periode getPeriode() {
        return periode;
    }

    public Beløp getVedtaksDagsats() {
        return vedtaksDagsats;
    }

    public YtelseGrunnlagDto getYtelseGrunnlag() {
        return ytelseGrunnlag;
    }
}
