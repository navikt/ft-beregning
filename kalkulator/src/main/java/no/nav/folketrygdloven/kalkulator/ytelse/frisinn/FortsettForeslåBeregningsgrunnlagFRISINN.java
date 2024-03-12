package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.Collections;

import no.nav.folketrygdloven.kalkulator.input.FortsettForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;

/**
 * FRISINN trenger ikke kjøre dette steget. Hvis det mot formodning skjer, skal ingenting gjøres her.
 * Alt håndteres i foreslå-steget, da frisinn ikke følger samme regler som resten av ytelsene
 * og ikke trenger beregne enkelte statuser før andre.
 */
public class FortsettForeslåBeregningsgrunnlagFRISINN {

    public FortsettForeslåBeregningsgrunnlagFRISINN() {
        super();
    }

    public BeregningsgrunnlagRegelResultat fortsettForeslåBeregningsgrunnlag(FortsettForeslåBeregningsgrunnlagInput input) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagHvisFinnes()
                .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her"));
        return new BeregningsgrunnlagRegelResultat(beregningsgrunnlag, Collections.emptyList());
    }
}
