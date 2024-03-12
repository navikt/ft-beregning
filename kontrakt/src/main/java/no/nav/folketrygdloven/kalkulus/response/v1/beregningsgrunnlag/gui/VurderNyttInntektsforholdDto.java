package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class VurderNyttInntektsforholdDto {

    @Valid
    @JsonProperty(value = "vurderInntektsforholdPerioder")
    @Size(max = 50)
    @NotNull
    private List<VurderInntektsforholdPeriodeDto> vurderInntektsforholdPerioder;

    /**
     * Angir om bruker har mottatt Kommunal omsorgsstønad eller fosterhjemsgodtgjørelse etter skjæringstidspunktet
     * <p>
     * Se <a href="https://www.skatteetaten.no/bedrift-og-organisasjon/arbeidsgiver/a-meldingen/veiledning/lonn-og-ytelser/oversikt-over-lonn-og-andre-ytelser/kommunal-omsorgslonn-og-fosterhjemgodtgjorelse/">...</a>
     * <p>
     * Relevant fordi disse ikke skal reduseres på samme måte som annen frilansinntekt
     *
     */
    @Valid
    @JsonProperty(value = "harMottattOmsorgsstønadEllerFosterhjemsgodtgjørelse")
    @NotNull
    private boolean harMottattOmsorgsstønadEllerFosterhjemsgodtgjørelse;


    public VurderNyttInntektsforholdDto() {
    }

    public VurderNyttInntektsforholdDto(List<VurderInntektsforholdPeriodeDto> vurderInntektsforholdPerioder, boolean harMottattOmsorgsstønadEllerFosterhjemsgodtgjørelse) {
        this.vurderInntektsforholdPerioder = vurderInntektsforholdPerioder;
        this.harMottattOmsorgsstønadEllerFosterhjemsgodtgjørelse = harMottattOmsorgsstønadEllerFosterhjemsgodtgjørelse;
    }

    public List<VurderInntektsforholdPeriodeDto> getVurderInntektsforholdPerioder() {
        return vurderInntektsforholdPerioder;
    }

    public boolean getHarMottattOmsorgsstønadEllerFosterhjemsgodtgjørelse() {
        return harMottattOmsorgsstønadEllerFosterhjemsgodtgjørelse;
    }


}
