package no.nav.folketrygdloven.besteberegning.modell.output;

import java.math.BigDecimal;

public class BesteberegnetAndel {

	private AktivitetNøkkel aktivitetNøkkel;
	private BigDecimal besteberegnetPrÅr;

	public BesteberegnetAndel(AktivitetNøkkel aktivitetNøkkel, BigDecimal besteberegnetPrÅr) {
		this.aktivitetNøkkel = aktivitetNøkkel;
		this.besteberegnetPrÅr = besteberegnetPrÅr;
	}

	public AktivitetNøkkel getAktivitetNøkkel() {
		return aktivitetNøkkel;
	}

	public BigDecimal getBesteberegnetPrÅr() {
		return besteberegnetPrÅr;
	}


	@Override
	public String toString() {
		return "BesteberegnetAndel{" +
				"aktivitetNøkkel=" + aktivitetNøkkel +
				", besteberegnetPrÅr=" + besteberegnetPrÅr +
				'}';
	}
}
