package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.FastsettBeregnetPrÅr;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(value = RegelBeregningsgrunnlagSNFRISINN.ID, specificationReference = "https://confluence.adeo.no/display/SIF/30.+Beregningsgrunnlag")
public class RegelBeregningsgrunnlagSNFRISINN implements RuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "FRISINN 2.7";

    @Override
    public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        var rs = new Ruleset<BeregningsgrunnlagPeriode>();

        var beregnBruttoSN =
            rs.beregningsRegel(BeregnBruttoBeregningsgrunnlagSNFRISINN.ID, "Beregn brutto beregningsgrunnlag selvstendig næringsdrivende",
                new BeregnBruttoBeregningsgrunnlagSNFRISINN(), new FastsettBeregnetPrÅr(AktivitetStatus.SN));


        var foreslåBeregningsgrunnlagForSelvstendigNæringsdrivende =
            rs.beregningsRegel(FastsettBeregningsperiodeSNFRISINN.ID, "Foreslå beregningsgrunnlag for selvstendig næringsdrivende",
                new FastsettBeregningsperiodeSNFRISINN(), beregnBruttoSN);

        return foreslåBeregningsgrunnlagForSelvstendigNæringsdrivende;
    }
}
