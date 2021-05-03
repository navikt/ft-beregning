package no.nav.folketrygdloven.besteberegning.modell.input;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.RelatertYtelseType;

import java.util.ArrayList;
import java.util.List;

public class Ytelsegrunnlag {
	private RelatertYtelseType ytelse;
	private List<YtelsegrunnlagPeriode> perioder = new ArrayList<>();

	public Ytelsegrunnlag(RelatertYtelseType ytelse, List<YtelsegrunnlagPeriode> perioder) {
		this.ytelse = ytelse;
		this.perioder = perioder;
	}

	public RelatertYtelseType getYtelse() {
		return ytelse;
	}

	public List<YtelsegrunnlagPeriode> getPerioder() {
		return perioder;
	}

	@Override
	public String toString() {
		return "Ytelsegrunnlag{" +
				"ytelse=" + ytelse +
				", perioder=" + perioder +
				'}';
	}
}
