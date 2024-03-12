package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import static no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak.REPRESENTERER_STORTINGET;
import static no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak.REPRESENTERER_STORTINGET_AVSLUTTET;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.PeriodeSplitter;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.SplittPeriodeConfig;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.VurderRepresentererStortingetHåndteringDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class VurderRepresentererStortingetTjeneste {

    public static BeregningsgrunnlagGrunnlagDto løsAvklaringsbehov(VurderRepresentererStortingetHåndteringDto vurderDto, HåndterBeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        if (vurderDto.getRepresentererStortinget()) {
            var stortingsperiodeTidslinje = new LocalDateTimeline<>(List.of(new LocalDateSegment<>(vurderDto.getFom(), vurderDto.getTom(), true)));
            var nyttBg = getPeriodeSplitter(input).splittPerioder(input.getBeregningsgrunnlag(), stortingsperiodeTidslinje);
            grunnlagBuilder.medBeregningsgrunnlag(nyttBg);
        }
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.FASTSATT_INN);
    }

    private static PeriodeSplitter<Boolean> getPeriodeSplitter(HåndterBeregningsgrunnlagInput input) {
        SplittPeriodeConfig<Boolean> splittPeriodeConfig = SplittPeriodeConfig.medAvsluttetPeriodeårsakConfig(REPRESENTERER_STORTINGET, REPRESENTERER_STORTINGET_AVSLUTTET, input.getForlengelseperioder());
        return new PeriodeSplitter<>(splittPeriodeConfig);
    }

}
