package no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

@FunctionalInterface
public interface SkjæringstidspunktFastsetter {

    static SkjæringstidspunktFastsetter utledFastsettSkjæringstidspunktTjeneste(FagsakYtelseType ytelseType) {
        return switch (ytelseType) {
            case FORELDREPENGER, SVANGERSKAPSPENGER -> FastsettSkjæringstidspunktOgStatuserK14::fastsett;
            case OMSORGSPENGER, OPPLÆRINGSPENGER, PLEIEPENGER_SYKT_BARN, PLEIEPENGER_NÆRSTÅENDE ->
                    FastsettSkjæringstidspunktOgStatuserK9::fastsett;
            default -> throw new IllegalStateException("Fant ikke FastsettSkjæringstidspunktOgStatuser for ytelse " + ytelseType.getKode());
        };
    }

    BeregningsgrunnlagRegelResultat fastsettSkjæringstidspunktOgStatuser(StegProsesseringInput input,
                                                                         BeregningAktivitetAggregatDto beregningAktiviteter,
                                                                         List<Grunnbeløp> grunnbeløpSatser);

}
