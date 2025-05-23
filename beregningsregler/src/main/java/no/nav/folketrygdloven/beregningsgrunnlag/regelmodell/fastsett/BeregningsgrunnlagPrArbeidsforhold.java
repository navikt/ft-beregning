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
	private BigDecimal aktivitetsgrad; // For bruk til å regne fortsatt arbeid (Mer nøyaktig enn å se på (1-utbetalingsprosent)
	private BigDecimal naturalytelseBortfaltPrÅr;
	private BigDecimal naturalytelseTilkommetPrÅr;
	private BigDecimal bruttoPrÅr;
	private Arbeidsforhold arbeidsforhold;
	private BigDecimal refusjonPrÅr;

	// Beregnet eller skjønnsfastsatt inntekt
	private BigDecimal inntektsgrunnlagPrÅr;

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
	private BigDecimal andelsmessigFørGraderingPrAar; //avkortet mot 6G, innført for å støtte at utbetalingsgrader settes i beregning for k9 og SVP (isdf i tilkjent ytelse-steg som for FP)


	public String getArbeidsgiverId() {
		return arbeidsforhold.getArbeidsgiverId();
	}

	public boolean erFrilanser() {
		return arbeidsforhold.erFrilanser();
	}

	public Optional<BigDecimal> getNaturalytelseBortfaltPrÅr() {
		return Optional.ofNullable(naturalytelseBortfaltPrÅr);
	}


	public Optional<BigDecimal> getNaturalytelseTilkommetPrÅr() {
		return Optional.ofNullable(naturalytelseTilkommetPrÅr);
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

	public BigDecimal getAktivitetsgradertBruttoPrÅr() {
		return finnAktivitetsgradert(getBruttoPrÅr().orElse(null));
	}


	public BigDecimal getInntektsgrunnlagPrÅr() {
		return inntektsgrunnlagPrÅr != null ? inntektsgrunnlagPrÅr : BigDecimal.ZERO;
	}

	public BigDecimal getGradertInntektsgrunnlagInkludertNaturalytelsePrÅr() {
		return finnGradert(getInntektsgrunnlagInkludertNaturalytelsePrÅr());
	}


	public BigDecimal getInntektsgrunnlagInkludertNaturalytelsePrÅr() {
        var bortfaltNaturalytelse = naturalytelseBortfaltPrÅr != null ? naturalytelseBortfaltPrÅr : BigDecimal.ZERO;
        var tilkommetNaturalytelse = naturalytelseTilkommetPrÅr != null ? naturalytelseTilkommetPrÅr : BigDecimal.ZERO;
		return getInntektsgrunnlagPrÅr().add(bortfaltNaturalytelse).subtract(tilkommetNaturalytelse); // NOSONAR
	}

	public Optional<BigDecimal> getAktivitetsgrad() {
		return Optional.ofNullable(aktivitetsgrad);
	}

	public Optional<BigDecimal> getBruttoInkludertNaturalytelsePrÅr() {
		if (getBruttoPrÅr().isEmpty()) {
			return Optional.empty();
		}
        var bortfaltNaturalytelse = naturalytelseBortfaltPrÅr != null ? naturalytelseBortfaltPrÅr : BigDecimal.ZERO;
        var tilkommetNaturalytelse = naturalytelseTilkommetPrÅr != null ? naturalytelseTilkommetPrÅr : BigDecimal.ZERO;
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

	public Optional<BigDecimal> getAktivitetsgradertRefusjonskravPrÅr() {
		return Optional.ofNullable(finnAktivitetsgradert(refusjonPrÅr));
	}

	public Optional<BigDecimal> getGradertBruttoInkludertNaturalytelsePrÅr() {
        var brutto = getBruttoInkludertNaturalytelsePrÅr();
		return brutto.map(this::finnGradert);
	}

	public Optional<BigDecimal> getAktivitetsgradertBruttoInkludertNaturalytelsePrÅr() {
        var brutto = getBruttoInkludertNaturalytelsePrÅr();
		return brutto.map(this::finnAktivitetsgradert);
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

	private BigDecimal finnAktivitetsgradert(BigDecimal verdi) {
		if (verdi == null) {
			return null;
		}
		if (aktivitetsgrad == null) {
			return finnGradert(verdi);
		}
		return verdi.multiply(BigDecimal.valueOf(100).subtract(aktivitetsgrad).scaleByPowerOfTen(-2));
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

		public Builder medInntektsgrunnlagPrÅr(BigDecimal inntekt) {
			mal.inntektsgrunnlagPrÅr = inntekt;
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

		public Builder medAktivitetsgrad(BigDecimal aktivitetsgrad) {
			if (aktivitetsgrad != null && (aktivitetsgrad.compareTo(BigDecimal.ZERO) < 0 || aktivitetsgrad.compareTo(BigDecimal.valueOf(100)) > 0)) {
				throw new IllegalArgumentException("Aktivitetsgrad må ha verdi fra 0 til 100, faktisk: " + aktivitetsgrad);
			}
			mal.aktivitetsgrad = aktivitetsgrad;
			return this;
		}

		public Builder medErSøktYtelseFor(boolean erSøktYtelseFor) {
			mal.erSøktYtelseFor = erSøktYtelseFor;
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
