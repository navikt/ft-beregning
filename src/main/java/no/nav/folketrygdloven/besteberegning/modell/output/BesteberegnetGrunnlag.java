package no.nav.folketrygdloven.besteberegning.modell.output;

import java.util.List;

public class BesteberegnetGrunnlag {

	private List<BesteberegnetAndel> besteberegnetAndelList;

	public BesteberegnetGrunnlag(List<BesteberegnetAndel> besteberegnetAndelList) {
		this.besteberegnetAndelList = besteberegnetAndelList;
	}

	public List<BesteberegnetAndel> getBesteberegnetAndelList() {
		return besteberegnetAndelList;
	}

	@Override
	public String toString() {
		return "BesteberegnetGrunnlag{" +
				"besteberegnetAndelList=" + besteberegnetAndelList +
				'}';
	}
}
