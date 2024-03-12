package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.util.List;

import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


public class FaktaBeregningLagreDto {

    private VurderNyoppstartetFLDto vurderNyoppstartetFL;
    private VurderTidsbegrensetArbeidsforholdDto vurderTidsbegrensetArbeidsforhold;
    private VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto vurderNyIArbeidslivet;
    private FastsettMånedsinntektFLDto fastsettMaanedsinntektFL;
    private VurderLønnsendringDto vurdertLonnsendring;
    private FastsettMånedsinntektUtenInntektsmeldingDto fastsattUtenInntektsmelding;
    private VurderATogFLiSammeOrganisasjonDto vurderATogFLiSammeOrganisasjon;
    private BesteberegningFødendeKvinneDto besteberegningAndeler;
    private List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller;
    private FastsettBgKunYtelseDto kunYtelseFordeling;
    private VurderEtterlønnSluttpakkeDto vurderEtterlønnSluttpakke;
    private FastsettEtterlønnSluttpakkeDto fastsettEtterlønnSluttpakke;
    private MottarYtelseDto mottarYtelse;
    private VurderMilitærDto vurderMilitaer;
    private List<RefusjonskravPrArbeidsgiverVurderingDto> refusjonskravGyldighet;

    public FaktaBeregningLagreDto(VurderNyoppstartetFLDto vurderNyoppstartetFL,
                                  VurderTidsbegrensetArbeidsforholdDto vurderTidsbegrensetArbeidsforhold,
                                  VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto vurderNyIArbeidslivet,
                                  FastsettMånedsinntektFLDto fastsettMaanedsinntektFL,
                                  VurderLønnsendringDto vurdertLonnsendring,
                                  FastsettMånedsinntektUtenInntektsmeldingDto fastsattUtenInntektsmelding,
                                  VurderATogFLiSammeOrganisasjonDto vurderATogFLiSammeOrganisasjon,
                                  BesteberegningFødendeKvinneDto besteberegningAndeler,
                                  List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller,
                                  FastsettBgKunYtelseDto kunYtelseFordeling,
                                  VurderEtterlønnSluttpakkeDto vurderEtterlønnSluttpakke,
                                  FastsettEtterlønnSluttpakkeDto fastsettEtterlønnSluttpakke,
                                  MottarYtelseDto mottarYtelse,
                                  VurderMilitærDto vurderMilitaer,
                                  List<RefusjonskravPrArbeidsgiverVurderingDto> refusjonskravGyldighet) {
        this.vurderNyoppstartetFL = vurderNyoppstartetFL;
        this.vurderTidsbegrensetArbeidsforhold = vurderTidsbegrensetArbeidsforhold;
        this.vurderNyIArbeidslivet = vurderNyIArbeidslivet;
        this.fastsettMaanedsinntektFL = fastsettMaanedsinntektFL;
        this.vurdertLonnsendring = vurdertLonnsendring;
        this.fastsattUtenInntektsmelding = fastsattUtenInntektsmelding;
        this.vurderATogFLiSammeOrganisasjon = vurderATogFLiSammeOrganisasjon;
        this.besteberegningAndeler = besteberegningAndeler;
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
        this.kunYtelseFordeling = kunYtelseFordeling;
        this.vurderEtterlønnSluttpakke = vurderEtterlønnSluttpakke;
        this.fastsettEtterlønnSluttpakke = fastsettEtterlønnSluttpakke;
        this.mottarYtelse = mottarYtelse;
        this.vurderMilitaer = vurderMilitaer;
        this.refusjonskravGyldighet = refusjonskravGyldighet;
    }

    public FaktaBeregningLagreDto(List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
    }

    public List<RefusjonskravPrArbeidsgiverVurderingDto> getRefusjonskravGyldighet() {
        return refusjonskravGyldighet;
    }

    public void setRefusjonskravGyldighet(List<RefusjonskravPrArbeidsgiverVurderingDto> refusjonskravGyldighet) {
        this.refusjonskravGyldighet = refusjonskravGyldighet;
    }

    public VurderNyoppstartetFLDto getVurderNyoppstartetFL() {
        return vurderNyoppstartetFL;
    }

    public void setVurderNyoppstartetFL(VurderNyoppstartetFLDto vurderNyoppstartetFL) {
        this.vurderNyoppstartetFL = vurderNyoppstartetFL;
    }

