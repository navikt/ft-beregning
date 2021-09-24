package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

public class Refusjon {

	private Arbeidsforhold arbeidsforhold;
	private Periode periode;
	private BigDecimal beløpPrÅr;

	public Refusjon(Arbeidsforhold arbeidsforhold, Periode periode, BigDecimal beløp) {
		this.arbeidsforhold = arbeidsforhold;
		this.periode = periode;
		this.beløpPrÅr = beløp;
	}

	public Arbeidsforhold getArbeidsforhold() {
		return arbeidsforhold;
	}

	public Periode getPeriode() {
		return periode;
	}

	public BigDecimal getBeløpPrÅr() {
		return beløpPrÅr;
	}
}
