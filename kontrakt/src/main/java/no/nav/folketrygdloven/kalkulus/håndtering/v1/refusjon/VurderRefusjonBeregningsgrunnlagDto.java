package no.nav.folketrygdloven.kalkulus.håndtering.v1.refusjon;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.RefusjonskravForSentDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VurderRefusjonBeregningsgrunnlagDto extends HåndterBeregningDto {

    @JsonProperty("fastsatteAndeler")
    @Size
    private List<@Valid VurderRefusjonAndelBeregningsgrunnlagDto> fastsatteAndeler;

    @JsonProperty("refusjonskravForSentListe")
    @Size
    private List<@Valid RefusjonskravForSentDto> refusjonskravForSentListe;

    public VurderRefusjonBeregningsgrunnlagDto() {
        // For Json deserialisering
    }

    public VurderRefusjonBeregningsgrunnlagDto(List<@Valid VurderRefusjonAndelBeregningsgrunnlagDto> fastsatteAndeler, List<@Valid RefusjonskravForSentDto> refusjonskravForSentListe) {
        super(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV);
        this.fastsatteAndeler = fastsatteAndeler;
        this.refusjonskravForSentListe = refusjonskravForSentListe;
    }

    public List<VurderRefusjonAndelBeregningsgrunnlagDto> getFastsatteAndeler() {
        return fastsatteAndeler;
    }

    public List<RefusjonskravForSentDto> getRefusjonskravForSentListe() {
        return refusjonskravForSentListe;
    }

    @AssertTrue(message = "En av 'fastsatteAndeler' eller 'refusjonskravForSentListe' må ha size > 0")
    public boolean isMinstEnAvListeneIkkeTom() {
        return (fastsatteAndeler != null && !fastsatteAndeler.isEmpty()) || (refusjonskravForSentListe != null
            && !refusjonskravForSentListe.isEmpty());
    }
}
