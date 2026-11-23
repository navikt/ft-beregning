package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.OverstyrRefusjonDto;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;

public class RefusjonOverstyringHåndterer {

    private RefusjonOverstyringHåndterer() {
        // Skjuler default konstruktør
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(HåndterBeregningsgrunnlagInput input, OverstyrRefusjonDto refusjonDto) {
	    var grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
	    var forrigeBg = input.getForrigeGrunnlagFraHåndteringTilstand().flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlagHvisFinnes);
        //TODO lagre den overstyrte refusjonen som et nytt fordelingsgrunnlag og oppdatere beregningsgrunnlaget med nye refusjonsperioder ?
        //FaktaOmBeregningTilfellerOppdaterer.oppdater(refusjonDto, forrigeBg, input, grunnlagBuilder);
        return grunnlagBuilder.build(input.getHåndteringTilstand());
    }
}
