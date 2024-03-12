package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FaktaOmBeregningVurderinger {

    @JsonProperty(value = "harEtterlønnSluttpakkeEndring")
    @Valid
    private ToggleEndring harEtterlønnSluttpakkeEndring;

    @JsonProperty(value = "harBesteBeregningEndring")
    @Valid
    private ToggleEndring harBesteBeregningEndring;

    @JsonProperty(value = "harLønnsendringIBeregningsperiodenEndring")
    @Valid
    private ToggleEndring harLønnsendringIBeregningsperiodenEndring;

    @JsonProperty(value = "harMilitærSiviltjenesteEndring")
    @Valid
    private ToggleEndring harMilitærSiviltjenesteEndring;

    @JsonProperty(value = "erSelvstendingNyIArbeidslivetEndring")
    @Valid
    private ToggleEndring erSelvstendingNyIArbeidslivetEndring;

    @JsonProperty(value = "erNyoppstartetFLEndring")
    @Valid
    private ToggleEndring erNyoppstartetFLEndring;

    @JsonProperty(value = "erMottattYtelseEndringer")
    @Valid
    private List<ErMottattYtelseEndring> erMottattYtelseEndringer = new ArrayList<>();

    @JsonProperty(value = "erTidsbegrensetArbeidsforholdEndringer")
    @Valid
    private List<ErTidsbegrensetArbeidsforholdEndring> erTidsbegrensetArbeidsforholdEndringer  = new ArrayList<>();

    @JsonProperty(value = "vurderRefusjonskravGyldighetEndringer")
    @Valid
    private List<RefusjonskravGyldighetEndring> vurderRefusjonskravGyldighetEndringer = new ArrayList<>();

    public ToggleEndring getHarEtterlønnSluttpakkeEndring() {
        return harEtterlønnSluttpakkeEndring;
    }

    public void setHarEtterlønnSluttpakkeEndring(ToggleEndring harEtterlønnSluttpakkeEndring) {
        this.harEtterlønnSluttpakkeEndring = harEtterlønnSluttpakkeEndring;
    }

    public ToggleEndring getHarBesteBeregningEndring() {
        return harBesteBeregningEndring;
    }

    public void setHarBesteBeregningEndring(ToggleEndring harBesteBeregningEndring) {
        this.harBesteBeregningEndring = harBesteBeregningEndring;
    }

    public List<RefusjonskravGyldighetEndring> getVurderRefusjonskravGyldighetEndringer() {
        return vurderRefusjonskravGyldighetEndringer;
    }

    public void setVurderRefusjonskravGyldighetEndringer(List<RefusjonskravGyldighetEndring> vurderRefusjonskravGyldighetEndringer) {
        this.vurderRefusjonskravGyldighetEndringer = vurderRefusjonskravGyldighetEndringer;
    }

    public ToggleEndring getHarLønnsendringIBeregningsperiodenEndring() {
        return harLønnsendringIBeregningsperiodenEndring;
    }

    public void setHarLønnsendringIBeregningsperiodenEndring(ToggleEndring harLønnsendringIBeregningsperiodenEndring) {
        this.harLønnsendringIBeregningsperiodenEndring = harLønnsendringIBeregningsperiodenEndring;
    }

    public ToggleEndring getHarMilitærSiviltjenesteEndring() {
        return harMilitærSiviltjenesteEndring;
    }

    public void setHarMilitærSiviltjenesteEndring(ToggleEndring harMilitærSiviltjenesteEndring) {
        this.harMilitærSiviltjenesteEndring = harMilitærSiviltjenesteEndring;
    }

    public ToggleEndring getErSelvstendingNyIArbeidslivetEndring() {
        return erSelvstendingNyIArbeidslivetEndring;
    }

    public void setErSelvstendingNyIArbeidslivetEndring(ToggleEndring erSelvstendingNyIArbeidslivetEndring) {
        this.erSelvstendingNyIArbeidslivetEndring = erSelvstendingNyIArbeidslivetEndring;
    }

    public ToggleEndring getErNyoppstartetFLEndring() {
        return erNyoppstartetFLEndring;
    }

    public void setErNyoppstartetFLEndring(ToggleEndring erNyoppstartetFLEndring) {
        this.erNyoppstartetFLEndring = erNyoppstartetFLEndring;
    }

    public List<ErMottattYtelseEndring> getErMottattYtelseEndringer() {
        return erMottattYtelseEndringer;
    }

    public void setErMottattYtelseEndringer(List<ErMottattYtelseEndring> erMottattYtelseEndringer) {
        this.erMottattYtelseEndringer = erMottattYtelseEndringer;
    }

    public List<ErTidsbegrensetArbeidsforholdEndring> getErTidsbegrensetArbeidsforholdEndringer() {
        return erTidsbegrensetArbeidsforholdEndringer;
    }

    public void setErTidsbegrensetArbeidsforholdEndringer(List<ErTidsbegrensetArbeidsforholdEndring> erTidsbegrensetArbeidsforholdEndringer) {
        this.erTidsbegrensetArbeidsforholdEndringer = erTidsbegrensetArbeidsforholdEndringer;
    }
}
