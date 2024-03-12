package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.FaktaOmBeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.FaktaOmBeregningTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

public class AvklaringsbehovUtlederFaktaOmBeregningFRISINN {

    public AvklaringsbehovUtlederFaktaOmBeregningFRISINN() {
        super();
    }

    public FaktaOmBeregningAvklaringsbehovResultat utledAvklaringsbehovFor(FaktaOmBeregningInput input,
                                                                           BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = FaktaOmBeregningTilfelleTjeneste.finnTilfellerForFellesAvklaringsbehov(input, beregningsgrunnlagGrunnlag);
        if (faktaOmBeregningTilfeller.contains(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON)) {
            return new FaktaOmBeregningAvklaringsbehovResultat(singletonList(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.VURDER_FAKTA_ATFL_SN)),
                    List.of(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON));
        }
        return FaktaOmBeregningAvklaringsbehovResultat.INGEN_AKSJONSPUNKTER;
    }
}
