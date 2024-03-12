package no.nav.folketrygdloven.kalkulus.request.v1;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsforholdReferanseDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;


/**
 * Spesifikasjon for å hente beregningsgrunnlagDto for GUI.
 * Henter DTO-struktur som brukes av beregning i frontend
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class HentBeregningsgrunnlagDtoForGUIRequest {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private FagsakYtelseType ytelseSomSkalBeregnes;

    @JsonProperty(value = "referanser")
    @Valid
    private Set<ArbeidsforholdReferanseDto> referanser;

    @JsonProperty(value = "vilkårsperiodeFom")
    @Valid
    private LocalDate vilkårsperiodeFom;

    protected HentBeregningsgrunnlagDtoForGUIRequest() {
        // default ctor
    }

    public HentBeregningsgrunnlagDtoForGUIRequest(@Valid @NotNull UUID eksternReferanse,
                                                  @NotNull @Valid FagsakYtelseType ytelseSomSkalBeregnes,
                                                  @Valid Set<ArbeidsforholdReferanseDto> referanser,
                                                  @Valid LocalDate vilkårsperiodeFom) {
        this.eksternReferanse = eksternReferanse;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.referanser = referanser;
        this.vilkårsperiodeFom = vilkårsperiodeFom;
    }

    public HentBeregningsgrunnlagDtoForGUIRequest(@Valid @NotNull UUID eksternReferanse,
                                                  @NotNull @Valid FagsakYtelseType ytelseSomSkalBeregnes,
                                                  @Valid Set<ArbeidsforholdReferanseDto> referanser) {
        this.eksternReferanse = eksternReferanse;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.referanser = referanser;
    }

    public UUID getKoblingReferanse() {
        return eksternReferanse;
    }

    public FagsakYtelseType getYtelseSomSkalBeregnes() {
        return ytelseSomSkalBeregnes;
    }

    public Set<ArbeidsforholdReferanseDto> getReferanser() {
        return referanser;
    }

    public LocalDate getVilkårsperiodeFom() {
        return vilkårsperiodeFom;
    }
}
