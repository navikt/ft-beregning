package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.felles.frist.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


public class VurderRefusjonskravTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input, BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        if (!InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
                input.getKoblingReferanse(),
                input.getIayGrunnlag(),
                beregningsgrunnlagGrunnlag,
                input.getKravPrArbeidsgiver(),
                input.getFagsakYtelseType()).isEmpty()) {
            return Optional.of(FaktaOmBeregningTilfelle.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT);
        }
        return Optional.empty();
    }
}
