package no.nav.folketrygdloven.besteberegning.modell.output;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class BeregnetMånedsgrunnlag implements Comparable<BeregnetMånedsgrunnlag> {

	private List<Inntekt> inntekter = new ArrayList<>();
	private YearMonth måned;

	public BeregnetMånedsgrunnlag(YearMonth måned) {
		this.måned = måned;
	}

	public void leggTilInntekt(Inntekt inntekt) {
		inntekter.add(inntekt);
	}

	public List<Inntekt> getInntekter() {
		return inntekter;
	}

	public YearMonth getMåned() {
		return måned;
	}

	public BigDecimal finnSum() {
		return inntekter.stream().map(Inntekt::getInntektPrÅr).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
	}

	@Override
	public String toString() {
		return "BeregnetMånedsgrunnlag{" +
				"inntekter=" + inntekter +
				", yearMonth=" + måned +
				'}';
	}

	@Override
	public int compareTo(BeregnetMånedsgrunnlag beregnetMånedsgrunnlag) {
		int compareSum = beregnetMånedsgrunnlag.finnSum().compareTo(this.finnSum());
		if (compareSum != 0) {
			return compareSum;
		}
		return beregnetMånedsgrunnlag.getMåned().compareTo(this.getMåned());
	}
}
