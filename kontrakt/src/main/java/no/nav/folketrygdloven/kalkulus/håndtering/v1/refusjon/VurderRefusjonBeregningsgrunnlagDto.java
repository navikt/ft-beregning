package no.nav.folketrygdloven.kalkulus.håndtering.v1.refusjon;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.RefusjonskravSomKommerForSentDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VurderRefusjonBeregningsgrunnlagDto extends HåndterBeregningDto {

    // TODO: Må huske å legge inn en sjekk på at minst en av listene er større enn 0, dette skal gjøres et annet sted

    @JsonProperty("fastsatteAndeler")
    @Valid
    @Size
    private List<VurderRefusjonAndelBeregningsgrunnlagDto> fastsatteAndeler;

    // TODO: Sjekk om dette er riktig dto å bruke her, eller om vi skal bruke en annen (ny?)
    @JsonProperty("refusjonskravSomKommerForSent")
    @Valid
    @Size
    private List<RefusjonskravSomKommerForSentDto> refusjonskravSomKommerForSent;

    public VurderRefusjonBeregningsgrunnlagDto() {
        // For Json deserialisering
    }

    public VurderRefusjonBeregningsgrunnlagDto(@Valid List<VurderRefusjonAndelBeregningsgrunnlagDto> fastsatteAndeler, @Valid List<RefusjonskravSomKommerForSentDto> refusjonskravSomKommerForSent) {
        super(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV);
        this.fastsatteAndeler = fastsatteAndeler;
        this.refusjonskravSomKommerForSent = refusjonskravSomKommerForSent;
    }

    public List<VurderRefusjonAndelBeregningsgrunnlagDto> getFastsatteAndeler() {
        return fastsatteAndeler;
    }

    public List<RefusjonskravSomKommerForSentDto> getRefusjonskravSomKommerForSent() {
        return refusjonskravSomKommerForSent;
    }
}
