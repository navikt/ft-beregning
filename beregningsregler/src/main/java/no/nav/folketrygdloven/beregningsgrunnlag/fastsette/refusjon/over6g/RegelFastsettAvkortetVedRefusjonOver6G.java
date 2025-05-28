package no.nav.folketrygdloven.beregningsgrunnlag.fastsette.refusjon.over6g;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Fastsatt;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFastsettAvkortetVedRefusjonOver6G implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_29.13";
    public static final String BESKRIVELSE = "Fastsett avkortet BG når refusjon over 6G";
    private BeregningsgrunnlagPeriode regelmodell;

    public RegelFastsettAvkortetVedRefusjonOver6G(BeregningsgrunnlagPeriode regelmodell) {
        super();
        this.regelmodell = regelmodell;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        var bgpsa = regelmodell.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (bgpsa == null) {
            return new Fastsatt();
        }

        var rs = new Ruleset<BeregningsgrunnlagPeriode>();

        Specification<BeregningsgrunnlagPeriode> fastsettBrukersAndelerTilNull = new FastsettBrukersAndelerTilNull();

        var antallKjøringer = bgpsa.getArbeidsforhold().size();
        Specification<BeregningsgrunnlagPeriode> fastsettAvkortetBeregningsgrunnlag = new Fastsatt();
        if (antallKjøringer > 0) {
            List<Specification<BeregningsgrunnlagPeriode>> prArbeidsforhold = new ArrayList<>();
            for (var nr = 1; nr <= antallKjøringer; nr++) {
                prArbeidsforhold.add(opprettRegelBeregnRefusjonPrArbeidsforhold());
            }
            fastsettAvkortetBeregningsgrunnlag = rs.beregningsRegel(ID, BESKRIVELSE, prArbeidsforhold, fastsettBrukersAndelerTilNull);
        }

        return fastsettAvkortetBeregningsgrunnlag;
    }

    private Specification<BeregningsgrunnlagPeriode> opprettRegelBeregnRefusjonPrArbeidsforhold() {
        return new RegelBeregnRefusjonPrArbeidsforhold().getSpecification();
    }
}
