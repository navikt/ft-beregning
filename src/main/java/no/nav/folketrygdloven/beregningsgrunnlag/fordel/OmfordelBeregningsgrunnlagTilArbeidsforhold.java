package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.specification.Specification;

public class OmfordelBeregningsgrunnlagTilArbeidsforhold extends DynamicRuleService<FordelPeriodeModell> {

    private static final String ID = "FP_BR 22.3.5";
    private static final String BESKRIVELSE = "Regelen skal omfordele beregningsgrunnlag fra arbeidsforhold som krever mer i refusjon enn det har i beregningsgrunnlag.";

    @SuppressWarnings("unchecked")
    @Override
    public Specification<FordelPeriodeModell> getSpecification() {
        Ruleset<FordelPeriodeModell> rs = new Ruleset<>();

        ServiceArgument arg = getServiceArgument();
        if (arg == null || !(arg.getVerdi() instanceof FordelAndelModell)) {
            throw new IllegalStateException("Utviklerfeil: Arbeidsforhold må angis som parameter");
        }
	    FordelAndelModell andelMedHøyereRefEnnBG = (FordelAndelModell) arg.getVerdi();

        Specification<FordelPeriodeModell> omfordelFraAT = rs.beregningsRegel(OmfordelFraArbeid.ID, OmfordelFraArbeid.BESKRIVELSE, new OmfordelFraArbeid(andelMedHøyereRefEnnBG), new Fordelt());

        Specification<FordelPeriodeModell> skalOmfordeleFraAT = rs.beregningHvisRegel(new SjekkOmRefusjonOverstigerBeregningsgrunnlag(andelMedHøyereRefEnnBG), omfordelFraAT, new Fordelt());

        Specification<FordelPeriodeModell> omfordelFraFL = rs.beregningsRegel(OmfordelFraFrilans.ID, OmfordelFraFrilans.BESKRIVELSE, new OmfordelFraFrilans(andelMedHøyereRefEnnBG), skalOmfordeleFraAT);

        Specification<FordelPeriodeModell> skalOmfordeleFraFL = rs.beregningHvisRegel(new SjekkOmRefusjonOverstigerBeregningsgrunnlag(andelMedHøyereRefEnnBG), omfordelFraFL, new Fordelt());

        Specification<FordelPeriodeModell> omfordelBeregningsgrunnlag = rs.beregningsRegel(ID, BESKRIVELSE, new OmfordelFraAktiviteterUtenArbeidsforhold(andelMedHøyereRefEnnBG), skalOmfordeleFraFL);

        return omfordelBeregningsgrunnlag;
    }

}
