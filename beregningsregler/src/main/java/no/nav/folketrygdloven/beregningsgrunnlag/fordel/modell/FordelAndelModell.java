package no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;

public class FordelAndelModell {
	private AktivitetStatus aktivitetStatus;
	private Boolean erSøktYtelseFor = Boolean.TRUE;
	private BigDecimal utbetalingsgrad = BigDecimal.valueOf(100);
	private BigDecimal foreslåttPrÅr;
	private BigDecimal fordeltPrÅr;
	private Inntektskategori inntektskategori;
	private BigDecimal naturalytelseBortfaltPrÅr;
	private BigDecimal naturalytelseTilkommetPrÅr;
	private BigDecimal gjeldendeRefusjonPrÅr;
	private BigDecimal fordeltRefusjonPrÅr;
	private BigDecimal beløpFraInntektsMeldingPrMnd;
	private Arbeidsforhold arbeidsforhold;
	private Long andelNr;
	private boolean erNytt;

	private FordelAndelModell() {
		// For bruk i builder
	}

	public AktivitetStatus getAktivitetStatus() {
		return aktivitetStatus;
	}

	public boolean erSøktYtelseFor() {
		return erSøktYtelseFor;
	}

	public BigDecimal getUtbetalingsgrad() {
		return utbetalingsgrad;
	}

	public Optional<BigDecimal> getBruttoPrÅr() {
		if (getFordeltPrÅr().isPresent()) {
			return getFordeltPrÅr();
		}
		return getForeslåttPrÅr();
	}

