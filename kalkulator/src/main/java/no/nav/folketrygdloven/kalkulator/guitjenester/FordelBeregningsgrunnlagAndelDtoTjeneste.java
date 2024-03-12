package no.nav.folketrygdloven.kalkulator.guitjenester;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.felles.FinnInntektsmeldingForAndel;
import no.nav.folketrygdloven.kalkulator.felles.MatchBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.RefusjonDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelTilkommetArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagAndelDto;

class FordelBeregningsgrunnlagAndelDtoTjeneste {

    private FordelBeregningsgrunnlagAndelDtoTjeneste() {
        // Skjul
    }

    static List<FordelBeregningsgrunnlagAndelDto> lagEndretBgAndelListe(BeregningsgrunnlagGUIInput input,
                                                                        BeregningsgrunnlagPeriodeDto periode) {
        List<FordelBeregningsgrunnlagAndelDto> endringAndeler = new ArrayList<>();
        for (var andel : periode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            var inntektsmelding = FinnInntektsmeldingForAndel.finnInntektsmelding(andel, input.getInntektsmeldinger());
            FordelBeregningsgrunnlagAndelDto endringAndel = lagEndretBGAndel(input, andel, inntektsmelding, periode);
            RefusjonDtoTjeneste.settRefusjonskrav(andel, endringAndel);
            endringAndel.setNyttArbeidsforhold(FordelTilkommetArbeidsforholdTjeneste.erAktivitetLagtTilIPeriodisering(andel));
            endringAndel.setArbeidsforholdType(andel.getArbeidsforholdType());
            endringAndeler.add(endringAndel);
        }
        return endringAndeler;
    }

    private static FordelBeregningsgrunnlagAndelDto lagEndretBGAndel(BeregningsgrunnlagGUIInput input,
                                                                     BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                     Optional<InntektsmeldingDto> inntektsmelding,
                                                                     BeregningsgrunnlagPeriodeDto periode) {
        FordelBeregningsgrunnlagAndelDto endringAndel = new FordelBeregningsgrunnlagAndelDto(BeregningsgrunnlagDtoUtil.lagFaktaOmBeregningAndel(
                andel,
                input.getYtelsespesifiktGrunnlag(),
                input.getIayGrunnlag(),
                periode
        ));
        settFordelingForrigeBehandling(input, andel, endringAndel);
        var fordelt = andel.getManueltFordeltPrÅr() == null ? andel.getFordeltPrÅr() : andel.getManueltFordeltPrÅr();
        endringAndel.setFordeltPrAar(ModellTyperMapper.beløpTilDto(fordelt));
        inntektsmelding.map(InntektsmeldingDto::getInntektBeløp)
                .map(b -> b.multipliser(KonfigTjeneste.getMånederIÅr()).map(v -> v.setScale(0, RoundingMode.HALF_UP)))
                .map(ModellTyperMapper::beløpTilDto)
                .ifPresent(endringAndel::setBelopFraInntektsmeldingPrÅr);
        return endringAndel;
    }

    private static void settFordelingForrigeBehandling(BeregningsgrunnlagGUIInput input,
                                                       BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                       FordelBeregningsgrunnlagAndelDto endringAndel) {
        if (andel.erLagtTilAvSaksbehandler()) {
            endringAndel.setFordelingForrigeBehandlingPrÅr(null);
            return;
        }
        Optional<BeregningsgrunnlagDto> bgForrigeBehandling = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling()
                .stream().findFirst()
                .flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlagHvisFinnes);
        if (bgForrigeBehandling.isEmpty()) {
            return;
        }
        BeregningsgrunnlagPeriodeDto periodeIOriginaltGrunnlag = MatchBeregningsgrunnlagTjeneste.finnPeriodeIBeregningsgrunnlag(andel.getBeregningsgrunnlagPeriode(), bgForrigeBehandling.get());
        var fastsattForrigeBehandling = periodeIOriginaltGrunnlag.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.matchUtenInntektskategori(andel.getAktivitetStatus(),
                        andel.getArbeidsgiver().orElse(null),
                        andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef).orElse(InternArbeidsforholdRefDto.nullRef()),
                        andel.getArbeidsforholdType()))
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getFordeltPrÅr)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
        endringAndel.setFordelingForrigeBehandlingPrÅr(ModellTyperMapper.beløpTilDto(fastsattForrigeBehandling));
    }
}
