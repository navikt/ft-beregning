package no.nav.folketrygdloven.kalkulator.felles;

import java.util.Collection;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;

public class FinnInntektsmeldingForAndel {

    private FinnInntektsmeldingForAndel() {
    }

    public static Optional<InntektsmeldingDto> finnInntektsmelding(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                   Collection<InntektsmeldingDto> inntektsmeldinger) {
        return inntektsmeldinger.stream()
            .filter(im -> andel.gjelderInntektsmeldingFor(im.getArbeidsgiver(), im.getArbeidsforholdRef()))
            .findFirst();
    }

}
