package no.nav.folketrygdloven.kalkulator.steg.fullføre;

import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.FullføreBeregningsgrunnlagFPImpl;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.FullføreBeregningsgrunnlagUtbgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public class FullføreBeregningsgrunnlagTjenesteVelger {

    public static FullføreBeregningsgrunnlag utledTjeneste(FagsakYtelseType ytelseType) {
        return switch (ytelseType) {
            case FORELDREPENGER -> new FullføreBeregningsgrunnlagFPImpl();
            case SVANGERSKAPSPENGER, OMSORGSPENGER, PLEIEPENGER_SYKT_BARN, PLEIEPENGER_NÆRSTÅENDE, OPPLÆRINGSPENGER -> new FullføreBeregningsgrunnlagUtbgrad();
            default -> throw new IllegalStateException("Utviklerfeil: AvklaringsbehovUtlederFastsettBeregningsaktiviteter ikke implementert for ytelse " + ytelseType.getKode());
        };
    }
}
