package no.nav.folketrygdloven.besteberegning.modell.input;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;

public class BesteberegningInput {

	private Inntektsgrunnlag inntektsgrunnlag;
	private List<Grunnbeløp> grunnbeløpSatser;
	private BigDecimal gjeldendeGverdi;
	private LocalDate skjæringstidspunktOpptjening;
	private List<Periode> perioderMedNæringsvirksomhet;

	public BesteberegningInput(Inntektsgrunnlag inntektsgrunnlag, List<Grunnbeløp> grunnbeløpSatser, BigDecimal gjeldendeGverdi, LocalDate skjæringstidspunktOpptjening, List<Periode> perioderMedNæringsvirksomhet) {
		this.inntektsgrunnlag = inntektsgrunnlag;
		this.grunnbeløpSatser = grunnbeløpSatser;
		this.gjeldendeGverdi = gjeldendeGverdi;
		this.skjæringstidspunktOpptjening = skjæringstidspunktOpptjening;
		this.perioderMedNæringsvirksomhet = perioderMedNæringsvirksomhet;
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
}
