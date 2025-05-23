package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

public class BeregningsgrunnlagPrStatus {
	// Input
	@JsonBackReference
	protected BeregningsgrunnlagPeriode beregningsgrunnlagPeriode;
	private AktivitetStatus aktivitetStatus;
	protected List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = new ArrayList<>();
	private Long andelNr;
	private Boolean erSøktYtelseFor;
	// Alltid full utbetaling for foreldrepenger
	private BigDecimal utbetalingsprosent = BigDecimal.valueOf(100);
	private BigDecimal aktivitetsgrad; // For bruk til å regne fortsatt arbeid (Mer nøyaktig enn å se på (1-utbetalingsprosent)
	private BigDecimal andelsmessigFørGraderingPrAar;
	private BigDecimal bruttoPrÅr;

	// Beregnet eller skjønnsfastsatt inntekt
	private BigDecimal inntektsgrunnlagPrÅr;

	// Output
	private BigDecimal avkortetPrÅr;
	private BigDecimal redusertPrÅr;


	@JsonIgnore
	public BeregningsgrunnlagPeriode getBeregningsgrunnlagPeriode() {
		return beregningsgrunnlagPeriode;
	}

	public AktivitetStatus getAktivitetStatus() {
		return aktivitetStatus;
	}

	public BigDecimal getAvkortetPrÅr() {
		return avkortetPrÅr != null ? avkortetPrÅr : getArbeidsforhold().stream()
				.map(BeregningsgrunnlagPrArbeidsforhold::getAvkortetPrÅr)
				.filter(Objects::nonNull)
				.reduce(BigDecimal::add)
				.orElse(null);
	}

	public BigDecimal getRedusertPrÅr() {
		return redusertPrÅr != null ? redusertPrÅr : getArbeidsforhold().stream()
				.map(BeregningsgrunnlagPrArbeidsforhold::getRedusertPrÅr)
				.filter(Objects::nonNull)
				.reduce(BigDecimal::add)
				.orElse(null);
	}


	public BigDecimal getInntektsgrunnlagPrÅr() {
		return inntektsgrunnlagPrÅr != null ? inntektsgrunnlagPrÅr : arbeidsforhold.stream()
				.map(BeregningsgrunnlagPrArbeidsforhold::getInntektsgrunnlagPrÅr)
				.filter(Objects::nonNull)
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
	}

