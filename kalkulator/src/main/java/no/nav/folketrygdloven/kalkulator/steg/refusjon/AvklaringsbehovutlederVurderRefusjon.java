package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.frist.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
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

        if (revurdererRefusjonsperiodeSomTidligereVarVurdert(vurderInput, periodisertMedRefusjonOgGradering)) {
            return true;
        }

        return harAndelerMedØktRefusjonIUtbetaltPeriode(input, periodisertMedRefusjonOgGradering, forrigeGrunnlagListe);
    }

    private static boolean revurdererRefusjonsperiodeSomTidligereVarVurdert(VurderRefusjonBeregningsgrunnlagInput vurderInput,
                                                                           BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        var forlengelseperioder = vurderInput.getForlengelseperioder();
        if (forlengelseperioder.isEmpty()) {
            return false;
        }
        boolean koblingenVarTidligereVurdertForRefusjon = vurderInput.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().stream()
            .map(BeregningsgrunnlagGrunnlagDto::getRefusjonOverstyringer)
            .flatMap(Optional::stream)
            .anyMatch(overstyringer -> !overstyringer.getRefusjonOverstyringer().isEmpty());
        if (!koblingenVarTidligereVurdertForRefusjon) {
            return false;
        }
        var refusjonsperiodeFraSkjæringstidspunkt = Intervall.fraOgMed(periodisertMedRefusjonOgGradering.getSkjæringstidspunkt());
        return forlengelseperioder.stream().anyMatch(refusjonsperiodeFraSkjæringstidspunkt::overlapper);
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
        return !InntektsmeldingMedRefusjonTjeneste.finnArbeidsgivereSomHarSøktRefusjonForSent(vurderInput.getIayGrunnlag(),
            vurderInput.getBeregningsgrunnlagGrunnlag(), vurderInput.getKravPrArbeidsgiver(), vurderInput.getFagsakYtelseType(),
            vurderInput.getFørsteUttaksdato()).isEmpty();
    }
}
