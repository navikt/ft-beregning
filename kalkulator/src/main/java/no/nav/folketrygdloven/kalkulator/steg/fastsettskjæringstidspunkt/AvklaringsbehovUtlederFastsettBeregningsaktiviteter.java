package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public interface AvklaringsbehovUtlederFastsettBeregningsaktiviteter {

    static AvklaringsbehovUtlederFastsettBeregningsaktiviteter utledTjeneste(FagsakYtelseType ytelseType) {
        return switch (ytelseType) {
            case FORELDREPENGER -> new AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP();
            case SVANGERSKAPSPENGER -> new AvklaringsbehovUtlederFastsettBeregningsaktiviteterSVP();
            case OMSORGSPENGER -> new AvklaringsbehovUtlederFastsettBeregningsaktiviteterOMP();
            case PLEIEPENGER_SYKT_BARN, PLEIEPENGER_NÆRSTÅENDE, OPPLÆRINGSPENGER -> new AvklaringsbehovUtlederFastsettBeregningsaktiviteterPleiepenger();
            default -> throw new IllegalStateException("Utviklerfeil: AvklaringsbehovUtlederFastsettBeregningsaktiviteter ikke implementert for ytelse " + ytelseType.getKode());
        };
    }

    List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagRegelResultat regelResultat,
                                                                BeregningsgrunnlagInput input,
                                                                boolean erOverstyrt);

}
