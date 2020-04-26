package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class OmfordelFraFrilans extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 22.3.8";
    static final String BESKRIVELSE = "Flytt beregningsgrunnlag fra frilans.";

    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    OmfordelFraFrilans(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        Map<String, Object> resultater = omfordelFraFrilansOmMulig(beregningsgrunnlagPeriode, arbeidsforhold);
        return beregnet(resultater);
    }

    private Map<String, Object> omfordelFraFrilansOmMulig(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, BeregningsgrunnlagPrArbeidsforhold aktivitet) {
        return new OmfordelBGForArbeidsforhold(beregningsgrunnlagPeriode).omfordelBGForArbeidsforhold(aktivitet, this::finnFrilansMedOmfordelbartBg);
    }

    private Optional<BeregningsgrunnlagPrArbeidsforhold> finnFrilansMedOmfordelbartBg(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL)
            .getFrilansArbeidsforhold()
            .stream()
            .filter(this::harBgSomKanFlyttes)
            .findFirst();
    }

    private boolean harBgSomKanFlyttes(BeregningsgrunnlagPrArbeidsforhold beregningsgrunnlagPrArbeidsforhold) {
        return beregningsgrunnlagPrArbeidsforhold.getGradertBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0
            && (beregningsgrunnlagPrArbeidsforhold.getFordeltPrÅr() == null || beregningsgrunnlagPrArbeidsforhold.getFordeltPrÅr().compareTo(BigDecimal.ZERO) > 0);
    }




}
