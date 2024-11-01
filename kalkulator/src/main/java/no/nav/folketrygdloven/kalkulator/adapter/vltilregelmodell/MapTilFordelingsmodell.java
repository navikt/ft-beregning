package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.kalkulator.adapter.util.FinnArbeidsperiode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapInntektskategoriFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class MapTilFordelingsmodell {

	private MapTilFordelingsmodell() {
		// Skjuler default ctor
	}

	public static List<FordelPeriodeModell> map(BeregningsgrunnlagDto beregningsgrunnlag,
	                                            BeregningsgrunnlagInput input) {
		var perioderTilVurderingTjeneste = new PerioderTilVurderingTjeneste(input.getForlengelseperioder(), beregningsgrunnlag);
		return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
				.filter(p -> perioderTilVurderingTjeneste.erTilVurdering(p.getPeriode()))
				.map(bgp -> mapPeriode(bgp, input))
				.collect(Collectors.toList());
	}

	private static FordelPeriodeModell mapPeriode(BeregningsgrunnlagPeriodeDto bgPeriode, BeregningsgrunnlagInput input) {
		var regelAndeler = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
				.map(bga -> mapAndel(bga, input))
				.collect(Collectors.toList());
		return new FordelPeriodeModell(Periode.of(bgPeriode.getBeregningsgrunnlagPeriodeFom(), bgPeriode.getBeregningsgrunnlagPeriodeTom()), regelAndeler);
	}

	private static FordelAndelModell mapAndel(BeregningsgrunnlagPrStatusOgAndelDto bgAndel, BeregningsgrunnlagInput input) {
		var regelstatus = mapStatus(bgAndel.getAktivitetStatus());
		var regelBuilder = FordelAndelModell.builder()
				.medAktivitetStatus(regelstatus)
				.medAndelNr(bgAndel.getAndelsnr())
				.erSøktYtelseFor(erSøktYtelseFor(bgAndel, input.getYtelsespesifiktGrunnlag()))
				.medUtbetalingsgrad(kanFordeleTilAndelen(bgAndel, input) ? BigDecimal.valueOf(100) : BigDecimal.ZERO)
				.medInntektskategori(MapInntektskategoriFraVLTilRegel.map(bgAndel.getGjeldendeInntektskategori()));
		mapArbeidsforhold(bgAndel).ifPresent(regelBuilder::medArbeidsforhold);
		bgAndel.getBgAndelArbeidsforhold().ifPresent(arb -> regelBuilder.medGjeldendeRefusjonPrÅr(Beløp.safeVerdi(arb.getGjeldendeRefusjonPrÅr())));
		bgAndel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr).map(Beløp::verdi).ifPresent(regelBuilder::medNaturalytelseBortfaltPrÅr);
		bgAndel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseTilkommetPrÅr).map(Beløp::verdi).ifPresent(regelBuilder::medNaturalytelseTilkommerPrÅr);
		if (bgAndel.getBruttoPrÅr() != null) {
			regelBuilder.medForeslåttPrÅr(Beløp.safeVerdi(bgAndel.getBruttoPrÅr()));
		} else if (erArbeidsandelMedSøktRefusjon(bgAndel)) {
			// Andel er opprettet etter foreslå steget, henter inntekt fra IM for bruk ved andelsmessig fordeling
			regelBuilder.medInntektFraInnektsmelding(finnInntektFraIM(bgAndel, input));
		}
		return regelBuilder.build();
	}

	private static boolean kanFordeleTilAndelen(BeregningsgrunnlagPrStatusOgAndelDto bgAndel, BeregningsgrunnlagInput input) {
		var periode = bgAndel.getBeregningsgrunnlagPeriode().getPeriode();
		var arbeidsforhold = bgAndel.getBgAndelArbeidsforhold();
		if (arbeidsforhold.isPresent()) {
			var ansattTidslinje = FinnArbeidsperiode.finnAnsettelseTidslinje(arbeidsforhold.get().getArbeidsgiver(),
					arbeidsforhold.get().getArbeidsforholdRef(),
					input.getIayGrunnlag(), input.getSkjæringstidspunktForBeregning());
			var aktuellPeriode = new LocalDateTimeline<>(periode.getFomDato(), periode.getTomDato(), Boolean.TRUE);
			var erAnsattIPeriode = !ansattTidslinje.intersection(aktuellPeriode).isEmpty();
			if (!erAnsattIPeriode) {
				return true;
			}
		}

		return erSøktYtelseFor(bgAndel, input.getYtelsespesifiktGrunnlag());
	}


	private static boolean erArbeidsandelMedSøktRefusjon(BeregningsgrunnlagPrStatusOgAndelDto bgAndel) {
		var gjeldendeRefusjon = bgAndel.getBgAndelArbeidsforhold()
				.map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr)
				.orElse(Beløp.ZERO);
		return bgAndel.getAktivitetStatus().erArbeidstaker() && gjeldendeRefusjon.compareTo(Beløp.ZERO) > 0;
	}

	private static BigDecimal finnInntektFraIM(BeregningsgrunnlagPrStatusOgAndelDto bgAndel, BeregningsgrunnlagInput input) {
		return input.getInntektsmeldinger().stream()
				.filter(im -> bgAndel.gjelderInntektsmeldingFor(im.getArbeidsgiver(), im.getArbeidsforholdRef()))
				.findFirst().map(InntektsmeldingDto::getInntektBeløp)
				.orElseThrow().verdi();
	}

	private static boolean erSøktYtelseFor(BeregningsgrunnlagPrStatusOgAndelDto bgAndel, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
		return UtbetalingsgradTjeneste.erSøktYtelseFor(bgAndel, bgAndel.getBeregningsgrunnlagPeriode().getPeriode(), ytelsespesifiktGrunnlag, false);
	}

	private static Optional<Arbeidsforhold> mapArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto bgAndel) {
		if (bgAndel.getAktivitetStatus().erArbeidstaker() || bgAndel.getAktivitetStatus().erFrilanser()) {
			return Optional.of(MapArbeidsforholdFraVLTilRegel.arbeidsforholdFor(bgAndel));
		}
		return Optional.empty();
	}

	private static AktivitetStatus mapStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus kalkulusAktivitetstatus) {
		try {
			return AktivitetStatus.valueOf(kalkulusAktivitetstatus.getKode());
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Ukjent AktivitetStatus: (" + kalkulusAktivitetstatus.getKode() + ").", e);
		}
	}

}
