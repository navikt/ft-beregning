package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;

class OmfordelFraFrilans extends OmfordelFraATFL {

    static final String ID = "FP_BR 22.3.8";
    static final String BESKRIVELSE = "Flytt beregningsgrunnlag fra frilans.";

    OmfordelFraFrilans(FordelAndelModell arbeidsforhold) {
        super(arbeidsforhold, ID, BESKRIVELSE);
    }

    @Override
    protected Inntektskategori finnInntektskategori() {
        return Inntektskategori.FRILANSER;
    }

    private boolean harBgSomKanFlyttes(FordelAndelModell beregningsgrunnlagPrArbeidsforhold) {
        return beregningsgrunnlagPrArbeidsforhold.getGradertBruttoPrÅr().orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0
            && (beregningsgrunnlagPrArbeidsforhold.getGradertFordeltPrÅr()
		        .map(fordelt -> fordelt.compareTo(BigDecimal.ZERO) > 0)
		        .orElse(true));
    }

	@Override
	protected Optional<FordelAndelModell> finnAktivitetMedOmfordelbartBg(FordelPeriodeModell beregningsgrunnlagPeriode) {
		return beregningsgrunnlagPeriode.getEnesteAndelForStatus(AktivitetStatus.FL)
				.stream()
				.filter(this::harBgSomKanFlyttes)
				.findFirst();
	}


}
