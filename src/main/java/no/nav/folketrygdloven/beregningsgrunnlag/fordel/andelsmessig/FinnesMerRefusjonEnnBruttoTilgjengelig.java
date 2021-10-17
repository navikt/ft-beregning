package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Sjekker om det finnes et tilkommet arbeidsforhold med refusjonskrav.
 *
 */
class FinnesMerRefusjonEnnBruttoTilgjengelig extends LeafSpecification<FordelModell> {

    static final String ID = "NOK_INNTEKT_TIL_Å_DEKKE_BRUTTO ";
    static final String BESKRIVELSE = "Er beregningsgrunnlaget høyt nok til å dekke all refusjon?";

    FinnesMerRefusjonEnnBruttoTilgjengelig() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(FordelModell grunnlag) {
	    var totalBrutto = finnTotalBrutto(grunnlag.getInput());
	    var totalRefusjon = finnTotalRefusjon(grunnlag.getInput());
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
