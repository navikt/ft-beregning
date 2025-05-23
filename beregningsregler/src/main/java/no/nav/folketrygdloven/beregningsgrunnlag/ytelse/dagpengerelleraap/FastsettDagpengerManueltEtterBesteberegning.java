package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.dagpengerelleraap;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettDagpengerManueltEtterBesteberegning.ID)
class FastsettDagpengerManueltEtterBesteberegning extends LeafSpecification<BeregningsgrunnlagPeriode> {

	static final String ID = "FP_BR_10.1";
	static final String BESKRIVELSE = "Foreslå beregningsgrunnlag for Dagpenger";

	FastsettDagpengerManueltEtterBesteberegning() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		var dpStatus = grunnlag.getBeregningsgrunnlagFraDagpenger();
		if (dpStatus.isPresent()) {
            var resultater = settBesteberegningForStatus(grunnlag, dpStatus.get());
			return beregnet(resultater);

		}
		throw new IllegalStateException("Forventet å ha en dagpengeandel");
	}

	private Map<String, Object> settBesteberegningForStatus(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagPrStatus dpStatus) {
        var hjemmel = BeregningsgrunnlagHjemmel.F_14_7;
		// TODO (PFP-8687): Migrere vekk BeregningsgrunnlagPrStatus#besteberegningPrÅr
        var beregnetPrÅr = dpStatus.getBesteberegningPrÅr() != null ? dpStatus.getBesteberegningPrÅr() : dpStatus.getBeregnetPrÅr();

        var dagsats = grunnlag.getInntektsgrunnlag().getPeriodeinntekt(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP, grunnlag.getSkjæringstidspunkt())
				.map(Periodeinntekt::getInntekt)
				.orElse(BigDecimal.ZERO);

		BeregningsgrunnlagPrStatus.builder(dpStatus)
				.medBeregnetPrÅr(beregnetPrÅr)
				.medÅrsbeløpFraTilstøtendeYtelse(beregnetPrÅr)
				.medOrginalDagsatsFraTilstøtendeYtelse(dagsats.longValue())
				.build();
		grunnlag.getBeregningsgrunnlag().getAktivitetStatus(dpStatus.getAktivitetStatus()).setHjemmel(hjemmel);

		Map<String, Object> resultater = new HashMap<>();
		resultater.put("bruttoPrÅr." + dpStatus.getAktivitetStatus().name(), beregnetPrÅr);
		resultater.put("tilstøtendeYtelserPrÅr." + dpStatus.getAktivitetStatus().name(), beregnetPrÅr);
		resultater.put("hjemmel", hjemmel);
		return resultater;
	}
}
