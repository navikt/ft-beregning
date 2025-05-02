package no.nav.folketrygdloven.skjæringstidspunkt.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellK9;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettKombinasjoner.ID)
public class FastsettStatusOgAndelPrPeriode extends LeafSpecification<AktivitetStatusModell> {

	public static final String ID = "FP_BR_19_2";
	public static final String BESKRIVELSE = "Fastsett status per andel og periode";

	protected FastsettStatusOgAndelPrPeriode() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(AktivitetStatusModell regelmodell) {
		opprettAktivitetStatuser(regelmodell);

		Map<String, Object> resultater = new HashMap<>();
		regelmodell.getAktivitetStatuser()
				.forEach(as -> resultater.put("Aktivitetstatus." + as.name(), as.getBeskrivelse()));
		regelmodell.getBeregningsgrunnlagPrStatusListe()
				.forEach(bgps -> resultater.put("BeregningsgrunnlagPrStatus." + bgps.getAktivitetStatus().name(), bgps.getAktivitetStatus().getBeskrivelse())); // TODO: Skal det inn noe regelsporing av inntekt?
		return beregnet(resultater);
	}

	private void opprettAktivitetStatuser(AktivitetStatusModell regelmodell) {
		List<AktivPeriode> aktivePerioder = regelmodell.getAktivePerioder();
		List<AktivPeriode> aktivePerioderVedStp = hentAktivePerioder(regelmodell.getBeregningstidspunkt(), aktivePerioder);

		var erAapPraksisendringAktiv = regelmodell.getToggles().isEnabled("aap.praksisendring");
		if (erAapPraksisendringAktiv && harAAPOgArbeidsinntektIBeregningsperioden(regelmodell, aktivePerioderVedStp)) {
			regelmodell.leggTilAktivitetStatus(AktivitetStatus.ATFL);
			leggTilArbeidUnderAAP(regelmodell);
		}

		if (harKunYtelsePåSkjæringstidspunkt(aktivePerioderVedStp)) {
			regelmodell.leggTilAktivitetStatus(AktivitetStatus.KUN_YTELSE);
			leggTilBrukersAndel(regelmodell);
		} else if (erMidlertidigInaktiv(regelmodell)) {
			regelmodell.leggTilAktivitetStatus(AktivitetStatus.MIDL_INAKTIV);
			leggTilBrukersAndel(regelmodell);
		} else {
			opprettStatusForAktiviteter(regelmodell, aktivePerioderVedStp);
		}
	}

	private boolean erMidlertidigInaktiv(AktivitetStatusModell regelmodell) {
		if (regelmodell instanceof AktivitetStatusModellK9 k9Modell) {
			return k9Modell.getMidlertidigInaktivType() != null;
		}
		return false;
	}

	private void leggTilBrukersAndel(AktivitetStatusModell regelmodell) {
		BeregningsgrunnlagPrStatus bgPrStatus = new BeregningsgrunnlagPrStatus(AktivitetStatus.BA);
		regelmodell.leggTilBeregningsgrunnlagPrStatus(bgPrStatus);
	}

