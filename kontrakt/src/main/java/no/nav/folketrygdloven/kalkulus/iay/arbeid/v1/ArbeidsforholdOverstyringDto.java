package no.nav.folketrygdloven.kalkulus.iay.arbeid.v1;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.IayProsent;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ArbeidsforholdOverstyringDto {

    @JsonProperty("arbeidsgiver")
    @Valid
    @NotNull
    private Aktør arbeidsgiver;

    @JsonProperty("arbeidsforholdRefDto")
    @Valid
    private InternArbeidsforholdRefDto arbeidsforholdRefDto;

    @JsonProperty("handling")
    @Valid
    private ArbeidsforholdHandlingType handling;

	@JsonProperty("stillingsprosent")
	@Valid
	private IayProsent stillingsprosent;

	@JsonProperty("arbeidsforholdOverstyrtePerioder")
	@Valid
	@Size()
	private List<Periode> arbeidsforholdOverstyrtePerioder;

    public ArbeidsforholdOverstyringDto() {
        // default ctor
    }

	@Deprecated
    public ArbeidsforholdOverstyringDto(@Valid @NotNull Aktør arbeidsgiver, @Valid InternArbeidsforholdRefDto arbeidsforholdRefDto, @Valid ArbeidsforholdHandlingType handling) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRefDto = arbeidsforholdRefDto;
        this.handling = handling;
    }

	public ArbeidsforholdOverstyringDto(@Valid @NotNull Aktør arbeidsgiver, @Valid InternArbeidsforholdRefDto arbeidsforholdRefDto, @Valid ArbeidsforholdHandlingType handling,
	                                    IayProsent stillingsprosent, List<Periode> arbeidsforholdOverstyrtePerioder) {
		this.arbeidsgiver = arbeidsgiver;
		this.arbeidsforholdRefDto = arbeidsforholdRefDto;
		this.handling = handling;
		this.stillingsprosent = stillingsprosent;
		this.arbeidsforholdOverstyrtePerioder = arbeidsforholdOverstyrtePerioder;
	}

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRefDto() {
        return arbeidsforholdRefDto;
    }

    public ArbeidsforholdHandlingType getHandling() {
        return handling;
    }

	public IayProsent getStillingsprosent() {
		return stillingsprosent;
	}

	public List<Periode> getArbeidsforholdOverstyrtePerioder() {
		return arbeidsforholdOverstyrtePerioder;
	}
}
