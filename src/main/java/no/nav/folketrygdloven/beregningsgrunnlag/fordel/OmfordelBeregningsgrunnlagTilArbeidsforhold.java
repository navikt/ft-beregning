package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

public class OmfordelBeregningsgrunnlagTilArbeidsforhold implements RuleService<FordelModell> {

    private static final String ID = "FP_BR 22.3.5";
    private static final String BESKRIVELSE = "Regelen skal omfordele beregningsgrunnlag fra arbeidsforhold som krever mer i refusjon enn det har i beregningsgrunnlag.";

	public OmfordelBeregningsgrunnlagTilArbeidsforhold() {
		super();
	}

	@SuppressWarnings("unchecked")
    @Override
    public Specification<FordelModell> getSpecification() {
        Ruleset<FordelModell> rs = new Ruleset<>();

		Specification<FordelModell> omfordelFraAT = rs.beregningsRegel(OmfordelFraArbeid.ID, OmfordelFraArbeid.BESKRIVELSE, new OmfordelFraArbeid(), new Fordelt());

        Specification<FordelModell> skalOmfordeleFraAT = rs.beregningHvisRegel(new SjekkOmRefusjonOverstigerBeregningsgrunnlag(), omfordelFraAT, new Fordelt());

        Specification<FordelModell> omfordelFraFL = rs.beregningsRegel(OmfordelFraFrilans.ID, OmfordelFraFrilans.BESKRIVELSE, new OmfordelFraFrilans(), skalOmfordeleFraAT);

        Specification<FordelModell> skalOmfordeleFraFL = rs.beregningHvisRegel(new SjekkOmRefusjonOverstigerBeregningsgrunnlag(), omfordelFraFL, new Fordelt());

        Specification<FordelModell> omfordelBeregningsgrunnlag = rs.beregningsRegel(ID, BESKRIVELSE, new OmfordelFraAktiviteterUtenArbeidsforhold(), skalOmfordeleFraFL);

        return omfordelBeregningsgrunnlag;
    }

}
