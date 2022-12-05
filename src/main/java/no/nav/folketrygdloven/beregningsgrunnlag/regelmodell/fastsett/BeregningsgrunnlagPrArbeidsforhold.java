package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

public class BeregningsgrunnlagPrArbeidsforhold {

	// Input
	private Long andelNr;
	private Boolean erSøktYtelseFor;
	private BigDecimal utbetalingsprosent = BigDecimal.valueOf(100);
	private BigDecimal naturalytelseBortfaltPrÅr;
	private BigDecimal naturalytelseTilkommetPrÅr;
	private BigDecimal bruttoPrÅr;
	private Arbeidsforhold arbeidsforhold;
	private BigDecimal refusjonPrÅr;
	private BigDecimal beregnetPrÅr;

	// Output
	private BigDecimal avkortetPrÅr;
	private BigDecimal redusertPrÅr;
	private BigDecimal maksimalRefusjonPrÅr;
	private BigDecimal avkortetRefusjonPrÅr;
	private BigDecimal redusertRefusjonPrÅr;
	private BigDecimal avkortetBrukersAndelPrÅr;
	private BigDecimal redusertBrukersAndelPrÅr;
	private Long dagsatsBruker;
	private Long dagsatsArbeidsgiver;
	private BigDecimal andelsmessigFørGraderingPrAar;


	public String getArbeidsgiverId() {
		return arbeidsforhold.getArbeidsgiverId();
	}

	public boolean erFrilanser() {
		return arbeidsforhold.erFrilanser();
	}

	public Optional<BigDecimal> getNaturalytelseBortfaltPrÅr() {
		return Optional.ofNullable(naturalytelseBortfaltPrÅr);
	}

	public Optional<BigDecimal> getGradertNaturalytelseBortfaltPrÅr() {
		return Optional.ofNullable(finnGradert(naturalytelseBortfaltPrÅr));
	}

	public Optional<BigDecimal> getNaturalytelseTilkommetPrÅr() {
		return Optional.ofNullable(naturalytelseTilkommetPrÅr);
	}

	public Optional<BigDecimal> getGradertNaturalytelseTilkommetPrÅr() {
		return Optional.ofNullable(finnGradert(naturalytelseTilkommetPrÅr));
	}

	public String getBeskrivelse() {
		return (erFrilanser() ? "FL:" : "AT:") + getArbeidsgiverId();
	}

	public Optional<BigDecimal> getBruttoPrÅr() {
		return Optional.ofNullable(bruttoPrÅr);
	}

	public BigDecimal getGradertBruttoPrÅr() {
		return finnGradert(getBruttoPrÅr().orElse(null));
	}


	public BigDecimal getGradertBeregnetPrÅr() {
		return beregnetPrÅr != null ? finnGradert(beregnetPrÅr) : BigDecimal.ZERO;
	}

	public BigDecimal getBeregnetPrÅr() {
		return beregnetPrÅr != null ? beregnetPrÅr : BigDecimal.ZERO;
	}

	public Optional<BigDecimal> getBruttoInkludertNaturalytelsePrÅr() {
		if (getBruttoPrÅr().isEmpty()) {
			return Optional.empty();
		}
		BigDecimal bortfaltNaturalytelse = naturalytelseBortfaltPrÅr != null ? naturalytelseBortfaltPrÅr : BigDecimal.ZERO;
		BigDecimal tilkommetNaturalytelse = naturalytelseTilkommetPrÅr != null ? naturalytelseTilkommetPrÅr : BigDecimal.ZERO;
		return Optional.of(getBruttoPrÅr().get().add(bortfaltNaturalytelse).subtract(tilkommetNaturalytelse)); // NOSONAR
	}

	public BigDecimal getAvkortetPrÅr() {
		return avkortetPrÅr;
	}

	public BigDecimal getRedusertPrÅr() {
		return redusertPrÅr;
	}

	public Arbeidsforhold getArbeidsforhold() {
		return arbeidsforhold;
	}

	public Optional<BigDecimal> getGradertRefusjonskravPrÅr() {
		return Optional.ofNullable(finnGradert(refusjonPrÅr));
	}

	public Optional<BigDecimal> getGradertBruttoInkludertNaturalytelsePrÅr() {
		Optional<BigDecimal> brutto = getBruttoInkludertNaturalytelsePrÅr();
		return brutto.map(this::finnGradert);
	}

	public BigDecimal getMaksimalRefusjonPrÅr() {
		return maksimalRefusjonPrÅr;
	}

	public Long getDagsats() {
		if (dagsatsBruker == null) {
			return dagsatsArbeidsgiver;
		}
		if (dagsatsArbeidsgiver == null) {
			return dagsatsBruker;
		}
		return dagsatsBruker + dagsatsArbeidsgiver;
	}

	public BigDecimal getAvkortetRefusjonPrÅr() {
		return avkortetRefusjonPrÅr;
	}

	public BigDecimal getRedusertRefusjonPrÅr() {
		return redusertRefusjonPrÅr;
	}

	public BigDecimal getAvkortetBrukersAndelPrÅr() {
		return avkortetBrukersAndelPrÅr;
	}

	public BigDecimal getRedusertBrukersAndelPrÅr() {
		return redusertBrukersAndelPrÅr;
	}

	public Long getDagsatsBruker() {
		return dagsatsBruker;
	}

	public Long getDagsatsArbeidsgiver() {
		return dagsatsArbeidsgiver;
	}

