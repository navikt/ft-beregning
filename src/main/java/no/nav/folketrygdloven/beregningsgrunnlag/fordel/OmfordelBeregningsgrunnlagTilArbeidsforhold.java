package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.specification.Specification;

public class OmfordelBeregningsgrunnlagTilArbeidsforhold extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    private static final String ID = "FP_BR 22.3.5";
    private static final String BESKRIVELSE = "Regelen skal omfordele beregningsgrunnlag fra arbeidsforhold som krever mer i refusjon enn det har i beregningsgrunnlag.";

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        ServiceArgument arg = getServiceArgument();
        if (arg == null || !(arg.getVerdi() instanceof BeregningsgrunnlagPrArbeidsforhold)) {
            throw new IllegalStateException("Utviklerfeil: Arbeidsforhold må angis som parameter");
        }
        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = (BeregningsgrunnlagPrArbeidsforhold) arg.getVerdi();

        Specification<BeregningsgrunnlagPeriode> omfordelFraAT = rs.beregningsRegel(OmfordelFraArbeid.ID, OmfordelFraArbeid.BESKRIVELSE, new OmfordelFraArbeid(arbeidsforhold), new Beregnet());

        Specification<BeregningsgrunnlagPeriode> skalOmfordeleFraAT = rs.beregningHvisRegel(new SjekkOmRefusjonOverstigerBeregningsgrunnlag(arbeidsforhold), omfordelFraAT, new Beregnet());

        Specification<BeregningsgrunnlagPeriode> omfordelFraFL = rs.beregningsRegel(OmfordelFraFrilans.ID, OmfordelFraFrilans.BESKRIVELSE, new OmfordelFraFrilans(arbeidsforhold), skalOmfordeleFraAT);

        Specification<BeregningsgrunnlagPeriode> skalOmfordeleFraFL = rs.beregningHvisRegel(new SjekkOmRefusjonOverstigerBeregningsgrunnlag(arbeidsforhold), omfordelFraFL, new Beregnet());

        Specification<BeregningsgrunnlagPeriode> omfordelBeregningsgrunnlag = rs.beregningsRegel(ID, BESKRIVELSE, new OmfordelFraAktiviteterUtenArbeidsforhold(arbeidsforhold), skalOmfordeleFraFL);

        return omfordelBeregningsgrunnlag;
    }

}
