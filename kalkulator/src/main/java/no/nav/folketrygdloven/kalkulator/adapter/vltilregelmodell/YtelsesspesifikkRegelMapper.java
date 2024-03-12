package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;

public interface YtelsesspesifikkRegelMapper {

    YtelsesSpesifiktGrunnlag map(BeregningsgrunnlagDto beregningsgrunnlagDto, BeregningsgrunnlagInput input);

}
