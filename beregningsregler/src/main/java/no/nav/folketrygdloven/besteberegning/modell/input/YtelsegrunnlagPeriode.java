package no.nav.folketrygdloven.besteberegning.modell.input;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;

public class YtelsegrunnlagPeriode {
	private Periode periode;
	private List<YtelsegrunnlagAndel> andeler;

	public YtelsegrunnlagPeriode(Periode periode, List<YtelsegrunnlagAndel> andeler) {
		this.periode = periode;
		this.andeler = andeler;
	}

	public Periode getPeriode() {
		return periode;
	}

	public List<YtelsegrunnlagAndel> getAndeler() {
		return andeler;
	}

	@Override
	public String toString() {
		return "YtelsegrunnlagPeriode{" +
				"periode=" + periode +
				", andeler=" + andeler +
				'}';
	}
}
