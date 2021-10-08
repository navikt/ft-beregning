package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

class FastsettNyFordeling implements RuleService<FordelPeriodeModell> {

    private static final String ID = "FP_BR 22.3.4";
    private static final String BESKRIVELSE = "Fastsett fordeling for arbeidstakerandeler der refusjon overstiger beregningsgrunnlag?";

    private FordelPeriodeModell regelmodell;

    public FastsettNyFordeling(FordelPeriodeModell regelmodell) {
        super();
        this.regelmodell = regelmodell;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<FordelPeriodeModell> getSpecification() {
        var refOverstigerBgAktivitetListe = finnListeMedAktiteterSomKreverFlyttingAvBeregningsgrunnlag(regelmodell);
        Ruleset<FordelPeriodeModell> rs = new Ruleset<>();
        var beregningsgrunnlagATFL = refOverstigerBgAktivitetListe.isEmpty() ? new Fordelt() :
            rs.beregningsRegel(ID, BESKRIVELSE,
                OmfordelBeregningsgrunnlagTilArbeidsforhold.class, regelmodell, "arbeidsforhold", refOverstigerBgAktivitetListe, new Fordelt());
        return beregningsgrunnlagATFL;
    }

    private List<FordelAndelModell> finnListeMedAktiteterSomKreverFlyttingAvBeregningsgrunnlag(FordelPeriodeModell beregningsgrunnlagPeriode) {
        var arbeidsandeler = beregningsgrunnlagPeriode.getAlleAndelerForStatus(AktivitetStatus.AT);
        return arbeidsandeler.stream()
            .filter(this::refusjonskravOverstigerBg)
            .collect(Collectors.toList());
    }

    private boolean refusjonskravOverstigerBg(FordelAndelModell arbeidsforhold) {
        BigDecimal refusjonskrav = arbeidsforhold.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO);
        return refusjonskrav.compareTo(arbeidsforhold.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO)) > 0;
    }

}
