package no.nav.folketrygdloven.kalkulator.avklaringsbehov.refusjon;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

public class VurderRefusjonBeregningsgrunnlagHåndterer {

    private VurderRefusjonBeregningsgrunnlagHåndterer() {
        // skjuler public konstruktør
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(VurderRefusjonBeregningsgrunnlagDto dto, BeregningsgrunnlagInput input) {
        // Lag overstyringsobjekter
        BeregningRefusjonOverstyringerDto refusjonOverstyringer;
        BeregningsgrunnlagDto periodisertPåFastsattRefusjon;
        var beregningsgrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagHvisFinnes().orElseThrow();
        if (input.isEnabled("refusjonsfrist.flytting", false)) {
            refusjonOverstyringer = MapTilRefusjonOverstyringNy.map(dto, input);

            // Todo Utled nytt beregningsgrunnlag basert på refusjonsoverstyringene - work in progress
            //trenger vi å utlede på nytt om aksjonspunkt ikke har oppstått?
//             var beregningsgrunnlagJustertForVurdertRefusjonsfrist = PeriodiserOgFastsettRefusjonTjenesteNy.justerBeregnigsgrunnlagForVurdertRefusjonsfrist(input, dto.getAndeler(), refusjonOverstyringer);

            // Periodiser og fastsett refusjon på eksisterende beregningsgrunnlag basert på data fra overstyringsobjekter
            periodisertPåFastsattRefusjon = PeriodiserOgFastsettRefusjonTjenesteNy.periodiserOgFastsett(beregningsgrunnlag, dto.getAndeler());
        } else {
            refusjonOverstyringer = MapTilRefusjonOverstyring.map(dto, input);
            // Periodiser og fastsett refusjon på eksisterende beregningsgrunnlag basert på data fra overstyringsobjekter
            periodisertPåFastsattRefusjon = PeriodiserOgFastsettRefusjonTjeneste.periodiserOgFastsett(beregningsgrunnlag, dto.getAndeler());
        }
        // Lag nytt aggregat og sett korrekt tilstand
        var grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        grunnlagBuilder.medRefusjonOverstyring(refusjonOverstyringer);
        grunnlagBuilder.medBeregningsgrunnlag(periodisertPåFastsattRefusjon);
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.VURDERT_REFUSJON_UT);
    }
}
