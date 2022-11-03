package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class BeregningsgrunnlagPeriode {
	@JsonManagedReference
	private final List<BeregningsgrunnlagPrStatus> beregningsgrunnlagPrStatus = new ArrayList<>();
	private Periode bgPeriode;
	@JsonBackReference
	private Beregningsgrunnlag beregningsgrunnlag;
	private BigDecimal grenseverdi;
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

	public BigDecimal getGradertBruttoPrÅr() {
		return getBeregningsgrunnlagPrStatus().stream()
				.map(BeregningsgrunnlagPrStatus::getGradertBruttoPrÅr)
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
		BigDecimal naturalytelser = getNaturalytelserBortfaltMinusTilkommetPrÅr();
		BigDecimal brutto = getBruttoPrÅr();
		return brutto.add(naturalytelser);
	}

	public BigDecimal getGradertBruttoPrÅrInkludertNaturalytelser() {
		BigDecimal naturalytelser = getGradertNaturalytelserBortfaltMinusTilkommetPrÅr();
		BigDecimal brutto = getGradertBruttoPrÅr();
		return brutto.add(naturalytelser);
	}

	private BigDecimal getNaturalytelserBortfaltMinusTilkommetPrÅr() {
		return beregningsgrunnlagPrStatus.stream()
				.map(BeregningsgrunnlagPrStatus::samletNaturalytelseBortfaltMinusTilkommetPrÅr)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal getGradertNaturalytelserBortfaltMinusTilkommetPrÅr() {
		return beregningsgrunnlagPrStatus.stream()
				.map(BeregningsgrunnlagPrStatus::samletGradertNaturalytelseBortfaltMinusTilkommetPrÅr)
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

	public List<BeregningsgrunnlagPrStatus> getBeregningsgrunnlagPrStatusSomSkalBrukes() {
		return beregningsgrunnlagPrStatus.stream().filter(BeregningsgrunnlagPrStatus::erSøktYtelseFor).toList();
	}

	public BigDecimal getGrunnbeløp() {
		return beregningsgrunnlag.getGrunnbeløp();
	}

	public Dekningsgrad getDekningsgrad() {
		return dekningsgrad;
	}

	public List<AktivitetStatusMedHjemmel> getAktivitetStatuser() {
		return beregningsgrunnlag.getAktivitetStatuser().stream()
				.sorted(Comparator.comparing(as -> as.getAktivitetStatus().getBeregningPrioritet())).toList();
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

	public static Builder builder() {
		return new Builder();
	}

	public static Builder oppdater(BeregningsgrunnlagPeriode eksisterendeBeregningsgrunnlagPeriode) {
		return new Builder(eksisterendeBeregningsgrunnlagPeriode);
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
