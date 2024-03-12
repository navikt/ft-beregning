package no.nav.folketrygdloven.kalkulus.felles.v1;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.beregning.v1.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.KravperioderPrArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.beregning.v1.YtelsespesifiktGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;

/**
 * NB: Denne blir lagret som json struktur i db for senere sammenligning. Endringer her kan derfor påvirke hvordan det blir lagret, og hva
 * som oppfattes som endret input senere.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class KalkulatorInputDto {

    /**
     * Informasjon vedrørende datoer for refusjon pr arbeidsgiver
     */
    @JsonProperty(value = "refusjonskravDatoer")
    @Valid
    @Size()
    @Deprecated(forRemoval = true)
    private List<RefusjonskravDatoDto> refusjonskravDatoer;

    /**
     * Informasjon vedrørende refusjonskrav pr arbeidsgiver
     */
    @JsonProperty(value = "refusjonskravPrArbeidsgiver")
    @Valid
    @Size()
    private List<KravperioderPrArbeidsforhold> refusjonskravPrArbeidsforhold;

    /**
     * Aggregat for inntekt, arbeid og ytelser
     */
    @JsonProperty(value = "iayGrunnlag", required = true)
    @NotNull
    @Valid
    private InntektArbeidYtelseGrunnlagDto iayGrunnlag;

    /**
     * Aktiviteter som skal brukes i utledning av aktivitetstatus på skjæringstidspunktet
     */
    @JsonProperty(value = "opptjeningAktiviteter", required = true)
    @NotNull
    @Valid
    private OpptjeningAktiviteterDto opptjeningAktiviteter;

    /**
     * Ytelsesspesifikt grunnlag
     */
    @JsonProperty(value = "ytelsespesifiktGrunnlag")
    @Valid
    private YtelsespesifiktGrunnlagDto ytelsespesifiktGrunnlag;

    /**
     * Skjæringstidspunkt for ytelse
     */
    @JsonProperty(value = "skjæringstidspunkt")
    @Valid
    @NotNull
    private LocalDate skjæringstidspunkt;

    protected KalkulatorInputDto() {
        // default ctor
    }

    public KalkulatorInputDto(@NotNull @Valid InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                              @NotNull @Valid OpptjeningAktiviteterDto opptjeningAktiviteter,
                              @NotNull @Valid LocalDate skjæringstidspunkt) {
        this.iayGrunnlag = iayGrunnlag;
        this.opptjeningAktiviteter = opptjeningAktiviteter;
        this.skjæringstidspunkt = skjæringstidspunkt;
    }

    public List<RefusjonskravDatoDto> getRefusjonskravDatoer() {
        return refusjonskravDatoer;
    }

    public List<KravperioderPrArbeidsforhold> getRefusjonskravPrArbeidsforhold() {
        return refusjonskravPrArbeidsforhold;
    }

    public InntektArbeidYtelseGrunnlagDto getIayGrunnlag() {
        return iayGrunnlag;
    }

    public OpptjeningAktiviteterDto getOpptjeningAktiviteter() {
        return opptjeningAktiviteter;
    }

    public YtelsespesifiktGrunnlagDto getYtelsespesifiktGrunnlag() {
        return ytelsespesifiktGrunnlag;
    }

    public KalkulatorInputDto medRefusjonskravDatoer(List<RefusjonskravDatoDto> refusjonskravDatoer) {
        this.refusjonskravDatoer = refusjonskravDatoer;
        return this;
    }

    public KalkulatorInputDto medRefusjonsperioderPrInntektsmelding(List<KravperioderPrArbeidsforhold> refusjonsperioderPrInntektsmelding) {
        this.refusjonskravPrArbeidsforhold = refusjonsperioderPrInntektsmelding;
        return this;
    }

    public KalkulatorInputDto medYtelsespesifiktGrunnlag(YtelsespesifiktGrunnlagDto ytelsespesifiktGrunnlag) {
        this.ytelsespesifiktGrunnlag = ytelsespesifiktGrunnlag;
        return this;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

}
