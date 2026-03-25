package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.dagpengerelleraap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.InntektPeriodeType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;
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

        var dagsatsOgGrad = regnUtSnittInntektOgGrad(grunnlag, bgPerStatus.getAktivitetStatus());
        var dagsats = dagsatsOgGrad.dagsats();
        var beregnetPrÅr = dagsats.multiply(dagsatsOgGrad.inntektPeriodeType().getAntallPrÅr()).multiply(dagsatsOgGrad.utbetalingsgrad());

		BeregningsgrunnlagPrStatus.builder(bgPerStatus)
				.medBeregnetPrÅr(beregnetPrÅr)
				.medÅrsbeløpFraTilstøtendeYtelse(beregnetPrÅr)
				.medOrginalDagsatsFraTilstøtendeYtelse(dagsats.setScale(0, RoundingMode.HALF_EVEN).longValue())
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

    private DagsatsOgGrad regnUtSnittInntektOgGrad(BeregningsgrunnlagPeriode grunnlag, AktivitetStatus aktivitetStatus) {
        if (aktivitetStatus.erDPFraYtelse()) {
            var dagpengerFraYtelseVedtak = grunnlag.getInntektsgrunnlag()
                .getSistePeriodeinntekterMedType(Inntektskilde.YTELSE_VEDTAK)
                .stream().filter(i -> i.getInntektskategori().equals(Inntektskategori.DAGPENGER))
                .findFirst();
            if (dagpengerFraYtelseVedtak.isPresent()) {
                return new DagsatsOgGrad(dagpengerFraYtelseVedtak.get().getInntekt(), dagpengerFraYtelseVedtak.get().getUtbetalingsfaktor().orElseThrow(), Inntektskilde.YTELSE_VEDTAK.getInntektPeriodeType());
            }
        }
        var stp = grunnlag.getSkjæringstidspunkt();
        var relevanteInntekter = grunnlag.getInntektsgrunnlag()
            .getPeriodeinntekter()
            .stream()
            .filter(pi -> pi.getInntektskilde().equals(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP))
            .filter(pi -> pi.getPeriode().getFom().isBefore(stp))
            .filter(pi -> pi.getFom().equals(pi.getTom())) // Skal kun ha endagersperioder
            .toList();

        var sisteDagMedYtelseUtbetaling = relevanteInntekter.stream()
            .max(Comparator.comparing(pi -> pi.getPeriode().getFom()))
            .orElseThrow()
            .getFom();
        var beregningsperiodeForYtelse = Periode.of(sisteDagMedYtelseUtbetaling.minusDays(13), sisteDagMedYtelseUtbetaling);
        var inntekterIBeregningsperiode = relevanteInntekter.stream().filter(inntekt -> inntekt.getPeriode().overlapper(beregningsperiodeForYtelse)).toList();
        var aggregertDagsats = relevanteInntekter.stream()
            .filter(inntekt -> inntekt.getPeriode().overlapper(beregningsperiodeForYtelse))
            .filter(pi -> Virkedager.beregnAntallVirkedager(pi.getFom(), pi.getTom()) == 1)
            .map(Periodeinntekt::getInntekt)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
        var antallVirkedager = Virkedager.beregnAntallVirkedager(beregningsperiodeForYtelse);
        var snittDagsats = aggregertDagsats.divide(BigDecimal.valueOf(antallVirkedager), 0, RoundingMode.HALF_EVEN);
        var aggregertUtbetalingsgrad = inntekterIBeregningsperiode.stream()
            .filter(inntekt -> inntekt.getPeriode().overlapper(beregningsperiodeForYtelse))
            .filter(pi -> Virkedager.beregnAntallVirkedager(pi.getFom(), pi.getTom()) == 1)
            .map(pi -> pi.getUtbetalingsfaktor().orElseThrow())
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
        var snittUtbetalingsfaktor = aggregertUtbetalingsgrad.divide(BigDecimal.valueOf(antallVirkedager), 2, RoundingMode.HALF_EVEN);

        return new DagsatsOgGrad(snittDagsats, snittUtbetalingsfaktor, Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP.getInntektPeriodeType());
    }

    private record DagsatsOgGrad(BigDecimal dagsats, BigDecimal utbetalingsgrad, InntektPeriodeType inntektPeriodeType){}
}
