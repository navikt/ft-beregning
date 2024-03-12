package no.nav.folketrygdloven.kalkulus.håndtering.v1.overstyring;

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
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter.BeregningsaktivitetLagreDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OverstyrBeregningsaktiviteterDto extends HåndterBeregningDto {

    @JsonProperty("beregningsaktivitetLagreDtoList")
    @Valid
    @NotNull
    @Size(max = 1000)
    private List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList;

    public OverstyrBeregningsaktiviteterDto() {
        super(AvklaringsbehovDefinisjon.OVST_BEREGNINGSAKTIVITETER, false);
        // Json deserialisering
    }

    private OverstyrBeregningsaktiviteterDto(boolean avbryt) {
        super(AvklaringsbehovDefinisjon.OVST_BEREGNINGSAKTIVITETER, avbryt);
    }

    public static OverstyrBeregningsaktiviteterDto avbryt() {
        return new OverstyrBeregningsaktiviteterDto(true);
    }

    public OverstyrBeregningsaktiviteterDto(@Valid @NotNull @Size(max = 1000) List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList) {
        super(AvklaringsbehovDefinisjon.OVST_BEREGNINGSAKTIVITETER, false);
        this.beregningsaktivitetLagreDtoList = beregningsaktivitetLagreDtoList;
    }

    public List<BeregningsaktivitetLagreDto> getBeregningsaktivitetLagreDtoList() {
        return beregningsaktivitetLagreDtoList;
    }

}
