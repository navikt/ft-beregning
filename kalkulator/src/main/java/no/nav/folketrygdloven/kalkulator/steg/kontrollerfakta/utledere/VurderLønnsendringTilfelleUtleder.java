package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.LønnsendringTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


public class VurderLønnsendringTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input, BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();
        List<YrkesaktivitetDto> lønnsendringer = LønnsendringTjeneste.finnAktiviteterMedLønnsendringUtenInntektsmeldingIBeregningsperiodenOgTilStp(beregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger());
        return lønnsendringer.isEmpty()
                ? Optional.empty()
                : Optional.of(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING);
    }

}
