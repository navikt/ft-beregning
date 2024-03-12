package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FaktaBeregningLagreDto {

    @JsonProperty("vurderNyoppstartetFL")
    @Valid
    private VurderNyoppstartetFLDto vurderNyoppstartetFL;

    @JsonProperty("vurderTidsbegrensetArbeidsforhold")
    @Valid
    private VurderTidsbegrensetArbeidsforholdDto vurderTidsbegrensetArbeidsforhold;

    @JsonProperty("vurderNyIArbeidslivet")
    @Valid
    private VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto vurderNyIArbeidslivet;

    @JsonProperty("fastsettMaanedsinntektFL")
    @Valid
    private FastsettMånedsinntektFLDto fastsettMaanedsinntektFL;

    @JsonProperty("vurdertLonnsendring")
    @Valid
    private VurderLønnsendringDto vurdertLonnsendring;

    @JsonProperty("fastsattUtenInntektsmelding")
    @Valid
    private FastsettMånedsinntektUtenInntektsmeldingDto fastsattUtenInntektsmelding;

    @JsonProperty("vurderATogFLiSammeOrganisasjon")
    @Valid
    private VurderATogFLiSammeOrganisasjonDto vurderATogFLiSammeOrganisasjon;

    @JsonProperty("besteberegningAndeler")
    @Valid
    private BesteberegningFødendeKvinneDto besteberegningAndeler;

    @JsonProperty("faktaOmBeregningTilfelleDto")
    @NotNull
    @Valid
    private FaktaOmBeregningTilfelleDto faktaOmBeregningTilfelleDto;

    @JsonProperty("kunYtelseFordeling")
    @Valid
    private FastsettBgKunYtelseDto kunYtelseFordeling;

    @JsonProperty("vurderEtterlønnSluttpakke")
    @Valid
    private VurderEtterlønnSluttpakkeDto vurderEtterlønnSluttpakke;

    @JsonProperty("fastsettEtterlønnSluttpakke")
    @Valid
    private FastsettEtterlønnSluttpakkeDto fastsettEtterlønnSluttpakke;

    @JsonProperty("mottarYtelse")
    @Valid
    private MottarYtelseDto mottarYtelse;

    @JsonProperty("vurderMilitaer")
    @Valid
    private VurderMilitærDto vurderMilitaer;

    @JsonProperty("refusjonskravGyldighet")
    @Valid
    @Size
    private List<RefusjonskravPrArbeidsgiverVurderingDto> refusjonskravGyldighet;

    public FaktaBeregningLagreDto() {
        // default ctor
    }

    public FaktaBeregningLagreDto(@Valid VurderNyoppstartetFLDto vurderNyoppstartetFL,
                                  @Valid VurderTidsbegrensetArbeidsforholdDto vurderTidsbegrensetArbeidsforhold,
                                  @Valid VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto vurderNyIArbeidslivet,
                                  @Valid FastsettMånedsinntektFLDto fastsettMaanedsinntektFL,
                                  @Valid VurderLønnsendringDto vurdertLonnsendring,
                                  @Valid FastsettMånedsinntektUtenInntektsmeldingDto fastsattUtenInntektsmelding,
                                  @Valid VurderATogFLiSammeOrganisasjonDto vurderATogFLiSammeOrganisasjon,
                                  @Valid BesteberegningFødendeKvinneDto besteberegningAndeler,
                                  @JsonProperty("faktaOmBeregningTilfelleDto") @NotNull @Valid FaktaOmBeregningTilfelleDto faktaOmBeregningTilfelleDto,
                                  @Valid FastsettBgKunYtelseDto kunYtelseFordeling,
                                  @Valid VurderEtterlønnSluttpakkeDto vurderEtterlønnSluttpakke,
                                  @Valid FastsettEtterlønnSluttpakkeDto fastsettEtterlønnSluttpakke,
                                  @Valid MottarYtelseDto mottarYtelse,
                                  @Valid VurderMilitærDto vurderMilitaer,
                                  @Valid List<RefusjonskravPrArbeidsgiverVurderingDto> refusjonskravGyldighet) {
        this.vurderNyoppstartetFL = vurderNyoppstartetFL;
        this.vurderTidsbegrensetArbeidsforhold = vurderTidsbegrensetArbeidsforhold;
        this.vurderNyIArbeidslivet = vurderNyIArbeidslivet;
        this.fastsettMaanedsinntektFL = fastsettMaanedsinntektFL;
        this.vurdertLonnsendring = vurdertLonnsendring;
        this.fastsattUtenInntektsmelding = fastsattUtenInntektsmelding;
        this.vurderATogFLiSammeOrganisasjon = vurderATogFLiSammeOrganisasjon;
        this.besteberegningAndeler = besteberegningAndeler;
        this.faktaOmBeregningTilfelleDto = faktaOmBeregningTilfelleDto;
        this.kunYtelseFordeling = kunYtelseFordeling;
        this.vurderEtterlønnSluttpakke = vurderEtterlønnSluttpakke;
        this.fastsettEtterlønnSluttpakke = fastsettEtterlønnSluttpakke;
        this.mottarYtelse = mottarYtelse;
        this.vurderMilitaer = vurderMilitaer;
        this.refusjonskravGyldighet = refusjonskravGyldighet;
    }

    public VurderNyoppstartetFLDto getVurderNyoppstartetFL() {
        return vurderNyoppstartetFL;
    }

    public VurderTidsbegrensetArbeidsforholdDto getVurderTidsbegrensetArbeidsforhold() {
        return vurderTidsbegrensetArbeidsforhold;
    }

    public VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto getVurderNyIArbeidslivet() {
        return vurderNyIArbeidslivet;
    }

    public FastsettMånedsinntektFLDto getFastsettMaanedsinntektFL() {
        return fastsettMaanedsinntektFL;
    }

    public VurderLønnsendringDto getVurdertLonnsendring() {
        return vurdertLonnsendring;
    }

    public FastsettMånedsinntektUtenInntektsmeldingDto getFastsattUtenInntektsmelding() {
        return fastsattUtenInntektsmelding;
    }

    public VurderATogFLiSammeOrganisasjonDto getVurderATogFLiSammeOrganisasjon() {
        return vurderATogFLiSammeOrganisasjon;
    }

    public BesteberegningFødendeKvinneDto getBesteberegningAndeler() {
        return besteberegningAndeler;
    }

    public FaktaOmBeregningTilfelleDto getFaktaOmBeregningTilfelleDto() {
        return faktaOmBeregningTilfelleDto;
    }

    public FastsettBgKunYtelseDto getKunYtelseFordeling() {
        return kunYtelseFordeling;
    }

    public VurderEtterlønnSluttpakkeDto getVurderEtterlønnSluttpakke() {
        return vurderEtterlønnSluttpakke;
    }

    public FastsettEtterlønnSluttpakkeDto getFastsettEtterlønnSluttpakke() {
        return fastsettEtterlønnSluttpakke;
    }

    public MottarYtelseDto getMottarYtelse() {
        return mottarYtelse;
    }

    public VurderMilitærDto getVurderMilitaer() {
        return vurderMilitaer;
    }

    public List<RefusjonskravPrArbeidsgiverVurderingDto> getRefusjonskravGyldighet() {
        return refusjonskravGyldighet;
    }
}