    public VurderTidsbegrensetArbeidsforholdDto getVurderTidsbegrensetArbeidsforhold() {
        return vurderTidsbegrensetArbeidsforhold;
    }

    public void setVurderTidsbegrensetArbeidsforhold(VurderTidsbegrensetArbeidsforholdDto vurderTidsbegrensetArbeidsforhold) {
        this.vurderTidsbegrensetArbeidsforhold = vurderTidsbegrensetArbeidsforhold;
    }

    public VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto getVurderNyIArbeidslivet() {
        return vurderNyIArbeidslivet;
    }

    public void setVurderNyIArbeidslivet(VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto vurderNyIArbeidslivet) {
        this.vurderNyIArbeidslivet = vurderNyIArbeidslivet;
    }

    public FastsettMånedsinntektFLDto getFastsettMaanedsinntektFL() {
        return fastsettMaanedsinntektFL;
    }

    public void setFastsettMaanedsinntektFL(FastsettMånedsinntektFLDto fastsettMaanedsinntektFL) {
        this.fastsettMaanedsinntektFL = fastsettMaanedsinntektFL;
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller;
    }

    public void setFaktaOmBeregningTilfeller(List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
    }

    public VurderLønnsendringDto getVurdertLonnsendring() {
        return vurdertLonnsendring;
    }

    public void setVurdertLonnsendring(VurderLønnsendringDto vurdertLonnsendring) {
        this.vurdertLonnsendring = vurdertLonnsendring;
    }

    public VurderATogFLiSammeOrganisasjonDto getVurderATogFLiSammeOrganisasjon() {
        return vurderATogFLiSammeOrganisasjon;
    }

    public void setVurderATogFLiSammeOrganisasjon(VurderATogFLiSammeOrganisasjonDto vurderATogFLiSammeOrganisasjon) {
        this.vurderATogFLiSammeOrganisasjon = vurderATogFLiSammeOrganisasjon;
    }

    public BesteberegningFødendeKvinneDto getBesteberegningAndeler() {
        return besteberegningAndeler;
    }

    public void setBesteberegningAndeler(BesteberegningFødendeKvinneDto besteberegningAndeler) {
        this.besteberegningAndeler = besteberegningAndeler;
    }

    public FastsettBgKunYtelseDto getKunYtelseFordeling() {
        return kunYtelseFordeling;
    }

    public void setKunYtelseFordeling(FastsettBgKunYtelseDto kunYtelseFordeling) {
        this.kunYtelseFordeling = kunYtelseFordeling;
    }

    public VurderEtterlønnSluttpakkeDto getVurderEtterlønnSluttpakke() {
        return vurderEtterlønnSluttpakke;
    }

    public void setVurderEtterlønnSluttpakke(VurderEtterlønnSluttpakkeDto vurderEtterlønnSluttpakke) {
        this.vurderEtterlønnSluttpakke = vurderEtterlønnSluttpakke;
    }

    public FastsettEtterlønnSluttpakkeDto getFastsettEtterlønnSluttpakke() {
        return fastsettEtterlønnSluttpakke;
    }

    public void setFastsettEtterlønnSluttpakke(FastsettEtterlønnSluttpakkeDto fastsettEtterlønnSluttpakke) {
        this.fastsettEtterlønnSluttpakke = fastsettEtterlønnSluttpakke;
    }

    public void setMottarYtelse(MottarYtelseDto mottarYtelse) {
        this.mottarYtelse = mottarYtelse;
    }

    public MottarYtelseDto getMottarYtelse() {
        return mottarYtelse;
    }

    public FastsettMånedsinntektUtenInntektsmeldingDto getFastsattUtenInntektsmelding() {
        return fastsattUtenInntektsmelding;
    }

    public void setFastsattUtenInntektsmelding(FastsettMånedsinntektUtenInntektsmeldingDto fastsattUtenInntektsmelding) {
        this.fastsattUtenInntektsmelding = fastsattUtenInntektsmelding;
    }

    public VurderMilitærDto getVurderMilitaer() {
        return vurderMilitaer;
    }

    public void setVurderMilitaer(VurderMilitærDto vurderMilitaer) {
        this.vurderMilitaer = vurderMilitaer;
    }
}
