package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

class FastsettNyFordeling implements RuleService<FordelModell> {

    private static final String ID = "FP_BR 22.3.4";
    private static final String BESKRIVELSE = "Fastsett fordeling for arbeidstakerandeler der refusjon overstiger beregningsgrunnlag?";

    private FordelModell modell;

    public FastsettNyFordeling(FordelModell modell) {
        super();
        this.modell = modell;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<FordelModell> getSpecification() {
        var refOverstigerBgAktivitetListe = finnListeMedAktiteterSomKreverFlyttingAvBeregningsgrunnlag(modell.getInput());
        Ruleset<FordelModell> rs = new Ruleset<>();
        var beregningsgrunnlagATFL = refOverstigerBgAktivitetListe.isEmpty() ? new Fordelt() :
            rs.beregningsForeachThenRegel(ID, BESKRIVELSE,
                new OmfordelBeregningsgrunnlagTilArbeidsforhold().getSpecification(), "arbeidsforhold", refOverstigerBgAktivitetListe, new Fordelt());
        return beregningsgrunnlagATFL;
    }

    private List<FordelAndelModell> finnListeMedAktiteterSomKreverFlyttingAvBeregningsgrunnlag(FordelPeriodeModell beregningsgrunnlagPeriode) {
        var arbeidsandeler = beregningsgrunnlagPeriode.getAlleAndelerForStatus(AktivitetStatus.AT);
        return arbeidsandeler.stream()
            .filter(this::refusjonskravOverstigerBg)
            .collect(Collectors.toList());
    }

    private boolean refusjonskravOverstigerBg(FordelAndelModell arbeidsforhold) {
        BigDecimal refusjonskrav = arbeidsforhold.getGradertRefusjonPrÅr().orElse(BigDecimal.ZERO);
        return refusjonskrav.compareTo(arbeidsforhold.getGradertBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO)) > 0;
    }

}
