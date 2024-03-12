package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fakta.Saksopplysninger;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FaktaOmBeregningDto {

    @Valid
    @JsonProperty(value = "saksopplysninger")
    private Saksopplysninger saksopplysninger;

    @Valid
    @JsonProperty(value = "kortvarigeArbeidsforhold")
    @Size(max=100)
    private List<KortvarigeArbeidsforholdDto> kortvarigeArbeidsforhold;

    @Valid
    @JsonProperty(value = "frilansAndel")
    private FaktaOmBeregningAndelDto frilansAndel;

    @Valid
    @JsonProperty(value = "kunYtelse")
    private KunYtelseDto kunYtelse;

    @Valid
    @JsonProperty(value = "faktaOmBeregningTilfeller")
    @Size(max=15)
    private List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller;

    @Valid
    @JsonProperty(value = "arbeidstakerOgFrilanserISammeOrganisasjonListe")
    @Size(max=100)
    private List<ATogFLISammeOrganisasjonDto> arbeidstakerOgFrilanserISammeOrganisasjonListe;

    @Valid
    @JsonProperty(value = "arbeidsforholdMedLønnsendringUtenIM")
    @Size(max=100)
    private List<FaktaOmBeregningAndelDto> arbeidsforholdMedLønnsendringUtenIM;

    @Valid
    @JsonProperty(value = "vurderMottarYtelse")
    private VurderMottarYtelseDto vurderMottarYtelse;

    @Valid
    @JsonProperty(value = "avklarAktiviteter")
    private AvklarAktiviteterDto avklarAktiviteter;

    @Valid
    @JsonProperty(value = "vurderBesteberegning")
    private VurderBesteberegningDto vurderBesteberegning;

    @Valid
    @JsonProperty(value = "andelerForFaktaOmBeregning")
    @Size
    private List<AndelForFaktaOmBeregningDto> andelerForFaktaOmBeregning = new ArrayList<>();

    @Valid
    @JsonProperty(value = "vurderMilitaer")
    private VurderMilitærDto vurderMilitaer;

    @Valid
    @JsonProperty(value = "refusjonskravSomKommerForSentListe")
    @Size
    private List<RefusjonskravSomKommerForSentDto> refusjonskravSomKommerForSentListe;

    public FaktaOmBeregningDto() {
        // Hibernate
    }

    public List<RefusjonskravSomKommerForSentDto> getRefusjonskravSomKommerForSentListe() {
        return refusjonskravSomKommerForSentListe;
    }

    public void setRefusjonskravSomKommerForSentListe(List<RefusjonskravSomKommerForSentDto> refusjonskravSomKommerForSentListe) {
        this.refusjonskravSomKommerForSentListe = refusjonskravSomKommerForSentListe;
    }

    public Saksopplysninger getSaksopplysninger() {
        return saksopplysninger;
    }

    public void setSaksopplysninger(Saksopplysninger saksopplysninger) {
        this.saksopplysninger = saksopplysninger;
    }

    public List<AndelForFaktaOmBeregningDto> getAndelerForFaktaOmBeregning() {
        return andelerForFaktaOmBeregning;
    }

    public void setAndelerForFaktaOmBeregning(List<AndelForFaktaOmBeregningDto> andelerForFaktaOmBeregning) {
        this.andelerForFaktaOmBeregning = andelerForFaktaOmBeregning;
    }

    public VurderBesteberegningDto getVurderBesteberegning() {
        return vurderBesteberegning;
    }

    public void setVurderBesteberegning(VurderBesteberegningDto vurderBesteberegning) {
        this.vurderBesteberegning = vurderBesteberegning;
    }

    public KunYtelseDto getKunYtelse() {
        return kunYtelse;
    }

    public void setKunYtelse(KunYtelseDto kunYtelse) {
        this.kunYtelse = kunYtelse;
    }

    public List<KortvarigeArbeidsforholdDto> getKortvarigeArbeidsforhold() {
        return kortvarigeArbeidsforhold;
    }

    public void setKortvarigeArbeidsforhold(List<KortvarigeArbeidsforholdDto> kortvarigeArbeidsforhold) {
        this.kortvarigeArbeidsforhold = kortvarigeArbeidsforhold;
    }

    public FaktaOmBeregningAndelDto getFrilansAndel() {
        return frilansAndel;
    }

    public void setFrilansAndel(FaktaOmBeregningAndelDto frilansAndel) {
        this.frilansAndel = frilansAndel;
    }

    public List<FaktaOmBeregningAndelDto> getArbeidsforholdMedLønnsendringUtenIM() {
        return arbeidsforholdMedLønnsendringUtenIM;
    }

    public void setArbeidsforholdMedLønnsendringUtenIM(List<FaktaOmBeregningAndelDto> arbeidsforholdMedLønnsendringUtenIM) {
        this.arbeidsforholdMedLønnsendringUtenIM = arbeidsforholdMedLønnsendringUtenIM;
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller;
    }

    public void setFaktaOmBeregningTilfeller(List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
    }

    public List<ATogFLISammeOrganisasjonDto> getArbeidstakerOgFrilanserISammeOrganisasjonListe() {
        return arbeidstakerOgFrilanserISammeOrganisasjonListe;
    }

    public void setArbeidstakerOgFrilanserISammeOrganisasjonListe(List<ATogFLISammeOrganisasjonDto> aTogFLISammeOrganisasjonListe) {
        this.arbeidstakerOgFrilanserISammeOrganisasjonListe = aTogFLISammeOrganisasjonListe;
    }

    public VurderMottarYtelseDto getVurderMottarYtelse() {
        return vurderMottarYtelse;
    }

    public void setVurderMottarYtelse(VurderMottarYtelseDto vurderMottarYtelse) {
        this.vurderMottarYtelse = vurderMottarYtelse;
    }

    public AvklarAktiviteterDto getAvklarAktiviteter() {
        return avklarAktiviteter;
    }

    public void setAvklarAktiviteter(AvklarAktiviteterDto avklarAktiviteter) {
        this.avklarAktiviteter = avklarAktiviteter;
    }

    public VurderMilitærDto getVurderMilitaer() {
        return vurderMilitaer;
    }

    public void setVurderMilitaer(VurderMilitærDto vurderMilitaer) {
        this.vurderMilitaer = vurderMilitaer;
    }

}
