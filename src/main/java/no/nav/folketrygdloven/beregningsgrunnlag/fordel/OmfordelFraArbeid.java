package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class OmfordelFraArbeid extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR 22.3.8";
    public static final String BESKRIVELSE = "Flytt beregnignsgrunnlag fra andre arbeidsforhold.";

    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    OmfordelFraArbeid(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        Map<String, Object> resultater = flyttFraArbeidsforholdOmMulig(beregningsgrunnlagPeriode);
        return beregnet(resultater);
    }

    private Map<String, Object> flyttFraArbeidsforholdOmMulig(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        return new OmfordelBGForArbeidsforhold(beregningsgrunnlagPeriode).omfordelBGForArbeidsforhold(arbeidsforhold, this::finnArbeidMedOmfordelbartBg);
    }

    private Optional<BeregningsgrunnlagPrArbeidsforhold> finnArbeidMedOmfordelbartBg(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
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
