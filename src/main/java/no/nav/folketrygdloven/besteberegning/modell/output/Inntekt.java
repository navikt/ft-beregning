package no.nav.folketrygdloven.besteberegning.modell.output;

import java.math.BigDecimal;

public class Inntekt {

	private AktivitetNøkkel aktivitetNøkkel;
	private BigDecimal inntektPrÅr;

	public Inntekt(AktivitetNøkkel aktivitetNøkkel, BigDecimal inntektPrÅr) {
		this.aktivitetNøkkel = aktivitetNøkkel;
		this.inntektPrÅr = inntektPrÅr;
	}

	public AktivitetNøkkel getAktivitetNøkkel() {
		return aktivitetNøkkel;
	}

	public BigDecimal getInntektPrÅr() {
		return inntektPrÅr;
	}

	@Override
	public String toString() {
		return "Inntekt{" +
				"aktivitetNøkkel=" + aktivitetNøkkel +
				", inntektPrÅr=" + inntektPrÅr +
				'}';
	}
}
