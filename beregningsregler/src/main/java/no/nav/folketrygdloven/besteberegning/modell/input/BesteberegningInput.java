package no.nav.folketrygdloven.besteberegning.modell.input;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;

public class BesteberegningInput {

	private Inntektsgrunnlag inntektsgrunnlag;
	private List<Grunnbeløp> grunnbeløpSatser = new ArrayList<>();
	private BigDecimal gjeldendeGverdi;
	private LocalDate skjæringstidspunktOpptjening;
	private List<Periode> perioderMedNæringsvirksomhet = new ArrayList<>();
	private List<Ytelsegrunnlag> ytelsegrunnlag = new ArrayList<>();

	/**
	 * Totalt brutto grunnlag beregnet etter kap 8 i folketrygdloven
	 */
	private BigDecimal beregnetGrunnlag;

	public BesteberegningInput() {
		// CDI
	}

	public Inntektsgrunnlag getInntektsgrunnlag() {
		return inntektsgrunnlag;
	}

	public LocalDate getSkjæringstidspunktOpptjening() {
		return skjæringstidspunktOpptjening;
	}

	public List<Grunnbeløp> getGrunnbeløpSatser() {
		return grunnbeløpSatser;
	}

	public BigDecimal getGjeldendeGverdi() {
		return gjeldendeGverdi;
	}

	public List<Periode> getPerioderMedNæringsvirksomhet() {
		return perioderMedNæringsvirksomhet;
	}

	public BigDecimal getBeregnetGrunnlag() {
		return beregnetGrunnlag;
	}

	public List<Ytelsegrunnlag> getYtelsegrunnlag() {
		return ytelsegrunnlag;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private BesteberegningInput mal;

		public Builder() {
			mal = new BesteberegningInput();
		}

		public Builder medInntektsgrunnlag(Inntektsgrunnlag inntektsgrunnlag) {
			mal.inntektsgrunnlag = inntektsgrunnlag;
			return this;
		}

		public Builder medGrunnbeløpSatser(List<Grunnbeløp> grunnbeløpSatser) {
			mal.grunnbeløpSatser = grunnbeløpSatser;
			return this;
		}

		public Builder medGjeldendeGVerdi(BigDecimal gjeldendeGverdi) {
			mal.gjeldendeGverdi = gjeldendeGverdi;
			return this;
		}

		public Builder medSkjæringstidspunktOpptjening(LocalDate skjæringstidspunktOpptjening) {
			mal.skjæringstidspunktOpptjening = skjæringstidspunktOpptjening;
			return this;
		}

		public Builder medPerioderMedNæringsvirksomhet(List<Periode> perioderMedNæringsvirksomhet) {
			mal.perioderMedNæringsvirksomhet = perioderMedNæringsvirksomhet;
			return this;
		}

		public Builder medBeregnetGrunnlag(BigDecimal beregnetGrunnlag) {
			mal.beregnetGrunnlag = beregnetGrunnlag;
			return this;
		}

		public Builder leggTilYtelsegrunnlag(Ytelsegrunnlag ytelsegrunnlag) {
			mal.ytelsegrunnlag.add(ytelsegrunnlag);
			return this;
		}

		public BesteberegningInput build() {
			valider();
			return mal;
		}

		private void valider() {
			Objects.requireNonNull(mal.beregnetGrunnlag, "Beregnet grunnlag");
			Objects.requireNonNull(mal.skjæringstidspunktOpptjening, "Skjæringstidspunkt opptjening");
		}
	}

}
