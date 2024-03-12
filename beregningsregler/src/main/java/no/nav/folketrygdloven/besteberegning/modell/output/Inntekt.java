package no.nav.folketrygdloven.besteberegning.modell.output;

import java.math.BigDecimal;

public class Inntekt {

	private AktivitetNøkkel aktivitetNøkkel;
	private BigDecimal inntektPrMåned;

	public Inntekt(AktivitetNøkkel aktivitetNøkkel, BigDecimal inntektPrMåned) {
		this.aktivitetNøkkel = aktivitetNøkkel;
		this.inntektPrMåned = inntektPrMåned;
	}

	public AktivitetNøkkel getAktivitetNøkkel() {
		return aktivitetNøkkel;
	}

	public BigDecimal getInntektPrMåned() {
		return inntektPrMåned;
	}

	@Override
	public String toString() {
		return "Inntekt{" +
				"aktivitetNøkkel=" + aktivitetNøkkel +
				", inntektPrÅr=" + inntektPrMåned +
				'}';
	}
}
