package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.ArbeidUnderAAPTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.EtterlønnSluttpakkeTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

import java.util.Optional;


public class ArbeidUnderAAPTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        return  ArbeidUnderAAPTjeneste.harAndelForArbeidUnderAAP(beregningsgrunnlagGrunnlag) ?
                Optional.of(FaktaOmBeregningTilfelle.FASTSETT_INNTEKT_FOR_ARBEID_UNDER_AAP) : Optional.empty();
    }
}
