package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.YtelsespesifiktGrunnlagDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FrisinnGrunnlagDto extends YtelsespesifiktGrunnlagDto {

    @JsonProperty("opplysningerFL")
    @Valid
    private SøknadsopplysningerDto opplysningerFL;

    @JsonProperty("opplysningerSN")
    @Valid
    private SøknadsopplysningerDto opplysningerSN;

    @Valid
    @JsonProperty("perioderSøktFor")
    @NotNull
    @Size(min = 1)
    private List<OpplystPeriodeDto> perioderSøktFor;

    @Valid
    @JsonProperty("frisinnPerioder")
    @NotNull
    @Size(min = 1)
    private List<FrisinnPeriodeDto> frisinnPerioder;

    @Valid
    @JsonProperty("avslagsårsakPrPeriode")
    @NotNull
    @Size()
    private List<AvslagsårsakPrPeriodeDto> avslagsårsakPrPeriode;

    public FrisinnGrunnlagDto() {
        super();
    }

    public SøknadsopplysningerDto getOpplysningerFL() {
        return opplysningerFL;
    }

    public SøknadsopplysningerDto getOpplysningerSN() {
        return opplysningerSN;
    }

    public void setOpplysningerSN(SøknadsopplysningerDto opplysningerSN) {
        this.opplysningerSN = opplysningerSN;
    }

    public void setOpplysningerFL(SøknadsopplysningerDto opplysningerFL) {
        this.opplysningerFL = opplysningerFL;
    }

    public List<OpplystPeriodeDto> getPerioderSøktFor() {
        return perioderSøktFor;
    }

    public void setPerioderSøktFor(List<OpplystPeriodeDto> perioderSøktFor) {
        this.perioderSøktFor = perioderSøktFor;
    }

    public List<FrisinnPeriodeDto> getFrisinnPerioder() {
        return frisinnPerioder;
    }

    public void setFrisinnPerioder(List<FrisinnPeriodeDto> frisinnPerioder) {
        this.frisinnPerioder = frisinnPerioder;
    }

    public List<AvslagsårsakPrPeriodeDto> getAvslagsårsakPrPeriode() {
        return avslagsårsakPrPeriode;
    }

    public void setAvslagsårsakPrPeriode(List<AvslagsårsakPrPeriodeDto> avslagsårsakPrPeriode) {
        this.avslagsårsakPrPeriode = avslagsårsakPrPeriode;
    }
}

