package no.nav.folketrygdloven.besteberegning.modell.output;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BeregnetMånedsgrunnlag implements Comparable<BeregnetMånedsgrunnlag> {

	private final List<Inntekt> inntekter = new ArrayList<>();
	private final YearMonth måned;

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
		return inntekter.stream().map(Inntekt::getInntektPrMåned).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
	}

	@Override
	public String toString() {
		return "BeregnetMånedsgrunnlag{" +
				"inntekter=" + inntekter +
				", yearMonth=" + måned +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BeregnetMånedsgrunnlag that = (BeregnetMånedsgrunnlag) o;
		return Objects.equals(this.finnSum(), that.finnSum()) && Objects.equals(måned, that.måned);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.finnSum(), måned);
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
