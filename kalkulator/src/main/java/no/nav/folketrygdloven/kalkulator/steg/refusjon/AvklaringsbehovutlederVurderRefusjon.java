package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.frist.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;

public final class AvklaringsbehovutlederVurderRefusjon {

    private AvklaringsbehovutlederVurderRefusjon() {
        // Skjuler default
    }

    public static boolean skalHaAvklaringsbehovVurderRefusjonskrav(BeregningsgrunnlagInput input, BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        var vurderInput = mapTilVurderRefusjonBeregningsgrunnlagInput(input);
        var orginaltBGGrunnlag = vurderInput.getBeregningsgrunnlagGrunnlagFraForrigeBehandling();
        if (orginaltBGGrunnlag.isEmpty() || orginaltBGGrunnlag.stream().noneMatch(gr -> gr.getBeregningsgrunnlagHvisFinnes().isPresent())) {
            return false;
        }
        var perioderTilVurderingTjeneste = new PerioderTilVurderingTjeneste(input.getForlengelseperioder(), periodisertMedRefusjonOgGradering);
        var grenseverdi = periodisertMedRefusjonOgGradering.getGrunnbeløp().multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi());
        var orginaleBG = orginaltBGGrunnlag.stream().flatMap(gr -> gr.getBeregningsgrunnlagHvisFinnes().stream()).toList();
	    var andelerMedØktRefusjonIUtbetaltPeriode = orginaleBG.stream()
                .flatMap(originaltBg -> AndelerMedØktRefusjonTjeneste.finnAndelerMedØktRefusjon(periodisertMedRefusjonOgGradering, originaltBg, grenseverdi, input.getYtelsespesifiktGrunnlag()).entrySet().stream())
                .filter(e -> perioderTilVurderingTjeneste.erTilVurdering(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return !andelerMedØktRefusjonIUtbetaltPeriode.isEmpty();
    }

    public static boolean skalHaAvklaringsbehovVurderRefusjonskravKommetForSent(BeregningsgrunnlagInput input) {
        var vurderInput = mapTilVurderRefusjonBeregningsgrunnlagInput(input);
        return !InntektsmeldingMedRefusjonTjeneste.finnArbeidsgivereSomHarSøktRefusjonForSent(vurderInput.getIayGrunnlag(), vurderInput.getBeregningsgrunnlagGrunnlag(), vurderInput.getKravPrArbeidsgiver(),
                vurderInput.getFagsakYtelseType()).isEmpty();
    }

    private static VurderRefusjonBeregningsgrunnlagInput mapTilVurderRefusjonBeregningsgrunnlagInput(BeregningsgrunnlagInput input) {
        if (!(input instanceof VurderRefusjonBeregningsgrunnlagInput)) {
            throw new IllegalStateException("Har ikke korrekt input for å vurdere aksjsonspunkt i vurder_refusjon steget");
        }
        return (VurderRefusjonBeregningsgrunnlagInput) input;
    }
}
