package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FaktaAvklaringer {

	@JsonProperty(value = "erNyIArbeidslivetSN")
	@Valid
	private Boolean erNyIArbeidslivetSN;

	@JsonProperty(value = "harVarigEndretNæring")
	@Valid
	private Boolean harVarigEndretNæring;

	@JsonProperty(value = "erNyoppstartetSN")
	@Valid
	private Boolean erNyoppstartetSN;

	@JsonProperty(value = "erNyoppstartetFL")
	@Valid
	private Boolean erNyoppstartetFL;

	@JsonProperty(value = "harMottattYtelseForFL")
	@Valid
	private Boolean harMottattYtelseForFL;

	@JsonProperty(value = "skalBeregnesSomMilitær")
	@Valid
	private Boolean skalBeregnesSomMilitær;

	@JsonProperty(value = "mottarEtterlønnSluttpakke")
	@Valid
	private Boolean mottarEtterlønnSluttpakke;


	@JsonProperty(value = "avklaringerForArbeidsforhold")
	@Valid
	private List<FaktaAvklaringerForArbeid> faktaAvklaringerForArbeidsforhold;

	public void setHarVarigEndretNæring(Boolean harVarigEndretNæring) {
		this.harVarigEndretNæring = harVarigEndretNæring;
	}

	public void setErNyoppstartetSN(Boolean erNyoppstartetSN) {
		this.erNyoppstartetSN = erNyoppstartetSN;
	}

	public void setErNyIArbeidslivetSN(Boolean erNyIArbeidslivetSN) {
		this.erNyIArbeidslivetSN = erNyIArbeidslivetSN;
	}

	public void setErNyoppstartetFL(Boolean erNyoppstartetFL) {
		this.erNyoppstartetFL = erNyoppstartetFL;
	}

	public void setHarMottattYtelseForFL(Boolean harMottattYtelseForFL) {
		this.harMottattYtelseForFL = harMottattYtelseForFL;
	}

	public void setSkalBeregnesSomMilitær(Boolean skalBeregnesSomMilitær) {
		this.skalBeregnesSomMilitær = skalBeregnesSomMilitær;
	}

	public void setMottarEtterlønnSluttpakke(Boolean mottarEtterlønnSluttpakke) {
		this.mottarEtterlønnSluttpakke = mottarEtterlønnSluttpakke;
	}

	public void setFaktaAvklaringerForArbeidsforhold(List<FaktaAvklaringerForArbeid> faktaAvklaringerForArbeidsforhold) {
		this.faktaAvklaringerForArbeidsforhold = faktaAvklaringerForArbeidsforhold;
	}

	public Boolean getHarVarigEndretNæring() {
		return harVarigEndretNæring;
	}

	public Boolean getErNyoppstartetSN() {
		return erNyoppstartetSN;
	}

	public Boolean getErNyIArbeidslivetSN() {
		return erNyIArbeidslivetSN;
	}

	public Boolean getErNyoppstartetFL() {
		return erNyoppstartetFL;
	}

	public Boolean getHarMottattYtelseForFL() {
		return harMottattYtelseForFL;
	}

	public Boolean getSkalBeregnesSomMilitær() {
		return skalBeregnesSomMilitær;
	}

	public Boolean getMottarEtterlønnSluttpakke() {
		return mottarEtterlønnSluttpakke;
	}

	public List<FaktaAvklaringerForArbeid> getFaktaAvklaringerForArbeidsforhold() {
		return faktaAvklaringerForArbeidsforhold;
	}
}
