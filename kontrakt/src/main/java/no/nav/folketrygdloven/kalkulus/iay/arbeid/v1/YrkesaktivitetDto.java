package no.nav.folketrygdloven.kalkulus.iay.arbeid.v1;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class YrkesaktivitetDto {

    @JsonProperty("arbeidsgiver")
    @Valid
    private Aktør arbeidsgiver;

    @JsonProperty("abakusReferanse")
    @Valid
    private InternArbeidsforholdRefDto abakusReferanse;

    @JsonProperty("arbeidType")
    @Valid
    @NotNull
    private ArbeidType arbeidType;

    @JsonProperty("aktivitetsAvtaler")
    @Valid
    @Size
    private List<AktivitetsAvtaleDto> aktivitetsAvtaler;

    @JsonProperty("permisjoner")
    @Valid
    @Size
    private List<PermisjonDto> permisjoner;

    protected YrkesaktivitetDto() {
        // default ctor
    }

    public YrkesaktivitetDto(@Valid Aktør arbeidsgiver,
                             @Valid InternArbeidsforholdRefDto abakusReferanse,
                             @Valid @NotNull ArbeidType arbeidType,
                             @Valid List<AktivitetsAvtaleDto> aktivitetsAvtaler) {

        this.arbeidsgiver = arbeidsgiver;
        this.abakusReferanse = abakusReferanse;
        this.arbeidType = arbeidType;
        this.aktivitetsAvtaler = aktivitetsAvtaler;
    }

    public YrkesaktivitetDto(@Valid Aktør arbeidsgiver,
                             @Valid InternArbeidsforholdRefDto abakusReferanse,
                             @Valid @NotNull ArbeidType arbeidType,
                             @Valid @Size List<AktivitetsAvtaleDto> aktivitetsAvtaler,
                             @Valid @Size List<PermisjonDto> permisjoner) {
        this.arbeidsgiver = arbeidsgiver;
        this.abakusReferanse = abakusReferanse;
        this.arbeidType = arbeidType;
        this.aktivitetsAvtaler = aktivitetsAvtaler;
        this.permisjoner = permisjoner;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getAbakusReferanse() {
        return abakusReferanse;
    }

    public ArbeidType getArbeidType() {
        return arbeidType;
    }

    public List<AktivitetsAvtaleDto> getAktivitetsAvtaler() {
        return aktivitetsAvtaler;
    }


    public List<PermisjonDto> getPermisjoner() {
        return permisjoner;
    }

    @AssertTrue(message = "Må ha arbeidsgiver for arbeidtype FRILANSER_OPPDRAGSTAKER eller ORDINÆRT_ARBEIDSFORHOLD.")
    public boolean isOkArbeidsgiver() {
        return arbeidsgiver != null || !(arbeidType.equals(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD) || arbeidType.equals(ArbeidType.FRILANSER_OPPDRAGSTAKER));
    }


}