	public Optional<BigDecimal> getGradertBruttoPrÅr() {
		return getBruttoPrÅr().map(b -> b.multiply(utbetalingsgrad).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
	}


	public Optional<BigDecimal> getFordeltPrÅr() {
		return Optional.ofNullable(fordeltPrÅr);
	}

	public Optional<BigDecimal> getGradertFordeltPrÅr() {
		return getFordeltPrÅr().map(b -> b.multiply(utbetalingsgrad).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
	}

	public Inntektskategori getInntektskategori() {
		return inntektskategori;
	}

	public Optional<BigDecimal> getNaturalytelseBortfaltPrÅr() {
		return Optional.ofNullable(naturalytelseBortfaltPrÅr);
	}

	public Optional<BigDecimal> getGradertNaturalytelseBortfaltPrÅr() {
		return getNaturalytelseBortfaltPrÅr().map(b -> b.multiply(utbetalingsgrad).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
	}

	public Optional<BigDecimal> getNaturalytelseTilkommetPrÅr() {
		return Optional.ofNullable(naturalytelseTilkommetPrÅr);
	}

	public Optional<BigDecimal> getGradertNaturalytelseTilkommetPrÅr() {
		return getNaturalytelseTilkommetPrÅr().map(b -> b.multiply(utbetalingsgrad).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
	}


	public Optional<BigDecimal> getGjeldendeRefusjonPrÅr() {
		return Optional.ofNullable(gjeldendeRefusjonPrÅr);
	}

	public Optional<BigDecimal> getGradertRefusjonPrÅr() {
		return getGjeldendeRefusjonPrÅr().map(b -> b.multiply(utbetalingsgrad).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
	}



	public Long getAndelNr() {
		return andelNr;
	}

	public boolean erNytt() {
		return erNytt;
	}

	public Optional<BigDecimal> getBeløpFraInntektsMeldingPrMnd() {
		return Optional.ofNullable(beløpFraInntektsMeldingPrMnd);
	}

	public Optional<BigDecimal> getFordeltRefusjonPrÅr() {
		return Optional.ofNullable(fordeltRefusjonPrÅr);
	}

	public Optional<Arbeidsforhold> getArbeidsforhold() {
		return Optional.ofNullable(arbeidsforhold);
	}

	public Optional<BigDecimal> getForeslåttPrÅr() {
		return Optional.ofNullable(foreslåttPrÅr);
	}

	public Optional<BigDecimal> getGradertForeslåttPrÅr() {
		return getForeslåttPrÅr().map(b -> b.multiply(utbetalingsgrad).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder oppdater(FordelAndelModell kopi) {
		return new Builder(kopi);
	}

	public Optional<BigDecimal> getBruttoInkludertNaturalytelsePrÅr() {
		var brutto = getBruttoPrÅr();
		if (brutto.isEmpty()) {
			return Optional.empty();
		}
		var bortfalt = getNaturalytelseBortfaltPrÅr().orElse(BigDecimal.ZERO);
		var tilkommet = getNaturalytelseTilkommetPrÅr().orElse(BigDecimal.ZERO);
		return Optional.of(brutto.get().add(bortfalt).subtract(tilkommet));
	}

	public Optional<BigDecimal> getGradertBruttoInkludertNaturalytelsePrÅr() {
		return getBruttoInkludertNaturalytelsePrÅr().map(b -> b.multiply(utbetalingsgrad).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
	}

	public Optional<String> getArbeidsgiverId() {
		return getArbeidsforhold().map(Arbeidsforhold::getArbeidsgiverId);
	}

	public String getBeskrivelse() {
		if (arbeidsforhold == null) {
			return inntektskategori == null ? aktivitetStatus.toString() : aktivitetStatus.toString() + inntektskategori.toString();
		} else {
			return (erFrilanser() ? "FL:" : "AT:") + getArbeidsgiverId();
		}
	}

	private boolean erFrilanser() {
		return aktivitetStatus.equals(AktivitetStatus.FL);
	}

	@Override
	public String toString() {
		return "FordelAndelModell{" +
				"aktivitetStatus=" + aktivitetStatus +
				", erSøktYtelseFor=" + erSøktYtelseFor +
				", foreslåttPrÅr=" + foreslåttPrÅr +
				", fordeltPrÅr=" + fordeltPrÅr +
				", inntektskategori=" + inntektskategori +
				", naturalytelseBortfaltPrÅr=" + naturalytelseBortfaltPrÅr +
				", naturalytelseTilkommetPrÅr=" + naturalytelseTilkommetPrÅr +
				", gjeldendeRefusjonPrÅr=" + gjeldendeRefusjonPrÅr +
				", fordeltRefusjonPrÅr=" + fordeltRefusjonPrÅr +
				", arbeidsforhold=" + arbeidsforhold +
				", erNytt=" + erNytt +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FordelAndelModell that = (FordelAndelModell) o;
		return aktivitetStatus == that.aktivitetStatus && inntektskategori == that.inntektskategori && Objects.equals(arbeidsforhold, that.arbeidsforhold) && Objects.equals(andelNr, that.andelNr);
	}

	@Override
	public int hashCode() {
		return Objects.hash(aktivitetStatus, inntektskategori, arbeidsforhold, andelNr);
	}

	public static class Builder {
		private FordelAndelModell mal;

		private Builder() {
			mal = new FordelAndelModell();
		}

		private Builder(FordelAndelModell kopi) {
			mal = kopi;
		}

		public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
			mal.aktivitetStatus = aktivitetStatus;
			return this;
		}

		public Builder medInntektskategori(Inntektskategori inntektskategori) {
			mal.inntektskategori = inntektskategori;
			return this;
		}

		public Builder medFordeltPrÅr(BigDecimal fordeltPrÅr) {
			mal.fordeltPrÅr = fordeltPrÅr;
			return this;
		}

		public Builder medForeslåttPrÅr(BigDecimal fordeltPrÅr) {
			mal.foreslåttPrÅr = fordeltPrÅr;
			return this;
		}

		public Builder medNaturalytelseBortfaltPrÅr(BigDecimal naturalytelseBortfaltPrÅr) {
			mal.naturalytelseBortfaltPrÅr = naturalytelseBortfaltPrÅr;
			return this;
		}

		public Builder medNaturalytelseTilkommerPrÅr(BigDecimal naturalytelseTilkommetPrÅr) {
			mal.naturalytelseTilkommetPrÅr = naturalytelseTilkommetPrÅr;
			return this;
		}

		public Builder medGjeldendeRefusjonPrÅr(BigDecimal gjeldendeRefusjonPrÅr) {
			mal.gjeldendeRefusjonPrÅr = gjeldendeRefusjonPrÅr;
			return this;
		}

		public Builder medFordeltRefusjonPrÅr(BigDecimal fordeltRefusjonPrÅr) {
			mal.fordeltRefusjonPrÅr = fordeltRefusjonPrÅr;
			return this;
		}

		public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
			mal.arbeidsforhold = arbeidsforhold;
			return this;
		}

		public Builder erNytt(boolean erNytt) {
			mal.erNytt = erNytt;
			return this;
		}

		public Builder medAndelNr(Long andelNr) {
			mal.andelNr = andelNr;
			return this;
		}

		public Builder erSøktYtelseFor(boolean erSøktYtelseFor) {
			mal.erSøktYtelseFor = erSøktYtelseFor;
			return this;
		}
		public Builder medUtbetalingsgrad(BigDecimal utbetalingsgrad) {
			mal.utbetalingsgrad = utbetalingsgrad;
			if (utbetalingsgrad != null && utbetalingsgrad.compareTo(BigDecimal.ZERO) == 0) {
				mal.erSøktYtelseFor = false;
			}
			return this;
		}

		public Builder medInntektFraInnektsmelding(BigDecimal beløpFraIMPrMnd) {
			mal.beløpFraInntektsMeldingPrMnd = beløpFraIMPrMnd;
			return this;
		}

		public FordelAndelModell build() {
			verifyStateForBuild();
			return mal;
		}

		private void verifyStateForBuild() {
			Objects.requireNonNull(mal.aktivitetStatus, "aktivitetstatus");
			if (mal.aktivitetStatus.equals(AktivitetStatus.AT) || mal.aktivitetStatus.equals(AktivitetStatus.FL)) {
				Objects.requireNonNull(mal.arbeidsforhold, "arbeidsforhold");
			}
		}
	}
}
