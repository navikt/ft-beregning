package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;

public class RefusjonskravFrist {

    private int antallMånederRefusjonskravFrist = 3;

    private BeregningsgrunnlagHjemmel anvendtHjemmel = BeregningsgrunnlagHjemmel.F_22_13_6;

    public RefusjonskravFrist() {
    }

    public RefusjonskravFrist(int antallMånederRefusjonskravFrist, BeregningsgrunnlagHjemmel anvendtHjemmel) {
        this.antallMånederRefusjonskravFrist = antallMånederRefusjonskravFrist;
        this.anvendtHjemmel = anvendtHjemmel;
    }

    public int getAntallMånederRefusjonskravFrist() {
        return antallMånederRefusjonskravFrist;
    }

    public BeregningsgrunnlagHjemmel getAnvendtHjemmel() {
        return anvendtHjemmel;
    }
}
