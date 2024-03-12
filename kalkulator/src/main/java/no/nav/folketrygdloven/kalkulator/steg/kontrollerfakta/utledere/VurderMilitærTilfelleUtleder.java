package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class VurderMilitærTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input, BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {

        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        return harOppgittMilitærIOpptjeningsperioden(input.getOpptjeningAktiviteterForBeregning()) ?
                Optional.of(FaktaOmBeregningTilfelle.VURDER_MILITÆR_SIVILTJENESTE) : Optional.empty();
    }

    private static boolean harOppgittMilitærIOpptjeningsperioden(Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> opptjeningRelevantForBeregning) {
        return opptjeningRelevantForBeregning.stream()
                .anyMatch(opptjening -> opptjening.getOpptjeningAktivitetType().equals(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE));
    }

}
