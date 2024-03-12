package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningRefusjonOverstyringDto {

    @JsonProperty(value = "arbeidsgiver")
    @Valid
    @NotNull
    private Arbeidsgiver arbeidsgiver;

    @JsonProperty(value = "førsteMuligeRefusjonFom")
    @Valid
    private LocalDate førsteMuligeRefusjonFom;

    @JsonProperty(value = "erFristUtvidet")
    @Valid
    private Boolean erFristUtvidet;


    @JsonProperty(value = "refusjonPerioder")
    @Valid
    @Size(min = 1)
    private List<BeregningRefusjonPeriodeDto> refusjonPerioder;

    public BeregningRefusjonOverstyringDto() {
    }

    public BeregningRefusjonOverstyringDto(Arbeidsgiver arbeidsgiver, LocalDate førsteMuligeRefusjonFom, Boolean erFristUtvidet) {
        this.arbeidsgiver = arbeidsgiver;
        this.førsteMuligeRefusjonFom = førsteMuligeRefusjonFom;
        this.erFristUtvidet = erFristUtvidet;
    }

    public BeregningRefusjonOverstyringDto(@Valid @NotNull Arbeidsgiver arbeidsgiver,
                                           @Valid LocalDate førsteMuligeRefusjonFom,
                                           @Valid List<BeregningRefusjonPeriodeDto> refusjonPerioder) {
        this.arbeidsgiver = arbeidsgiver;
        this.førsteMuligeRefusjonFom = førsteMuligeRefusjonFom;
        this.refusjonPerioder = refusjonPerioder;
    }


    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public LocalDate getFørsteMuligeRefusjonFom() {
        return førsteMuligeRefusjonFom;
    }

    public Boolean getErFristUtvidet() {
        return erFristUtvidet;
    }
}
