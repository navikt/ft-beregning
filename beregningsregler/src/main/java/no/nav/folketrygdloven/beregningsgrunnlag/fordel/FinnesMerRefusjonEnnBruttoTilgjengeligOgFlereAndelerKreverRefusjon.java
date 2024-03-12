package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.math.BigDecimal;

class FinnesMerRefusjonEnnBruttoTilgjengeligOgFlereAndelerKreverRefusjon extends LeafSpecification<FordelModell> {

    static final String ID = "MER_REFUSJON_ENN_BRUTTO_TILGJENGELIG_PÅ_FLERE_ANDELER";
    static final String BESKRIVELSE = "Er refusjonskrav høyere enn brutto tilgjengelig og det finnes flere andeler som krever refusjon?";

    FinnesMerRefusjonEnnBruttoTilgjengeligOgFlereAndelerKreverRefusjon() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(FordelModell modell) {
	    var totalBrutto = finnTotalBrutto(modell.getInput());
	    var totalRefusjon = finnTotalRefusjon(modell.getInput());
	    var finnesFlereAndelerMedRefkrav = antallAndelerMedRefusjonskrav(modell.getInput());
	    var finnesMerRefusjonEnnBrutto = totalRefusjon.compareTo(totalBrutto) > 0;
		// Trenger ikke fordele andelsmessig om det kun finnes et refusjonskrav
	    return finnesMerRefusjonEnnBrutto && finnesFlereAndelerMedRefkrav ? ja() : nei();
	}

	private boolean antallAndelerMedRefusjonskrav(FordelPeriodeModell input) {
		var antallAndelerMedRefKrav = input.getAndeler().stream()
				.filter(a -> a.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0)
				.count();
		return antallAndelerMedRefKrav > 1;
	}

	private BigDecimal finnTotalBrutto(FordelPeriodeModell grunnlag) {
		return grunnlag.getAndeler()
				.stream()
				.map(andel -> andel.getGradertBruttoPrÅr().orElse(BigDecimal.ZERO))
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
	}

	private BigDecimal finnTotalRefusjon(FordelPeriodeModell grunnlag) {
		return grunnlag.getAndeler()
				.stream()
				.map(andel -> andel.getGradertRefusjonPrÅr().orElse(BigDecimal.ZERO))
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
	}
}
