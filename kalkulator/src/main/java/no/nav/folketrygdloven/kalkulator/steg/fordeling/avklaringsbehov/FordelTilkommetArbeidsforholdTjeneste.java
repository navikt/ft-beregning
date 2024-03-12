package no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;

public final class FordelTilkommetArbeidsforholdTjeneste {

    private FordelTilkommetArbeidsforholdTjeneste() {
        // Skjuler default
    }

    public static boolean erAktivitetLagtTilIPeriodisering(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getKilde().equals(AndelKilde.PROSESS_PERIODISERING) || andel.getKilde().equals(AndelKilde.PROSESS_PERIODISERING_TILKOMMET_INNTEKT);
    }

}
