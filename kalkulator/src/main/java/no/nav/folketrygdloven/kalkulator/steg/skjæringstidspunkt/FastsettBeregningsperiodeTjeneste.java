package no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt;

import static no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt.FastsettBeregningsperiodeATFL.fastsettBeregningsperiodeForATFL;
import static no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt.FastsettBeregningsperiodeForLønnsendring.fastsettBeregningsperiodeForLønnsendring;

import java.util.Collection;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;

public class FastsettBeregningsperiodeTjeneste {

    public static BeregningsgrunnlagDto fastsettBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag,
                                                           InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                           Collection<InntektsmeldingDto> inntektsmeldinger) {
        // Fastsetter først for alle ATFL-andeler
        var fastsattForATFL = fastsettBeregningsperiodeForATFL(beregningsgrunnlag, new BeregningsperiodeTjeneste().fastsettBeregningsperiodeForATFLAndeler(beregningsgrunnlag.getSkjæringstidspunkt()));
        // Fastsetter for arbeidsforhold med lønnsendring innenfor siste 3 måneder før skjæringstidspunktet
        if (KonfigurasjonVerdi.instance().get("AUTOMATISK_BEREGNE_LONNSENDRING", false) || KonfigurasjonVerdi.instance().get("AUTOMATISK_BEREGNE_LONNSENDRING_V2", false)) {
            var fastsattForLønnsendring = fastsettBeregningsperiodeForLønnsendring(fastsattForATFL, inntektArbeidYtelseGrunnlag, inntektsmeldinger);
            return fastsattForLønnsendring;

        } else {
            return fastsattForATFL;
        }
    }


}
