package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.EtterlønnSluttpakkeTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


public class EtterlønnSluttpakkeTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        return EtterlønnSluttpakkeTjeneste.skalVurdereOmBrukerHarEtterlønnSluttpakke(beregningsgrunnlagGrunnlag) ?
                Optional.of(FaktaOmBeregningTilfelle.VURDER_ETTERLØNN_SLUTTPAKKE) : Optional.empty();
    }
}
