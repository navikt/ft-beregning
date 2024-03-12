package no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.FastsettGrunnlagOmsorgspenger;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.OmsorgspengeGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.YtelsespesifiktGrunnlagDto;

public class YtelsespesifiktGrunnlagTjenesteOMP implements YtelsespesifiktGrunnlagTjeneste {

    @Override
    public Optional<YtelsespesifiktGrunnlagDto> map(BeregningsgrunnlagGUIInput input) {
        var beregningsgrunnlag = input.getBeregningsgrunnlagGrunnlag();
        var omsorgspengeGrunnlagDto = new OmsorgspengeGrunnlagDto();

        if (harForeslåttBeregning(beregningsgrunnlag) && erBrukerKunArbeidstaker(input)) {
            omsorgspengeGrunnlagDto.setSkalAvviksvurdere(!FastsettGrunnlagOmsorgspenger.finnesKunFullRefusjon(input));
        }

        return Optional.of(omsorgspengeGrunnlagDto);
    }

    private boolean harForeslåttBeregning(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagDto) {
        return !beregningsgrunnlagGrunnlagDto.getBeregningsgrunnlagTilstand().erFør(BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    private static boolean erBrukerKunArbeidstaker(BeregningsgrunnlagGUIInput input) {
        return input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .allMatch(a -> AktivitetStatus.ARBEIDSTAKER.equals(a.getAktivitetStatus()));
    }
}
