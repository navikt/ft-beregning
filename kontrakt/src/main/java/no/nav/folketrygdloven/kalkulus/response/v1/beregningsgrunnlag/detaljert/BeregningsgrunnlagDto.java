package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

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

	@Deprecated // Gå over til å bruke aktivitetStatuserMedHjemmel liste
	@JsonProperty(value = "aktivitetStatuser")
    @NotNull
    @Size(min = 1, max = 20)
    private List<@Valid AktivitetStatus> aktivitetStatuser;

	@JsonProperty(value = "aktivitetStatuserMedHjemmel")
	@NotNull
	@Size(min = 1, max = 20)
    private List<@Valid BeregningsgrunnlagAktivitetStatusDto> aktivitetStatuserMedHjemmel;

    @JsonProperty(value = "beregningsgrunnlagPerioder")
    @NotNull
    @Size(min = 1, max = 100)
    private List<@Valid BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder;

    @JsonProperty(value = "sammenligningsgrunnlagPrStatusListe")
    @Size(max = 10)
    private List<@Valid SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagPrStatusListe;

    @JsonProperty(value = "faktaOmBeregningTilfeller")
    @Size(max = 50)
    private List<@Valid FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller;

    @JsonProperty(value = "overstyrt")
    private boolean overstyrt;

    @JsonProperty(value = "grunnbeløp")
    @Valid
    private Beløp grunnbeløp;

    public BeregningsgrunnlagDto() {
    }

    public BeregningsgrunnlagDto(@NotNull @Valid LocalDate skjæringstidspunkt,
                                 @NotNull @Size(min = 1, max = 20) List<@Valid AktivitetStatus> aktivitetStatuser,
                                 @NotNull @Size(min = 1, max = 100) List<@Valid BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder,
                                 @Size(max = 10) List<@Valid SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagPrStatusListe,
                                 @Size(max = 50) List<@Valid FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller,
                                 boolean overstyrt,
                                 @Valid Beløp grunnbeløp,
                                 @NotNull @Size(min = 1, max = 20) List<@Valid BeregningsgrunnlagAktivitetStatusDto> aktivitetStatuserMedHjemmel) {
        this.skjæringstidspunkt = skjæringstidspunkt;
        this.aktivitetStatuser = aktivitetStatuser;
        this.beregningsgrunnlagPerioder = beregningsgrunnlagPerioder;
        this.sammenligningsgrunnlagPrStatusListe = sammenligningsgrunnlagPrStatusListe;
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
        this.overstyrt = overstyrt;
        this.grunnbeløp = grunnbeløp;
		this.aktivitetStatuserMedHjemmel = aktivitetStatuserMedHjemmel;
    }

	// Kan fjernes når alle aktører sender med status med hjemmel
	@Deprecated
	public BeregningsgrunnlagDto(@NotNull @Valid LocalDate skjæringstidspunkt,
	                             @NotNull @Size(min = 1, max = 20) List<@Valid AktivitetStatus> aktivitetStatuser,
	                             @NotNull @Size(min = 1, max = 100) List<@Valid BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder,
	                             @Size(max = 10) List<@Valid SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagPrStatusListe,
	                             @Size(max = 50) List<@Valid FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller,
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

	public List<BeregningsgrunnlagAktivitetStatusDto> getAktivitetStatuserMedHjemmel() {
		return Collections.unmodifiableList(aktivitetStatuserMedHjemmel);
	}

	public List<AktivitetStatus> getAktivitetStatuser() {
		if (aktivitetStatuserMedHjemmel != null && !aktivitetStatuserMedHjemmel.isEmpty()) {
			return aktivitetStatuserMedHjemmel.stream().map(BeregningsgrunnlagAktivitetStatusDto::getAktivitetStatus).toList();
		}
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
