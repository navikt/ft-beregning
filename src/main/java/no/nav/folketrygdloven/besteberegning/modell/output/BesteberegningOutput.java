package no.nav.folketrygdloven.besteberegning.modell.output;

import java.util.ArrayList;
import java.util.List;

public class BesteberegningOutput {

	private List<BeregnetMånedsgrunnlag> besteMåneder = new ArrayList<>();
	private BesteberegnetGrunnlag besteberegnetGrunnlag;
	private Boolean skalBeregnesEtterSeksBesteMåneder;

	public List<BeregnetMånedsgrunnlag> getBesteMåneder() {
		return besteMåneder;
	}

	public void setBesteMåneder(List<BeregnetMånedsgrunnlag> besteMåneder) {
		this.besteMåneder = besteMåneder;
	}

	public BesteberegnetGrunnlag getBesteberegnetGrunnlag() {
		return besteberegnetGrunnlag;
	}

	public void setBesteberegnetGrunnlag(BesteberegnetGrunnlag besteberegnetGrunnlag) {
		this.besteberegnetGrunnlag = besteberegnetGrunnlag;
	}

	public Boolean getSkalBeregnesEtterSeksBesteMåneder() {
		return skalBeregnesEtterSeksBesteMåneder;
	}

	public void setSkalBeregnesEtterSeksBesteMåneder(Boolean skalBeregnesEtterSeksBesteMåneder) {
		this.skalBeregnesEtterSeksBesteMåneder = skalBeregnesEtterSeksBesteMåneder;
	}
}
