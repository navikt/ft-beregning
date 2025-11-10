package no.nav.folketrygdloven.kalkulator.avklaringsbehov.refusjon;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonAndelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

import java.util.List;

public class VurderRefusjonBeregningsgrunnlagHåndterer {

    private VurderRefusjonBeregningsgrunnlagHåndterer() {
        // skjuler public konstruktør
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(VurderRefusjonBeregningsgrunnlagDto dto, BeregningsgrunnlagInput input) {
        BeregningRefusjonOverstyringerDto refusjonOverstyringer;
        BeregningsgrunnlagDto oppdatertGrunnlag;
        var beregningsgrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagHvisFinnes().orElseThrow();
        if (input.isEnabled("refusjonsfrist.flytting", false)) {
            refusjonOverstyringer = MapTilRefusjonOverstyringNy.map(dto, input.getSkjæringstidspunktForBeregning());
            var refusjonsfristAndeler = getAndelerMedVurdertRefusjonsfrist(dto);
            var justertGrunnlag = refusjonsfristAndeler.isEmpty() ? beregningsgrunnlag : VurderRefusjonUtfallTjeneste.justerBeregningsgrunnlagForVurdertRefusjonsfrist(
                beregningsgrunnlag, refusjonsfristAndeler);

            var overlappAndeler = getAndelerMedVurdertOverlapp(dto);
            oppdatertGrunnlag = overlappAndeler.isEmpty() ? justertGrunnlag : PeriodiserOgFastsettRefusjonTjeneste.periodiserOgFastsett(
                justertGrunnlag, overlappAndeler);
        } else {
            refusjonOverstyringer = MapTilRefusjonOverstyring.map(dto, input);
            // Periodiser og fastsett refusjon på eksisterende beregningsgrunnlag for vurderte refusjonandeler
            oppdatertGrunnlag = PeriodiserOgFastsettRefusjonTjeneste.periodiserOgFastsett(beregningsgrunnlag, dto.getAndeler());
        }
        var grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        grunnlagBuilder.medRefusjonOverstyring(refusjonOverstyringer);
        grunnlagBuilder.medBeregningsgrunnlag(oppdatertGrunnlag);
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.VURDERT_REFUSJON_UT);
    }

    private static List<VurderRefusjonAndelBeregningsgrunnlagDto> getAndelerMedVurdertOverlapp(VurderRefusjonBeregningsgrunnlagDto dto) {
        return dto.getAndeler().stream().filter(andel -> andel.getFastsattRefusjonFom() != null).toList();
    }

    private static List<VurderRefusjonAndelBeregningsgrunnlagDto> getAndelerMedVurdertRefusjonsfrist(VurderRefusjonBeregningsgrunnlagDto dto) {
        return dto.getAndeler().stream().filter(andel -> andel.getErFristUtvidet().isPresent()).toList();
    }
}
