package no.nav.folketrygdloven.besteberegning.modell.output;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ForeslåttBesteberegning {

	private List<BeregnetMånedsgrunnlag> besteMåneder = new ArrayList<>();
	private BigDecimal gjennomsnittligPGI;

	private BesteberegnetGrunnlag besteberegnetGrunnlag;

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

	public BigDecimal getGjennomsnittligPGI() {
		return gjennomsnittligPGI;
	}

	public void setGjennomsnittligPGI(BigDecimal gjennomsnittligPGI) {
		this.gjennomsnittligPGI = gjennomsnittligPGI;
	}
}
