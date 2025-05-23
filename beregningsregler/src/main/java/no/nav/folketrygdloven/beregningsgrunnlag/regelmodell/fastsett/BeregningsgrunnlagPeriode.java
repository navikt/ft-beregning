package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class BeregningsgrunnlagPeriode {
	@JsonManagedReference
	private final List<BeregningsgrunnlagPrStatus> beregningsgrunnlagPrStatus = new ArrayList<>();
	private final List<TilkommetInntekt> tilkommetInntektsforholdListe = new ArrayList<>();
	private Periode bgPeriode;
	@JsonBackReference
	private Beregningsgrunnlag beregningsgrunnlag;
	private BigDecimal grenseverdi;
	/**
	 * Graderingsprosent fra totalt brutto beregningsgrunnlag
	 */
	private BigDecimal inntektsgraderingFraBruttoBeregningsgrunnlag;

	private BigDecimal totalUtbetalingsgradFraUttak;
	private BigDecimal totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;

	/**
	 * satt hvis folketrygdloven §8-47a påvirker perioden
	 */
	private BigDecimal reduksjonsfaktorInaktivTypeA;

	private Dekningsgrad dekningsgrad = Dekningsgrad.DEKNINGSGRAD_100;
	private boolean erVilkårOppfylt = true;


	private BeregningsgrunnlagPeriode() {
	}

	public BeregningsgrunnlagPrStatus getBeregningsgrunnlagPrStatus(AktivitetStatus aktivitetStatus) {
		return getBeregningsgrunnlagPrStatus().stream()
				.filter(af -> aktivitetStatus.equals(af.getAktivitetStatus()))
				.findFirst()
				.orElse(null);
	}

	public Optional<BeregningsgrunnlagPrStatus> getBeregningsgrunnlagFraDagpenger() {
		return getBeregningsgrunnlagPrStatus().stream()
				.filter(af -> af.getAktivitetStatus().erDP())
				.findFirst();
	}

	@JsonIgnore
	public Beregningsgrunnlag getBeregningsgrunnlag() {
		return beregningsgrunnlag;
	}

	void setBeregningsgrunnlag(Beregningsgrunnlag beregningsgrunnlag) {
		this.beregningsgrunnlag = beregningsgrunnlag;
	}

	void addBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus) {
		Objects.requireNonNull(beregningsgrunnlagPrStatus, "beregningsgrunnlagPrStatus");
		Objects.requireNonNull(beregningsgrunnlagPrStatus.getAktivitetStatus(), "aktivitetStatus");
		this.beregningsgrunnlagPrStatus.add(beregningsgrunnlagPrStatus);
	}

	public BigDecimal getBruttoPrÅr() {
		return getBeregningsgrunnlagPrStatus().stream()
				.map(BeregningsgrunnlagPrStatus::getBruttoPrÅr)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal getAvkortetPrÅr() {
		return getBeregningsgrunnlagPrStatus().stream()
				.map(BeregningsgrunnlagPrStatus::getAvkortetPrÅr)
				.filter(Objects::nonNull)
				.reduce(BigDecimal::add)
				.orElse(null);
	}

	public BigDecimal getRedusertPrÅr() {
		return beregningsgrunnlagPrStatus.stream()
				.map(BeregningsgrunnlagPrStatus::getRedusertPrÅr)
				.filter(Objects::nonNull)
				.reduce(BigDecimal::add)
				.orElse(null);
	}

	public BigDecimal getBruttoPrÅrInkludertNaturalytelser() {
		var naturalytelser = getNaturalytelserBortfaltMinusTilkommetPrÅr();
		var brutto = getBruttoPrÅr();
		return brutto.add(naturalytelser);
	}

	public BigDecimal getAktivitetsgradertBruttoPrÅrInkludertNaturalytelser() {
		return getBeregningsgrunnlagPrStatus().stream()
				.map(BeregningsgrunnlagPrStatus::getAktivitetsgradertBruttoInkludertNaturalytelsePrÅr)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal getNaturalytelserBortfaltMinusTilkommetPrÅr() {
		return beregningsgrunnlagPrStatus.stream()
				.map(BeregningsgrunnlagPrStatus::samletNaturalytelseBortfaltMinusTilkommetPrÅr)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public Periode getBeregningsgrunnlagPeriode() {
		return bgPeriode;
	}

	public LocalDate getPeriodeFom() {
		return bgPeriode.getFom();
	}

	public LocalDate getPeriodeTom() {
		return bgPeriode.getTom();
	}

	public Collection<BeregningsgrunnlagPrStatus> getBeregningsgrunnlagPrStatus() {
		return Collections.unmodifiableCollection(beregningsgrunnlagPrStatus);
	}

	public BigDecimal getGrunnbeløp() {
		return beregningsgrunnlag.getGrunnbeløp();
	}

	public Dekningsgrad getDekningsgrad() {
		return dekningsgrad;
	}

	public List<TilkommetInntekt> getTilkommetInntektsforholdListe() {
		return tilkommetInntektsforholdListe;
	}

	public BigDecimal getGrenseverdi() {
		if (grenseverdi == null) {
			return getBeregningsgrunnlag().getGrunnbeløp().multiply(BigDecimal.valueOf(6));
		}
		return grenseverdi;
	}

	public BigDecimal getYtelsedagerPrÅr() {
		return getBeregningsgrunnlag().getYtelsedagerPrÅr();
	}


	public void setGrenseverdi(BigDecimal grenseverdi) {
		this.grenseverdi = grenseverdi;
	}


	public boolean getErVilkårOppfylt() {
		return erVilkårOppfylt;
	}

	public BigDecimal getInntektsgraderingFraBruttoBeregningsgrunnlag() {
		return inntektsgraderingFraBruttoBeregningsgrunnlag;
	}

	public void setInntektsgraderingFraBruttoBeregningsgrunnlag(BigDecimal inntektsgraderingFraBruttoBeregningsgrunnlag) {
		this.inntektsgraderingFraBruttoBeregningsgrunnlag = inntektsgraderingFraBruttoBeregningsgrunnlag;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder oppdater(BeregningsgrunnlagPeriode eksisterendeBeregningsgrunnlagPeriode) {
		return new Builder(eksisterendeBeregningsgrunnlagPeriode);
	}

	public BigDecimal getTotalUtbetalingsgradFraUttak() {
		return totalUtbetalingsgradFraUttak;
	}

	public void setTotalUtbetalingsgradFraUttak(BigDecimal totalUtbetalingsgradFraUttak) {
		this.totalUtbetalingsgradFraUttak = totalUtbetalingsgradFraUttak;
	}

	public BigDecimal getTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt() {
		return totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;
	}

	public void setTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt(BigDecimal totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt) {
		this.totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt = totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;
	}

	public BigDecimal getReduksjonsfaktorInaktivTypeA() {
		return reduksjonsfaktorInaktivTypeA;
	}

	public void setReduksjonsfaktorInaktivTypeA(BigDecimal reduksjonsfaktorInaktivTypeA) {
		this.reduksjonsfaktorInaktivTypeA = reduksjonsfaktorInaktivTypeA;
	}

	public static class Builder {
		private BeregningsgrunnlagPeriode beregningsgrunnlagPeriodeMal;

		private Builder() {
			beregningsgrunnlagPeriodeMal = new BeregningsgrunnlagPeriode();
		}

		public Builder(BeregningsgrunnlagPeriode eksisterendeBeregningsgrunnlagPeriod) {
			beregningsgrunnlagPeriodeMal = eksisterendeBeregningsgrunnlagPeriod;
		}

		public Builder medPeriode(Periode beregningsgrunnlagPeriode) {
			beregningsgrunnlagPeriodeMal.bgPeriode = beregningsgrunnlagPeriode;
			return this;
		}


		public Builder medErVilkårOppfylt(boolean erVilkårOppfylt) {
			beregningsgrunnlagPeriodeMal.erVilkårOppfylt = erVilkårOppfylt;
			return this;
		}

		public Builder medDekningsgrad(Dekningsgrad dekningsgrad) {
			beregningsgrunnlagPeriodeMal.dekningsgrad = dekningsgrad;
			return this;
		}


		public Builder leggTilTilkommetInntektsforhold(List<TilkommetInntekt> tilkomneInntekter) {
			beregningsgrunnlagPeriodeMal.tilkommetInntektsforholdListe.addAll(tilkomneInntekter);
			return this;
		}


		public Builder medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus) {
			if (beregningsgrunnlagPrStatus.getAndelNr() != null && beregningsgrunnlagPeriodeMal.beregningsgrunnlagPrStatus.stream()
					.anyMatch(bps -> beregningsgrunnlagPrStatus.getAndelNr().equals(bps.getAndelNr()))) {
				throw new IllegalArgumentException("AndelNr er null eller finnes allerede: " + beregningsgrunnlagPrStatus.getAndelNr());
			}
			beregningsgrunnlagPeriodeMal.addBeregningsgrunnlagPrStatus(beregningsgrunnlagPrStatus);
			beregningsgrunnlagPrStatus.setBeregningsgrunnlagPeriode(beregningsgrunnlagPeriodeMal);
			return this;
		}

		public BeregningsgrunnlagPeriode build() {
			verifyStateForBuild();
			return beregningsgrunnlagPeriodeMal;
		}

		private void verifyStateForBuild() {
			Objects.requireNonNull(beregningsgrunnlagPeriodeMal.beregningsgrunnlagPrStatus, "beregningsgrunnlagPrStatus");
			Objects.requireNonNull(beregningsgrunnlagPeriodeMal.bgPeriode, "bgPeriode");
			Objects.requireNonNull(beregningsgrunnlagPeriodeMal.bgPeriode.getFom(), "bgPeriode.getFom()");
		}
	}
}
