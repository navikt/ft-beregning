package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett;

import java.math.BigDecimal;

import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class PleiepengerSyktBarnGrunnlag extends YtelsesSpesifiktGrunnlag {

	private final LocalDateTimeline<BigDecimal> tilsynsgraderingsprosent;

	public PleiepengerSyktBarnGrunnlag(LocalDateTimeline<BigDecimal> tilsynsgraderingsprosent) {
		super("PSB");
		this.tilsynsgraderingsprosent = tilsynsgraderingsprosent;
	}

	public LocalDateTimeline<BigDecimal> getTilsynsgraderingsprosent() {
		if (tilsynsgraderingsprosent == null) {
			return LocalDateTimeline.empty();
		}
		return tilsynsgraderingsprosent;
	}
}
