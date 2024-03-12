package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.SammenligningsgrunnlagPrStatusDto;

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

    @JsonProperty(value = "grunnbeløp")
    @Valid
    private Beløp grunnbeløp;

    public BeregningsgrunnlagDto() {
    }

    public BeregningsgrunnlagDto(LocalDate skjæringstidspunkt,
                                 List<AktivitetStatus> aktivitetStatuser,
                                 List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder,
                                 List<SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagPrStatusListe,
                                 Beløp grunnbeløp) {
        this.skjæringstidspunkt = skjæringstidspunkt;
        this.aktivitetStatuser = aktivitetStatuser;
        this.beregningsgrunnlagPerioder = beregningsgrunnlagPerioder;
        this.sammenligningsgrunnlagPrStatusListe = sammenligningsgrunnlagPrStatusListe;
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
                .sorted(Comparator.comparing(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPeriodeFom)).toList();
    }

    public Beløp getGrunnbeløp() {
        return grunnbeløp;
    }

    public List<SammenligningsgrunnlagPrStatusDto> getSammenligningsgrunnlagPrStatusListe() {
        return sammenligningsgrunnlagPrStatusListe;
    }
}
