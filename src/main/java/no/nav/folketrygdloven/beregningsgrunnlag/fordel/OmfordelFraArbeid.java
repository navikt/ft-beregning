package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class OmfordelFraArbeid extends OmfordelFraATFL {

    public static final String ID = "FP_BR 22.3.8";
    public static final String BESKRIVELSE = "Flytt beregnignsgrunnlag fra andre arbeidsforhold.";

    OmfordelFraArbeid(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(arbeidsforhold, ID, BESKRIVELSE);
    }

    @Override
    protected Inntektskategori finnInntektskategori() {
        return Inntektskategori.ARBEIDSTAKER;
    }

    @Override
    protected Optional<BeregningsgrunnlagPrArbeidsforhold> finnAktivitetMedOmfordelbartBg(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL)
            .getArbeidsforholdIkkeFrilans()
            .stream()
            .filter(this::harBgSomKanFlyttes)
            .filter(beregningsgrunnlagPrArbeidsforhold -> !refusjonskravOverstigerEllerErLikBg(beregningsgrunnlagPrArbeidsforhold))
            .findFirst();
    }

    private boolean refusjonskravOverstigerEllerErLikBg(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        BigDecimal refusjonskrav = arbeidsforhold.getRefusjonskravPrÅr().orElse(BigDecimal.ZERO);
        return refusjonskrav.compareTo(arbeidsforhold.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO)) >= 0;
    }

    private boolean harBgSomKanFlyttes(BeregningsgrunnlagPrArbeidsforhold beregningsgrunnlagPrArbeidsforhold) {
        return beregningsgrunnlagPrArbeidsforhold.getBruttoPrÅr().orElse(BigDecimal.ZERO)
            .subtract(beregningsgrunnlagPrArbeidsforhold.getNaturalytelseTilkommetPrÅr().orElse(BigDecimal.ZERO)).compareTo(BigDecimal.ZERO) > 0
            && (beregningsgrunnlagPrArbeidsforhold.getFordeltPrÅr() == null || beregningsgrunnlagPrArbeidsforhold.getFordeltPrÅr().compareTo(BigDecimal.ZERO) > 0);
    }

}
