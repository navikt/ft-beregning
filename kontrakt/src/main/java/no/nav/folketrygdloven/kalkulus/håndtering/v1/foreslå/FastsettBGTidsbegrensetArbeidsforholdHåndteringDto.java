package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FastsettBGTidsbegrensetArbeidsforholdHåndteringDto extends HåndterBeregningDto {

    @JsonProperty("fastsettBGTidsbegrensetArbeidsforholdDto")
    @Valid
    @NotNull
    private FastsettBGTidsbegrensetArbeidsforholdDto fastsettBGTidsbegrensetArbeidsforholdDto;

    public FastsettBGTidsbegrensetArbeidsforholdHåndteringDto() {
        super(AvklaringsbehovDefinisjon.FASTSETT_BG_TB_ARB);
    }

    public FastsettBGTidsbegrensetArbeidsforholdHåndteringDto(@Valid @NotNull FastsettBGTidsbegrensetArbeidsforholdDto fastsettBGTidsbegrensetArbeidsforholdDto) {
        super(AvklaringsbehovDefinisjon.FASTSETT_BG_TB_ARB);
        this.fastsettBGTidsbegrensetArbeidsforholdDto = fastsettBGTidsbegrensetArbeidsforholdDto;
    }

    public FastsettBGTidsbegrensetArbeidsforholdDto getFastsettBGTidsbegrensetArbeidsforholdDto() {
        return fastsettBGTidsbegrensetArbeidsforholdDto;
    }

}
