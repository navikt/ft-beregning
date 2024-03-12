package no.nav.folketrygdloven.kalkulator.guitjenester.fakta.saksopplysninger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fakta.ArbeidsforholdDto;

class KortvarigArbeidsforholdTjeneste {

    static List<ArbeidsforholdDto> lagKortvarigeArbeidsforhold(BeregningsgrunnlagGUIInput input) {
        var tidsbegrensedeArbeidsforhold = input.getFaktaAggregat().stream()
                .flatMap(f -> f.getFaktaArbeidsforhold().stream())
                .filter(f -> f.getErTidsbegrensetVurdering() != null && f.getErTidsbegrensetVurdering());
        return tidsbegrensedeArbeidsforhold.map(a -> {
                    var matchendeAndel = finnMatchendeAndel(input, a);
                    return new ArbeidsforholdDto(matchendeAndel.map(BeregningsgrunnlagPrStatusOgAndelDto::getAndelsnr).orElse(null), a.getArbeidsgiver().getIdentifikator(), a.getArbeidsforholdRef().getReferanse());
                })
                .collect(Collectors.toList());
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnMatchendeAndel(BeregningsgrunnlagGUIInput input, FaktaArbeidsforholdDto a) {
        return input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(andel -> andel.getKilde().equals(AndelKilde.PROSESS_START))
                .filter(andel -> andel.getBgAndelArbeidsforhold().isPresent() && andel.getBgAndelArbeidsforhold().get().getArbeidsgiver().equals(a.getArbeidsgiver()) &&
                        andel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef().gjelderFor(a.getArbeidsforholdRef()))
                .findFirst();
    }

}
