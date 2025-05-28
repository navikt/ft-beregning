package no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;

public class FordelteAndelerModell {
	private final FordelAndelModell inputAndel;
	private BigDecimal fraksjonsbestemmendeBeløp;
	private BigDecimal fraksjonAvBrutto;
	private BigDecimal målbeløp;
	private List<FordelAndelModell> fordelteAndeler = new ArrayList<>();

	public FordelteAndelerModell(FordelAndelModell andel, BigDecimal fraksjonsbestemmendeBeløp) {
		Objects.requireNonNull(andel, "andel");
		Objects.requireNonNull(fraksjonsbestemmendeBeløp, "fraksjonsbestemmendeBeløp");
		this.fraksjonsbestemmendeBeløp = fraksjonsbestemmendeBeløp;
		this.inputAndel = andel;
	}

	public FordelteAndelerModell(FordelAndelModell andel) {
		Objects.requireNonNull(andel, "andel");
		this.inputAndel = andel;
	}

	public FordelAndelModell getInputAndel() {
		return inputAndel;
	}

	public BigDecimal getFraksjonAvBrutto() {
		return fraksjonAvBrutto;
	}

	public List<FordelAndelModell> getFordelteAndeler() {
		return fordelteAndeler;
	}

	public Optional<FordelAndelModell> getEnesteFordelteAndel() {
		if (fordelteAndeler.size() > 1) {
			throw new IllegalStateException("Forenter å ha maks én fordelt andel, men fant " + fordelteAndeler.size());
		}
		return fordelteAndeler.stream().findFirst();
	}

	public BigDecimal getFraksjonsbestemmendeBeløp() {
		return fraksjonsbestemmendeBeløp;
	}

	public BigDecimal getMålbeløp() {
		return målbeløp;
	}

	public void leggTilFordeltAndel(FordelAndelModell fordeltAndel) {
		Objects.requireNonNull(fordeltAndel, "fordeltAndel");
		this.fordelteAndeler.add(fordeltAndel);
	}

	public void setFraksjonAvBrutto(BigDecimal fraksjonAvBrutto) {
		this.fraksjonAvBrutto = Objects.requireNonNull(fraksjonAvBrutto, "fraksjonAvBrutto");
	}

	public void setMålbeløp(BigDecimal målbeløp) {
		if (this.målbeløp != null) {
			throw new IllegalStateException("Prøver å sette målbeløp, men beløpet er allerede satt til " + this.målbeløp);
		}
		this.målbeløp = Objects.requireNonNull(målbeløp, "målbeløp");
	}

	public Optional<FordelAndelModell> getFordeltAndelMedInntektskategori(Inntektskategori inntektskategori) {
		List<FordelAndelModell> matcher = fordelteAndeler.stream().filter(fordeltAndel -> fordeltAndel.getInntektskategori().equals(inntektskategori)).toList();
		if (matcher.size() > 1) {
			throw new IllegalStateException("Forventet ikke å finne mer enn 1 fordelt andel med inntektskategori " + inntektskategori + " men fant " + matcher);
		}
		return matcher.stream().findFirst();
	}
}
