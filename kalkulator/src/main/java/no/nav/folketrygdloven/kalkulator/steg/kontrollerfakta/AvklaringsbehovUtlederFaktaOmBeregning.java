package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.FaktaOmBeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

public class AvklaringsbehovUtlederFaktaOmBeregning {

    public AvklaringsbehovUtlederFaktaOmBeregning() {
        // for CDI proxy
    }

    public FaktaOmBeregningAvklaringsbehovResultat utledAvklaringsbehovFor(FaktaOmBeregningInput input,
                                                                           BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                           boolean erOverstyrt) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");

        List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = FaktaOmBeregningTilfelleTjeneste.finnTilfellerForFellesAvklaringsbehov(input, beregningsgrunnlagGrunnlag);

        if (erOverstyrt && !KonfigurasjonVerdi.instance().get("TREKKE_OVERSTYRING_ENABLED", false)) {
            return new FaktaOmBeregningAvklaringsbehovResultat(singletonList(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.OVST_INNTEKT)),
                    faktaOmBeregningTilfeller);
        }
        if (faktaOmBeregningTilfeller.isEmpty()) {
            return FaktaOmBeregningAvklaringsbehovResultat.INGEN_AKSJONSPUNKTER;
        }
        return new FaktaOmBeregningAvklaringsbehovResultat(singletonList(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.VURDER_FAKTA_ATFL_SN)),
                faktaOmBeregningTilfeller);
    }
}
