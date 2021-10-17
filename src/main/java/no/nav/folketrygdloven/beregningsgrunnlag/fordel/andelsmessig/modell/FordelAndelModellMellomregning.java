package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class FordelAndelModellMellomregning {
	private final FordelAndelModell inputAndel;
	private BigDecimal fraksjonsbestemmendeBeløp;
	private BigDecimal fraksjonAvBrutto;
	private BigDecimal bruttoTilgjengeligForFordeling;
	private BigDecimal målbeløp;
	private List<FordelAndelModell> fordelteAndeler = new ArrayList<>();

	public FordelAndelModellMellomregning(FordelAndelModell andel, BigDecimal fraksjonsbestemmendeBeløp) {
		Objects.requireNonNull(andel, "andel");
		Objects.requireNonNull(fraksjonsbestemmendeBeløp, "fraksjonsbestemmendeBeløp");
		this.fraksjonsbestemmendeBeløp = fraksjonsbestemmendeBeløp;
		this.inputAndel = andel;
		this.bruttoTilgjengeligForFordeling = andel.getForeslåttPrÅr().orElse(BigDecimal.ZERO);
	}

	public FordelAndelModellMellomregning(FordelAndelModell andel) {
		Objects.requireNonNull(andel, "andel");
		this.inputAndel = andel;
		this.bruttoTilgjengeligForFordeling = andel.getForeslåttPrÅr().orElse(BigDecimal.ZERO);
	}

	public FordelAndelModell getInputAndel() {
		return inputAndel;
	}

	public BigDecimal getFraksjonAvBrutto() {
		return fraksjonAvBrutto;
	}

	public void setBruttoTilgjengeligForFordeling(BigDecimal bruttoTilgjengeligForFordeling) {
		this.bruttoTilgjengeligForFordeling = bruttoTilgjengeligForFordeling;
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

	public BigDecimal getBruttoTilgjengeligForFordeling() {
		return bruttoTilgjengeligForFordeling;
	}

	public BigDecimal getMålbeløp() {
		return målbeløp;
	}

	public void leggTilFordeltAndel(FordelAndelModell fordeltAndel) {
		Objects.requireNonNull(fordeltAndel, "fordeltAndel");
		this.fordelteAndeler.add(fordeltAndel);
	}

	public void setFraksjonAvBrutto(BigDecimal fraksjonAvBrutto) {
		if (this.fraksjonAvBrutto != null) {
			throw new IllegalStateException("Prøver å sette faksjon av brutto, men beløpet er allerede satt til " + this.fraksjonAvBrutto);
		}
		this.fraksjonAvBrutto = Objects.requireNonNull(fraksjonAvBrutto, "fraksjonAvBrutto");
	}

	public void setMålbeløp(BigDecimal målbeløp) {
		if (this.målbeløp != null) {
			throw new IllegalStateException("Prøver å sette målbeløp, men beløpet er allerede satt til " + this.målbeløp);
		}
		this.målbeløp = Objects.requireNonNull(målbeløp, "målbeløp");
	}

	public Optional<FordelAndelModell> getFordeltAndelMedInntektskategori(Inntektskategori inntektskategori) {
		List<FordelAndelModell> matcher = fordelteAndeler.stream().filter(fordeltAndel -> fordeltAndel.getInntektskategori().equals(inntektskategori)).collect(Collectors.toList());
		if (matcher.size() > 1) {
			throw new IllegalStateException("Forventet ikke å finne mer enn 1 fordelt andel med inntektskategori " + inntektskategori + " men fant " + matcher);
		}
		return matcher.stream().findFirst();
	}
}
