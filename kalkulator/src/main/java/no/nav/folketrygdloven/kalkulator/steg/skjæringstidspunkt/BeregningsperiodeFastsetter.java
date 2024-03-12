package no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt;

import java.util.Collection;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

/*
 * Denne ligger på
 */
@FunctionalInterface
public interface BeregningsperiodeFastsetter {

    static BeregningsperiodeFastsetter utledFastsettBeregningsperiodeTjeneste(FagsakYtelseType ytelseType) {
        return switch (ytelseType) {
            case FORELDREPENGER -> FastsettBeregningsperiodeTjenesteFP::fastsettBeregningsperiode;
            case OMSORGSPENGER, OPPLÆRINGSPENGER, PLEIEPENGER_SYKT_BARN, PLEIEPENGER_NÆRSTÅENDE, SVANGERSKAPSPENGER ->
                    FastsettBeregningsperiodeTjeneste::fastsettBeregningsperiode;
            default -> throw new IllegalStateException("Fant ikke FastsettSkjæringstidspunktOgStatuser for ytelse " + ytelseType.getKode());
        };
    }

    BeregningsgrunnlagDto fastsettBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag,
                                                    InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                    Collection<InntektsmeldingDto> inntektsmeldinger);

}
