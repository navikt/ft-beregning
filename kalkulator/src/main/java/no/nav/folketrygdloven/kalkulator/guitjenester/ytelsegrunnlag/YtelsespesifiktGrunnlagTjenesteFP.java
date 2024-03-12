package no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningMånedGrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningVurderingGrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.Inntekt;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.YtelsespesifiktGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fp.BesteberegningInntektDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fp.BesteberegningMånedGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fp.BesteberegninggrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fp.ForeldrepengerGrunnlagDto;

public class YtelsespesifiktGrunnlagTjenesteFP implements YtelsespesifiktGrunnlagTjeneste {

    @Override
    public Optional<YtelsespesifiktGrunnlagDto> map(BeregningsgrunnlagGUIInput input) {
        ForeldrepengerGrunnlag foreldrepengerGrunnlag = input.getYtelsespesifiktGrunnlag();
        ForeldrepengerGrunnlagDto foreldrepengerGrunnlagDto = new ForeldrepengerGrunnlagDto();
        foreldrepengerGrunnlagDto.setBesteberegninggrunnlag(mapBesteberenginggrunnlag(foreldrepengerGrunnlag.getBesteberegningVurderingGrunnlag()));
        return Optional.of(foreldrepengerGrunnlagDto);
    }

    private BesteberegninggrunnlagDto mapBesteberenginggrunnlag(BesteberegningVurderingGrunnlag besteberegningVurderingGrunnlag) {
        if (besteberegningVurderingGrunnlag == null) {
            return null;
        }
        List<BesteberegningMånedGrunnlagDto> besteMåneder = besteberegningVurderingGrunnlag.getSeksBesteMåneder().stream()
                .map(YtelsespesifiktGrunnlagTjenesteFP::mapMånedsgrunnlag)
                .collect(Collectors.toList());
        return new BesteberegninggrunnlagDto(besteMåneder, ModellTyperMapper.beløpTilDto(besteberegningVurderingGrunnlag.getAvvikFraFørsteLedd()));
    }

    private static BesteberegningMånedGrunnlagDto mapMånedsgrunnlag(BesteberegningMånedGrunnlag besteberegningMånedGrunnlag) {
        List<BesteberegningInntektDto> inntekter = besteberegningMånedGrunnlag.getInntekter().stream().map(YtelsespesifiktGrunnlagTjenesteFP::mapBesteberegningInntekt).collect(Collectors.toList());
        YearMonth måned = besteberegningMånedGrunnlag.getMåned();
        return new BesteberegningMånedGrunnlagDto(inntekter, måned.atDay(1), måned.atEndOfMonth());
    }

    private static BesteberegningInntektDto mapBesteberegningInntekt(Inntekt inntekt) {
        if (inntekt.getArbeidsgiver() != null) {
            return new BesteberegningInntektDto(inntekt.getArbeidsgiver().getIdentifikator(), inntekt.getArbeidsgiver().getIdentifikator(), inntekt.getArbeidsforholdRef().getReferanse(), ModellTyperMapper.beløpTilDto(inntekt.getInntekt()));
        }
        return new BesteberegningInntektDto(inntekt.getOpptjeningAktivitetType(), ModellTyperMapper.beløpTilDto(inntekt.getInntekt()));
    }
}
