package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagFRISINNDto {

    @JsonProperty(value = "skjæringstidspunkt")
    @NotNull
    @Valid
    private LocalDate skjæringstidspunkt;

    @JsonProperty(value = "aktivitetStatuser")
    @NotNull
    @Size(min = 1, max = 20)
    @Valid
    private List<AktivitetStatus> aktivitetStatuser;

    @JsonProperty(value = "beregningsgrunnlagPerioder")
    @NotNull
    @Size(min = 1, max = 100)
    @Valid
    private List<BeregningsgrunnlagPeriodeFRISINNDto> beregningsgrunnlagPerioder;

    public BeregningsgrunnlagFRISINNDto() {
        // jackson
    }

    public BeregningsgrunnlagFRISINNDto(@JsonProperty(value = "skjæringstidspunkt") @NotNull @Valid LocalDate skjæringstidspunkt,
                                        @JsonProperty(value = "aktivitetStatuser") @NotNull @Size(min = 1, max = 20) @Valid List<AktivitetStatus> aktivitetStatuser,
                                        @JsonProperty(value = "beregningsgrunnlagPerioder") @NotNull @Size(min = 1, max = 100) @Valid List<BeregningsgrunnlagPeriodeFRISINNDto> beregningsgrunnlagPerioder) {
        this.skjæringstidspunkt = skjæringstidspunkt;
        this.aktivitetStatuser = aktivitetStatuser;
        this.beregningsgrunnlagPerioder = beregningsgrunnlagPerioder;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public List<AktivitetStatus> getAktivitetStatuser() {
        return Collections.unmodifiableList(aktivitetStatuser);
    }

    public List<BeregningsgrunnlagPeriodeFRISINNDto> getBeregningsgrunnlagPerioder() {
        return beregningsgrunnlagPerioder
                .stream()
                .sorted(Comparator.comparing(BeregningsgrunnlagPeriodeFRISINNDto::getBeregningsgrunnlagPeriodeFom))
                .collect(Collectors.toUnmodifiableList());
    }

}
