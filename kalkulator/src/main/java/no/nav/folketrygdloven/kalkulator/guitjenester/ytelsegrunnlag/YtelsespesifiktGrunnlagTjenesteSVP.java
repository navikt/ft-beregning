package no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.SvangerskapspengerGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.YtelsespesifiktGrunnlagDto;

public class YtelsespesifiktGrunnlagTjenesteSVP implements YtelsespesifiktGrunnlagTjeneste {

    @Override
    public Optional<YtelsespesifiktGrunnlagDto> map(BeregningsgrunnlagGUIInput input) {
        return Optional.of(new SvangerskapspengerGrunnlagDto());
    }
}