	public Long getAndelNr() {
		return andelNr;
	}

	public BigDecimal getUtbetalingsprosent() {
		return utbetalingsprosent;
	}

	public boolean getErSøktYtelseFor() {
		return erSøktYtelseFor != null ? erSøktYtelseFor : utbetalingsprosent.compareTo(BigDecimal.ZERO) > 0;
	}

	public void setErSøktYtelseFor(boolean erSøktYtelseFor) {
		this.erSøktYtelseFor = erSøktYtelseFor;
	}

	public BigDecimal getAndelsmessigFørGraderingPrAar() {
		return andelsmessigFørGraderingPrAar;
	}


	@Override
	public String toString() {
		return getBeskrivelse();
	}

	private BigDecimal finnGradert(BigDecimal verdi) {
		return verdi == null ? null : verdi.multiply(utbetalingsprosent.scaleByPowerOfTen(-2));
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(BeregningsgrunnlagPrArbeidsforhold af) {
		return new Builder(af);
	}

	public static class Builder {

		private BeregningsgrunnlagPrArbeidsforhold mal;
		private boolean erNytt;

		public Builder() {
			mal = new BeregningsgrunnlagPrArbeidsforhold();
		}

		public Builder(BeregningsgrunnlagPrArbeidsforhold af) {
			mal = af;
		}

		public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
			mal.arbeidsforhold = arbeidsforhold;
			return this;
		}

		public Builder erNytt(boolean erNytt) {
			this.erNytt = erNytt;
			return this;
		}

		public Builder medAndelsmessigFørGraderingPrAar(BigDecimal andelsmessigFørGraderingPrAar) {
			mal.andelsmessigFørGraderingPrAar = andelsmessigFørGraderingPrAar;
			return this;
		}

		public Builder medBeregnetPrÅr(BigDecimal beregnetPrÅr) {
			mal.beregnetPrÅr = beregnetPrÅr;
			return this;
		}

		public Builder medBruttoPrÅr(BigDecimal bruttoPrÅr) {
			mal.bruttoPrÅr = bruttoPrÅr;
			return this;
		}

		public Builder medAvkortetPrÅr(BigDecimal avkortetPrÅr) {
			mal.avkortetPrÅr = avkortetPrÅr;
			return this;
		}

		public Builder medRedusertPrÅr(BigDecimal redusertPrÅr) {
			mal.redusertPrÅr = redusertPrÅr;
			return this;
		}

		public Builder medNaturalytelseBortfaltPrÅr(BigDecimal naturalytelseBortfaltPrÅr) {
			mal.naturalytelseBortfaltPrÅr = naturalytelseBortfaltPrÅr;
			return this;
		}

		public Builder medNaturalytelseTilkommetPrÅr(BigDecimal naturalytelseTilkommetPrÅr) {
			mal.naturalytelseTilkommetPrÅr = naturalytelseTilkommetPrÅr;
			return this;
		}

		public Builder medRefusjonPrÅr(BigDecimal refusjonPrÅr) {
			mal.refusjonPrÅr = refusjonPrÅr;
			return this;
		}

		public Builder medMaksimalRefusjonPrÅr(BigDecimal maksimalRefusjonPrÅr) {
			mal.maksimalRefusjonPrÅr = maksimalRefusjonPrÅr;
			return this;
		}

		public Builder medAvkortetRefusjonPrÅr(BigDecimal avkortetRefusjonPrÅr) {
			mal.avkortetRefusjonPrÅr = avkortetRefusjonPrÅr;
			return this;
		}

		public Builder medRedusertRefusjonPrÅr(BigDecimal redusertRefusjonPrÅr, BigDecimal ytelsesdagerPrÅr) {
			mal.redusertRefusjonPrÅr = redusertRefusjonPrÅr;
			mal.dagsatsArbeidsgiver = redusertRefusjonPrÅr == null || ytelsesdagerPrÅr == null ? null : redusertRefusjonPrÅr.divide(ytelsesdagerPrÅr, 0, RoundingMode.HALF_UP).longValue();
			return this;
		}

		public Builder medAvkortetBrukersAndelPrÅr(BigDecimal avkortetBrukersAndelPrÅr) {
			mal.avkortetBrukersAndelPrÅr = avkortetBrukersAndelPrÅr;
			return this;
		}

		public Builder medRedusertBrukersAndelPrÅr(BigDecimal redusertBrukersAndelPrÅr, BigDecimal ytelsesdagerPrÅr) {
			mal.redusertBrukersAndelPrÅr = redusertBrukersAndelPrÅr;
			mal.dagsatsBruker = redusertBrukersAndelPrÅr == null || ytelsesdagerPrÅr == null ? null : redusertBrukersAndelPrÅr.divide(ytelsesdagerPrÅr, 0, RoundingMode.HALF_UP).longValue();
			return this;
		}


		public Builder medAndelNr(long andelNr) {
			mal.andelNr = andelNr;
			return this;
		}

		public Builder medUtbetalingsprosent(BigDecimal utbetalingsprosent) {
			mal.utbetalingsprosent = utbetalingsprosent;
			return this;
		}

		public BeregningsgrunnlagPrArbeidsforhold build() {
			verifyStateForBuild();
			return mal;
		}

		private void verifyStateForBuild() {
			Objects.requireNonNull(mal.arbeidsforhold, "arbeidsforhold");
			if (!erNytt) {
				Objects.requireNonNull(mal.andelNr, "andelNr");
			}
		}
	}
}
