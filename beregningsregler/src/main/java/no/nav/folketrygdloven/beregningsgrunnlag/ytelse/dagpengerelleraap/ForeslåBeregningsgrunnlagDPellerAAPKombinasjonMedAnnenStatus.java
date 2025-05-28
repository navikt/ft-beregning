package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.dagpengerelleraap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ForeslåBeregningsgrunnlagDPellerAAPKombinasjonMedAnnenStatus.ID)
class ForeslåBeregningsgrunnlagDPellerAAPKombinasjonMedAnnenStatus extends LeafSpecification<BeregningsgrunnlagPeriode> {

	static final String ID = "FP_BR_10.2";
	static final String BESKRIVELSE = "Foreslå beregningsgrunnlag for Dagpenger/AAP i kombinasjon med annen aktivitetstatus";

	ForeslåBeregningsgrunnlagDPellerAAPKombinasjonMedAnnenStatus() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var bgPerStatus = grunnlag.getBeregningsgrunnlagPrStatus().stream()
				.filter(bgps -> bgps.getAktivitetStatus().erAAPellerDP())
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Ingen aktivitetstatus av type DP eller AAP funnet."));

        var inntekt = finnPeriodeInntektForDPEllerAAP(grunnlag, bgPerStatus.getAktivitetStatus());
        var utbetalingsFaktor = inntekt.getUtbetalingsfaktor()
				.orElseThrow(() -> new IllegalStateException("Utbetalingsgrad for DP/AAP mangler."));

        var antallPerioderPrÅr = inntekt.getInntektPeriodeType().getAntallPrÅr();
        var beregnetPrÅr = inntekt.getInntekt().multiply(antallPerioderPrÅr).multiply(utbetalingsFaktor);
		Long originalDagsats = inntekt.getInntekt().longValue();

		BeregningsgrunnlagPrStatus.builder(bgPerStatus)
				.medBeregnetPrÅr(beregnetPrÅr)
				.medÅrsbeløpFraTilstøtendeYtelse(beregnetPrÅr)
				.medOrginalDagsatsFraTilstøtendeYtelse(originalDagsats)
				.build();

        var hjemmel = AktivitetStatus.AAP.equals(bgPerStatus.getAktivitetStatus()) ?
				BeregningsgrunnlagHjemmel.F_14_7 : BeregningsgrunnlagHjemmel.F_14_7_8_49;
		grunnlag.getBeregningsgrunnlag().getAktivitetStatus(bgPerStatus.getAktivitetStatus()).setHjemmel(hjemmel);

		Map<String, Object> resultater = new HashMap<>();
		resultater.put("beregnetPrÅr." + bgPerStatus.getAktivitetStatus().name(), beregnetPrÅr);
		resultater.put("tilstøtendeYtelserPrÅr." + bgPerStatus.getAktivitetStatus().name(), beregnetPrÅr);
		resultater.put("hjemmel", hjemmel);
		return beregnet(resultater);
	}

	private Periodeinntekt finnPeriodeInntektForDPEllerAAP(BeregningsgrunnlagPeriode grunnlag, AktivitetStatus aktivitetStatus) {
		if (aktivitetStatus.erDPFraYtelse()) {
			var dagpengerFraYtelseVedtak = grunnlag.getInntektsgrunnlag()
					.getSistePeriodeinntekterMedType(Inntektskilde.YTELSE_VEDTAK)
					.stream().filter(i -> i.getInntektskategori().equals(Inntektskategori.DAGPENGER))
					.findFirst();
			if (dagpengerFraYtelseVedtak.isPresent()) {
				return dagpengerFraYtelseVedtak.get();
			}
		}
		return finnPeriodeInntektFraMeldekort(grunnlag)
				.orElseThrow(() -> new IllegalStateException("Ingen inntekter fra tilstøtende ytelser funnet i siste måned med inntekt"));
	}

	private Optional<Periodeinntekt> finnPeriodeInntektFraMeldekort(BeregningsgrunnlagPeriode grunnlag) {
		return grunnlag.getInntektsgrunnlag().getPeriodeinntekt(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP, grunnlag.getSkjæringstidspunkt());
	}

}
