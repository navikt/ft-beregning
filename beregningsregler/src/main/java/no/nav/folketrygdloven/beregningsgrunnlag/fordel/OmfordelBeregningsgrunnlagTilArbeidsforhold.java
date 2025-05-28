package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class OmfordelBeregningsgrunnlagTilArbeidsforhold implements RuleService<FordelModell> {

    private static final String ID = "FP_BR 22.3.5";
    private static final String BESKRIVELSE = "Regelen skal omfordele beregningsgrunnlag fra arbeidsforhold som krever mer i refusjon enn det har i beregningsgrunnlag.";

	private FordelAndelModell andelMedHøyereRefEnnBG;

	public OmfordelBeregningsgrunnlagTilArbeidsforhold(FordelAndelModell andelModell) {
		this.andelMedHøyereRefEnnBG = andelModell;
	}

	@SuppressWarnings("unchecked")
    @Override
    public Specification<FordelModell> getSpecification() {
        var rs = new Ruleset<FordelModell>();

        var omfordelFraAT = rs.beregningsRegel(OmfordelFraArbeid.ID, OmfordelFraArbeid.BESKRIVELSE, new OmfordelFraArbeid(andelMedHøyereRefEnnBG), new Fordelt());

        var skalOmfordeleFraAT = rs.beregningHvisRegel(new SjekkOmRefusjonOverstigerBeregningsgrunnlag(andelMedHøyereRefEnnBG), omfordelFraAT, new Fordelt());

        var omfordelFraFL = rs.beregningsRegel(OmfordelFraFrilans.ID, OmfordelFraFrilans.BESKRIVELSE, new OmfordelFraFrilans(andelMedHøyereRefEnnBG), skalOmfordeleFraAT);

        var skalOmfordeleFraFL = rs.beregningHvisRegel(new SjekkOmRefusjonOverstigerBeregningsgrunnlag(andelMedHøyereRefEnnBG), omfordelFraFL, new Fordelt());

        var omfordelBeregningsgrunnlag = rs.beregningsRegel(ID, BESKRIVELSE, new OmfordelFraAktiviteterUtenArbeidsforhold(andelMedHøyereRefEnnBG), skalOmfordeleFraFL);

        return omfordelBeregningsgrunnlag;
    }

}
