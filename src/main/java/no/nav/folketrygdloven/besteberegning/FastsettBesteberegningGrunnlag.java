package no.nav.folketrygdloven.besteberegning;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.besteberegning.modell.BesteberegningRegelmodell;
import no.nav.folketrygdloven.besteberegning.modell.output.AktivitetNøkkel;
import no.nav.folketrygdloven.besteberegning.modell.output.BeregnetMånedsgrunnlag;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegnetAndel;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegnetGrunnlag;
import no.nav.folketrygdloven.besteberegning.modell.output.Inntekt;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettBesteberegningGrunnlag.ID)
class FastsettBesteberegningGrunnlag extends LeafSpecification<BesteberegningRegelmodell> {

	// Trenger dokumentasjon på confluence og referanse til denne
    static final String ID = "14-7-3.2";
    static final String BESKRIVELSE = "Fastsett grunnlag basert på de 6 måneder med høyest inntekt";
	private static final int ANTALL_MÅNEDER_I_BESTEBERGNING = 6;
	public static final int MÅNEDER_I_ÅRET = 12;

	FastsettBesteberegningGrunnlag() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BesteberegningRegelmodell regelmodell) {
        Map<String, Object> resultater = new HashMap<>();
	    BesteberegnetGrunnlag besteberegnetGrunnlag = lagBesteberegningGrunnlag(regelmodell.getOutput().getBesteMåneder());
	    regelmodell.getOutput().setBesteberegnetGrunnlag(besteberegnetGrunnlag);
	    resultater.put("BesteberegningGrunnlag", besteberegnetGrunnlag);
	    return beregnet(resultater);
    }

	private BesteberegnetGrunnlag lagBesteberegningGrunnlag(List<BeregnetMånedsgrunnlag> besteMåneder) {
		Map<AktivitetNøkkel, List<Inntekt>> nøkkelTilInntekter = lagAktivitetTilInntekterMap(besteMåneder);
		List<BesteberegnetAndel> andeler = nøkkelTilInntekter.entrySet().stream().map(entry -> new BesteberegnetAndel(entry.getKey(), finnSnittÅrsinntekt(entry.getValue())))
				.collect(Collectors.toList());
		return new BesteberegnetGrunnlag(andeler);
	}

	private Map<AktivitetNøkkel, List<Inntekt>> lagAktivitetTilInntekterMap(List<BeregnetMånedsgrunnlag> besteMåneder) {
		return besteMåneder.stream().flatMap(beregnetMånedsgrunnlag -> beregnetMånedsgrunnlag.getInntekter().stream())
				.collect(Collectors.groupingBy(Inntekt::getAktivitetNøkkel));
	}

	private BigDecimal finnSnittÅrsinntekt(List<Inntekt> inntekter) {
		BigDecimal sum = inntekter.stream().map(Inntekt::getInntektPrMåned)
				.reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
		return sum.divide(BigDecimal.valueOf(ANTALL_MÅNEDER_I_BESTEBERGNING), 10, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(MÅNEDER_I_ÅRET));

	}

}
