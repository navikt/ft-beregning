package no.nav.folketrygdloven.beregningsgrunnlag.perioder.refusjon;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagResultat.Refusjonsandel;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.folketrygdloven.regelmodelloversetter.RegelJsonMapper;
import no.nav.folketrygdloven.regelmodelloversetter.RegelmodellOversetter;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(FastsettRefusjonsgrunnlagRegel.ID)
public class FastsettRefusjonsgrunnlagRegel implements EksportRegel<RefusjonsgrunnlagModell> {

    static final String ID = "FT_44";

    public RefusjonsgrunnlagResultat fastsett(RefusjonsgrunnlagModell input) {
        var inputJson = RegelJsonMapper.asJson(input);
        var prosesstruktur = new RefusjonsgrunnlagProsesstruktur(input, new ArrayList<>());
        var evaluation = getSpecification().evaluate(prosesstruktur);
        return new RefusjonsgrunnlagResultat(prosesstruktur.refusjonsandeler(), RegelmodellOversetter.getRegelResultat(evaluation, inputJson));
    }

    @Override
    public Evaluation evaluer(RefusjonsgrunnlagModell input) {
        return getSpecification().evaluate(new RefusjonsgrunnlagProsesstruktur(input, new ArrayList<>()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Specification<RefusjonsgrunnlagProsesstruktur> getSpecification() {
        return new FastsettRefusjonsgrunnlag();
    }

    record RefusjonsgrunnlagProsesstruktur(RefusjonsgrunnlagModell input, List<Refusjonsandel> refusjonsandeler) {
    }
}
