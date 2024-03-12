package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPrStatusOgAndelEndring {

    @JsonProperty(value = "inntektEndring")
    @Valid
    private InntektEndring inntektEndring;

    @JsonProperty(value = "refusjonEndring")
    @Valid
    private RefusjonEndring refusjonEndring;

    @JsonProperty(value = "inntektskategoriEndring")
    @Valid
    private InntektskategoriEndring inntektskategoriEndring;


    @JsonProperty(value = "andelsnr")
    @NotNull
    @Valid
    private Long andelsnr;

    @JsonProperty(value = "aktivitetStatus")
    @NotNull
    @Valid
    private AktivitetStatus aktivitetStatus;

    @JsonProperty(value = "arbeidsforholdType")
    @Valid
    private OpptjeningAktivitetType arbeidsforholdType;

    @JsonProperty(value = "arbeidsgiver")
    @Valid
    private Aktør arbeidsgiver;

    @JsonProperty(value = "arbeidsforholdRef")
    @Valid
    private UUID arbeidsforholdRef;

    public BeregningsgrunnlagPrStatusOgAndelEndring() {
        // For Json deserialisering
    }

    public BeregningsgrunnlagPrStatusOgAndelEndring(Long andelsnr, AktivitetStatus aktivitetStatus) {
        this.andelsnr = andelsnr;
        this.aktivitetStatus = aktivitetStatus;
    }

    private BeregningsgrunnlagPrStatusOgAndelEndring(Long andelsnr, OpptjeningAktivitetType arbeidsforholdType) {
        this.andelsnr = andelsnr;
        this.aktivitetStatus = AktivitetStatus.ARBEIDSTAKER;
        this.arbeidsforholdType = arbeidsforholdType;
    }

    public BeregningsgrunnlagPrStatusOgAndelEndring(Long andelsnr, Aktør arbeidsgiver, UUID arbeidsforholdRef) {
        this.andelsnr = andelsnr;
        this.aktivitetStatus = AktivitetStatus.ARBEIDSTAKER;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public UUID getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public InntektEndring getInntektEndring() {
        return inntektEndring;
    }

    public void setInntektEndring(InntektEndring inntektEndring) {
        this.inntektEndring = inntektEndring;
    }



    public InntektskategoriEndring getInntektskategoriEndring() {
        return inntektskategoriEndring;
    }

    public void setInntektskategoriEndring(InntektskategoriEndring inntektskategoriEndring) {
        this.inntektskategoriEndring = inntektskategoriEndring;
    }

    public RefusjonEndring getRefusjonEndring() {
        return refusjonEndring;
    }

    public void setRefusjonEndring(RefusjonEndring refusjonEndring) {
        this.refusjonEndring = refusjonEndring;
    }

    public static BeregningsgrunnlagPrStatusOgAndelEndring opprettForArbeidstakerUtenArbeidsgiver(OpptjeningAktivitetType arbeidsforholdType, @NotNull @Valid Long andelsnr) {
        return new BeregningsgrunnlagPrStatusOgAndelEndring(andelsnr, arbeidsforholdType);
    }

}
