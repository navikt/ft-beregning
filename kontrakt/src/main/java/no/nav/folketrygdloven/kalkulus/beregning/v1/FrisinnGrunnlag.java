package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.kodeverk.FrisinnBehandlingType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class FrisinnGrunnlag extends YtelsespesifiktGrunnlagDto {

    /**
     * Er det søkt ytelse for frilansaktivitet
     */
    @JsonProperty("søkerYtelseForFrilans")
    @Valid
    @Deprecated // Fjernes herfra når vi har gått over til å bruke frisinnPerioder
    private Boolean søkerYtelseForFrilans;

    /**
     * Er det søkt ytelse for næringsinntekt
     */
    @JsonProperty("søkerYtelseForNæring")
    @Valid
    @Deprecated // Fjernes herfra når vi har gått over til å bruke frisinnPerioder
    private Boolean søkerYtelseForNæring;

    @Valid
    @JsonProperty("perioderMedSøkerInfo")
    @Size(max = 40)
    private List<PeriodeMedSøkerInfoDto> perioderMedSøkerInfo;

    @JsonProperty(value = "frisinnBehandlingType")
    @Valid
    private FrisinnBehandlingType frisinnBehandlingType;


    protected FrisinnGrunnlag() {
        // default ctor
    }

    @Deprecated
    public FrisinnGrunnlag(@Valid Boolean søkerYtelseForFrilans, @Valid Boolean søkerYtelseForNæring) {
        this.søkerYtelseForFrilans = søkerYtelseForFrilans;
        this.søkerYtelseForNæring = søkerYtelseForNæring;
    }

    public FrisinnGrunnlag(@Valid @Size(max = 20) List<PeriodeMedSøkerInfoDto> perioderMedSøkerInfo) {
        this.perioderMedSøkerInfo = perioderMedSøkerInfo;
    }

    public FrisinnGrunnlag(@Valid @Size(max = 20) List<PeriodeMedSøkerInfoDto> perioderMedSøkerInfo, @Valid FrisinnBehandlingType frisinnBehandlingType) {
        this.perioderMedSøkerInfo = perioderMedSøkerInfo;
        this.frisinnBehandlingType = frisinnBehandlingType;
    }

    public FrisinnGrunnlag medPerioderMedSøkerInfo(List<PeriodeMedSøkerInfoDto> perioderMedSøkerInfo) {
        this.perioderMedSøkerInfo = perioderMedSøkerInfo;
        return this;
    }

    public List<PeriodeMedSøkerInfoDto> getPerioderMedSøkerInfo() {
        return perioderMedSøkerInfo;
    }

    public Boolean getSøkerYtelseForFrilans() {
        return søkerYtelseForFrilans;
    }

    public Boolean getSøkerYtelseForNæring() {
        return søkerYtelseForNæring;
    }

    public FrisinnBehandlingType getFrisinnBehandlingType() {
        return frisinnBehandlingType;
    }

    @Override
    public String toString() {
        return "FrisinnGrunnlag{" +
                "perioderMedSøkerInfo=" + perioderMedSøkerInfo +
                '}';
    }
}
