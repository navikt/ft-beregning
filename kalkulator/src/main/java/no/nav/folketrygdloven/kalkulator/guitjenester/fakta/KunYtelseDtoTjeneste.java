package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.RoundingMode;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.AndelMedBeløpDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.KunYtelseDto;

public class KunYtelseDtoTjeneste {

    KunYtelseDtoTjeneste() {
        // For CDI
    }


    public void lagDto(BeregningsgrunnlagGUIInput input,
                       FaktaOmBeregningDto faktaOmBeregningDto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        if (beregningsgrunnlag.getFaktaOmBeregningTilfeller().contains(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE)) {
            faktaOmBeregningDto.setKunYtelse(lagKunYtelseDto(input));
        }
    }

    KunYtelseDto lagKunYtelseDto(BeregningsgrunnlagGUIInput input) {
        var dto = new KunYtelseDto();
	    harBesteberegning(input.getBeregningsgrunnlag(), input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagTilstand()).ifPresent(dto::setErBesteberegning);
        settVerdier(dto, input.getBeregningsgrunnlag(), input.getIayGrunnlag());
        if (input.getYtelsespesifiktGrunnlag() instanceof ForeldrepengerGrunnlag foreldrepengerGrunnlag) {
            dto.setFodendeKvinneMedDP(foreldrepengerGrunnlag.isKvalifisererTilBesteberegning());
        }
        return dto;
    }

    private Optional<Boolean> harBesteberegning(BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagTilstand aktivTilstand) {
        if (aktivTilstand.erFør(BeregningsgrunnlagTilstand.KOFAKBER_UT)) {
            return Optional.empty();
        }
	    var erBesteberegnet = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
			    .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()).anyMatch(andel -> andel.getBesteberegningPrÅr() != null);
	    return Optional.of(erBesteberegnet);
    }

    private void settVerdier(KunYtelseDto dto, BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        var periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(andel -> andel.getKilde().equals(AndelKilde.PROSESS_START) || andel.getKilde().equals(AndelKilde.SAKSBEHANDLER_KOFAKBER))
                .forEach(andel -> {
                    var brukersAndel = initialiserStandardAndelProperties(andel, inntektArbeidYtelseGrunnlag);
                    brukersAndel.setFastsattBelopPrMnd(ModellTyperMapper.beløpTilDto(finnFastsattMånedsbeløp(andel)));
                    dto.leggTilAndel(brukersAndel);
                });
    }

    private Beløp finnFastsattMånedsbeløp(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return Optional.ofNullable(andel.getBeregnetPrÅr())
                .map(b -> b.map(v -> v.divide(KonfigTjeneste.getMånederIÅr(), RoundingMode.HALF_UP)))
                .orElse(null);
    }

    private AndelMedBeløpDto initialiserStandardAndelProperties(BeregningsgrunnlagPrStatusOgAndelDto andel, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        var andelDto = new AndelMedBeløpDto();
        andelDto.setAndelsnr(andel.getAndelsnr());
        BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), inntektArbeidYtelseGrunnlag)
                .ifPresent(andelDto::setArbeidsforhold);
        andelDto.setLagtTilAvSaksbehandler(andel.erLagtTilAvSaksbehandler());
        andelDto.setKilde(andel.getKilde());
        andelDto.setFastsattAvSaksbehandler(Boolean.TRUE.equals(andel.getFastsattAvSaksbehandler()));
        andelDto.setAktivitetStatus(andel.getAktivitetStatus());
        andelDto.setInntektskategori(andel.getGjeldendeInntektskategori());
        return andelDto;
    }

}
