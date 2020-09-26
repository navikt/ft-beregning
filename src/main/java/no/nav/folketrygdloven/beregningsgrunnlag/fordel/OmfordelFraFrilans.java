package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;

class OmfordelFraFrilans extends OmfordelFraATFL {

    static final String ID = "FP_BR 22.3.8";
    static final String BESKRIVELSE = "Flytt beregningsgrunnlag fra frilans.";

    OmfordelFraFrilans(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(arbeidsforhold, ID, BESKRIVELSE);
    }

    @Override
    protected Inntektskategori finnInntektskategori() {
        return Inntektskategori.FRILANSER;
    };

    @Override
    protected Optional<BeregningsgrunnlagPrArbeidsforhold> finnAktivitetMedOmfordelbartBg(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL)
            .getFrilansArbeidsforhold()
            .stream()
            .filter(this::harBgSomKanFlyttes)
            .findFirst();
    };

    private boolean harBgSomKanFlyttes(BeregningsgrunnlagPrArbeidsforhold beregningsgrunnlagPrArbeidsforhold) {
        return beregningsgrunnlagPrArbeidsforhold.getBruttoPrÅr().orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0
            && (beregningsgrunnlagPrArbeidsforhold.getFordeltPrÅr() == null || beregningsgrunnlagPrArbeidsforhold.getFordeltPrÅr().compareTo(BigDecimal.ZERO) > 0);
    }


}
