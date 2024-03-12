package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import static no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt.FastsettBeregningsperiodeATFL.fastsettBeregningsperiodeForATFL;

import java.time.LocalDate;
import java.util.Collection;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class FastsettBeregningsperiodeTjenesteFRISINN {

    public static BeregningsgrunnlagDto fastsettBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag,
                                                           InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                           Collection<InntektsmeldingDto> inntektsmeldinger) {
        return fastsettBeregningsperiodeForATFL(beregningsgrunnlag, fastsettBeregningsperiodeForATFLAndeler(beregningsgrunnlag.getSkjæringstidspunkt()));
    }

    private static Intervall fastsettBeregningsperiodeForATFLAndeler(LocalDate skjæringstidspunkt) {
        return Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusYears(1).withDayOfMonth(1), skjæringstidspunkt.withDayOfMonth(1).minusDays(1));
    }


}