	public BigDecimal getGradertInntektsgrunnlagInkludertNaturalytelsePrÅr() {
		return inntektsgrunnlagPrÅr != null ? finnGradert(inntektsgrunnlagPrÅr) : getArbeidsforhold().stream()
				.map(BeregningsgrunnlagPrArbeidsforhold::getGradertInntektsgrunnlagInkludertNaturalytelsePrÅr)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public boolean erArbeidstakerEllerFrilanser() {
		return AktivitetStatus.erArbeidstaker(aktivitetStatus) || AktivitetStatus.erFrilanser(aktivitetStatus);
	}

	public BigDecimal samletNaturalytelseBortfaltMinusTilkommetPrÅr() {
        var sumBortfaltNaturalYtelse = getArbeidsforhold().stream()
				.map(BeregningsgrunnlagPrArbeidsforhold::getNaturalytelseBortfaltPrÅr)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
        var sumTilkommetNaturalYtelse = getArbeidsforhold().stream()
				.map(BeregningsgrunnlagPrArbeidsforhold::getNaturalytelseTilkommetPrÅr)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return sumBortfaltNaturalYtelse.subtract(sumTilkommetNaturalYtelse);
	}

	public BigDecimal getBruttoPrÅr() {
		return bruttoPrÅr != null ? bruttoPrÅr : getArbeidsforhold().stream()
				.map(BeregningsgrunnlagPrArbeidsforhold::getBruttoPrÅr)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal getGradertBruttoPrÅr() {
		return bruttoPrÅr != null ? finnGradert(bruttoPrÅr) : getArbeidsforhold().stream()
				.map(BeregningsgrunnlagPrArbeidsforhold::getGradertBruttoPrÅr)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal getAktivitetsgradertBruttoPrÅr() {
		return bruttoPrÅr != null ? finnAktivitetsgradert(bruttoPrÅr) : getArbeidsforhold().stream()
				.map(BeregningsgrunnlagPrArbeidsforhold::getAktivitetsgradertBruttoPrÅr)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal finnGradert(BigDecimal verdi) {
		return verdi == null ? null : verdi.multiply(utbetalingsprosent.scaleByPowerOfTen(-2));
	}

	private BigDecimal finnAktivitetsgradert(BigDecimal verdi) {
		if (verdi == null) {
			return null;
		}
		if (aktivitetsgrad == null) {
			return finnGradert(verdi);
		}
		return verdi.multiply(BigDecimal.valueOf(100).subtract(aktivitetsgrad).scaleByPowerOfTen(-2));
	}


	public BigDecimal getBruttoInkludertNaturalytelsePrÅr() {
        var brutto = getBruttoPrÅr();
        var samletNaturalytelse = samletNaturalytelseBortfaltMinusTilkommetPrÅr();
		return brutto.add(samletNaturalytelse);
	}

	public BigDecimal getInntektsgrunnlagInkludertNaturalytelsePrÅr() {
        var inntektsgrunnlag = getInntektsgrunnlagPrÅr();
        var samletNaturalytelse = samletNaturalytelseBortfaltMinusTilkommetPrÅr();
		return inntektsgrunnlag.add(samletNaturalytelse);
	}

	public BigDecimal getGradertBruttoInkludertNaturalytelsePrÅr() {
		return bruttoPrÅr != null ? finnGradert(getBruttoInkludertNaturalytelsePrÅr()) : getArbeidsforhold().stream()
				.map(BeregningsgrunnlagPrArbeidsforhold::getGradertBruttoInkludertNaturalytelsePrÅr)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal getAktivitetsgradertBruttoInkludertNaturalytelsePrÅr() {
		return bruttoPrÅr != null ? finnAktivitetsgradert(getBruttoInkludertNaturalytelsePrÅr()) : getArbeidsforhold().stream()
				.map(BeregningsgrunnlagPrArbeidsforhold::getAktivitetsgradertBruttoInkludertNaturalytelsePrÅr)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public List<BeregningsgrunnlagPrArbeidsforhold> getArbeidsforhold() {
		return Collections.unmodifiableList(arbeidsforhold);
	}

	public Optional<BigDecimal> getAktivitetsgrad() {
		return Optional.ofNullable(aktivitetsgrad);
	}

	public List<BeregningsgrunnlagPrArbeidsforhold> getArbeidsforholdIkkeFrilans() {
		return getArbeidsforhold().stream().filter(af -> !af.erFrilanser()).toList();
	}

	public Optional<BeregningsgrunnlagPrArbeidsforhold> getFrilansArbeidsforhold() {
		return getArbeidsforhold().stream().filter(BeregningsgrunnlagPrArbeidsforhold::erFrilanser).findAny();
	}


	public Long getAndelNr() {
		return andelNr;
	}

	public BigDecimal getUtbetalingsprosent() {
		return utbetalingsprosent;
	}

	public boolean erSøktYtelseFor() {
		return (erSøktYtelseFor != null && erSøktYtelseFor)
				|| (erSøktYtelseFor == null && utbetalingsprosent.compareTo(BigDecimal.ZERO) > 0)
				|| aktivitetStatus.equals(AktivitetStatus.ATFL);
	}

	public void setErSøktYtelseFor(boolean erSøktYtelseFor) {
		this.erSøktYtelseFor = erSøktYtelseFor;
	}

	void setBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
		this.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
	}

	public BigDecimal getAndelsmessigFørGraderingPrAar() {
		return andelsmessigFørGraderingPrAar;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus) {
		return new Builder(beregningsgrunnlagPrStatus);
	}

	public static class Builder {
		private BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatusMal;

		public Builder() {
			beregningsgrunnlagPrStatusMal = new BeregningsgrunnlagPrStatus();
		}

		public Builder(BeregningsgrunnlagPrStatus eksisterendeBGPrStatusMal) {
			beregningsgrunnlagPrStatusMal = eksisterendeBGPrStatusMal;
		}

		public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
			beregningsgrunnlagPrStatusMal.aktivitetStatus = aktivitetStatus;
			return this;
		}

		public Builder medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold beregningsgrunnlagPrArbeidsforhold) {
			beregningsgrunnlagPrStatusMal.arbeidsforhold.add(beregningsgrunnlagPrArbeidsforhold);
			return this;
		}

		public Builder medBruttoPrÅr(BigDecimal bruttoPrÅr) {
			sjekkIkkeArbeidstaker();
			beregningsgrunnlagPrStatusMal.bruttoPrÅr = bruttoPrÅr;
			return this;
		}

		public Builder medInntektsgrunnlagPrÅr(BigDecimal inntekt) {
			sjekkIkkeArbeidstaker();
			beregningsgrunnlagPrStatusMal.inntektsgrunnlagPrÅr = inntekt;
			return this;
		}

		public Builder medAndelsmessigFørGraderingPrAar(BigDecimal andelsmessigFørGraderingPrAar) {
			sjekkIkkeArbeidstaker();
			beregningsgrunnlagPrStatusMal.andelsmessigFørGraderingPrAar = andelsmessigFørGraderingPrAar;
			return this;
		}

		public Builder medAvkortetPrÅr(BigDecimal avkortetPrÅr) {
			sjekkIkkeArbeidstaker();
			beregningsgrunnlagPrStatusMal.avkortetPrÅr = avkortetPrÅr;
			return this;
		}

		public Builder medRedusertPrÅr(BigDecimal redusertPrÅr) {
			sjekkIkkeArbeidstaker();
			beregningsgrunnlagPrStatusMal.redusertPrÅr = redusertPrÅr;
			return this;
		}

		public Builder medBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
			beregningsgrunnlagPrStatusMal.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
			beregningsgrunnlagPeriode.addBeregningsgrunnlagPrStatus(beregningsgrunnlagPrStatusMal);
			return this;
		}

		private void sjekkIkkeArbeidstaker() {
			if (beregningsgrunnlagPrStatusMal.aktivitetStatus == null || AktivitetStatus.erArbeidstaker(beregningsgrunnlagPrStatusMal.aktivitetStatus)) {
				throw new IllegalArgumentException("Kan ikke overstyre aggregert verdi for status ATFL");
			}
		}

		public Builder medArbeidsforhold(List<Arbeidsforhold> arbeidsforhold) {
			if (arbeidsforhold != null) {
                var andelNr = 1;
				for (var af : arbeidsforhold) {
					beregningsgrunnlagPrStatusMal.arbeidsforhold.add(BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(af).medAndelNr(andelNr++).build());
				}
			}
			return this;
		}

		// Kun brukt i test
		public Builder medArbeidsforhold(List<Arbeidsforhold> arbeidsforhold, List<BigDecimal> refusjonskravPrÅr) {
			if (arbeidsforhold != null) {
				if (!refusjonskravPrÅr.isEmpty() && arbeidsforhold.size() != refusjonskravPrÅr.size()) {
					throw new IllegalArgumentException("Lengde på arbeidsforhold og refusjonskravPrÅr må vere like");
				}
                var andelNr = 1;
				for (var i = 0; i < arbeidsforhold.size(); i++) {
					beregningsgrunnlagPrStatusMal.arbeidsforhold.add(BeregningsgrunnlagPrArbeidsforhold.builder()
							.medArbeidsforhold(arbeidsforhold.get(i))
							.medAndelNr(andelNr++)
							.medRefusjonPrÅr(refusjonskravPrÅr.isEmpty() ? null : refusjonskravPrÅr.get(i))
							.build());
				}
			}
			return this;
		}


		public Builder medAndelNr(Long andelNr) {
			beregningsgrunnlagPrStatusMal.andelNr = andelNr;
			return this;
		}

		public Builder medUtbetalingsprosent(BigDecimal utbetalingsprosent) {
			beregningsgrunnlagPrStatusMal.utbetalingsprosent = utbetalingsprosent;
			return this;
		}

		public Builder medAktivitetsgrad(BigDecimal aktivitetsgrad) {
			if (aktivitetsgrad != null && (aktivitetsgrad.compareTo(BigDecimal.ZERO) < 0 || aktivitetsgrad.compareTo(BigDecimal.valueOf(100)) > 0)) {
					throw new IllegalArgumentException("Aktivitetsgrad må ha verdi fra 0 til 100, faktisk: " + aktivitetsgrad);
			}
			beregningsgrunnlagPrStatusMal.aktivitetsgrad = aktivitetsgrad;
			return this;
		}

		public Builder medErSøktYtelseFor(boolean erSøktYtelseFor) {
			beregningsgrunnlagPrStatusMal.erSøktYtelseFor = erSøktYtelseFor;
			return this;
		}

		public BeregningsgrunnlagPrStatus build() {
			verifyStateForBuild();
			return beregningsgrunnlagPrStatusMal;
		}

		private void verifyStateForBuild() {
			Objects.requireNonNull(beregningsgrunnlagPrStatusMal.aktivitetStatus, "aktivitetStatus");
			if (AktivitetStatus.ATFL.equals(beregningsgrunnlagPrStatusMal.aktivitetStatus) || AktivitetStatus.AT.equals(beregningsgrunnlagPrStatusMal.aktivitetStatus)) {
				if (beregningsgrunnlagPrStatusMal.andelNr != null) {
					throw new IllegalArgumentException("Andelsnr kan ikke angis for andel med status " + beregningsgrunnlagPrStatusMal.aktivitetStatus);
				}
			} else {
				Objects.requireNonNull(beregningsgrunnlagPrStatusMal.andelNr, "andelNr");
			}
		}
	}
}
