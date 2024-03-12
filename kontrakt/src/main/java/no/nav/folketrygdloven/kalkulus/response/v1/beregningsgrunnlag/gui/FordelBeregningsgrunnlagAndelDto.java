package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FordelBeregningsgrunnlagAndelDto extends FaktaOmBeregningAndelDto {

    @Valid
    @JsonProperty(value = "fordelingForrigeBehandlingPrAar")
    private Beløp fordelingForrigeBehandlingPrAar;

    @Valid
    @JsonProperty(value = "refusjonskravPrAar")
    private Beløp refusjonskravPrAar = Beløp.ZERO;

    @Valid
    @JsonProperty(value = "fordeltPrAar")
    private Beløp fordeltPrAar;

    @Valid
    @JsonProperty(value = "belopFraInntektsmeldingPrAar")
    private Beløp belopFraInntektsmeldingPrAar;

    @Valid
    @JsonProperty(value = "refusjonskravFraInntektsmeldingPrAar")
    private Beløp refusjonskravFraInntektsmeldingPrAar;

    @Valid
    @JsonProperty(value = "nyttArbeidsforhold")
    private boolean nyttArbeidsforhold;

    @Valid
    @JsonProperty(value = "arbeidsforholdType")
    @NotNull
    private OpptjeningAktivitetType arbeidsforholdType;

    public FordelBeregningsgrunnlagAndelDto() {
        super();
        // For deserialisering av json
    }

    public FordelBeregningsgrunnlagAndelDto(FaktaOmBeregningAndelDto superDto) {
        super(superDto.getAndelsnr(), superDto.getArbeidsforhold(), superDto.getInntektskategori(),
            superDto.getAktivitetStatus(), superDto.getLagtTilAvSaksbehandler(), superDto.getFastsattAvSaksbehandler(), superDto.getAndelIArbeid(), superDto.getKilde());
    }

    public void setBelopFraInntektsmeldingPrÅr(Beløp belopFraInntektsmeldingPrAar) {
        this.belopFraInntektsmeldingPrAar = belopFraInntektsmeldingPrAar;
    }

    public void setFordelingForrigeBehandlingPrÅr(Beløp fordelingForrigeBehandling) {
        this.fordelingForrigeBehandlingPrAar = fordelingForrigeBehandling;
    }

    public void setRefusjonskravPrAar(Beløp refusjonskravPrAar) {
        this.refusjonskravPrAar = refusjonskravPrAar;
    }


    public void setRefusjonskravFraInntektsmeldingPrÅr(Beløp refusjonskravFraInntektsmelding) {
        this.refusjonskravFraInntektsmeldingPrAar = refusjonskravFraInntektsmelding;
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public void setArbeidsforholdType(OpptjeningAktivitetType arbeidsforholdType) {
        this.arbeidsforholdType = arbeidsforholdType;
    }

    public boolean isNyttArbeidsforhold() {
        return nyttArbeidsforhold;
    }

    public void setNyttArbeidsforhold(boolean nyttArbeidsforhold) {
        this.nyttArbeidsforhold = nyttArbeidsforhold;
    }

    public Beløp getFordelingForrigeBehandlingPrAar() {
        return fordelingForrigeBehandlingPrAar;
    }

    public Beløp getRefusjonskravPrAar() {
        return refusjonskravPrAar;
    }

    public Beløp getBelopFraInntektsmeldingPrAar() {
        return belopFraInntektsmeldingPrAar;
    }

    public Beløp getRefusjonskravFraInntektsmeldingPrAar() {
        return refusjonskravFraInntektsmeldingPrAar;
    }

    public Beløp getFordeltPrAar() {
        return fordeltPrAar;
    }

    public void setFordeltPrAar(Beløp fordeltPrAar) {
        this.fordeltPrAar = fordeltPrAar;
    }
}
