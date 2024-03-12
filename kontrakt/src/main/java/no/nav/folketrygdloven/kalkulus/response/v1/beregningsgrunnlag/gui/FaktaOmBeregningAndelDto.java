package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FaktaOmBeregningAndelDto {

    @Valid
    @JsonProperty("andelsnr")
    @Min(0)
    @Max(1000)
    @NotNull
    private Long andelsnr;

    @Valid
    @JsonProperty("arbeidsforhold")
    private BeregningsgrunnlagArbeidsforholdDto arbeidsforhold;

    @Valid
    @JsonProperty("inntektskategori")
    @NotNull
    private Inntektskategori inntektskategori;

    @Valid
    @JsonProperty("aktivitetStatus")
    @NotNull
    private AktivitetStatus aktivitetStatus;

    @Valid
    @JsonProperty("kilde")
    @NotNull
    private AndelKilde kilde;

    @Valid
    @JsonProperty("lagtTilAvSaksbehandler")
    private Boolean lagtTilAvSaksbehandler = false;

    @Valid
    @JsonProperty("fastsattAvSaksbehandler")
    private Boolean fastsattAvSaksbehandler = false;

    @Valid
    @JsonProperty("andelIArbeid")
    @Size(max = 100)
    @NotNull
    private List<BigDecimal> andelIArbeid = new ArrayList<>();

    public FaktaOmBeregningAndelDto(Long andelsnr,
                                    BeregningsgrunnlagArbeidsforholdDto arbeidsforhold,
                                    Inntektskategori inntektskategori,
                                    AktivitetStatus aktivitetStatus,
                                    Boolean lagtTilAvSaksbehandler,
                                    Boolean fastsattAvSaksbehandler,
                                    List<BigDecimal> andelIArbeid, AndelKilde kilde) {
        this.andelsnr = andelsnr;
        this.arbeidsforhold = arbeidsforhold;
        this.inntektskategori = inntektskategori;
        this.aktivitetStatus = aktivitetStatus;
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
        this.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
        this.andelIArbeid = andelIArbeid;
        this.kilde = kilde;
    }

    public FaktaOmBeregningAndelDto() {
        // Hibernate
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FaktaOmBeregningAndelDto that = (FaktaOmBeregningAndelDto) o;
        return Objects.equals(arbeidsforhold, that.arbeidsforhold) &&
            Objects.equals(inntektskategori, that.inntektskategori) &&
            Objects.equals(aktivitetStatus, that.aktivitetStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsforhold, inntektskategori, aktivitetStatus);
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public void setAndelsnr(Long andelsnr) {
        this.andelsnr = andelsnr;
    }

    public BeregningsgrunnlagArbeidsforholdDto getArbeidsforhold() {
        return arbeidsforhold;
    }

    public void setArbeidsforhold(BeregningsgrunnlagArbeidsforholdDto arbeidsforhold) {
        this.arbeidsforhold = arbeidsforhold;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public void setInntektskategori(Inntektskategori inntektskategori) {
        this.inntektskategori = inntektskategori;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public void setAktivitetStatus(AktivitetStatus aktivitetStatus) {
        this.aktivitetStatus = aktivitetStatus;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public void setLagtTilAvSaksbehandler(Boolean lagtTilAvSaksbehandler) {
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
    }

    public Boolean getFastsattAvSaksbehandler() {
        return fastsattAvSaksbehandler;
    }

    public void setFastsattAvSaksbehandler(Boolean fastsattAvSaksbehandler) {
        this.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
    }

    public List<BigDecimal> getAndelIArbeid() {
        return andelIArbeid;
    }

    public void leggTilAndelIArbeid(BigDecimal andelIArbeid) {
        this.andelIArbeid.add(andelIArbeid);
    }

    public AndelKilde getKilde() {
        return kilde;
    }

    public void setKilde(AndelKilde kilde) {
        this.kilde = kilde;
    }
}
