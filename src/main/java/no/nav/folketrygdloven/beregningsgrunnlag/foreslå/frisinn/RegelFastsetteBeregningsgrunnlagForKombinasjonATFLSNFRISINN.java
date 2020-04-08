package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.kombinasjon.BeregnSelvstendigAndelForKombinasjon;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

import java.util.Arrays;

public class RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNFRISINN extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FRISINN 2.8";

    public RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNFRISINN(BeregningsgrunnlagPeriode regelmodell) {
        super(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        Specification<BeregningsgrunnlagPeriode> beregnBruttoSN =
            rs.beregningsRegel("FP_BR 2.2 - 2.10", "Beregn SN-andel", new BeregnSelvstendigAndelForKombinasjon(), new Beregnet());

        Specification<BeregningsgrunnlagPeriode> beregnPGI =
            rs.beregningsRegel("FP_BR 2", "Fastsett beregningsperiode og beregn oppjusterte inntekter og pgi-snitt.",
                Arrays.asList(new FastsettBeregningsperiodeSNFRISINN(), new BeregnOppjustertInntektFRISINN(), new BeregnGjennomsnittligPGIFRISINN()), beregnBruttoSN);

        Specification<BeregningsgrunnlagPeriode> beregningsgrunnlagKombinasjon;

        beregningsgrunnlagKombinasjon =
            rs.beregningsRegel("FP_BR_14-15-27-28", "Beregn beregningsgrunnlag for arbeidstaker/frilanser)",
                new RegelBeregningsgrunnlagATFLFRISINN(regelmodell).getSpecification(), beregnPGI);

        return beregningsgrunnlagKombinasjon;
    }
}
