package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.util.List;

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

        if (erFPEllerSVP(vurderInput) && vurderInput.isEnabled("refusjonsfrist.flytting", false)
            && skalHaAvklaringsbehovVurderRefusjonskravKommetForSent(vurderInput)) {
            return true;
        }

        var forrigeGrunnlagListe = vurderInput.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().stream()
            .flatMap(gr -> gr.getBeregningsgrunnlagHvisFinnes().stream())
            .toList();

        if (forrigeGrunnlagListe.isEmpty()) {
            return false;
        }

        return harAndelerMedØktRefusjonIUtbetaltPeriode(input, periodisertMedRefusjonOgGradering, forrigeGrunnlagListe);
    }

    private static boolean harAndelerMedØktRefusjonIUtbetaltPeriode(BeregningsgrunnlagInput input,
                                                                    BeregningsgrunnlagDto periodisertMedRefusjonOgGradering,
                                                                    List<BeregningsgrunnlagDto> forrigeGrunnlagListe) {
        var perioderTilVurderingTjeneste = new PerioderTilVurderingTjeneste(input.getForlengelseperioder(), periodisertMedRefusjonOgGradering);
        var grenseverdi = periodisertMedRefusjonOgGradering.getGrunnbeløp().multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi());
        return forrigeGrunnlagListe.stream()
            .flatMap(
                forrigeGrunnlag -> AndelerMedØktRefusjonTjeneste.finnAndelerMedØktRefusjon(periodisertMedRefusjonOgGradering, forrigeGrunnlag, grenseverdi,
                    input.getYtelsespesifiktGrunnlag()).entrySet().stream())
            .anyMatch(e -> perioderTilVurderingTjeneste.erTilVurdering(e.getKey()));
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
