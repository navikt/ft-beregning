package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.math.BigDecimal;
import java.util.Optional;

class FinnesMerRefusjonEnnBruttoTilgjengelig extends LeafSpecification<FordelModell> {

    static final String ID = "MER_REFUSJON_ENN_BRUTTO_TILGJENGELIG ";
    static final String BESKRIVELSE = "Er refusjonskrav høyere enn brutto tilgjengelig?";

    FinnesMerRefusjonEnnBruttoTilgjengelig() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(FordelModell modell) {
	    var totalBrutto = finnTotalBrutto(modell.getInput());
	    var totalRefusjon = finnTotalRefusjon(modell.getInput());
		var finnesMerRefusjonEnnBrutto = totalRefusjon.compareTo(totalBrutto) > 0;
	    return finnesMerRefusjonEnnBrutto ? ja() : nei();
	}

	private BigDecimal finnTotalBrutto(FordelPeriodeModell grunnlag) {
		return grunnlag.getAndeler()
				.stream()
				.map(andel -> andel.getBruttoPrÅr().orElse(BigDecimal.ZERO))
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
	}

	private BigDecimal finnTotalRefusjon(FordelPeriodeModell grunnlag) {
		return grunnlag.getAndeler()
				.stream()
				.map(andel -> andel.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO))
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
	}
}
