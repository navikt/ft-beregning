package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public final class AvklaringsbehovutlederVurderRefusjon {

    private AvklaringsbehovutlederVurderRefusjon() {
        // Skjuler default
    }

    public static boolean skalHaAvklaringsbehovVurderRefusjonskrav(BeregningsgrunnlagInput input, BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        if (!(input instanceof VurderRefusjonBeregningsgrunnlagInput)) {
            throw new IllegalStateException("Har ikke korrekt input for å vurdere aksjsonspunkt i vurder_refusjon steget");
        }
        VurderRefusjonBeregningsgrunnlagInput vurderInput = (VurderRefusjonBeregningsgrunnlagInput) input;
        List<BeregningsgrunnlagGrunnlagDto> orginaltBGGrunnlag = vurderInput.getBeregningsgrunnlagGrunnlagFraForrigeBehandling();
        if (orginaltBGGrunnlag.isEmpty() || orginaltBGGrunnlag.stream().noneMatch(gr -> gr.getBeregningsgrunnlagHvisFinnes().isPresent())) {
            return false;
        }
        var perioderTilVurderingTjeneste = new PerioderTilVurderingTjeneste(input.getForlengelseperioder(), periodisertMedRefusjonOgGradering);
        var grenseverdi = periodisertMedRefusjonOgGradering.getGrunnbeløp().multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi());
        var orginaleBG = orginaltBGGrunnlag.stream().flatMap(gr -> gr.getBeregningsgrunnlagHvisFinnes().stream())
                .collect(Collectors.toList());
        Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjonIUtbetaltPeriode = orginaleBG.stream()
                .flatMap(originaltBg -> AndelerMedØktRefusjonTjeneste.finnAndelerMedØktRefusjon(periodisertMedRefusjonOgGradering, originaltBg, grenseverdi, input.getYtelsespesifiktGrunnlag()).entrySet().stream())
                .filter(e -> perioderTilVurderingTjeneste.erTilVurdering(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return !andelerMedØktRefusjonIUtbetaltPeriode.isEmpty();
    }
}
