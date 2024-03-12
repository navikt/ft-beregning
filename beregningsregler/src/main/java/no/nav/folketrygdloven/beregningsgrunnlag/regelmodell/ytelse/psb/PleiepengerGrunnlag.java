package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.psb;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class PleiepengerGrunnlag extends YtelsesSpesifiktGrunnlag {

    public PleiepengerGrunnlag(String ytelsetype) {
        super(ytelsetype);
    }

	public static PleiepengerGrunnlag forSyktBarn() {
		return new PleiepengerGrunnlag("PSB");
	}

	public static PleiepengerGrunnlag forNærstående() {
		return new PleiepengerGrunnlag("PPN");
	}

}
