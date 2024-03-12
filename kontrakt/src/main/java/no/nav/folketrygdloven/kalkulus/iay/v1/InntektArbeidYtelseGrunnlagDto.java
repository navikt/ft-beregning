package no.nav.folketrygdloven.kalkulus.iay.v1;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntekterDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntektsmeldingerDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelserDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OppgittOpptjeningDto;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class InntektArbeidYtelseGrunnlagDto {

    /**
     * Informasjon om arbeid og arbeidsperioder fra med kilde aa-reg
     */
    @JsonProperty(value = "arbeidDto")
    @Valid
    private ArbeidDto arbeidDto;

    /**
     * Informasjon om inntekter. Kilder er Sigrun og A-inntekt
     */
    @JsonProperty(value = "inntekterDto")
    @Valid
    private InntekterDto inntekterDto;

    /**
     * Informasjon om ytelse. Kilder er arena, infotrygd, fp-sak, k9-sak og SPokelse
     */
    @JsonProperty(value = "ytelserDto")
    @Valid
    private YtelserDto ytelserDto;

    /**
     * Informasjon om opptjening som er oppgitt av bruker
     */
    @JsonProperty(value = "oppgittOpptjening")
    @Valid
    private OppgittOpptjeningDto oppgittOpptjening;

    /**
     * Inntektsmeldinger
     */
    @JsonProperty(value = "inntektsmeldinger")
    @Valid
    private InntektsmeldingerDto inntektsmeldinger;

    /**
     * Informasjon om overstyring av arbeidsforholdinformasjon
     */
    @JsonProperty(value = "arbeidsforholdInformasjon")
    @Valid
    private ArbeidsforholdInformasjonDto arbeidsforholdInformasjon;

    public InntektArbeidYtelseGrunnlagDto() {
        // default ctor
    }

    public InntektArbeidYtelseGrunnlagDto medArbeidDto(ArbeidDto arbeidDto) {
        this.arbeidDto = arbeidDto;
        return this;
    }

    public InntektArbeidYtelseGrunnlagDto medInntekterDto(InntekterDto inntekterDto) {
        this.inntekterDto = inntekterDto;
        return this;
    }

    public InntektArbeidYtelseGrunnlagDto medYtelserDto(YtelserDto ytelserDto) {
        this.ytelserDto = ytelserDto;
        return this;
    }

    public InntektArbeidYtelseGrunnlagDto medOppgittOpptjeningDto(OppgittOpptjeningDto oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
        return this;
    }

    public InntektArbeidYtelseGrunnlagDto medInntektsmeldingerDto(InntektsmeldingerDto inntektsmeldinger) {
        this.inntektsmeldinger = inntektsmeldinger;
        return this;
    }

    public InntektArbeidYtelseGrunnlagDto medArbeidsforholdInformasjonDto(ArbeidsforholdInformasjonDto arbeidsforholdInformasjon) {
        this.arbeidsforholdInformasjon = arbeidsforholdInformasjon;
        return this;
    }

    public ArbeidDto getArbeidDto() {
        return arbeidDto;
    }

    public InntekterDto getInntekterDto() {
        return inntekterDto;
    }

    public YtelserDto getYtelserDto() {
        return ytelserDto;
    }

    public OppgittOpptjeningDto getOppgittOpptjening() {
        return oppgittOpptjening;
    }

    public InntektsmeldingerDto getInntektsmeldingDto() {
        return inntektsmeldinger;
    }

    public ArbeidsforholdInformasjonDto getArbeidsforholdInformasjon() {
        return arbeidsforholdInformasjon;
    }
}
