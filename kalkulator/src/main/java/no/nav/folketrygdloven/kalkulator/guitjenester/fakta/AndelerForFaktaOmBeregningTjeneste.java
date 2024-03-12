package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.folketrygdloven.kalkulator.felles.FinnInntektsmeldingForAndel.finnInntektsmelding;
import static no.nav.folketrygdloven.kalkulator.guitjenester.fakta.FinnInntektForVisning.finnInntektForKunLese;
import static no.nav.folketrygdloven.kalkulator.guitjenester.fakta.FinnInntektForVisning.finnInntektForPreutfylling;
import static no.nav.folketrygdloven.kalkulator.guitjenester.fakta.SkalKunneEndreAktivitet.skalKunneEndreAktivitet;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde.PROSESS_START;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde.SAKSBEHANDLER_KOFAKBER;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.AndelForFaktaOmBeregningDto;

public class AndelerForFaktaOmBeregningTjeneste {

    private AndelerForFaktaOmBeregningTjeneste() {
        // Skjul
    }

    public static List<AndelForFaktaOmBeregningDto> lagAndelerForFaktaOmBeregning(BeregningsgrunnlagGUIInput input) {
        return input.getBeregningsgrunnlagGrunnlag()
                .getBeregningsgrunnlagHvisFinnes()
                .map(BeregningsgrunnlagDto::getBeregningsgrunnlagPerioder)
                .filter(c -> !c.isEmpty())
                .map(b -> b.get(0))
                .map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList)
                .orElseThrow()
                .stream()
                .filter(a -> a.getKilde().equals(PROSESS_START) || a.getKilde().equals(SAKSBEHANDLER_KOFAKBER))
                .map(andel -> mapTilAndelIFaktaOmBeregning(input, andel))
                .collect(Collectors.toList());
    }

    private static AndelForFaktaOmBeregningDto mapTilAndelIFaktaOmBeregning(BeregningsgrunnlagGUIInput input,
            BeregningsgrunnlagPrStatusOgAndelDto andel) {
        var ref = input.getKoblingReferanse();
        var inntektsmeldinger = input.getInntektsmeldinger();
        var inntektsmeldingForAndel = finnInntektsmelding(andel, inntektsmeldinger);
        var dto = new AndelForFaktaOmBeregningDto();
        dto.setFastsattBelop(ModellTyperMapper.beløpTilDto(finnInntektForPreutfylling(andel)));
        dto.setInntektskategori(andel.getGjeldendeInntektskategori());
        dto.setAndelsnr(andel.getAndelsnr());
        dto.setAktivitetStatus(andel.getAktivitetStatus());
        var inntektArbeidYtelseGrunnlag = input.getIayGrunnlag();
        dto.setSkalKunneEndreAktivitet(skalKunneEndreAktivitet(andel, input.getBeregningsgrunnlag().isOverstyrt()));
        dto.setLagtTilAvSaksbehandler(andel.erLagtTilAvSaksbehandler());
        BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, inntektsmeldingForAndel, inntektArbeidYtelseGrunnlag)
                .ifPresent(dto::setArbeidsforhold);
        finnRefusjonskravFraInntektsmelding(inntektsmeldingForAndel).map(ModellTyperMapper::beløpTilDto).ifPresent(dto::setRefusjonskrav);
        finnInntektForKunLese(ref, andel, inntektsmeldingForAndel, inntektArbeidYtelseGrunnlag,
                input.getBeregningsgrunnlag().getFaktaOmBeregningTilfeller(),
                input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPrStatusOgAndelList())
                .map(ModellTyperMapper::beløpTilDto).ifPresent(dto::setBelopReadOnly);
        return dto;
    }

    private static Optional<Beløp> finnRefusjonskravFraInntektsmelding(Optional<InntektsmeldingDto> inntektsmeldingForAndel) {
        return inntektsmeldingForAndel
                .map(InntektsmeldingDto::getRefusjonBeløpPerMnd);
    }
}
