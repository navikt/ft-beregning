package no.nav.folketrygdloven.kalkulator.avklaringsbehov.refusjon;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

public class VurderRefusjonBeregningsgrunnlagHåndterer {

    private VurderRefusjonBeregningsgrunnlagHåndterer() {
        // skjuler public konstruktør
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(VurderRefusjonBeregningsgrunnlagDto dto, BeregningsgrunnlagInput input) {

        // Lag overstyringsobjekter
        var refusjonOverstyringer = MapTilRefusjonOverstyring.map(dto, input);

        // Periodiser og fastsett refusjon på eksisterende beregningsgrunnlag basert på data fra overstyringsobjekter
        var beregningsgrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagHvisFinnes().orElseThrow();
        var periodisertPåFastsattRefusjon = PeriodiserOgFastsettRefusjonTjeneste.periodiserOgFastsett(beregningsgrunnlag, dto.getAndeler());

        // Lag nytt aggregat og sett korrekt tilstand
        var grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        grunnlagBuilder.medRefusjonOverstyring(refusjonOverstyringer);
        grunnlagBuilder.medBeregningsgrunnlag(periodisertPåFastsattRefusjon);
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.VURDERT_REFUSJON_UT);
    }

}
