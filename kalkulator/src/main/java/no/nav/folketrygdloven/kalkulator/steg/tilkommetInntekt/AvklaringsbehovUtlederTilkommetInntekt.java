package no.nav.folketrygdloven.kalkulator.steg.tilkommetInntekt;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.AvklaringsbehovUtlederNyttInntektsforhold;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

public class AvklaringsbehovUtlederTilkommetInntekt {

    private AvklaringsbehovUtlederTilkommetInntekt() {
        // Skjul
    }


    public static List<BeregningAvklaringsbehovResultat> utledAvklaringsbehovFor(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                                 YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                                 List<Intervall> forlengelseperioder) {
        var skalVurdereNyttInntektsforhold = AvklaringsbehovUtlederNyttInntektsforhold.skalVurdereNyttInntektsforhold(
                beregningsgrunnlagGrunnlag,
                ytelsespesifiktGrunnlag,
                forlengelseperioder);
        var utledetbehovForAvklaring = new ArrayList<BeregningAvklaringsbehovResultat>();
        if (skalVurdereNyttInntektsforhold) {
            utledetbehovForAvklaring.add(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.VURDER_NYTT_INNTKTSFRHLD));
        }
        return utledetbehovForAvklaring;
    }

}
