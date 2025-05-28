package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.specification.Specification;

public class RegelBeregningsgrunnlagATFLFRISINN implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_14-15-27-28";
    private BeregningsgrunnlagPeriode regelmodell;

    public RegelBeregningsgrunnlagATFLFRISINN(BeregningsgrunnlagPeriode regelmodell) {
        super();
        this.regelmodell = regelmodell;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        var rs = new Ruleset<BeregningsgrunnlagPeriode>();

        var fastsettBeregnetPrÅr =
            rs.beregningsRegel("FRISINN 2.10", "Fastsett beregnet pr år for ATFL",
                new FastsettBeregnetPrÅrFRISINN(), new Beregnet());

        var vurderMotOppgittArbeidstakerinntekt =
            rs.beregningsRegel("FRISINN 2.9", "Vurder brutto fra register oppmot oppgitt arbeidstakerinntekt",
                new RegelVurderRegisterinntektMotOppgittInntektATFLFRISINN(), fastsettBeregnetPrÅr);

        var harInntektForATFLBlittManueltFastsatt =
            rs.beregningHvisRegel(new SjekkManueltFastsattAvSBH(), fastsettBeregnetPrÅr, vurderMotOppgittArbeidstakerinntekt);

        var arbeidsforhold = regelmodell.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
	    var speclist = arbeidsforhold.stream()
			    .map(a -> new RegelBeregnBruttoPrArbeidsforholdFRISINN(a).getSpecification().medEvaluationProperty(new ServiceArgument("arbeidsforhold", a.getArbeidsforhold())))
			    .toList();
        var beregningsgrunnlagATFL =
			    rs.beregningsRegel("FRISINN 2.X", "Fastsett beregningsgrunnlag pr arbeidsforhold",
					    speclist, harInntektForATFLBlittManueltFastsatt);


        return beregningsgrunnlagATFL;
    }
}