	private void leggTilArbeidUnderAAP(AktivitetStatusModell regelmodell) {
		var arbeidsforhold = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEID_UNDER_AAP).build();
		var bgPrStatus = new BeregningsgrunnlagPrStatus(AktivitetStatus.AT, arbeidsforhold);
		regelmodell.leggTilBeregningsgrunnlagPrStatus(bgPrStatus);
	}

	private void opprettStatusForAktiviteter(AktivitetStatusModell regelmodell, List<AktivPeriode> aktivePerioderVedStp) {
		List<BeregningsgrunnlagPrStatus> andeler = new ArrayList<>();
		for (AktivPeriode ap : aktivePerioderVedStp) {
			AktivitetStatus aktivitetStatus = mapAktivitetTilStatus(ap.getAktivitet());
			if (!AktivitetStatus.KUN_YTELSE.equals(aktivitetStatus)
					&& ikkeMilitærMedAndreAktiviteterPåStp(aktivePerioderVedStp, aktivitetStatus)) {
				regelmodell.leggTilAktivitetStatus(aktivitetStatus);
				var arbeidsforhold = AktivitetStatus.ATFL.equals(aktivitetStatus) ? ap.getArbeidsforhold() : null;
				andeler.add(new BeregningsgrunnlagPrStatus(aktivitetStatus, arbeidsforhold));
			}
		}
		andeler = justerListeForFlereDagpengekilder(andeler);
		andeler.forEach(regelmodell::leggTilBeregningsgrunnlagPrStatus);
	}

	private List<BeregningsgrunnlagPrStatus> justerListeForFlereDagpengekilder(List<BeregningsgrunnlagPrStatus> andeler) {
		if (andeler.stream().filter(a -> a.getAktivitetStatus().erDP()).count() > 1) {
			andeler = andeler.stream().filter(a -> !a.getAktivitetStatus().erDP())
					.collect(Collectors.toList());
			andeler.add(new BeregningsgrunnlagPrStatus(AktivitetStatus.DP));
		}
		return andeler;
	}

	private boolean ikkeMilitærMedAndreAktiviteterPåStp(List<AktivPeriode> aktivPerioderVedSkjæringtidspunkt, AktivitetStatus aktivitetStatus) {
		return !(AktivitetStatus.MS.equals(aktivitetStatus) && aktivPerioderVedSkjæringtidspunkt.size() > 1);
	}

	private boolean harKunYtelsePåSkjæringstidspunkt(List<AktivPeriode> aktivPerioderVedSkjæringtidspunkt) {
		return !aktivPerioderVedSkjæringtidspunkt.isEmpty() && aktivPerioderVedSkjæringtidspunkt.stream()
				.allMatch(ap -> mapAktivitetTilStatus(ap.getAktivitet()).equals(AktivitetStatus.KUN_YTELSE));
	}

	private AktivitetStatus mapAktivitetTilStatus(Aktivitet aktivitet) {
		List<Aktivitet> arbeistaker = Arrays.asList(Aktivitet.ARBEIDSTAKERINNTEKT, Aktivitet.FRILANSINNTEKT,
				Aktivitet.VENTELØNN_VARTPENGER, Aktivitet.ETTERLØNN_SLUTTPAKKE, Aktivitet.VIDERE_ETTERUTDANNING,
				Aktivitet.UTDANNINGSPERMISJON);
		List<Aktivitet> tilstøtendeYtelse = Arrays.asList(Aktivitet.SYKEPENGER_MOTTAKER, Aktivitet.FORELDREPENGER_MOTTAKER,
				Aktivitet.PLEIEPENGER_MOTTAKER, Aktivitet.SVANGERSKAPSPENGER_MOTTAKER, Aktivitet.OPPLÆRINGSPENGER, Aktivitet.FRISINN_MOTTAKER,
				Aktivitet.OMSORGSPENGER);
		AktivitetStatus aktivitetStatus;

		if (Aktivitet.NÆRINGSINNTEKT.equals(aktivitet)) {
			aktivitetStatus = AktivitetStatus.SN;
		} else if (Aktivitet.DAGPENGEMOTTAKER.equals(aktivitet)) {
			aktivitetStatus = AktivitetStatus.DP;
		} else if (Aktivitet.AAP_MOTTAKER.equals(aktivitet)) {
			aktivitetStatus = AktivitetStatus.AAP;
		} else if (Aktivitet.MILITÆR_ELLER_SIVILTJENESTE.equals(aktivitet)) {
			aktivitetStatus = AktivitetStatus.MS;
		} else if (tilstøtendeYtelse.contains(aktivitet)) {
			aktivitetStatus = AktivitetStatus.KUN_YTELSE;
		} else if (arbeistaker.contains(aktivitet)) {
			aktivitetStatus = AktivitetStatus.ATFL;
		} else {
			aktivitetStatus = AktivitetStatus.UDEFINERT;
		}
		return aktivitetStatus;
	}

	private List<AktivPeriode> hentAktivePerioder(LocalDate beregningstidspunkt, List<AktivPeriode> aktivePerioder) {
		return aktivePerioder.stream()
				.filter(ap -> ap.inneholder(beregningstidspunkt)).toList();
	}

	private boolean harAAPOgArbeidsinntektIBeregningsperioden(AktivitetStatusModell regelmodell, List<AktivPeriode> aktivePerioderVedStp) {
		if (aktivePerioderVedStp.stream().noneMatch(ap -> ap.getAktivitet().equals(Aktivitet.AAP_MOTTAKER))) {
			return false;
		}

		var inntektsgrunnlag = regelmodell.getInntektsgrunnlag();
		if (inntektsgrunnlag == null) {
			return false;
		}

		var skjæringstidspunkt = regelmodell.getSkjæringstidspunktForBeregning();
		var beregningsperiode = new Periode(
				skjæringstidspunkt.minusMonths(3).withDayOfMonth(1),
				skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));
		return inntektsgrunnlag.getPeriodeinntekter().stream()
				.anyMatch(p -> p.getInntektskilde().equals(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING)
						&& p.getPeriode().overlapper(beregningsperiode)
						&& p.getInntekt().compareTo(BigDecimal.ZERO) != 0); // TODO: Antakelse om at negative inntekter skal med, undersøk
	}
}
