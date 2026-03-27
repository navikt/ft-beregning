package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.dagpengerelleraap;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ForeslåBeregningsgrunnlagDPellerAAP.ID)
class ForeslåBeregningsgrunnlagDPellerAAP extends LeafSpecification<BeregningsgrunnlagPeriode> {

	static final String ID = "FP_BR_10.1";
	static final String BESKRIVELSE = "Foreslå beregningsgrunnlag for Dagpenger/AAP";

	ForeslåBeregningsgrunnlagDPellerAAP() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var bgPerStatus = grunnlag.getBeregningsgrunnlagPrStatus().stream()
				.filter(bgps -> bgps.getAktivitetStatus().erAAPellerDP())
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Ingen aktivitetstatus av type DP eller AAP funnet."));

        var dagsatsOgBeregnetPrÅr = grunnlag.getInntektsgrunnlag().regnUtSnittInntektForDPellerAAP(bgPerStatus.getAktivitetStatus(), grunnlag.getSkjæringstidspunkt(), false);
        var dagsats = dagsatsOgBeregnetPrÅr.dagsats();
        var beregnetPrÅr = dagsatsOgBeregnetPrÅr.beregnetPrÅr();
		BeregningsgrunnlagPrStatus.builder(bgPerStatus)
				.medBeregnetPrÅr(beregnetPrÅr)
				.medÅrsbeløpFraTilstøtendeYtelse(beregnetPrÅr)
				.medOrginalDagsatsFraTilstøtendeYtelse(dagsats.setScale(0, RoundingMode.HALF_EVEN).longValue())
				.build();

        var hjemmel = AktivitetStatus.AAP.equals(bgPerStatus.getAktivitetStatus()) ? BeregningsgrunnlagHjemmel.F_14_7
				: BeregningsgrunnlagHjemmel.F_14_7_8_49;
		grunnlag.getBeregningsgrunnlag().getAktivitetStatus(bgPerStatus.getAktivitetStatus()).setHjemmel(hjemmel);

		Map<String, Object> resultater = new HashMap<>();
		resultater.put("beregnetPrÅr." + bgPerStatus.getAktivitetStatus().name(), beregnetPrÅr);
		resultater.put("tilstøtendeYtelserPrÅr." + bgPerStatus.getAktivitetStatus().name(), beregnetPrÅr);
		resultater.put("hjemmel", hjemmel);
		return beregnet(resultater);
	}

}
