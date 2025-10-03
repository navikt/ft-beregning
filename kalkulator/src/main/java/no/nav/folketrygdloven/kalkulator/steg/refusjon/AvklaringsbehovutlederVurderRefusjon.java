package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.frist.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public final class AvklaringsbehovutlederVurderRefusjon {

    private AvklaringsbehovutlederVurderRefusjon() {
        // Skjuler default
    }

    public static boolean skalHaAvklaringsbehovVurderRefusjonskrav(BeregningsgrunnlagInput input, BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        if (!(input instanceof VurderRefusjonBeregningsgrunnlagInput vurderInput)) {
            throw new IllegalStateException("Har ikke korrekt input for å vurdere aksjsonspunkt i vurder_refusjon steget");
        }

        var orginaltBGGrunnlag = vurderInput.getBeregningsgrunnlagGrunnlagFraForrigeBehandling();
        if (orginaltBGGrunnlag.isEmpty() || orginaltBGGrunnlag.stream().noneMatch(gr -> gr.getBeregningsgrunnlagHvisFinnes().isPresent())) {
            return false;
        }

        if (erFPEllerSVP(vurderInput) && vurderInput.isEnabled("refusjonsfrist.flytting", false)
            && skalHaAvklaringsbehovVurderRefusjonskravKommetForSent(vurderInput)) {
            return true;
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

    private static boolean erFPEllerSVP(VurderRefusjonBeregningsgrunnlagInput vurderInput) {
        return vurderInput.getFagsakYtelseType() == FagsakYtelseType.FORELDREPENGER
            || vurderInput.getFagsakYtelseType() == FagsakYtelseType.SVANGERSKAPSPENGER;
    }

    private static boolean skalHaAvklaringsbehovVurderRefusjonskravKommetForSent(BeregningsgrunnlagInput vurderInput) {
        return !InntektsmeldingMedRefusjonTjeneste.finnArbeidsgivereSomHarSøktRefusjonForSent(vurderInput.getIayGrunnlag(), vurderInput.getBeregningsgrunnlagGrunnlag(), vurderInput.getKravPrArbeidsgiver(),
                vurderInput.getFagsakYtelseType()).isEmpty();
    }
}
