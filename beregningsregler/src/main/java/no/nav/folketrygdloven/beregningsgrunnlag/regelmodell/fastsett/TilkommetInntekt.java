package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

public class TilkommetInntekt {

	private final AktivitetStatus aktivitetStatus;
	private final Arbeidsforhold arbeidsforhold;

	/**
	 * Tilkommet inntekt pr år
	 * <p>
	 * Dette tilsvarer det faktiske beløpet som bruker har i tilkommet inntekt.
	 * <p>
	 * Dersom kilde til beløpet er inntektsmelding må inntekten graderes mot inversen av utbetalingsgrad for å komme fram til tilkommetPRÅr
	 */
	private final BigDecimal tilkommetPrÅr;

	public TilkommetInntekt(AktivitetStatus aktivitetStatus,
	                        Arbeidsforhold arbeidsforhold,
	                        BigDecimal tilkommetPrÅr) {
		this.aktivitetStatus = aktivitetStatus;
		this.arbeidsforhold = arbeidsforhold;
		this.tilkommetPrÅr = tilkommetPrÅr;
	}

	public AktivitetStatus getAktivitetStatus() {
		return aktivitetStatus;
	}

	public Arbeidsforhold getArbeidsforhold() {
		return arbeidsforhold;
	}

	public BigDecimal getTilkommetPrÅr() {
		return tilkommetPrÅr;
	}


}
