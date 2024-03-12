package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

/**
 * Spesifikasjon for å hente aktivt beregningsgrunnlag.
 * Henter aktivt beregningsgrunnlag
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class HentBeregningsgrunnlagRequest implements KalkulusRequest {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private FagsakYtelseType ytelseSomSkalBeregnes;

    @JsonProperty(value = "inkluderRegelSporing", required = false)
    private boolean inkluderRegelSporing;

    //TODO: set saksnummer required + @NotNull når fpsak/k9-sak er oppdatert
    @JsonProperty(value = "saksnummer", required = false)
    @Valid
    private Saksnummer saksnummer;

    @JsonProperty(value = "behandlingUuid", required = true)
    @Valid
    private UUID behandlingUuid;

    protected HentBeregningsgrunnlagRequest() {
        // default ctor
    }

    public HentBeregningsgrunnlagRequest(@Valid @NotNull UUID eksternReferanse,
                                         @Valid Saksnummer saksnummer,
                                         @Valid UUID behandlingUuid,
                                         @NotNull @Valid FagsakYtelseType ytelseSomSkalBeregnes) {

        this.eksternReferanse = eksternReferanse;
        this.saksnummer = saksnummer;
        this.behandlingUuid = behandlingUuid;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
    }

    public HentBeregningsgrunnlagRequest(@Valid @NotNull UUID eksternReferanse,
                                         @Valid Saksnummer saksnummer,
                                         @NotNull @Valid FagsakYtelseType ytelseSomSkalBeregnes,
                                         Boolean inkluderRegelSporing) {

        this.eksternReferanse = eksternReferanse;
        this.saksnummer = saksnummer;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.inkluderRegelSporing = inkluderRegelSporing==null?false:inkluderRegelSporing;
    }

    public UUID getKoblingReferanse() {
        return eksternReferanse;
    }

    public FagsakYtelseType getYtelseSomSkalBeregnes() {
        return ytelseSomSkalBeregnes;
    }

    public boolean getInkluderRegelSporing() {
        return inkluderRegelSporing;
    }

    @Override
    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    @Override
    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }
}
