package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.psb;

import java.time.LocalDate;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.YtelsesSpesifiktGrunnlag;

public class PleiepengerGrunnlagFastsettGrenseverdi extends YtelsesSpesifiktGrunnlag {

	private LocalDate startdatoNyeGraderingsregler;

	public PleiepengerGrunnlagFastsettGrenseverdi(String ytelsetype) {
		super(ytelsetype);
	}

	public static PleiepengerGrunnlagFastsettGrenseverdi forSyktBarn() {
		return new PleiepengerGrunnlagFastsettGrenseverdi("PSB");
	}

	public static PleiepengerGrunnlagFastsettGrenseverdi forNærstående() {
		return new PleiepengerGrunnlagFastsettGrenseverdi("PPN");
	}


	public LocalDate getStartdatoNyeGraderingsregler() {
		return startdatoNyeGraderingsregler;
	}

	public void setStartdatoNyeGraderingsregler(LocalDate startdatoNyeGraderingsregler) {
		this.startdatoNyeGraderingsregler = startdatoNyeGraderingsregler;
	}
}
