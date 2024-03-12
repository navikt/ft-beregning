package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagDto {

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
    private List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder;

    @JsonProperty(value = "sammenligningsgrunnlagPrStatusListe")
    @Size(max = 10)
    @Valid
    private List<SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagPrStatusListe;

    @JsonProperty(value = "faktaOmBeregningTilfeller")
    @Size(max = 50)
    @Valid
    private List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller;

    @JsonProperty(value = "overstyrt")
    private boolean overstyrt;

    @JsonProperty(value = "grunnbeløp")
    @Valid
    private Beløp grunnbeløp;

    public BeregningsgrunnlagDto() {
    }

    public BeregningsgrunnlagDto(@NotNull @Valid LocalDate skjæringstidspunkt,
                                 @NotNull @Size(min = 1, max = 20) @Valid List<AktivitetStatus> aktivitetStatuser,
                                 @NotNull @Size(min = 1, max = 100) @Valid List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder,
                                 @Size(max = 10) @Valid List<SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagPrStatusListe,
                                 @Size(max = 50) @Valid List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller,
                                 boolean overstyrt,
                                 @Valid Beløp grunnbeløp) {
        this.skjæringstidspunkt = skjæringstidspunkt;
        this.aktivitetStatuser = aktivitetStatuser;
        this.beregningsgrunnlagPerioder = beregningsgrunnlagPerioder;
        this.sammenligningsgrunnlagPrStatusListe = sammenligningsgrunnlagPrStatusListe;
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
        this.overstyrt = overstyrt;
        this.grunnbeløp = grunnbeløp;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public List<AktivitetStatus> getAktivitetStatuser() {
        return Collections.unmodifiableList(aktivitetStatuser);
    }

    public List<BeregningsgrunnlagPeriodeDto> getBeregningsgrunnlagPerioder() {
        return beregningsgrunnlagPerioder
            .stream()
            .sorted(Comparator.comparing(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPeriodeFom))
            .collect(Collectors.toUnmodifiableList());
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller;
    }

    public List<SammenligningsgrunnlagPrStatusDto> getSammenligningsgrunnlagPrStatusListe() {
        return sammenligningsgrunnlagPrStatusListe;
    }

    public boolean isOverstyrt() {
        return overstyrt;
    }

    public Beløp getGrunnbeløp() {
        return grunnbeløp;
    }
}
