package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

class FastsettNyFordeling implements RuleService<BeregningsgrunnlagPeriode> {

    private static final String ID = "FP_BR 22.3.4";
    private static final String BESKRIVELSE = "Fastsett fordeling for arbeidstakerandeler der refusjon overstiger beregningsgrunnlag?";

    private BeregningsgrunnlagPeriode regelmodell;

    public FastsettNyFordeling(BeregningsgrunnlagPeriode regelmodell) {
        super();
        this.regelmodell = regelmodell;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        List<BeregningsgrunnlagPrArbeidsforhold> refOverstigerBgAktivitetListe = finnListeMedAktiteterSomKreverFlyttingAvBeregningsgrunnlag(regelmodell);
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();
        Specification<BeregningsgrunnlagPeriode> beregningsgrunnlagATFL = refOverstigerBgAktivitetListe.isEmpty() ? new Beregnet() :
            rs.beregningsRegel(ID, BESKRIVELSE,
                OmfordelBeregningsgrunnlagTilArbeidsforhold.class, regelmodell, "arbeidsforhold", refOverstigerBgAktivitetListe, new Beregnet());
        return beregningsgrunnlagATFL;
    }

    private List<BeregningsgrunnlagPrArbeidsforhold> finnListeMedAktiteterSomKreverFlyttingAvBeregningsgrunnlag(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        BeregningsgrunnlagPrStatus atfl = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        return atfl == null ? Collections.emptyList() : atfl
            .getArbeidsforholdIkkeFrilans()
            .stream()
            .filter(af -> af.getGradertBruttoInkludertNaturalytelsePrÅr().isPresent())
            .filter(this::refusjonskravOverstigerBg)
            .collect(Collectors.toList());
    }

    private boolean refusjonskravOverstigerBg(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        BigDecimal refusjonskrav = arbeidsforhold.getGradertRefusjonskravPrÅr().orElse(BigDecimal.ZERO);
        return refusjonskrav.compareTo(arbeidsforhold.getGradertBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO)) > 0;
    }

}
