package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;


public class SkalKunneEndreAktivitet {

    private SkalKunneEndreAktivitet() {
        // Hide constructor
    }

    /**
     * Vurderer om ein gitt andel skal kunne endres i gui.
     *
     * Endring vil seie å kunne slette eller endre arbeidsforhold i nedtrekksmeny for andelen.
     *
     * @param andel Ein gitt beregningsgrunnlagsandel
     * @param erBeregningsgrunnlagOverstyrt er beregningsgrunnlaget overstyrt
     * @return boolean som seier om andel/aktivitet skal kunne endres i gui
     */
    public static Boolean skalKunneEndreAktivitet(BeregningsgrunnlagPrStatusOgAndelDto andel, boolean erBeregningsgrunnlagOverstyrt) {
        return andel.erLagtTilAvSaksbehandler() &&
                (!andel.getAktivitetStatus().equals(AktivitetStatus.DAGPENGER) ||
                        (erBeregningsgrunnlagOverstyrt && andel.getAktivitetStatus().equals(AktivitetStatus.DAGPENGER)));
    }
}
