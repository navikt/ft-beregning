package no.nav.folketrygdloven.kalkulus.håndtering.v1;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter.AvklarAktiviteterHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FaktaOmBeregningHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FaktaOmFordelingHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.VurderTilkommetInntektHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBGTidsbegrensetArbeidsforholdHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBeregningsgrunnlagATFLHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.VurderVarigEndretArbeidssituasjonHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.VurderVarigEndringEllerNyoppstartetSNHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.overstyring.OverstyrBeregningsaktiviteterDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.overstyring.OverstyrBeregningsgrunnlagHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.refusjon.VurderRefusjonBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.KodeKonstanter;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "avklaringsbehovKode", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AvklarAktiviteterHåndteringDto.class, name = KodeKonstanter.AB_AVKLAR_AKTIVITETER),
        @JsonSubTypes.Type(value = FaktaOmBeregningHåndteringDto.class, name = KodeKonstanter.AB_VURDER_FAKTA_ATFL_SN),
        @JsonSubTypes.Type(value = OverstyrBeregningsaktiviteterDto.class, name =  KodeKonstanter.OVST_BEREGNINGSAKTIVITETER),
        @JsonSubTypes.Type(value = OverstyrBeregningsgrunnlagHåndteringDto.class, name = KodeKonstanter.OVST_INNTEKT),
        @JsonSubTypes.Type(value = FaktaOmFordelingHåndteringDto.class, name = KodeKonstanter.AB_FORDEL_BG),
        @JsonSubTypes.Type(value = FastsettBeregningsgrunnlagATFLHåndteringDto.class, name = KodeKonstanter.AB_FASTSETT_BG_AT_FL),
        @JsonSubTypes.Type(value = FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto.class, name = KodeKonstanter.AB_FASTSETT_BG_SN_NY_I_ARB_LIVT),
        @JsonSubTypes.Type(value = FastsettBGTidsbegrensetArbeidsforholdHåndteringDto.class, name = KodeKonstanter.AB_FASTSETT_BG_TB_ARB),
        @JsonSubTypes.Type(value = VurderRefusjonBeregningsgrunnlagDto.class, name = KodeKonstanter.AB_VURDER_REFUSJONSKRAV),
        @JsonSubTypes.Type(value = VurderVarigEndringEllerNyoppstartetSNHåndteringDto.class, name = KodeKonstanter.AB_VURDER_VARIG_ENDRT_NYOPPSTR_NAERNG_SN),
        @JsonSubTypes.Type(value = VurderVarigEndretArbeidssituasjonHåndteringDto.class, name = KodeKonstanter.AB_VURDER_VARIG_ENDRT_ARB_SITSJN_MDL_INAKTV),
        @JsonSubTypes.Type(value = VurderTilkommetInntektHåndteringDto.class, name = KodeKonstanter.AB_VURDER_NYTT_INNTKTSFRHLD),
})
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class HåndterBeregningDto {

    @JsonProperty(value = "avklaringsbehovDefinisjon")
    @Valid
    private AvklaringsbehovDefinisjon avklaringsbehovDefinisjon;

    @JsonProperty("begrunnelse")
    @Size(max = 4000)
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}§]+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String begrunnelse;

    @JsonProperty(value = "avbrutt")
    @Valid
    private boolean avbrutt;


    public HåndterBeregningDto(@NotNull @Valid AvklaringsbehovDefinisjon avklaringsbehovDefinisjon) {
        this.avklaringsbehovDefinisjon = avklaringsbehovDefinisjon;
    }

    public HåndterBeregningDto(@NotNull @Valid AvklaringsbehovDefinisjon avklaringsbehovDefinisjon, boolean avbrutt) {
        this.avklaringsbehovDefinisjon = avklaringsbehovDefinisjon;
        this.avbrutt = avbrutt;
    }

    public HåndterBeregningDto() {
        // default ctor
    }

    public AvklaringsbehovDefinisjon getAvklaringsbehovDefinisjon() {
        return avklaringsbehovDefinisjon;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public boolean skalAvbrytes() {
        return avbrutt;
    }
}

